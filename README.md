# LH-BANK

|Version | Date       | Author    | Description         |
|------- | ---------- | --------- | ------------------- |
|1.0     | 03-07-2020 | Tran Thi Lang | Init document       |

# I. API Document
# Index
1. [Registert](#1-register)
2. [Deposit](#2-deposit)
3. [Get Accounts](#3-get-accounts)
4. [Login](#3-login)


# II. API Document
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
|URL | 127.0.0.1:8080/lh-bank/get-accounts/{userId}/{type}       | 
|Method | GET       | 
## Raw Data
**HTTP Request:**

**Response:**
```json
{
    "requestId": "1867e7a504c24ac082b3645f67bb791c",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1593768407676,
    "data": {
    "accounts": [
            {
                "id": 4,
                "cardNumber": 11,
                "cardName": "Lang Lang",
                "closeDate": "02/07/2024 15:59:08",
                "createdAt": "03/07/2020 15:59:08",
                "updatedAt": "03/07/2020 15:59:08",
                "description": null,
                "type": 1,
                "balance": 0,
                "userId": 3
            }
     ]
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|userId|Number|x|L3|Định danh chủ tài khoản|
|type|Number|x|PathVariable|1 - Tài khoản thanh toán, 2 - Tài khoản tiết kiệm, 0 - get all|
**Response:**

|Name|Type|Required|Level|Description|
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
    "requestId": "263ef3e4a8d04ba2aeb81f865a7d6cd4",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1593786139161,
    "data": {
        "id": 33,
        "userName": "tranlang",
        "password": "yvlikcfnzywqdcj",
        "email": "tranlang.dtnt@gmail.com",
        "name": "Tran Lang",
        "phone": "0327421137",
        "createdAt": "03/07/2020 21:21:24",
        "account": [
            {
                "id": 34,
                "cardNumber": 1387184392910303,
                "cardName": "Tran Lang",
                "closeDate": "02/07/2024 21:21:24",
                "createdAt": "03/07/2020 21:21:24",
                "updatedAt": "03/07/2020 21:21:24",
                "description": null,
                "type": 1,
                "balance": 0,
                "userId": 33
            }
        ]
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|userName|String|x|L2|Tên đăng nhập|
|password|String|x|L2|Mật khẩu|
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
|data.accounts.cardNnumber|Number|String|x|L3|Số tài khoản|
|data.accounts.cardName|String|x|L3|Tên tài khoản|
|data.accounts.closeDate|String|x|L3|Hạn sử dụng tài khoản|
|data.accounts.createdAt|String|x|L3|Ngày tạo tài khoản|
|data.accounts.description|String||L3|Thông tin thêm |
|data.accounts.type|Number|x|L3|Loại tài khoản: 1 - Tài khoản thanh toán, 2 - Tài khoản tiết kiệm|