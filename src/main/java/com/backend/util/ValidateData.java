package com.backend.util;

import com.backend.request.AccountBankReq;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ValidateData {
    private static final Logger logger = LogManager.getLogger(ValidateData.class);


    public static boolean isValidDataRequest(String logId, AccountBankReq accountBankReqRequest) {
        try {
            if (accountBankReqRequest.getRequestTime() == null
                || StringUtils.isBlank(accountBankReqRequest.getPartnerCode())
                || StringUtils.isBlank(accountBankReqRequest.getNumberAccount())
                || StringUtils.isBlank(accountBankReqRequest.getHash())
                || accountBankReqRequest.getTypeAccount() != 1) {
                logger.warn("{}| Validate request data: Fail!", logId);
                return false;
            }
            return true;
        }
        catch (Exception ex) {
            logger.error("{}| Validate request data catch exception: ", logId, ex);
            return false;
        }
    }
}
