# LH-BANK

|Version | Date       | Author    | Description         |
|------- | ---------- | --------- | ------------------- |
|1.0     | 03-07-2020 | Tran Thi Lang | Init document       |

# I. API Document
# Index
1. [Registert](#1-register)

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