package com.cefriel.coneyapi.model.other;

public class Checkbox {

    int order;
    String text;
    String type;


    public Checkbox(){}

    public Checkbox(String text, String type, int order){
        this.order = order;
        this.text = text;
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }
}

