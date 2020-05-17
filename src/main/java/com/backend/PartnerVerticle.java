package com.backend;

import com.backend.config.PartnerConfig;
import com.backend.constant.DatabaseConstants;
import com.backend.constant.ErrorConstants;
import com.backend.constant.StringConstant;
import com.backend.database.DatabaseVerticle;
import com.backend.database.MySqlManager;
import com.backend.request.AccountBankReq;
import com.backend.request.BaseRequest;
import com.backend.response.AccountBankRes;
import com.backend.response.BaseResponse;
import com.backend.util.*;
import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PartnerVerticle extends AbstractVerticle {

    private static final Logger logger = LogManager.getLogger(PartnerVerticle.class);
    private static final Gson PARSER = new Gson();
    private static final long TIME_EXPIRE = 180000; //3'

    @Override
    public void start() {
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.post("/transfer/bank").consumes("application/json").produces("application/json").handler(ctx -> {
            LogUtil.logHttpRequest(logger, ctx.request());
            final String logId = DataUtil.createRequestId();
            HttpServerResponse response = ctx.response();
            ctx.request().bodyHandler(buffer -> {
                String privateKey = PartnerConfig.getPrivateKey();
                String data = new JsonObject().put("value", "lang123").toString();
                String hashRSA = Misc.encryptRSA("1", data, PartnerConfig.getPublicKey());
//                String hash = "BphKmdC2XlkFt1XeyXoNr4l1pzrFgFIQtM60RhqtOAn9mG9tmNF4dgATy0fgQvvsqLr9UyXxAZtF61ZPE3Lij82CHXkCb8Z+n1DkZwnwQup3b8ivWXYoMzFLIxV4nAvfXAko1fMy1pICOfWFcEAKvjPqwBu6d+o9R9IAuJ6d2Qc=";
//                String parseHash = Misc.parseRSAData(logId, hash, privateKey);
                MySqlManager.getPartnerByPartnerCode(logId, "VCB_123", partnerInf -> {
                    //todo
                        });
                DeliveryOptions options = new DeliveryOptions()
                        .addHeader(DatabaseConstants.ACTION, DatabaseConstants.QUERY_GET_PARTNER)
                        .addHeader(DatabaseConstants.REQUEST_ID_UPPER, logId);
                String finalLogId = logId;
                vertx.eventBus().request(DatabaseVerticle.class.getName(), data, options, event -> {

                });
                ResponseUtil.responseToClient(logId, response,
                        responseBody(new JsonObject(), logId, 0, hashRSA).toString(), ResponseUtil.HTTP_OK);
                return;
            });
        });

        router.post("/account/bank").consumes("application/json").produces("application/json").handler(ctx -> {
            LogUtil.logHttpRequest(logger, ctx.request());
            final String logId = DataUtil.createRequestId();
            HttpServerResponse response = ctx.response();
            ctx.request().bodyHandler(buffer -> {
                logger.info("{}| Raw data request get account bank information: {}", logId, buffer.toString());
                try {
                    JsonObject request = buffer.toJsonObject();
                    String requestId = request.getString(StringConstant.REQUEST_TYPE);
                    long currentTime = System.currentTimeMillis();

                    AccountBankReq dataRequest = PARSER.fromJson(request.getJsonObject("data", new JsonObject()).toString(), AccountBankReq.class);
                    //Step 1: Valid base request
                    if (!BaseRequest.isValidBaseDataRequest(logId, buffer.toJsonObject())) {
                        ResponseUtil.responseError(logId, requestId, response, ResponseUtil.HTTP_BAD_REQUEST, ErrorConstants.BAD_FORMAT_DATA);
                        return;
                    }
                    logger.info("{}| Valid base data request success!", logId);

                    //Step 2: Valid request data
                    if (!ValidateData.isValidDataRequest(logId, dataRequest)) {
                        ResponseUtil.responseError(logId, requestId, response, ResponseUtil.HTTP_BAD_REQUEST, ErrorConstants.BAD_FORMAT_DATA);
                        return;
                    }
                    logger.info("{}| Valid data request success!", logId);

                    //Step 3: Valid partner
                    MySqlManager.getPartnerByPartnerCode(logId, dataRequest.getPartnerCode(), partnerInf -> {
                        if (partnerInf == null || partnerInf.getId() <= 0 || !partnerInf.isAcive()) {
                            logger.warn("{}| Partner - {} not existed!", logId, dataRequest.getPartnerCode());
                            ResponseUtil.responseError(logId, requestId, response, ResponseUtil.HTTP_BAD_REQUEST, ErrorConstants.BAD_FORMAT_DATA);
                            return;
                        }
                        logger.info("{}| Valid partner information success!", logId);

                        //Step 4: Valid expire request, default 3'
                        if (currentTime - dataRequest.getRequestTime() < TIME_EXPIRE) {
                            logger.warn("{}| Request was {} minutes overdue!", logId, currentTime - dataRequest.getRequestTime());
                            ResponseUtil.responseError(logId, requestId, response, ResponseUtil.HTTP_BAD_REQUEST, ErrorConstants.TIME_EXPIRED);
                            return;
                        }
                        logger.info("{}| Valid request time success!", logId);

                        //Step 5: Valid hash with SHA256 by SecretKey
                        JsonObject hashObject = new JsonObject();
                        hashObject.put(StringConstant.NUMBER_ACCOUNT, dataRequest.getNumberAccount());
                        hashObject.put(StringConstant.PARTNER_CODE, dataRequest.getPartnerCode());
                        hashObject.put(StringConstant.REQUEST_TIME, dataRequest.getRequestTime());
                        hashObject.put(StringConstant.TYPE_ACCOUNT, dataRequest.getTypeAccount());

                        String hashGen = DataUtil.createHash(hashObject, partnerInf.getSecretKey(), logId);
                        logger.info("{}| LHBank hash - Partner hash: {}", logId, hashGen);
                        if (!hashGen.equalsIgnoreCase(dataRequest.getHash())) {
                            logger.warn("{}| Valid signature: Fail!", logId);
                            ResponseUtil.responseError(logId, requestId, response, ResponseUtil.HTTP_BAD_REQUEST, ErrorConstants.HASH_NOT_VALID);
                            return;
                        }
                        logger.info("{}| Validate hash: Success!", logId);

                        //Step 6: Response
                        //todo: build response format
                        MySqlManager.getAccountBankInfo(logId, dataRequest.getNumberAccount(), dataRequest.getTypeAccount(), accountInfo -> {
                            String dataResponse = new BaseResponse(requestId, ErrorConstants.SUCCESS, AccountBankRes.parseToAccountBankRes(accountInfo).toJson()).toString();
                            logger.info("{}| Response to client: {}", logId, dataResponse);
                            ResponseUtil.responseToClient(logId, response, dataResponse, ResponseUtil.HTTP_OK);
                        });

                    });
                }
                catch (Exception ex) {
                    logger.error("{}| Request catch exception: ", logId, ex);
                    ResponseUtil.responseError(logId, buffer.toJsonObject().getString(StringConstant.REQUEST_TYPE, ""),
                            response, ResponseUtil.HTTP_BAD_REQUEST, ErrorConstants.BAD_FORMAT_DATA);
                }
            });
        });
        httpServer.requestHandler(router).listen(1123);
    }

    private JsonObject responseBody(JsonObject dataResponse, String requestId, int resultCode, String message) {
        return new JsonObject()
                .put("requestId", requestId)
                .put("resultCode", resultCode)
                .put("message", message)
                .put("dataResponse", dataResponse);
    }
}
