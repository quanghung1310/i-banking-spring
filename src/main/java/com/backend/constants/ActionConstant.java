package com.backend.constants;


public enum ActionConstant {
    INIT (1),
    DELETE(2),
    UPDATE(3),
    COMPLETED(4),
    CONFIRM(5);

    private int value;

    public int getValue() {
        return value;
    }

    ActionConstant(int value){
        this.value = value;
    }
}
