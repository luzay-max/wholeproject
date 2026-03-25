package com.lzy.lostandfound.service;

import com.lzy.lostandfound.dto.ItemReportRequest;
import com.lzy.lostandfound.vo.Result;

public interface IReportService {
    Result reportItem(String reporterId, String reporterName, ItemReportRequest request);
}

