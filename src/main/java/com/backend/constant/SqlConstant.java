package com.backend.constant;

public class SqlConstant {
    //---------------------------- QUERY ----------------------------//
    public static final String QUERY_GET_PARTNER_BY_PARTNER_CODE = "SELECT *" +
            " FROM banking_service.partner WHERE PARTNER_CODE = ?";


    //---------------------------- UPDATE ----------------------------//


    //---------------------------- INSERT ----------------------------//
    public static final String QUERY_INSERT_PARTNER =
            "INSERT INTO banking_service.partner(PARTNER_CODE, PRIVATE_KEY, PUBLIC_KEY, EMAIL, PHONE_NUMBER, PASSWORD)" +
                    " VALUES(?, ?, ?, ?, ?, ?)";


    //---------------------------- DELETE ----------------------------//
}
