package com.lzy.lostandfound.service;

import com.lzy.lostandfound.dto.AiDescriptionSuggestRequest;
import com.lzy.lostandfound.dto.AiDescriptionSuggestResponse;

public interface IAiDescriptionService {
    AiDescriptionSuggestResponse suggestDescription(String userId, AiDescriptionSuggestRequest request);
}

