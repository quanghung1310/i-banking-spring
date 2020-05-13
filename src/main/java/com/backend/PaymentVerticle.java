package com.backend;

import com.backend.config.PartnerConfig;
import com.backend.response.BaseResponse;
import com.backend.util.DataUtil;
import com.backend.util.LogUtil;
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
                String priavteKey = PartnerConfig.getPrivateKey();
                ResponseUtil.responseToClient(logId, response,
                        responseBody(new JsonObject(), logId, 0, priavteKey).toString(), ResponseUtil.HTTP_OK);
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
