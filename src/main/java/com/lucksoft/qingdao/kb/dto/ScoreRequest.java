package com.lucksoft.qingdao.kb.dto;

import java.util.Map;

public class ScoreRequest {
    private String checker;
    private Map<String, Integer> scores;

    // Getters and Setters
    public String getChecker() { return checker; }
    public void setChecker(String checker) { this.checker = checker; }
    public Map<String, Integer> getScores() { return scores; }
    public void setScores(Map<String, Integer> scores) { this.scores = scores; }
}
