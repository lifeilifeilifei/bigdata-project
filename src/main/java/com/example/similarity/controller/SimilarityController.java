package com.example.similarity.controller;

import com.example.similarity.dto.SimilarityRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.similarity.service.SimilarityService;
import com.example.similarity.model.SimilarityResponse;
import org.apache.logging.log4j.*;

@RestController
public class SimilarityController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SimilarityController.class);
    @Autowired
    private SimilarityService similarityService;

    @RequestMapping(value = "/InterSimlary", method = {RequestMethod.GET, RequestMethod.POST})
    public SimilarityResponse getSimilarity(@RequestBody SimilarityRequest request) {
        // 从实体类中获取 a 和 b 参数
        String a = request.getA();
        String b = request.getB();
        log.info("日志输入进去");
        log.info("这里是二勇要打印的数据");
        System.out.println("二勇说你好");
        // 原来的业务逻辑不变
        return similarityService.calculateSimilarity(a, b);
    }
}
