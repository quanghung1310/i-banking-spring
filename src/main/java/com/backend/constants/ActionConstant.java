package com.backend.constants;


public enum ActionConstant {
    INIT (1),
    DELETE(2),
    COMPLETED(3);
    private int value;

    public int getValue() {
        return value;
    }

    ActionConstant(int value){
        this.value = value;
    }
}
