package com.example.demo.multiagent;

import com.example.demo.multiagent.agent.AnalysisAgent.AnalysisResult;

import lombok.Data;

@Data
public class State {
	private String userId;
	private String userInquiry;
	private AnalysisResult analysisResult;
	private String userInfo;
	private String knowledgeBase;
	private String finalResponse;
}
