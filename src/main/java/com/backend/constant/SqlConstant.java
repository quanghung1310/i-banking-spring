package com.backend.constant;

public class SqlConstant {
    //---------------------------- QUERY ----------------------------//
    public static final String QUERY_GET_PARTNER_BY_PARTNER_CODE = "SELECT PARTNER_CODE, PARTNER_SECRET_KEY, PARTNER_PRIVATE_KEY, PARTNER_PUB_KEY " +
            " FROM SDK_ADMIN.SDK_PAYMENT_PARTNER WHERE AND PARTNER_CODE = ?";


    //---------------------------- UPDATE ----------------------------//


    //---------------------------- INSERT ----------------------------//



    //---------------------------- DELETE ----------------------------//
}
