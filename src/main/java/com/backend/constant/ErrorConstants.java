package com.backend.constant;

import java.util.HashMap;

public class ErrorConstants {

    public static final int SUCCESS = 0;//	success
    public static final int PARTNER_NOT_FOUND = 1;
    public static final int SYSTEM_ERROR = 2;
    public static final int SDK_CHECK_SIGNATURE_FAIL = 3;
    public static final int SDK_BILL_NOT_EXIST_OR_EXPIRED = 4;
    public static final int SDK_BILL_PAID = 5;
    public static final int BAD_FORMAT_DATA = 6;
    public static final int REQUEST_ID_EXISTED = 7;
    public static final int TIME_EXPIRED = 8;
    public static final int HASH_NOT_VALID = 9;

    //ERROR ONLINE STORE
    public static final int BAD_REQUEST = -1;
    public static final int DATABASE_ERROR = 205;
    public static final int NOT_EXISTED = 1;

    private static final HashMap<Integer, String> errorMapEn = new HashMap<>();
    private static final HashMap<Integer, String> errorMapVn = new HashMap<>();

    static {
        errorMapEn.put(SUCCESS, "Success");
        errorMapEn.put(PARTNER_NOT_FOUND, "Partner is not active/create");
        errorMapEn.put(SYSTEM_ERROR, "System error");
        errorMapEn.put(SDK_CHECK_SIGNATURE_FAIL, "Signature not match");
        errorMapEn.put(BAD_FORMAT_DATA, "Bad format data");
        errorMapEn.put(BAD_REQUEST, "Request not existed");
        errorMapEn.put(REQUEST_ID_EXISTED, "RequestId already exists. Please create a new requestId");
        errorMapVn.put(TIME_EXPIRED, "Request expired, please try again");
    }

    static {
        errorMapVn.put(SUCCESS, "Thành công");
        errorMapVn.put(PARTNER_NOT_FOUND, "Dữ liệu đối tác không được tìm thấy hoặc chưa được kích hoạt");
        errorMapVn.put(SYSTEM_ERROR, "Hệ thống xảy ra lỗi. Vui lòng liên hệ quản trị viên");
        errorMapVn.put(SDK_CHECK_SIGNATURE_FAIL, "Signature không đúng. Vui lòng kiểm tra lại");
        errorMapVn.put(SDK_BILL_PAID, "Giao dịch đã được xử lý. Vui lòng kiểm tra lại");
        errorMapVn.put(BAD_FORMAT_DATA, "Dữ liệu sai định dạng");
        errorMapVn.put(BAD_REQUEST, "Yêu cầu không tồn tại");
        errorMapVn.put(REQUEST_ID_EXISTED, "RequestId đã tồn tại. Vui lòng tạo requestId mới");
        errorMapVn.put(TIME_EXPIRED, "Yêu cầu hết hạn, vui lòng thử lại!");
        errorMapVn.put(TIME_EXPIRED, "Chữ ký không hợp lệ");
    }

    public static String getDescEn(int errorCode) {
        String s = errorMapEn.get(errorCode);
        if (s == null) {
            return "There was an error during processing, so sorry for the inconvenience. Please try again later, thank you!";
        }
        return s;
    }

    public static String getDescVn(int errorCode) {
        String s = errorMapVn.get(errorCode);
        if (s == null) {
            return "Có lỗi trong quá trình xử lý, rất xin lỗi vì sự bất tiện này. Vui lòng thử lại sau, xin cám ơn!";
        }
        return s;
    }
}
