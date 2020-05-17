# "Internet Banking" Document

|Version | Date       | Author    | Description         |
|------- | ---------- | --------- | ------------------- |
|1.0     | 15-05-2020 | Tran Thi Lang | Init document       |
# I. Getting Started
1. What You Need
    * JDK 1.8
    * IDE: IntelliJ IDEA
    * Lombok
    * Gradle 4+ or Maven 3.2+
    * MySql
2. Run application:
    * Step 1: open "Edit Run/Debug configurations "
    * Step 2: "Configuration" tab -> Enter "com.backend.Application" to Main Class
    * Step 3: Run/ Debug
    * 
# II. API Document
# Index

1. [Get Bank Account Info](#1-get-bank-account-info)
2. [Transfer Bank](#2-transfer-bank)

# 1. Get Bank Account Info

## Raw Data
**Request:**

```json
{
  "requestId": "0e28ddd4-4017-decf-8ade-972e8c4d0cc6",
  "initiator": "abc",
  "requestType": "GET_BANK_ACCOUNT_INFO",
  "data": {
	    "requestTime": 1555472829549,
	    "partnerCode": "Merchant123556666",
	    "numberAccount": "23645895232623",
	    "typeAccount": 1,
	    "hash": "cd0d82ad983098a2fb99b8e49266ed7bd4db85ebf77d13b2db2f755ff0600fa0",
	    "description": "kiểm tra số dư tài khoản thanh toán"
  }
}
```

**Response:**
```json
{
  "requestId": "0e28ddd4-4017-decf-8ade-972e8c4d0cc6",
  "resultCode": 0,
  "message": "success",
  "data": {
        "responseTime": 1555472829580,
        "customerName": "TRAN THI LANG",
        "customerPhone": "0963****714",
        "customerEmail": "tranlang.dtnt@gmail.com",
        "isActive": true,
        "agency": 1, 
        "openDate": "2018-07-12 09:46:32",
        "closeDate": "2020-07-12 09:46:33"
  }
}
```

**Request:**

|Name|Type|Length|Required|Level|Description|
|----|----|:----:|:------:|:---:|-----------|
|requestId|String|50|x|L1|Định danh mỗi yêu cầu|
|initiator|String|15|x|L1|Định danh người gửi yêu cầu(đối tác/ người dùng)|
|requestType|String|20|x|L1|Bắt buộc là GET_BANK_ACCOUNT_INFO|
|**data**|**JsonObject**||**x**|**L1**||
|requestTime|long||x|L2|Thời gian gọi request (tính theo millisecond) Múi giờ: GMT +7|
|partnerCode|String|50|x|L2|Mã đối tác|
|numberAccount|String|15|x|L2|Số tài khoản cần truy vấn|
|typeAccount|int||x|L2|Loại tài khoản [1: thanh toán, 2: tiết kiệm]|
|hash|String|1000|x|L2|Chữ ký để kiểm tra thông tin. Sử dụng thuật toán HMAC_SHA256. Dữ liệu đầu vào bao gồm SecretKey (LHBank cung cấp) và data, data được tạo ra theo định dạng:  numberAccount=$numberAccount&partnerCode=$partnerCode&requestTime=$requestTime&typeAccount=$typeAccount|
|description|String|50||L2|Thông tin thêm|


**Response:**

|Name|Type|Length|Required|Level|Description|
|----|----|:----:|:------:|:---:|-----------|
|requestId|String|50|x|L1|Giống với yêu cầu ban đầu|
|resultCode|number|2|x|L1|Kết quả của request|
|message|String|24|x|L1|Mô tả chi tiết kết quả request|
|**data**|**JsonObject**||**x**|**L1**||
|responseTime|long||x|L2|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|customerName|String|50|x|L2|Tên chủ tài khoản|
|customerPhone|String|10|x|L2|Số điện thoại chủ tài khoản,  chỉ hiển thị 4 số đầu và 3 số cuối|
|customerEmail|String|100|x|L2|Địa chỉ mail của khách hàng|
|isActive|boolean||x|L2|true: Tài khoản còn hoạt động, false: Tài khoản bị khóa|
|agency|int||x|L2|Mã chi nhánh ngân hàng của tài khoản|
|openDate|String|19|x|L2|Ngày tạo tài khoản Định dạng: yyyy-MM-dd HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|
|closeDate|String|19|x|L2|Ngày tài khoản hết hạn Định dạng: yyyy-MM-dd HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|



# 2. Transfer Bank

## Raw Data
**Request:**
```json
{
  "requestId": "0e28ddd4-4017-decf-8ade-972e8c4d0cc6",
  "initiatior": "VIETCOMBANK",
  "requestType": "TRANSFER_BANK",
  "data": {
    "requestTime": 1555472829549,
    "partnerCode": "VCB_1",
    "partnerRefId": "VCB_t123556666",
    "amount": 40000,
    "accountNumber": 1234567852135,
    "hash": "b6e7302c7a2df244bc76e3592b2e3f7ff39abc2a3b6ea161830acea57a427b5f",
    "signature": "A7WFmmnpn6TRX42Akh/....Gr/0BQUWgunpDPrmCosf9A==",
    "description": "Nop tien tu Vietcombank vao LangBank"
  }
}
```

**Response:**
```json
{
  "requestId": "0e28ddd4-4017-decf-8ade-972e8c4d0cc6",
  "resultCode": 0,
  "message": "success",
  "data": {
  }
}
```

**Request:**

|Name|Type|Length|Required|Level|Description|
|----|----|:----:|:------:|:---:|-----------|
|requestId|String|50|x|L1|Định danh mỗi yêu cầu|
|initiator|String|15|x|L1|Định danh người gửi yêu cầu(đối tác/ người dùng)|
|requestType|String|20|x|L1|Bắt buộc là TRANSFER_BANK|
|**data**|**JsonObject**||**x**|**L1**|**detail**|
|||||||


**Response:**

|Name|Type|Length|Required|Level|Description|
|---|---|:---:|:---:|:--:|---------------|
|requestId|String|50|x|L1|Giống với yêu cầu ban đầu|
|resultCode|number|2|x|L1|Mã lỗi|
|message|String|24|x|L1|Mô tả lỗi|
|**data**|**JsonObject**||**x**|**L1**|**detail**|
|||||||




