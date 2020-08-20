# LH-BANK

|Version | Date       | Author    | Description         |
|------- | ---------- | --------- | ------------------- |
|1.0     | 03-07-2020 | Tran Thi Lang | Init document   |
|1.1     | 05-07-2020 | Tran Thi Lang | Users API       |

# I. API Document
# Index
0. [Thông tin đối tác](#0-thông-tin-đối-tác)
1. [Account Bank](#1-account-bank)
2. [Transfer Bank](#2-transfer-bank)

# II. API Document
# 0. Thông tin đối tác
- Bank Code
- Partner Code
- Partner Name
- Email
- Password
- Phone Number
- Private File

- Public File

- Secret key

# 1. Account Bank
|Key | Value       | 
|------- | ---------- |
|URL | https://i-banking.herokuapp.com/lh-bank/account-bank| 
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


# 2. Transfer Bank
|Key | Value       | 
|------- | ---------- |
|URL | https://i-banking.herokuapp.com/lh-bank/transfer-bank| 
|Method | POST       | 
|Content-Type| application/json |
|Body| JSON String |

## Raw Data
**HTTP Request:**
```json
{
  "bankCode": "LH_BANK",
  "from": 47550680,
  "isTransfer": true,
  "partnerCode": "PGP_BANK",
  "requestId": "1597870733259",
  "requestTime": 1597870733259,
  "to": 1344416348290507,
  "typeFee": 1,
  "cardName": "datay",
  "value": 100000,
  "description": "test generate transfer api",
  "hash": "172f0f1d1e65e19c962fecbf8916029787168f4bdd6f14a0165b570a606feb09",
  "signature": "-----BEGIN PGP MESSAGE-----\r\nVersion: BCPG v1.60\r\n\r\nowJ4nJvAy8zAxXjyUvAuTu3kGMbTB1KTOOKd/f2C/X1c420n9VUrJSXmZTvnp6Qq\r\nWSn5eMQ7Ofp5K+koJScWpfgl5oIEUxJLEiuBQmlF+blKVibmpqYGZhYGOkqZxSFF\r\niXnFaalFSlYlRaWpOkoFiUUlealFUMMC3ANgphWlFpamFpd4pgCFDU0tzS3MDcyN\r\njY1MLRFyIZkgy1AkdZRK8oFCxiYmJoZmxiYWRpYGpgbmQNHKglS3VJBqHaWyxJxS\r\nEMsABHSUMhKLM0B2mBulGaQZphimmpmmGlomW5oZpaUmJ6VZWBqaGRiBrDA0s0gz\r\nSUpJMUszNEk0MDQzTTI1N0g0MzBLS00ysFSq7djFwsDIxaDCygQKJFkZoGcUnIAB\r\npWBTkF4ACjGH9NzEzBy95PxcOwYuTgFYAF9LZv4ftbJYc82e+W+Of2NkPMHpEj/j\r\n/usJXmd6Q1RqL6zl/J43t8N8HWeferWzspX+s8NpzW9Pzg5Q3m4ifejWkw/XZ7Ls\r\n0VAPdnQIvjz3V0nTBlPrfzYHot9JCS7hecbOse6D0NP//pxnjbK1vB50uPx+ov7B\r\ndHbDPP5gP4f1S7o/GuSslFpy3KIMAHgmq40=\r\n=NOM8\r\n-----END PGP MESSAGE-----\r\n"
}
```

**Response:**
```json
{
    "requestId": "1597870733259",
    "resultCode": 0,
    "message": "Thành công",
    "responseTime": 1597870848157,
    "data": {
        "id": 0,
        "transId": 1886386608,
        "cardNumber": 1344416348290507,
        "cardName": "Lang Tran",
        "amount": 100000,
        "typeFee": 1,
        "fee": 500,
        "content": "test generate transfer api",
        "status": "COMPLETED",
        "merchantId": 2,
        "createDate": "20/08/2020 03:58:53"
    }
}
```

**Request:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|bankCode|String|x|L1|Mã code của LH-BANK|
|from|Number|x|L1|Số tài khoản chuyển tiền (của ngân hàng đối tác)|
|to|Number|x|L1|Số tài khoản nhận tiền (của LH-BANK)|
|typeFee|Number|x|L1|1.From trả, 2.To trả|
|cardName|String|x|L1|Tên thẻ của To|
|value|Number|x|L1|Số tiền chuyển|
|description|String||L1|Ghi chú cho giao dịch|
|isTransfer|boolean|x|L1|Luôn luôn là **true**|
|partnerCode|String|x|L1|Mã code của ngân hàng đối tác|
|requestTime|Number|x|L1|Thời gian gửi reuqest (tính bằng  milliseconds)|
|hash|String|x|L1|Hash data for security. Hash is a string was hashed by Hmac_SHA256 algorithm using partner secretKey with format bankCode=**$bankCode**&cardName=**$cardName**&from=**$from**&isTransfer=**$isTransfer**&merchantCode=**$merchantCode**&requestId=**$requestId**&requestTime=**$requestTime**&to=**$to**&typeFee=**$typeFee**&value=**$value**|
|signature|String|x|L1|Chữ ký điện tử|

- **hash** Sử dụng Secret Key (LH-Bank cung cấp) để mã hóa dữ liệu. Ví dụ:
    + Dữ liệu trước khi hash: bankCode=**LH_BANK**&cardName=**datay**&from=**47550680**&isTransfer=**true**&partnerCode=**PGP_BANK**&requestId=**1597870733259**&requestTime=**1597870733259**&to=**1344416348290507**&typeFee=**1**&value=**100000**
    + Dữ liệu được tạo ra sau khi sử dụng thuật toán **Hmac_SHA256** và Secret Key (**lQIFBF8cr9ABBAD1FzxZdPVeUgR2k+cdcqtmuoWfxwtQTZyNY6NZzExDnDf+2+6c9nx/RRi9k3oPFk4phZ+JKSnEvxaWa2PecAyuuKfvCMkwptCNqWVOevweVzbF11VLHfURK5S8rvvw7etnh4lKUcEds0I5+tlbEFSV3f0zTfRpXIwlGgc8Y95hKwARAQAB/gkDCBw9ODvvUAdvYFDItyV8VaFtcEmnkNjMjfJNL+BpbvaKCjd5wuzt4n5HeYNADfpokYnFFRCzmly/zhElVAyuW0tS1ry1P+6IOKugDQestczlx4NRdVMAmIBkuhtU7WEhRzlUnXQmYn6GiLBBc2C1yZWmxJMghv0bA5pHXkjdmKGz+xdCrbmuHxLAFg0yBWbau8PvFJ5+DGgJTBhHElj6HDWj+eYHaulQiWDFmO/EASnmL5+Y7zOQRss2Y65WUX8vT/vOHr63lFI0qUu7mMJOAJVmyvx3Jx31F4B/7oETDY9XCMTfZrYINTR82uQMMYlxynI6OYU5eM5P20gDC/1umCPrb2XkVPyEz+wQq6d75vt7lPqxi6mrMhMD2r7r9In3cZSz7+u7bFcyyCsAf7d1pVvyNeXbHgKJB7HZugnp/r1IrLxWpdM//1cThQRQwRicEJS5niSvoD0DT6zoOiUh2XjoeZCtBz7MVNXlLs8RzT4TTP4enrQcSEhMIEJhbmsgPGhobGJhbmtAZ21haWwuY29tPoitBBMBCgAXBQJfHK/QAhsvAwsJBwMVCggCHgECF4AACgkQRIlwUZR8Cgz0vQP+MCvW4fduIKp0PDNwtKDExASjJMLwFuCnYwPfb3byxZbV5fcgvu1Vsujmgf1ZG5v4I8I6mNccy7mIx6qZXPmibSQQXYbv7VKD5qYX9r6l0Jv2EEYv9u+pCKIsbON0k64OLD4Kq3vzzsnpfzvN/I4n5NZFCpGBETWZ9Wnhr2uBXCg=**):
      **0665385988a192c12beeeaa1544958fdef4dc9b9f2d63c9cc47ce11ea04b8807**
    - **signature** 
        + Dữ liệu trước khi sig
       {"bankCode":"LH_BANK","cardName":"PGP Bank card name","from":111111111111,"isTransfer":true,"partnerCode":"PGP_BANK","requestId":"0e28ddd4-4017-decf-8ade-972e8c4d0cc6","requestTime":1595152797916,"to":1344416348290507,"typeFee":1,"value":100000,"hash":"0665385988a192c12beeeaa1544958fdef4dc9b9f2d63c9cc47ce11ea04b8807"} 
        +  Dữ liệu được tạo ra sau sig bằng Private key: **-----BEGIN PGP MESSAGE-----\r\nVersion: BCPG v1.60\r\n\r\nowJ4nJvAy8zAxXjyUvAuTu3kGMbTB1KTOOKd/f2C/X1c420n9VUrJSXmZTvnp6Qq\r\nWSn5eMQ7Ofp5K+koJScWpfgl5oIEUxJLEiuBQmlF+blKVibmpqYGZhYGOkqZxSFF\r\niXnFaalFSlYlRaWpOkoFiUUlealFUMMC3ANgphWlFpamFpd4pgCFDU0tzS3MDcyN\r\njY1MLRFyIZkgy1AkdZRK8oFCxiYmJoZmxiYWRpYGpgbmQNHKglS3VJBqHaWyxJxS\r\nEMsABHSUMhKLM0B2mBulGaQZphimmpmmGlomW5oZpaUmJ6VZWBqaGRiBrDA0s0gz\r\nSUpJMUszNEk0MDQzTTI1N0g0MzBLS00ysFSq7djFwsDIxaDCygQKJFkZoGcUnIAB\r\npWBTkF4ACjGH9NzEzBy95PxcOwYuTgFYAF9LZv4ftbJYc82e+W+Of2NkPMHpEj/j\r\n/usJXmd6Q1RqL6zl/J43t8N8HWeferWzspX+s8NpzW9Pzg5Q3m4ifejWkw/XZ7Ls\r\n0VAPdnQIvjz3V0nTBlPrfzYHot9JCS7hecbOse6D0NP//pxnjbK1vB50uPx+ov7B\r\ndHbDPP5gP4f1S7o/GuSslFpy3KIMAHgmq40=\r\n=NOM8\r\n-----END PGP MESSAGE-----\r\n**
    
**Response:**

|Name|Type|Required|Level|Description|
|----|----|:------:|:---:|-----------|
|requestId|String|x|L1|Định danh request phía trên|
|resultCode|Number|x|L1|Kết quả của request|
|message|String|x|L1|Mô tả chi tiết kết quả request|
|responseTime|long|x|L1|Thời gian trả kết quả cho request (tính theo millisecond) Múi giờ: GMT +7|
|data.transId|Number|x|L2|Mã giao dịch|
|data.cardNnumber|Number|x|L2|Số tài khoản người nhận|
|data.cardName|String|x|L2|Tên tài khoản người nhận | 
|data.amount|String|x|L2|Số tiền giao dịch|
|data.typeFee|Number|x|L2|1:  Người chuyển trả, 2: Người nhận trả|
|data.fee|Number|x|L2|Phí giao dịch|
|data.content|String||L2|Nội dung giao dịch|
|data.status|String|x|L2|Trạng thái giao dịch|
|data.merchantId|Number|x|L2|Định danh merchant (2: PGP Bank, 3: RSA Bank)|
|data.createdAt|String|x|L2|Thời gian tạo tài khoản - dd/MM/yyyy HH:mm:ss (định dạng 24h) Múi giờ: GMT +7|

