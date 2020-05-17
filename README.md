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
|requestType|String|20|x|L1|Bắt buộc là GET_BANK_ACCOUNT_INFO|
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
    "description": "Nop tien tu Vietcombank vao LangBank",
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
|requestType|String|20|x|L1|Bắt buộc là GET_BANK_ACCOUNT_INFO|
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




