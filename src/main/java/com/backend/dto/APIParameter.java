package com.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class APIParameter {

    private int id;
    private String key;
    private String value;
    private String dataType;
    private Integer parentId;
    private String apiLink;
    private String inputKey;
    private Integer type;
}

