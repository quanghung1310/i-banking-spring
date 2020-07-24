# LH-BANK

|Version | Date       | Author    | Description         |
|------- | ---------- | --------- | ------------------- |
|1.0     | 03-07-2020 | Tran Thi Lang | Init document       |
|1.1     | 05-07-2020 | Tran Thi Lang | Users API       |

# I. API Document
# Index
0. [Authenticate](#0-authenticate)
1. [Register](#1-register)
2. [Deposit](#2-deposit)
3. [Get Accounts](#3-get-accounts)
4. [Login](#4-login)
5. [Create Reminder](#5-create-reminder)
6. [Get Reminders](#6-get-reminders)
7. [Query Account](#7-query-account)
8. [Create Debt](#8-create-debt) 
9. [Get Debts](#9-get-debts) 
10. [Account Bank](#10-account-bank)
11. [Delete Debt](#11-delete-debt)
12. [Update Reminder](#12-update-reminder)
13. [Get Banks](#13-get-banks)





# II. API Document
# 0. Authenticate
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:8080/lh-bank/authenticate       | 
|Method | POS       | 
## Raw Data
**HTTP Request:**
```json
{
    "userName": "tranthilang",
    "password": "mvfkvuztexxbwxz"
}
```
**Response:**
```json
{
    "bearerToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cmFudGhpbGFuZyIsImV4cCI6MTU5NTYyMTQyNywiaWF0IjoxNTk1NTg1NDI3fQ.BheYyeb0zlEZg14hpS1T6BCc05p_TDyKBzany6kWAQc"
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|userName|String|x|L1|Tên đăng nhập|
|password|String|x|L1|Mật khẩu|
**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|bearerToken|String|x|L1|token|

# 1. Register
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:8080/lh-bank/register       | 
|Method | POST       | 
## Raw Data
**HTTP Request:**

```json
{
    "email": "tranlang.dtnt@gmail.com",
    "name": "Tran Thi Lang",
    "phone": "0327421137",
    "cardName": "Lang Lang",
    "adminId": 1
}
```

**Response:**
```json
{
    "requestId": "1867e7a504c24ac082b3645f67bb791c",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1593768407676,
    "data": {
        "userName": "tranthilang",
        "password": "bjfvpzhnebzhvoo",
        "createDate": "03/07/2020 16:26:45",
        "account": {
            "id": 20,
            "cardNumber": 13,
            "cardName": "Lang Lang",
            "closeDate": "02/07/2024 16:26:45",
            "createdAt": "03/07/2020 16:26:45",
            "updatedAt": "03/07/2020 16:26:45",
            "description": null,
            "type": 1
        }
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|email|String|x|L2|Địa chỉ email|
|name|String|x|L2|Tên khách hàng|
|phone|String|x|L2|Số điện thoại (Đầu số mới)|
|cardName|String|x|L2|Định danh thẻ|
|adminId|Number|x|L2|Định danh employee thực hiện tạo tài khoản|

**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.userName|String|x|L2|Tên đăng nhập|
|data.password|String|x|L2|Mật khẩu|
|data.createDate|String|x|L2|Thời gian tạo tài khoản - dd/MM/yyyy HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|
|data.account.id|Number|x|L3|Định danh tài khoản|
|data.account.cardNnumber|Number|String|x|L3|Số tài khoản|
|data.account.cardName|String|x|L3|Tên tài khoản|
|data.account.closeDate|String|x|L3|Hạn sử dụng tài khoản|
|data.account.createdAt|String|x|L3|Ngày tạo tài khoản|
|data.account.description|String||L3|Thông tin thêm |
|data.account.type|Number|x|L3|Loại tài khoản: 1 - Tài khoản thanh toán, 2 - Tài khoản tiết kiệm( Luôn luôn là 1)|
|data.account.updatedAt|String|x|L3|Thời gian chỉnh sửa thông tin tài khoản gần nhất|

# 2. Deposit
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:8080/lh-bank/deposit       | 
|Method | POST       | 
## Raw Data
**HTTP Request:**

```json
{
    "userName": "langlang4",
    "cardNumber": 1670707699074197,
    "balance": 1000000
}
```

**Response:**
```json
{
    "requestId": "1867e7a504c24ac082b3645f67bb791c",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1593768407676,
    "data": {
        "totalBalance": 15300000
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|userName|String|x|L2|Tên đăng nhập (Nếu để trống -> phải truyền cardNumber)|
|cardNumber|String|x|L2|Số tài khoản (Nếu để trống phải truyền userName)|
|balance|Number|x|L2|Số tiền cần nạp vào tài khoản|

**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.totalBalance|Number|x|L2|Tổng số dư|

# 3. Get Accounts
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:8080/lh-bank/get-accounts/{type}      | 
|Method | GET       | 
|Authorization| Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cmFudGhpbGFuZyIsImV4cCI6MTU5NTU5NzI3NiwiaWF0IjoxNTk1NTYxMjc2fQ.cyWnQadmHjSPqowU-dBkB5CX1YWE-TU3_4ru5QGUFM8|

## Raw Data
**HTTP Request:**
127.0.0.1:1111/lh-bank/get-accounts/1
**Response:**
```json
{
    "requestId": "5c50bdb1ed2f4363a9f7c3d3aeae4c8f",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1595588261056,
    "data": {
        "accounts": [
            {
                "id": 2,
                "cardNumber": 1006530338737501,
                "cardName": "Tran Thi Lang",
                "closeDate": "09/07/2024 08:17:37",
                "createdAt": "10/07/2020 08:17:37",
                "updatedAt": "21/07/2020 09:30:30",
                "description": null,
                "type": "payment",
                "balance": 3094000,
                "userId": 1
            }
        ]
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|userId|Number|x|PathVariable|Định danh chủ tài khoản|
|type|String||PathVariable|"payment" - Tài khoản thanh toán, "saving" - Tài khoản tiết kiệm, không truyền - lấy hết|
**Response:*|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.accounts.id|Number|x|L3|Định danh tài khoản|
|data.accounts.userId|Number|x|L3|Định danh chủ tài khoản|
|data.accounts.cardNnumber|Number|String|x|L3|Số tài khoản|
|data.accounts.cardName|String|x|L3|Tên tài khoản|
|data.accounts.closeDate|String|x|L3|Hạn sử dụng tài khoản|
|data.accounts.createdAt|String|x|L3|Ngày tạo tài khoản|
|data.accounts.description|String||L3|Thông tin thêm |
|data.accounts.type|Number|x|L3|Loại tài khoản: 1 - Tài khoản thanh toán, 2 - Tài khoản tiết kiệm|

# 4. Login
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:8080/lh-bank/login       | 
|Method | POS       | 
|Authorization| Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cmFudGhpbGFuZyIsImV4cCI6MTU5NTU5NzI3NiwiaWF0IjoxNTk1NTYxMjc2fQ.cyWnQadmHjSPqowU-dBkB5CX1YWE-TU3_4ru5QGUFM8|
## Raw Data
**HTTP Request:**
```json
{
    "userName": "tranlang",
    "password": "yvlikcfnzywqdcj"
}
```
**Response:**
```json
{
    "requestId": "09633a244ff544f9b52505612f3415e9",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1595585601360,
    "data": {
        "id": 1,
        "email": "tranthilang.dtnt@gmail.com",
        "name": "Tran Thi Lang",
        "phone": "0327421137",
        "createdAt": "10/07/2020 08:17:37"
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|userName|String|x|L1|Tên đăng nhập|
|password|String|x|L1|Mật khẩu|
**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.id|Number|x|L2|Định danh user|
|data.userName|String|x|L2|Tên đăng nhập|
|data.password|String|x|L2|Mật khẩu|
|data.createdAt|String|x|L2|Thời gian tạo tài khoản - dd/MM/yyyy HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|
|data.email|String|x|L2|Địa chỉ email|
|data.name|String|x|L2|Tên khách hàng|
|data.phone|String|x|L2|Số điện thoại (Đầu số mới)

# 5. Create Reminder
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:8080/lh-bank/create-reminder       | 
|Method | POST       | 
|Authorization| Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cmFudGhpbGFuZyIsImV4cCI6MTU5NTU5NzI3NiwiaWF0IjoxNTk1NTYxMjc2fQ.cyWnQadmHjSPqowU-dBkB5CX1YWE-TU3_4ru5QGUFM8|

## Raw Data
**HTTP Request:**

```json
{
    "nameReminisce": "Debtor 005",
    "cardNumber": 1915954019734406,
    "type": 1,
    "merchantId": 2
}
```

**Response:**
```json
{
    "requestId": "63e6a633c5c14d47a5640038e928d9ce",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1595607151592,
    "data": {
        "id": 1,
        "email": "tranthilang.dtnt@gmail.com",
        "name": "Tran Thi Lang",
        "phone": "0327421137",
        "createdAt": "10/07/2020 08:17:37",
        "account": [
            {
                "id": 0,
                "cardNumber": 1915954019734406,
                "cardName": "Debtor 005",
                "reminderId": 42,
                "typeReminder": "send",
                "merchantId": 2
            }
        ]
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|nameReminisce|String||L1|Tên gợi nhớ, Mặc định tên user|
|cardNumber|String|x|L1|Số tài khoản|
|type|Number|x|L1|Loại tài khoản cần lưu, 1 - chuyển tiền, 2 - Nhắc nợ|
|merchantId|Number||L1|Định danh ngân hàng, Mặc định là myBank|

**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.id|Number|x|L2|Định danh user|
|data.createdAt|String|x|L2|Thời gian tạo tài khoản - dd/MM/yyyy HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|
|data.email|String|x|L2|Địa chỉ email|
|data.name|String|x|L2|Tên khách hàng|
|data.phone|String|x|L2|Số điện thoại (Đầu số mới)|
|data.accounts.cardNumber|Number|x|L3|Số tài khoản đã lưu gợi nhớ|
|data.accounts.cardName|String|x|L3|Tên gợi nhớ|
|data.accounts.reminderId|Number|x|L3|Định danh gợi nhớ|
|data.accounts.typeReminder|String|x|L3|Loại gợi nhớ đã lưu ("send" = 1: chuyển tiền, "deb" = 2: Nhắc nợ)|
|data.accounts.merchantId|Number|x|L3|Định danh tài khoản gợi nhớ là liên ngân hàng hay cùng ngân hàng|


# 6. Get Reminders
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:8080/lh-bank/get-reminders/{type}/{cardNumber}        | 
|Method | POST       | 
|Authorization| Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cmFudGhpbGFuZyIsImV4cCI6MTU5NTU5NzI3NiwiaWF0IjoxNTk1NTYxMjc2fQ.cyWnQadmHjSPqowU-dBkB5CX1YWE-TU3_4ru5QGUFM8|

## Raw Data
**HTTP Request:**
127.0.0.1:1111/lh-bank/get-reminders/2/1113797607879108
**Response:**
```json
{
    "requestId": "0ec08be20c4341459e488e0355ebefe6",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1595606338346,
    "data": {
        "id": 1,
        "email": "tranthilang.dtnt@gmail.com",
        "name": "Tran Thi Lang",
        "phone": "0327421137",
        "createdAt": "10/07/2020 08:17:37",
        "account": [
            {
                "cardNumber": 1113797607879108,
                "cardName": "Debtor 002",
                "reminderId": 39,
                "typeReminder": "debt",
                "merchantId": 1
            }
        ]
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|type|Number|x|PathVariable|Loại tài khoản ghi nhớ, 1 - chuyển tiền, 2 - Nhắc nợ|
|cardNumber|Number||PathVariable|Số tài khoản, Mặc định lấy hết các tài khoản đã lưu theo type|

**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.id|Number|x|L2|Định danh user|
|data.createdAt|String|x|L2|Thời gian tạo tài khoản - dd/MM/yyyy HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|
|data.email|String|x|L2|Địa chỉ email|
|data.name|String|x|L2|Tên khách hàng|
|data.phone|String|x|L2|Số điện thoại (Đầu số mới)
|data.accounts.cardNumber|Number|x|L3|Số tài khoản đã lưu gợi nhớ|
|data.accounts.cardName|String|x|L3|Tên gợi nhớ|
|data.accounts.reminderId|Number|x|L3|Định danh gợi nhớ|
|data.accounts.typeReminder|String|x|L3|Loại gợi nhớ đã lưu ("send" = 1: chuyển tiền, "deb" = 2: Nhắc nợ)|
|data.accounts.merchantId|Number|x|L3|Định danh tài khoản gợi nhớ là liên ngân hàng hay cùng ngân hàng|

# 7. Get Account Info
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:1111/lh-bank/get-account-info/{cardNumber}/{merchantId}| 
|Method | GET       | 
|Authorization| Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cmFudGhpbGFuZyIsImV4cCI6MTU5NTU5NzI3NiwiaWF0IjoxNTk1NTYxMjc2fQ.cyWnQadmHjSPqowU-dBkB5CX1YWE-TU3_4ru5QGUFM8|
127.0.0.1:1111/lh-bank/get-account-info/1448127665849225/2
## Raw Data
**HTTP Request:**

**Response:**
```json
{
    "requestId": "e9c5aa1f2f024c68b0477c81e8e11e84",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1595616406834,
    "data": {
        "id": 5,
        "email": "tranthilang.dtnt@gmail.com",
        "name": "Lang Debtor",
        "phone": "0327421111",
        "createdAt": "20/07/2020 18:33:10",
        "account": [
            {
                "id": 6,
                "cardNumber": 1448127665849225,
                "cardName": "Tran Lang Debtor",
                "type": "payment",
                "userId": 5
            }
        ]
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|cardNumber|Number|x|PathVariable|Số tài khoản|
|merchantId|Number|x|PathVariable|Định danh ngân hàng|
**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.id|Number|x|L2|Định danh user|
|data.userName|String|x|L2|Tên đăng nhập|
|data.password|String|x|L2|Mật khẩu|
|data.createdAt|String|x|L2|Thời gian tạo tài khoản - dd/MM/yyyy HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|
|data.email|String|x|L2|Địa chỉ email|
|data.name|String|x|L2|Tên khách hàng|
|data.phone|String|x|L2|Số điện thoại (Đầu số mới)
|data.accounts.id|Number|x|L3|Định danh tài khoản|
|data.accounts.userId|Number|x|L3|Định danh chủ tài khoản|
|data.accounts.cardNnumber|Number|x|L3|Số tài khoản|
|data.accounts.cardName|String|x|L3|Tên tài khoản|
|data.accounts.type|Number|x|L3|Loại tài khoản: 1 - Tài khoản thanh toán, 2 - Tài khoản tiết kiệm|

# 8. Create Debt
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:8080/lh-bank/create-debtor       | 
|Method | POST       | 
## Raw Data
**HTTP Request:**

```json
{
    "debtorId": 3,
    "cardNumber": 1575750842294193,
    "userId": 1,
    "amount": 1000,
    "content": "Trả tiền đi má, nợ gì lâu "
}
```

**Response:**
```json
{
    "requestId": "0291bd9a5df44e25b36f15f372c45264",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1593951532323,
    "data": {
        "debtId": 11
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|cardNumber|String|x|L1|Số tài khoản của con nợ|
|debtorId|Number|x|L1|Định danh con nợ|
|userId|Number|x|L1|Người nhắc nợ|
|amount|Number|x|L1|Số tiền cần nhắc nợ|
|content|String|x|L1|Nội dung nhắc nợ|

**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.debtId|Number|x|L2|Định danh nhắc nợ|

# 9. Get Debts
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:8080/lh-bank/get-debts/{userId}/{action}/{type}| 
|Method | GET       | 
## Raw Data
**HTTP Request:**

**Response:**
```json
{
    "requestId": "479f65c15f564c5f97f602ae2633d084",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1593955000145,
    "data": {
        "debts": [
            {
                "creditorId": 1,
                "creditEmail": "tranlang.dtnt@gmail.com",
                "creditName": "Tran Lang",
                "creditPhone": "0327421137",
                "amount": 1500,
                "content": "Trả tiền đi má, nợ gì lâu ",
                "action": 1,
                "createdAt": "05/07/2020 20:11:42",
                "updatedAt": "05/07/2020 20:11:42"
            }
        ]
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|userId|Number|x|PathVariable|Người thực hiện hành động|
|action|Number|x|PathVariable|1 - Nhắc nợ được khởi tạo|
|type|Number|x|PathVariable|1 - Nhắc nợ do userId tạo, 2 - Nhắc nợ được gửi tới userId|

**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.debts.id|Number|x|L3|Định danh nhắc nợ|
|data.debts.creditorId|Number|x|L3|Định danh: người nhắc nợ (type = 2), người bị nhắc nợ(type = 1 )|
|data.debts.creditEmail|String|x|L3|Địa chỉ email: người nhắc nợ (type = 2), người bị nhắc nợ(type = 1)|
|data.debts.creditPhone|String|x|L3|Số điện thoại: người nhắc nợ (type = 2), người bị nhắc nợ(type = 1)|
|data.debts.amount|Number|x|L3|Số tiền nợ|
|data.debts.action|Number|x|L3|1 - Nợ được khởi tạo|
|data.debts.content|String|x|L3|Nội dung nhắc nợ|
|data.debts.createdAt|String|x|L3|Ngày khởi tạo - dd/MM/yyyy HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|
|data.debts.updatedAt|String|x|L2|Thời gian thay đổi gần nhất - dd/MM/yyyy HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|

# 10. Account Bank
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:1111/lh-bank/account/bank| 
|Method | POST       | 
|Content-Type| application/json |
|Body| JSON String |

## Raw Data
**HTTP Request:**
```json
{
  "cardNumber": 1006530338737501,
  "partnerCode": "PGP_BANK",
  "requestId": "0e28ddd4-4017-decf-8ade-972e8c4d0cc6",
  "requestTime": 1595147701989,
  "hash": "4e91d7a09b833a8bd5a3574095cf017772f4823f7c045ab78b3ef174348ea16e"
}
```

**Response:**
```json
{
    "requestId": "0e28ddd4-4017-decf-8ade-972e8c4d0cc6",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1595148815102,
    "data": {
        "id": 1,
        "userName": null,
        "password": null,
        "email": "tranthilang.dtnt@gmail.com",
        "name": "Tran Thi Lang",
        "phone": "0327421137",
        "createdAt": "10/07/2020 08:17:37",
        "account": [
            {
                "id": 2,
                "cardNumber": 1006530338737501,
                "cardName": "Tran Thi Lang",
                "closeDate": "09/07/2024 08:17:37",
                "createdAt": "10/07/2020 08:17:37",
                "updatedAt": "14/07/2020 14:21:12",
                "description": null,
                "type": 1,
                "balance": null,
                "userId": 1
            }
        ]
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|cardNumber|Number|x|L1|Số tài khoản|
|partnerCode|String|x|L1|Định danh đối tác (LH-Bank cung cấp)|
|requestId|String|x|L1|Định danh cho 1 request|
|requestTime|Number|x|L1|Thời gian gửi reuqest (tính bằng  milliseconds)|
|hash|String|x|L1|Hash data for security. Hash is a string was hashed by Hmac_SHA256 algorithm using partner secretKey with format cardNumber=**$cardNumber**&partnerCode=**partnerCode**&requestId=**requestId**&requestTime=**requestTime**|

- Sử dụng Secret Key (LH-Bank cung cấp) để mã hóa dữ liệu. Ví dụ:
    + Dữ liệu trước khi hash: cardNumber=**1006530338737501**&partnerCode=**PGP_BANK**&requestId=**0e28ddd4-4017-decf-8ade-972e8c4d0cc6**&requestTime=**1595147701989**
    + Dữ liệu được tạo ra sau khi sử dụng thuật toán **Hmac_SHA256** và **Secret Key** (lQIGBF8T+UMBBADHi3alEb5V09qlKGYe7HtHG0p/Fq5nZ9/f96D0ZuW1YnmhV5imk+SdJtQygjL9rjCmA8QRdjGXOoFFfUNBIkGrzW5bdkjsuHc9AU6wQxVAOWiEuiQjZ1l15QOLoU/ROlEpYEjy4MlX05R3H+NrxW5L2GkfAu/k51stXU6HkrUDBQARAQAB/gkDCJMIx1jUDw0RYFPPo+b1Rox71tDIt2LQieyh+9YsdbUk2gRqAAsjZo04oQdX1/jgDzfUHfSBxy1/uBaQ0qWZhe62nfjRXZB1Pf9FGzCtyBNfbt1IwFTSPlSCOi3bHfc20WK61hbHeIuw1u6cXSzPKpSS7xP3rohW+64vg31pPBzP+szSFfbFEKW/r+LOPZIbt8eZpH8pCUZbQXPG6fRE1+Dn8QwwAY8RZauFd/r2BkIdWUkSdj9djgwNJSzWtr/YDkLKkxGF2GyfA2HPoo+RwpQZ6r2mKgDYJvSNKjI7k+M4bjmoGj6sNsvzd+ta9LDENLBP9SW17cydA9Hve3OZLrgJug3VD0ErC8QnuNYTzSyBF/Fyt7JEEc9fxwEl73U1T6ec2lTlh+a2BjdXLNkW4lP3RglzIZZ9Sp2WdKCEOtKcmOboiOapzwPeQh8JvrlemDjAwFvxoBDrHci8AgZtHJ8Ha1QCE0N+nI0Xz9uuGxVIQ7a57em0G3BncGJhbmsgPHBncGJhbmtAZ21haWwuY29tPoitBBMBCgAXBQJfE/lDAhsvAwsJBwMVCggCHgECF4AACgkQn6e+iZkF7Mpw2wQAid6jmQVSWa1qJ60GK89i3cr7hZBqjXnfrX/9gba4pzE3fD4CI3BeH7x+I0gcxTFS96n6zog5c8+wnSb/S2qn9XzbN9yI/RuU10ATmSx6QUy7/64fc7dk9PlDCH4r2o+qxPNyDQE7QErM1kO39NhQuRem3anr1fBd55/tP5V+VAU=):
      **4e91d7a09b833a8bd5a3574095cf017772f4823f7c045ab78b3ef174348ea16e**
      
**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.id|Number|x|L2|Định danh user|
|data.userName|String|x|L2|Tên đăng nhập|
|data.password|String|x|L2|Mật khẩu|
|data.createdAt|String|x|L2|Thời gian tạo tài khoản - dd/MM/yyyy HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|
|data.email|String|x|L2|Địa chỉ email|
|data.name|String|x|L2|Tên khách hàng|
|data.phone|String|x|L2|Số điện thoại (Đầu số mới)
|data.accounts.id|Number|x|L3|Định danh tài khoản|
|data.accounts.userId|Number|x|L3|Định danh chủ tài khoản|
|data.accounts.cardNnumber|Number|x|L3|Số tài khoản|
|data.accounts.cardName|String|x|L3|Tên tài khoản|
|data.accounts.closeDate|String|x|L3|Hạn sử dụng tài khoản|
|data.accounts.createdAt|String|x|L3|Ngày tạo tài khoản|
|data.accounts.description|String||L3|Thông tin thêm |
|data.accounts.type|Number|x|L3|Loại tài khoản: 1 - Tài khoản thanh toán, 2 - Tài khoản tiết kiệm|

# 11. Delete Debt
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:8080/lh-bank/delete-debt       | 
|Method | POST       | 
## Raw Data
**HTTP Request:**

```json
{
    "debtId": 12,
    "userId": 5,
    "content": "Xóa lần nợ 8 lần 1"
}
```

**Response:**
```json
{
    "requestId": "1da69e2f98914b5880bfa3b3efe7615c",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1595250464036,
    "data": {
        "debtId": 13,
        "action": "DELETE"
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|debtId|Number|x|L1|Định danh nhắc nợ cần xóa |
|userId|Number|x|L1|Người gửi yêu cầu xóa nợ|
|content|String|x|L1|Nội dung xáoguiwr xóa nhắc nợ|

**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.debtId|Number|x|L2|Định danh nhắc nợ|
|data.action|Number|x|L2|Luôn là "DELETE"|

# 12. Update Reminder
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:8080/lh-bank/update-reminder      | 
|Method | POST       | 
|Authorization| Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cmFudGhpbGFuZyIsImV4cCI6MTU5NTU5NzI3NiwiaWF0IjoxNTk1NTYxMjc2fQ.cyWnQadmHjSPqowU-dBkB5CX1YWE-TU3_4ru5QGUFM8|

## Raw Data
**HTTP Request:**

```json
{
    "nameReminisce": "Debtor 002 UPDATE 1",
    "cardNumber": 1448127665849225,
    "reminderId": 39,
    "action": "UPDATE"
}
```

**Response:**
```json
{
    "requestId": "63e6a633c5c14d47a5640038e928d9ce",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1595607151592,
    "data": {
        "email": "tranthilang.dtnt@gmail.com",
        "name": "Tran Thi Lang",
        "phone": "0327421137",
        "createdAt": "10/07/2020 08:17:37",
        "account": [
            {
                "id": 0,
                "cardNumber": 1448127665849225,
                "cardName": "Debtor 002 UPDATE 1",
                "reminderId": 39,
                "typeReminder": "debt",
                "merchantId": 1
            }
        ]
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|nameReminisce|String||L1|Tên gợi nhớ cần update|
|cardNumber|String||L1|Số tài khoản cần update|
|reminderId|Number|x|L1|Định danh gợi nhớ cần update/delete|
|action|String||L1|"UPDATE" hoặc "DELETE"|

**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.id|Number|x|L2|Định danh user|
|data.createdAt|String|x|L2|Thời gian tạo tài khoản - dd/MM/yyyy HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|
|data.email|String|x|L2|Địa chỉ email|
|data.name|String|x|L2|Tên khách hàng|
|data.phone|String|x|L2|Số điện thoại (Đầu số mới)|
|data.accounts.cardNumber|Number|x|L3|Số tài khoản đã lưu gợi nhớ|
|data.accounts.cardName|String|x|L3|Tên gợi nhớ|
|data.accounts.reminderId|Number|x|L3|Định danh gợi nhớ|
|data.accounts.typeReminder|String|x|L3|Loại gợi nhớ đã lưu ("send" = 1: chuyển tiền, "deb" = 2: Nhắc nợ)|
|data.accounts.merchantId|Number|x|L3|Định danh tài khoản gợi nhớ là liên ngân hàng hay cùng ngân hàng|


# 13. Get Banks
|Key | Value       | 
|------- | ---------- |
|URL | 127.0.0.1:1111/lh-bank/get-banks/{bankId}| 
|Method | GET       | 
## Raw Data
**HTTP Request:**
127.0.0.1:1111/lh-bank/get-banks/2
**Response:**
```json
{
    "requestId": "c2744373718d4adfbd776a3f3a7fc5b5",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1595615234119,
    "data": {
        "partners": [
            {
                "id": 2,
                "partnerCode": "MY_BANK",
                "email": "mybank@gmail.com",
                "phoneNumber": "0327421138",
                "name": "mybank"
            }
        ]
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|bankId|Number||PathVariable|Mã ngân hàng, Mặc định lấy hết|
**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.partners.id|Number|x|L3|Định danh ngân hàng|
|data.partners.partnerCode|String|x|L3|Mã đối tác|
|data.partners.email|String|x|L3|Địa chỉ email|
|data.partners.phoneNumber|String|x|L3|Số điện thoại|
|data.partners.name|String|x|L3|Tên ngân hàng|