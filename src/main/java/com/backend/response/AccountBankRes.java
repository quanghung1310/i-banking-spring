package com.backend.response;

import com.backend.dto.AccountDTO;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Setter;

@Setter
public class AccountBankRes {

    private long responseTime;

    private String customerName;

    private String customerPhone;

    private String customerEmail;

    private boolean isActive;

    private int agency;

    private String openDate;

    private String closeDate;

    public JsonObject toJson() {
        return new JsonObject(this.toString());
    }

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
    //todo: code ngu
    public static AccountBankRes parseToAccountBankRes(AccountDTO accountInfo) {
        AccountBankRes accountBankRes = new AccountBankRes();
        try {
            accountBankRes.setActive(accountInfo.isActive());
            accountBankRes.setAgency(accountInfo.getAgency());
            accountBankRes.setCloseDate(accountInfo.getCloseDate());
            accountBankRes.setOpenDate(accountInfo.getOpenDate());
            accountBankRes.setCustomerName(accountInfo.getCustomerName());
            accountBankRes.setCustomerEmail(accountInfo.getCustomerEmail());
            accountBankRes.setCustomerPhone(accountInfo.getCustomerPhone());
            accountBankRes.setResponseTime(System.currentTimeMillis());
            return accountBankRes;
        }
       catch (Exception ex) {
           return null;
       }
    }
}
