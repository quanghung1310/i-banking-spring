package com.backend.constants;

import java.util.HashMap;

public class ErrorConstant {

    public static final int SUCCESS = 0;//	success
    public static final int TRANS_NOT_FOUND = 10;
    public static final int SYSTEM_ERROR = 2;
    public static final int CHECK_SIGNATURE_FAIL = 3;
    public static final int TRANS_PAID = 5;
    public static final int BAD_FORMAT_DATA = 6;
    public static final int REQUEST_ID_EXISTED = 7;
    public static final int TIME_EXPIRED = 8;
    public static final int HASH_NOT_VALID = 9;
    public static final int BAD_REQUEST = -1;
    public static final int NOT_EXISTED = 1;
    public static final int CANT_MINE = 11;

    private static final HashMap<Integer, String> errorMap = new HashMap<>();

    static {
        errorMap.put(SUCCESS, "Thành công");
        errorMap.put(TRANS_NOT_FOUND, "Giao dịch không tìm thấy. Vui lòng kiểm tra lại");
        errorMap.put(SYSTEM_ERROR, "Hệ thống xảy ra lỗi. Vui lòng liên hệ quản trị viên");
        errorMap.put(CHECK_SIGNATURE_FAIL, "Signature không đúng. Vui lòng kiểm tra lại");
        errorMap.put(TRANS_PAID, "Giao dịch đã được xử lý .Vui lòng kiểm tra lại");
        errorMap.put(BAD_FORMAT_DATA, "Dữ liệu sai định dạng");
        errorMap.put(BAD_REQUEST, "Yêu cầu không tồn tại");
        errorMap.put(REQUEST_ID_EXISTED, "RequestId đã tồn tại. Vui lòng tạo requestId mới");
        errorMap.put(CANT_MINE, "Người dùng không thể mining");
        errorMap.put(TIME_EXPIRED, "Chữ ký không hợp lệ");
    }

    public static String getMessage(int errorCode) {
        String s = errorMap.get(errorCode);
        if (s == null) {
            return "Có lỗi trong quá trình xử lý, rất xin lỗi vì sự bất tiện này. Vui lòng thử lại sau, xin cám ơn!";
        }
        return s;
    }
}
