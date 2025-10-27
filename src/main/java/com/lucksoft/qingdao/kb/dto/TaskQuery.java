package com.lucksoft.qingdao.kb.dto;

public class TaskQuery {
    private String machine;
    private String view; // Corresponds to activeFilter: pending, scoring, history
    private String subView; // Corresponds to sub-views like 'pending', 'completed', 'unscored', 'scored'
    private String date; // For history view

    // Getters and Setters
    public String getMachine() { return machine; }
    public void setMachine(String machine) { this.machine = machine; }
    public String getView() { return view; }
    public void setView(String view) { this.view = view; }
    public String getSubView() { return subView; }
    public void setSubView(String subView) { this.subView = subView; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
