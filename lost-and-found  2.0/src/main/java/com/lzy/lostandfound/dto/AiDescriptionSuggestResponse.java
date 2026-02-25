package com.lzy.lostandfound.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDescriptionSuggestResponse {
    private String suggestedDescription;
    private String sourceMode;
    private String notice;
    private Boolean truncated;
}

