package com.lzy.lostandfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzy.lostandfound.entity.FindInfo;
import com.lzy.lostandfound.entity.LostInfo;
import com.lzy.lostandfound.service.IFindInfoService;
import com.lzy.lostandfound.service.ILostInfoService;
import com.lzy.lostandfound.service.ISmartMatchService;
import com.lzy.lostandfound.vo.MatchCandidateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SmartMatchServiceImpl implements ISmartMatchService {

    private static final String ITEM_TYPE_LOST = "lost";
    private static final String ITEM_TYPE_FIND = "find";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;
    private static final int FETCH_MULTIPLE = 10;

    private static final int SCORE_TYPE_MATCH = 40;
    private static final int SCORE_LOCATION_STRONG = 24;
    private static final int SCORE_LOCATION_WEAK = 8;
    private static final int SCORE_TIME_STRONG = 20;
    private static final int SCORE_TIME_MEDIUM = 12;
    private static final int SCORE_TIME_WEAK = 6;
    private static final int SCORE_KEYWORD_PER = 5;
    private static final int SCORE_KEYWORD_MAX = 20;

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-zA-Z0-9]{2,}|[\\u4e00-\\u9fa5]{2,}");
    private static final Set<String> STOP_WORDS = Set.of("失物", "招领", "信息", "物品", "一个", "这个", "那个", "同学");

    @Autowired
    private ILostInfoService lostInfoService;
    @Autowired
    private IFindInfoService findInfoService;

    @Override
    public List<MatchCandidateVO> recommend(String itemType, String itemId, Integer limit) {
        String normalizedType = normalizeItemType(itemType);
        if (!StringUtils.hasText(normalizedType) || !StringUtils.hasText(itemId)) {
            return Collections.emptyList();
        }
        SourceItem source = loadSourceItem(normalizedType, itemId);
        if (source == null) {
            return Collections.emptyList();
        }

        int safeLimit = normalizeLimit(limit);
        String targetType = ITEM_TYPE_LOST.equals(normalizedType) ? ITEM_TYPE_FIND : ITEM_TYPE_LOST;
        int fetchLimit = Math.max(safeLimit * FETCH_MULTIPLE, safeLimit);
        List<CandidateItem> candidates = loadCandidateItems(targetType, source.userId, fetchLimit);
        List<ScoredCandidate> scoredList = new ArrayList<>();
        for (CandidateItem candidate : candidates) {
            ScoreResult score = score(source, candidate);
            if (score.value <= 0) {
                continue;
            }
            scoredList.add(new ScoredCandidate(candidate, score));
        }
        scoredList.sort((a, b) -> {
            int byScore = Integer.compare(b.score.value, a.score.value);
            if (byScore != 0) {
                return byScore;
            }
            return compareDateTimeDesc(a.candidate.publishTime, b.candidate.publishTime);
        });
        return scoredList.stream()
                .limit(safeLimit)
                .map(it -> toVO(it.candidate, it.score))
                .collect(Collectors.toList());
    }

    private String normalizeItemType(String itemType) {
        if (!StringUtils.hasText(itemType)) {
            return null;
        }
        String val = itemType.trim().toLowerCase();
        if (ITEM_TYPE_LOST.equals(val) || ITEM_TYPE_FIND.equals(val)) {
            return val;
        }
        return null;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private SourceItem loadSourceItem(String itemType, String itemId) {
        if (ITEM_TYPE_LOST.equals(itemType)) {
            LostInfo lostInfo = lostInfoService.getById(itemId);
            if (lostInfo == null) {
                return null;
            }
            return new SourceItem(
                    lostInfo.getId(),
                    ITEM_TYPE_LOST,
                    lostInfo.getUserId(),
                    safeText(lostInfo.getName()),
                    safeText(lostInfo.getType()),
                    safeText(lostInfo.getLocation()),
                    safeText(lostInfo.getDescription()),
                    pickTime(lostInfo.getLostTime(), lostInfo.getPublishTime())
            );
        }
        FindInfo findInfo = findInfoService.getById(itemId);
        if (findInfo == null) {
            return null;
        }
        return new SourceItem(
                findInfo.getId(),
                ITEM_TYPE_FIND,
                findInfo.getUserId(),
                safeText(findInfo.getName()),
                safeText(findInfo.getType()),
                safeText(findInfo.getLocation()),
                safeText(findInfo.getDescription()),
                pickTime(findInfo.getFoundTime(), findInfo.getPublishTime())
        );
    }

    private List<CandidateItem> loadCandidateItems(String targetType, String excludedUserId, int fetchLimit) {
        if (ITEM_TYPE_LOST.equals(targetType)) {
            LambdaQueryWrapper<LostInfo> wrapper = new LambdaQueryWrapper<LostInfo>()
                    .eq(LostInfo::getStatus, STATUS_APPROVED)
                    .orderByDesc(LostInfo::getPublishTime)
                    .last("limit " + fetchLimit);
            if (StringUtils.hasText(excludedUserId)) {
                wrapper.ne(LostInfo::getUserId, excludedUserId);
            }
            List<LostInfo> list = lostInfoService.list(wrapper);
            return list.stream()
                    .map(item -> new CandidateItem(
                            item.getId(),
                            ITEM_TYPE_LOST,
                            item.getUserId(),
                            safeText(item.getName()),
                            safeText(item.getType()),
                            safeText(item.getLocation()),
                            safeText(item.getDescription()),
                            item.getImages(),
                            pickTime(item.getLostTime(), item.getPublishTime()),
                            item.getPublishTime()
                    ))
                    .collect(Collectors.toList());
        }
        LambdaQueryWrapper<FindInfo> wrapper = new LambdaQueryWrapper<FindInfo>()
                .eq(FindInfo::getStatus, STATUS_APPROVED)
                .orderByDesc(FindInfo::getPublishTime)
                .last("limit " + fetchLimit);
        if (StringUtils.hasText(excludedUserId)) {
            wrapper.ne(FindInfo::getUserId, excludedUserId);
        }
        List<FindInfo> list = findInfoService.list(wrapper);
        return list.stream()
                .map(item -> new CandidateItem(
                        item.getId(),
                        ITEM_TYPE_FIND,
                        item.getUserId(),
                        safeText(item.getName()),
                        safeText(item.getType()),
                        safeText(item.getLocation()),
                        safeText(item.getDescription()),
                        item.getImages(),
                        pickTime(item.getFoundTime(), item.getPublishTime()),
                        item.getPublishTime()
                ))
                .collect(Collectors.toList());
    }

    private ScoreResult score(SourceItem source, CandidateItem candidate) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (equalsIgnoreCase(source.type, candidate.type)) {
            score += SCORE_TYPE_MATCH;
            reasons.add("类型一致");
        }

        int locationScore = scoreLocation(source.location, candidate.location);
        if (locationScore > 0) {
            score += locationScore;
            reasons.add(locationScore >= SCORE_LOCATION_STRONG ? "地点高度相关" : "地点相关");
        }

        int timeScore = scoreTime(source.timeValue, candidate.timeValue);
        if (timeScore > 0) {
            score += timeScore;
            reasons.add("时间接近");
        }

        int keywordScore = scoreKeyword(source, candidate);
        if (keywordScore > 0) {
            score += keywordScore;
            reasons.add("关键词匹配");
        }

        return new ScoreResult(score, String.join("、", reasons));
    }

    private int scoreLocation(String sourceLocation, String candidateLocation) {
        String s = normalizeText(sourceLocation);
        String c = normalizeText(candidateLocation);
        if (!StringUtils.hasText(s) || !StringUtils.hasText(c)) {
            return 0;
        }
        if (s.contains(c) || c.contains(s)) {
            return SCORE_LOCATION_STRONG;
        }
        Set<String> tokens = extractTokens(s);
        int overlap = 0;
        for (String token : tokens) {
            if (c.contains(token)) {
                overlap++;
            }
        }
        if (overlap <= 0) {
            return 0;
        }
        return Math.min(SCORE_LOCATION_STRONG, overlap * SCORE_LOCATION_WEAK);
    }

    private int scoreTime(LocalDateTime sourceTime, LocalDateTime candidateTime) {
        if (sourceTime == null || candidateTime == null) {
            return 0;
        }
        long hours = Math.abs(Duration.between(sourceTime, candidateTime).toHours());
        if (hours <= 24) {
            return SCORE_TIME_STRONG;
        }
        if (hours <= 72) {
            return SCORE_TIME_MEDIUM;
        }
        if (hours <= 168) {
            return SCORE_TIME_WEAK;
        }
        return 0;
    }

    private int scoreKeyword(SourceItem source, CandidateItem candidate) {
        String sourceText = normalizeText(source.name + " " + source.description);
        String targetText = normalizeText(candidate.name + " " + candidate.description);
        if (!StringUtils.hasText(sourceText) || !StringUtils.hasText(targetText)) {
            return 0;
        }
        Set<String> sourceTokens = extractTokens(sourceText);
        int hit = 0;
        for (String token : sourceTokens) {
            if (targetText.contains(token)) {
                hit++;
            }
        }
        if (hit <= 0) {
            return 0;
        }
        return Math.min(SCORE_KEYWORD_MAX, hit * SCORE_KEYWORD_PER);
    }

    private Set<String> extractTokens(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptySet();
        }
        Set<String> tokens = new LinkedHashSet<>();
        Matcher matcher = TOKEN_PATTERN.matcher(text);
        while (matcher.find()) {
            String token = matcher.group().trim().toLowerCase();
            if (token.length() < 2 || STOP_WORDS.contains(token)) {
                continue;
            }
            tokens.add(token);
            if (tokens.size() >= 20) {
                break;
            }
        }
        return tokens;
    }

    private int compareDateTimeDesc(LocalDateTime a, LocalDateTime b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return b.compareTo(a);
    }

    private MatchCandidateVO toVO(CandidateItem item, ScoreResult score) {
        MatchCandidateVO vo = new MatchCandidateVO();
        vo.setId(item.id);
        vo.setItemType(item.itemType);
        vo.setName(item.name);
        vo.setType(item.type);
        vo.setLocation(item.location);
        vo.setDescription(item.description);
        vo.setImages(item.images);
        vo.setTimeValue(item.timeValue);
        vo.setPublishTime(item.publishTime);
        vo.setMatchScore(score.value);
        vo.setMatchReason(score.reason);
        return vo;
    }

    private LocalDateTime pickTime(LocalDateTime first, LocalDateTime second) {
        return first != null ? first : second;
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return safeText(a).equalsIgnoreCase(safeText(b));
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeText(String value) {
        return safeText(value).toLowerCase();
    }

    private record SourceItem(
            String id,
            String itemType,
            String userId,
            String name,
            String type,
            String location,
            String description,
            LocalDateTime timeValue
    ) {
    }

    private record CandidateItem(
            String id,
            String itemType,
            String userId,
            String name,
            String type,
            String location,
            String description,
            String images,
            LocalDateTime timeValue,
            LocalDateTime publishTime
    ) {
    }

    private record ScoreResult(int value, String reason) {
    }

    private record ScoredCandidate(CandidateItem candidate, ScoreResult score) {
    }
}

