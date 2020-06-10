package com.backend.database;

import com.backend.config.MainConfig;
import com.backend.constant.DatabaseConstants;
import com.backend.constant.StringConstant;
import com.backend.util.Misc;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(DatabaseVerticle.class);
    public static JDBCClient jdbcClient;

    private static final String DEFAULT_EMAIL           = "tranlang.dtnt@gmail.com";
    private static final String DEFAULT_PHONE_NUMBER    = "0327421137";
    private static final String DEFAULT_PASSWORD        = "123456789";

    @Override
    public void start() throws Exception {
        jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
                .put("jdbcUrl", MainConfig.getOracleDbConfig().getString("jdbcUrl"))
                .put("driverClassName", "com.mysql.jdbc.Driver")
                .put("username", MainConfig.getOracleDbConfig().getString("username"))
                .put("password", MainConfig.getOracleDbConfig().getString("password"))
                .put("maximumPoolSize", MainConfig.getOracleDbConfig().getInteger("max_pool_size"))
        );

        vertx.eventBus().consumer(DatabaseVerticle.class.getName(), event -> {
            String action = event.headers().get(DatabaseConstants.ACTION);
            String requestId = event.headers().get(DatabaseConstants.REQUEST_ID_UPPER);
            JsonObject requestData = (JsonObject) event.body();
            try {
                switch (action) {
                    case DatabaseConstants.QUERY_GET_PARTNER:
                        MySqlManager.getPartnerByPartnerCode(
                                requestId,
                                requestData.getString(StringConstant.PARTNER_CODE, StringUtils.EMPTY),
                                event::reply);
                        break;
                    case DatabaseConstants.QUERY_INSERT_PARTNER:
                        insertPartner(requestId, requestData, event);
                    default:
                        LOGGER.warn("{}| Unsupported action: {}", requestId, action);
                        event.fail(2, "Unsupported action: " + action);
                        break;
                }
            } catch (Exception e) {
                LOGGER.error("{}| Something went wrong with action: {}", requestId, action, e);
                event.fail(1, "Bad action: " + action);
            }
        });
    }
    private static void insertPartner(String logId, JsonObject data, Message<Object> event) {
        String partnerCode  = data.getString(StringConstant.PARTNER_CODE, "TEST" + System.currentTimeMillis());
        String privateKey   = data.getString(StringConstant.PRIVATE_KEY, StringUtils.EMPTY);
        String publicKey    = data.getString(StringConstant.PUBLIC_KEY, StringUtils.EMPTY);
        String email        = data.getString(StringConstant.EMAIL, DEFAULT_EMAIL);
        String phoneNumber  = Misc.convertToNewPhone(data.getString(StringConstant.PHONE_NUMBER, DEFAULT_PHONE_NUMBER));
        String password     = data.getString(StringConstant.PASSWORD, DEFAULT_PASSWORD);

        MySqlManager.insertPartner(logId, partnerCode, privateKey, publicKey, email, phoneNumber, password, event::reply);
    }
}
