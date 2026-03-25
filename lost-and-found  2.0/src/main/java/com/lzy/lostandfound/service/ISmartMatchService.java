package com.lzy.lostandfound.service;

import com.lzy.lostandfound.vo.MatchCandidateVO;

import java.util.List;

public interface ISmartMatchService {
    List<MatchCandidateVO> recommend(String itemType, String itemId, Integer limit);
}

