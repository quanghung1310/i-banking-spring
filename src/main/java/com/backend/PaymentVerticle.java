package com.backend;

import com.backend.cryption.pgp.PGPHelper;
import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.openpgp.PGPException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

public class PaymentVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(PaymentVerticle.class);
    private static final Gson PARSER = new Gson();

    @Override
    public void start() {
//        HttpServer httpServer = vertx.createHttpServer();
//        Router router = Router.router(vertx);
//
//        router.post("/transfer/bank").consumes("application/json").produces("application/json").handler(ctx -> {
//            LogUtil.logHttpRequest(LOGGER, ctx.request());
//            final String logId = DataUtil.createRequestId();
//            HttpServerResponse response = ctx.response();
//            ctx.request().bodyHandler(buffer -> {
//                String privateKey = PartnerConfig.getPrivateKey();
//                String data = new JsonObject().put("value", "lang123").toString();
//                String hashRSA = Misc.encryptRSA("1", data, PartnerConfig.getPublicKey());
////                String hash = "BphKmdC2XlkFt1XeyXoNr4l1pzrFgFIQtM60RhqtOAn9mG9tmNF4dgATy0fgQvvsqLr9UyXxAZtF61ZPE3Lij82CHXkCb8Z+n1DkZwnwQup3b8ivWXYoMzFLIxV4nAvfXAko1fMy1pICOfWFcEAKvjPqwBu6d+o9R9IAuJ6d2Qc=";
////                String parseHash = Misc.parseRSAData(logId, hash, privateKey);
//                MySqlManager.getPartnerByPartnerCode(logId, "VCB_123", partnerInf -> {
//                    //todo
//                        });
//                DeliveryOptions options = new DeliveryOptions()
//                        .addHeader(DatabaseConstants.ACTION, DatabaseConstants.QUERY_GET_PARTNER)
//                        .addHeader(DatabaseConstants.REQUEST_ID_UPPER, logId);
//                String finalLogId = logId;
//                vertx.eventBus().request(DatabaseVerticle.class.getName(), data, options, event -> {
//
//                });
//                ResponseUtil.responseToClient(logId, response,
//                        responseBody(new JsonObject(), logId, 0, hashRSA).toString(), ResponseUtil.HTTP_OK);
//                return;
//            });
//        });
//
//        httpServer.requestHandler(router).listen(1123);
    }

    private JsonObject responseBody(JsonObject dataResponse, String requestId, int resultCode, String message) {
        return new JsonObject()
                .put("requestId", requestId)
                .put("resultCode", resultCode)
                .put("message", message)
                .put("dataResponse", dataResponse);
    }

    public static void main(String[] args) throws PGPException, NoSuchAlgorithmException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException {
        PGPHelper.genKeyPair();
    }
}
