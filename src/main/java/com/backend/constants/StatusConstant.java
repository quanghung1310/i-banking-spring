package com.backend.constants;


public enum StatusConstant {
    PENDING ("pending"),
    SUCCESS("success");

    private String value;

    public String getValue() {
        return value;
    }

    StatusConstant(String value){
        this.value = value;
    }
}
