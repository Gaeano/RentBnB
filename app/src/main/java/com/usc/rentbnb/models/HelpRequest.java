package com.usc.rentbnb.models;

import java.util.List;

public class HelpRequest {
    private final String question;
    private final List<String> history;

    public HelpRequest(String question, List<String> history) {
        this.question = question;
        this.history  = history;
    }

    public String getQuestion()       { return question; }
    public List<String> getHistory()  { return history; }
}