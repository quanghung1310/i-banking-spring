package com.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class APIInfor {

    private String id;
    private String description;
    private String paramKey;
    private String apiLink;
    private String type;
    private String linkPrefix;
    private String returnType;
    private int bodyDatatype;
    private String apiKey;
    private List<APIParameter> params;

    public List<APIParameter> getParams(int type, Integer parentId) {
        List<APIParameter> listParam = new ArrayList<>();
        if (params == null) {
            return listParam;
        }
        if (parentId == null) {
            for (APIParameter param : params) {
                if (param.getType() == type
                        && (param.getParentId() == null || param.getParentId() == -1)) {
                    listParam.add(param);
                }
            }
        } else {
            for (APIParameter param : params) {
                if (param.getType() == type
                        && param.getParentId() != null
                        && param.getParentId().equals(parentId)) {
                    listParam.add(param);
                }
            }
        }
        return listParam;
    }

    public List<APIParameter> getMappingOut(Integer parentId) {
        List<APIParameter> listParam = new ArrayList<>();
        if (params == null) {
            return listParam;
        }

        if (parentId == null) {
            for (APIParameter param : params) {
                if (param.getType() == 4 && param.getParentId() == null) {
                    listParam.add(param);
                }
            }
        } else {
            for (APIParameter param : params) {
                if (param.getType() == 4 && param.getParentId() != null && param.getParentId().equals(parentId)) {
                    listParam.add(param);
                }
            }
        }
        return listParam;
    }
}
