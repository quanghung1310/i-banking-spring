package com.backend;

import com.backend.config.PartnerConfig;
import com.backend.util.DataUtil;
import com.backend.util.LogUtil;
import com.backend.util.Misc;
import com.backend.util.ResponseUtil;
import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PaymentVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(PaymentVerticle.class);
    private static final Gson PARSER = new Gson();

    @Override
    public void start() {
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.post("/payment").consumes("application/json").produces("application/json").handler(ctx -> {
            LogUtil.logHttpRequest(LOGGER, ctx.request());
            final String logId = DataUtil.createRequestId();
            HttpServerResponse response = ctx.response();
            ctx.request().bodyHandler(buffer -> {
                String privateKey = PartnerConfig.getPrivateKey();
                String data = new JsonObject().put("value", "lang123").toString();
                String hashRSA = Misc.encryptRSA("1", data, PartnerConfig.getPublicKey());
//                String hash = "BphKmdC2XlkFt1XeyXoNr4l1pzrFgFIQtM60RhqtOAn9mG9tmNF4dgATy0fgQvvsqLr9UyXxAZtF61ZPE3Lij82CHXkCb8Z+n1DkZwnwQup3b8ivWXYoMzFLIxV4nAvfXAko1fMy1pICOfWFcEAKvjPqwBu6d+o9R9IAuJ6d2Qc=";
//                String parseHash = Misc.parseRSAData(logId, hash, privateKey);
                ResponseUtil.responseToClient(logId, response,
                        responseBody(new JsonObject(), logId, 0, hashRSA).toString(), ResponseUtil.HTTP_OK);
                return;
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
