package com.lzy.lostandfound.ai;

import java.util.List;

public interface AiModelClient {
    String suggestByText(String systemPrompt, String userPrompt);

    String suggestByVision(String systemPrompt, String userPrompt, List<String> imageUrls);
}

