package com.backend.controller;

import com.backend.constants.ErrorConstant;
import com.backend.firebase.FcmClient;
import com.backend.model.request.notify.PushNotificationRequest;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.NotifyResponse;
import com.backend.model.response.PushNotificationResponse;
import com.backend.model.response.UserResponse;
import com.backend.service.INotifyService;
import com.backend.service.IUserService;
import com.backend.service.notify.PushNotificationService;
import com.backend.util.DataUtil;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class PushNotificationController {
    private static final Logger logger = LogManager.getLogger(UserController.class);

    private PushNotificationService pushNotificationService;
    private final FcmClient fcmClient;
    final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private IUserService userService;
    private INotifyService notifyService;


    public PushNotificationController(PushNotificationService pushNotificationService, FcmClient fcmClient, IUserService userService, INotifyService notifyService) {
        this.pushNotificationService = pushNotificationService;
        this.fcmClient = fcmClient;
        this.userService = userService;
        this.notifyService = notifyService;
    }

    @PostMapping("/notification-token")
    public ResponseEntity sendNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotificationToToken(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }

//    @GetMapping("/notification")
//    public ResponseEntity sendSampleNotification() {
//        pushNotificationService.sendSamplePushNotification();
//        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
//    }

    @GetMapping("/notification")
    public ResponseEntity<String> getNotification() {
        String logId = DataUtil.createRequestId();
        BaseResponse response;
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponse user = getUser(logId, principal);
            long userId = user.getId();

            NotifyResponse notifyResponse = notifyService.getNotification(logId, userId);

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, notifyResponse.toString());
            logger.info("{}| Response to client: {}", logId, response.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request get notification catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/notification-fcm")
    public ResponseEntity<SseEmitter> doNotify() throws IOException {
        final SseEmitter emitter = new SseEmitter();
        fcmClient.addEmitter(emitter);
        fcmClient.doNotify();
        emitter.onCompletion(() -> fcmClient.removeEmitter(emitter));
        emitter.onTimeout(() -> fcmClient.removeEmitter(emitter));
        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }

    @PostMapping(value = "/seen-notification")
    public ResponseEntity<String> seenNotification(@RequestBody String requestData) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: {}", logId, requestData);
        BaseResponse response;
        try {
            JsonObject request = new JsonObject(requestData);
            if (request.isEmpty()) {
                logger.warn("{}| Request not empty", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            int notifyId = request.getInteger("notifyId");
            if (notifyId < 0) {
                logger.warn("{}| notifyId bad data", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            NotifyResponse notifyResponse = notifyService.updateSeenNotification(logId, user.getId(), notifyId);

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, notifyResponse.toString());
            logger.info("{}| Response to client: {}", logId, response.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);

        } catch (Exception ex) {
            logger.error("{}| Request get users catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }
    private UserResponse getUser(String logId, Object principal) {
        return userService.getUser(logId, ((UserDetails)principal).getUsername());
    }
}
