package com.backend;

import com.backend.model.response.BaseResponse;
import com.backend.process.PartnerProcess;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;


public class MerchantTest {
    private static final Logger logger = LogManager.getLogger(MerchantTest.class);

    private static final Gson PARSER = new Gson();

    private static final String publicKeyBank1 =
            "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                    "Version: Keybase OpenPGP v1.0.0\n" +
                    "Comment: https://keybase.io/crypto\n" +
                    "\n" +
                    "xo0EXxP5QwEEAMeLdqURvlXT2qUoZh7se0cbSn8Wrmdn39/3oPRm5bVieaFXmKaT\n" +
                    "5J0m1DKCMv2uMKYDxBF2MZc6gUV9Q0EiQavNblt2SOy4dz0BTrBDFUA5aIS6JCNn\n" +
                    "WXXlA4uhT9E6USlgSPLgyVfTlHcf42vFbkvYaR8C7+TnWy1dToeStQMFABEBAAHN\n" +
                    "G3BncGJhbmsgPHBncGJhbmtAZ21haWwuY29tPsKtBBMBCgAXBQJfE/lDAhsvAwsJ\n" +
                    "BwMVCggCHgECF4AACgkQn6e+iZkF7Mpw2wQAid6jmQVSWa1qJ60GK89i3cr7hZBq\n" +
                    "jXnfrX/9gba4pzE3fD4CI3BeH7x+I0gcxTFS96n6zog5c8+wnSb/S2qn9XzbN9yI\n" +
                    "/RuU10ATmSx6QUy7/64fc7dk9PlDCH4r2o+qxPNyDQE7QErM1kO39NhQuRem3anr\n" +
                    "1fBd55/tP5V+VAXOjQRfE/lDAQQAyuZD+gD2aYSmG4CE3wHmkI0VlU22zkd0kyXe\n" +
                    "PcihVDq4RyRAGHiGx5FyQDB7HejOBRaBNzOuLSR43Bjqzhm34ePXo2LUtoP9yQN2\n" +
                    "crToqK2avwzMknRSdfavsLc2NYaT1/jVmEzPDu2yOHKLzuebpEwT0WCEsd5bsNGQ\n" +
                    "Y2doZQMAEQEAAcLAgwQYAQoADwUCXxP5QwUJDwmcAAIbLgCoCRCfp76JmQXsyp0g\n" +
                    "BBkBCgAGBQJfE/lDAAoJEO5Addx9+21gHloEAJR/YUFBhM5gFb9tpzH0GGOfg7dN\n" +
                    "QU6GlMe9R/Aq1aKEk7D6BKvR6R4aOtarAJkh2IlWx+jrkKwEecsQZCdriVaALaoI\n" +
                    "Z0JV0rczGqQ8AAGdH0TlbSGsfjP3+9rScuNvIUggtyGu9y0uicovi4J5L9LvxP0P\n" +
                    "3So9odNvj3i4VMqma1QD+wV0pp+2Kqs5QRrMnNheiwh6qnbdkTyBVL26CeflBaGt\n" +
                    "se/as4fRUUNfBGXPLtuE9dD8ElF7XGyGV9u9+UEluFWu880OqXXUGj99plcpQ+LE\n" +
                    "yHY3lT9N6Sc+SAo7o/PG11IVzTdi2IdKFSX+R2p6dFJNvmsCtqEhzgz3kCmch2jA\n" +
                    "zo0EXxP5QwEEAMhTs8eGdft9C0CeJHlIFlQZCGmG3HS38pNFI+RA6RQiJxiIueJv\n" +
                    "iAFbX2kKMecYlMaD5CjTcLksmnSERdmTeiqsyG5Um4uxHzPZD6U9cvpQMvXmFh0t\n" +
                    "uLLBQuwkWrxgA/2zn66F8SArLId77sMvaS7M2k6k7/4gmvOe1HwLRIXHABEBAAHC\n" +
                    "wIMEGAEKAA8FAl8T+UMFCQ8JnAACGy4AqAkQn6e+iZkF7MqdIAQZAQoABgUCXxP5\n" +
                    "QwAKCRCiBVNyeD8LsJ9GBACYVsUNiP0ekxZSKVAws3LjDb1wsQhchrUJ2aY7hExe\n" +
                    "7/0sc4anHxMgzxQzTCZoBhT3Uojwi6kM/skvN+572G7LCSxPhGDVPUJs+k3bpim/\n" +
                    "he2MHIXm+8ZU37N1cmWd+kQtdJ1QCCgOfChCaJnh0MVuXKJ21EEmBpp50pfag6PI\n" +
                    "Z96GBACKomjQUXJ0a/TtoQ6BNdYeYYcjwWEHIUMFW+WBIbVvU976imDqUDQXac5B\n" +
                    "JusPsyu0LXgnK4PBGkewF7bzDzk6qg5JYcJ3nmyXsNBRgYnZYRJFxw5aKmCcWzHe\n" +
                    "J0phuS/i2OKa1arZ9mQ3oA10LDePkZcau/pdyeBCMaTH231GJg==\n" +
                    "=RdyP\n" +
                    "-----END PGP PUBLIC KEY BLOCK-----\n";


    private static final String privateKeyBank1 =
            "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
                    "Version: Keybase OpenPGP v1.0.0\n" +
                    "Comment: https://keybase.io/crypto\n" +
                    "\n" +
                    "xcFGBF8T+UMBBADHi3alEb5V09qlKGYe7HtHG0p/Fq5nZ9/f96D0ZuW1YnmhV5im\n" +
                    "k+SdJtQygjL9rjCmA8QRdjGXOoFFfUNBIkGrzW5bdkjsuHc9AU6wQxVAOWiEuiQj\n" +
                    "Z1l15QOLoU/ROlEpYEjy4MlX05R3H+NrxW5L2GkfAu/k51stXU6HkrUDBQARAQAB\n" +
                    "/gkDCJMIx1jUDw0RYFPPo+b1Rox71tDIt2LQieyh+9YsdbUk2gRqAAsjZo04oQdX\n" +
                    "1/jgDzfUHfSBxy1/uBaQ0qWZhe62nfjRXZB1Pf9FGzCtyBNfbt1IwFTSPlSCOi3b\n" +
                    "Hfc20WK61hbHeIuw1u6cXSzPKpSS7xP3rohW+64vg31pPBzP+szSFfbFEKW/r+LO\n" +
                    "PZIbt8eZpH8pCUZbQXPG6fRE1+Dn8QwwAY8RZauFd/r2BkIdWUkSdj9djgwNJSzW\n" +
                    "tr/YDkLKkxGF2GyfA2HPoo+RwpQZ6r2mKgDYJvSNKjI7k+M4bjmoGj6sNsvzd+ta\n" +
                    "9LDENLBP9SW17cydA9Hve3OZLrgJug3VD0ErC8QnuNYTzSyBF/Fyt7JEEc9fxwEl\n" +
                    "73U1T6ec2lTlh+a2BjdXLNkW4lP3RglzIZZ9Sp2WdKCEOtKcmOboiOapzwPeQh8J\n" +
                    "vrlemDjAwFvxoBDrHci8AgZtHJ8Ha1QCE0N+nI0Xz9uuGxVIQ7a57enNG3BncGJh\n" +
                    "bmsgPHBncGJhbmtAZ21haWwuY29tPsKtBBMBCgAXBQJfE/lDAhsvAwsJBwMVCggC\n" +
                    "HgECF4AACgkQn6e+iZkF7Mpw2wQAid6jmQVSWa1qJ60GK89i3cr7hZBqjXnfrX/9\n" +
                    "gba4pzE3fD4CI3BeH7x+I0gcxTFS96n6zog5c8+wnSb/S2qn9XzbN9yI/RuU10AT\n" +
                    "mSx6QUy7/64fc7dk9PlDCH4r2o+qxPNyDQE7QErM1kO39NhQuRem3anr1fBd55/t\n" +
                    "P5V+VAXHwUYEXxP5QwEEAMrmQ/oA9mmEphuAhN8B5pCNFZVNts5HdJMl3j3IoVQ6\n" +
                    "uEckQBh4hseRckAwex3ozgUWgTczri0keNwY6s4Zt+Hj16Ni1LaD/ckDdnK06Kit\n" +
                    "mr8MzJJ0UnX2r7C3NjWGk9f41ZhMzw7tsjhyi87nm6RME9FghLHeW7DRkGNnaGUD\n" +
                    "ABEBAAH+CQMImuWnbpI6tGRgLAAKIbHHPIbh5msOqXQ7yQqFGnnlhvdkDQOndZfm\n" +
                    "all3Y+4OPIaImzNxPWJhLxvEUB+tteA/3o7SEYMvtnaQiFLlB0kARo9dbFrqtcFw\n" +
                    "kVDqLSSlcpTiaJH6BteZYsxomtxfXH3ysVYkL0xiRw3oXjvFdh05Et6D87nOv5J1\n" +
                    "I7Bxzy3jPzen0xnvmbyqIpHBgchubAjs4DeVjXeg3QHtTq5yll1DV8+gQhc4rizK\n" +
                    "TZd9OunecLhvGt/hTvhXvRRjHiYL37DeQwZ2Ox4Q0RnEoK1KIpyJScSycWRxu/If\n" +
                    "1qCAbcd+TWiY4xcArfgqi795sRY64nos/f0+Klli3zZzk6/s1ntn4OIq2yKAYcGi\n" +
                    "oaxTcTenWaMukbr+6Ra03P04tDcbVWiFZUNaOAWOnhUiNBpk3SLnSlYc1BxhQ201\n" +
                    "Kq58xhPeTkxggbEyLCg5F0p6AzAl/tEXSOvBHjEgDI5tCu1T2TNqdT4S1fXRAcLA\n" +
                    "gwQYAQoADwUCXxP5QwUJDwmcAAIbLgCoCRCfp76JmQXsyp0gBBkBCgAGBQJfE/lD\n" +
                    "AAoJEO5Addx9+21gHloEAJR/YUFBhM5gFb9tpzH0GGOfg7dNQU6GlMe9R/Aq1aKE\n" +
                    "k7D6BKvR6R4aOtarAJkh2IlWx+jrkKwEecsQZCdriVaALaoIZ0JV0rczGqQ8AAGd\n" +
                    "H0TlbSGsfjP3+9rScuNvIUggtyGu9y0uicovi4J5L9LvxP0P3So9odNvj3i4VMqm\n" +
                    "a1QD+wV0pp+2Kqs5QRrMnNheiwh6qnbdkTyBVL26CeflBaGtse/as4fRUUNfBGXP\n" +
                    "LtuE9dD8ElF7XGyGV9u9+UEluFWu880OqXXUGj99plcpQ+LEyHY3lT9N6Sc+SAo7\n" +
                    "o/PG11IVzTdi2IdKFSX+R2p6dFJNvmsCtqEhzgz3kCmch2jAx8FGBF8T+UMBBADI\n" +
                    "U7PHhnX7fQtAniR5SBZUGQhphtx0t/KTRSPkQOkUIicYiLnib4gBW19pCjHnGJTG\n" +
                    "g+Qo03C5LJp0hEXZk3oqrMhuVJuLsR8z2Q+lPXL6UDL15hYdLbiywULsJFq8YAP9\n" +
                    "s5+uhfEgKyyHe+7DL2kuzNpOpO/+IJrzntR8C0SFxwARAQAB/gkDCH2Ob8r7Y4LC\n" +
                    "YHX8q/UYViPfnA+67r1yb+VBlEl271s1jfiO5kCjjWQe+55nFqSrJ06LsWoVWvaS\n" +
                    "o05uKWZQUqeauXgwaYoy4847wtz4sJUeAT5+JJ3H2H3zBtm8s6wNFIm28o7Ql25r\n" +
                    "4WwpdxKPtsWU3e6saI1XwEEG1DCi+F6VG8BmU5XVkwr9RjVAuu/SsXPe4lmqWvjP\n" +
                    "/uWgp+HUjzSEm3q7eBLbiQ4EJHLDHKVjsD4jHYMo68vFo8W5cCYA2lbLwTvcuECi\n" +
                    "QhHxbizUGGqG471JWGGGTTK1NOFPXwI0mLppESUzf9TFqy35gNNwxcPGjexgLJap\n" +
                    "CLkWK1/XE2nFOE8CJqRQsYep/5rCXegfORcdSCGu1QeBJtiy2JGxSwsESp5oy0b3\n" +
                    "wxIS4iK9F0+5S/jOAdU7aCNagJyUP+gaJ3dKsEmLl/tbMUzVX1KTC3ghwotqOKoQ\n" +
                    "6Ve9q+TjzIHTa6+s1TxsrNkLGc0hOgAHVpblZ5LCwIMEGAEKAA8FAl8T+UMFCQ8J\n" +
                    "nAACGy4AqAkQn6e+iZkF7MqdIAQZAQoABgUCXxP5QwAKCRCiBVNyeD8LsJ9GBACY\n" +
                    "VsUNiP0ekxZSKVAws3LjDb1wsQhchrUJ2aY7hExe7/0sc4anHxMgzxQzTCZoBhT3\n" +
                    "Uojwi6kM/skvN+572G7LCSxPhGDVPUJs+k3bpim/he2MHIXm+8ZU37N1cmWd+kQt\n" +
                    "dJ1QCCgOfChCaJnh0MVuXKJ21EEmBpp50pfag6PIZ96GBACKomjQUXJ0a/TtoQ6B\n" +
                    "NdYeYYcjwWEHIUMFW+WBIbVvU976imDqUDQXac5BJusPsyu0LXgnK4PBGkewF7bz\n" +
                    "Dzk6qg5JYcJ3nmyXsNBRgYnZYRJFxw5aKmCcWzHeJ0phuS/i2OKa1arZ9mQ3oA10\n" +
                    "LDePkZcau/pdyeBCMaTH231GJg==\n" +
                    "=n4Cn\n" +
                    "-----END PGP PRIVATE KEY BLOCK-----\n";

    private final static String pass = "pgpbank";
    public final static String signBank1 =
    "-----BEGIN PGP MESSAGE-----\n" +
            "Version: BCPG v1.46\n" +
            "\n" +
            "owJ4nJvAy8zAxBj1IXFKzeG9KoynJZM44p39/YL9fVzjhc66lxQl5pVkZOYk5qV3\n" +
            "bGVhYGRikGdlAklIyIDEDBVswJRDem5iZo5ecn6uHQMXpwDMuMdlzP/9JzKs/nO+\n" +
            "7b7QjFfV9/o196+XqYxnEFibk5r4VXvG4uUHdlc97pi7h+lmxdTcM9b+m5nUNkro\n" +
            "3/sQoGy1onGO96XzLnti+D6v+rrsoHn6DqZjd5nX3/Z4pHYzuOKO+Jb5/jM/vA8v\n" +
            "edWV8M1aj4M7+LRt9Ndb6xzEjuXlx5dOzP2TX/XlTqouAB6gYzQ=\n" +
            "=ogLQ\n" +
            "-----END PGP MESSAGE-----";
//    @PostMapping(value = "/transfer/bank")
//    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
    public static String transfer() {
        String logId = "request.getRequestId()";
//        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
//            if (!request.isValidData()) {
//                logger.warn("{}| Validate request transfer data: Fail!", logId);
//                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
//                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
//            }
//            logger.info("{}| Valid data request transfer success!", logId);

            String name = "lang1";
            String email = "lang1@gmail.com";
            int size  = 1024;
            String privateKeyPassword = "lang1";
            String message = "tranthilang";

            // add Bouncy JCE Provider, http://bouncycastle.org/latest_releases.html

            BASE64Encoder base64Encoder = new BASE64Encoder();

            PGPPublicKey pubKey = null;
            // Load public key
            try {
                pubKey = PartnerProcess.readPublicKey(publicKeyBank1);
//                InputStream in = new ByteArrayInputStream(publicKeyBank1.getBytes());
//                in = PGPUtil.getDecoderStream(in);
//                pubKey = PGPUtils.readPublicKey(in);
            } catch (IOException | PGPException e) {
                e.printStackTrace();
            }
            if (pubKey != null) {
                logger.info("{}| Successfully read public key: {}", logId, base64Encoder.encode(pubKey.getEncoded()).replaceAll("\\r\\n|\\r|\\n|", ""));
                logger.info("{}| Read public key with alg: {}", logId, pubKey.getAlgorithm());
            }

            // Load private key, **NOTE: still secret, we haven't unlocked it yet**
            PGPSecretKey pgpSec = null;
            try {
                assert pubKey != null;
                pgpSec = PartnerProcess.readSecretKey(privateKeyBank1, pubKey.getKeyID());
                logger.info("{}| Successfully read secret key: {}", logId, base64Encoder.encode(pgpSec.getEncoded()).replaceAll("\\r\\n|\\r|\\n|", ""));
                logger.info("{}| Read secret key with id: {} and alg: {}", logId, pgpSec.getKeyID(), pgpSec.getKeyEncryptionAlgorithm());
            } catch (IOException | PGPException e) {
                e.printStackTrace();
            }

            // sign our message
            String messageSignature = null;
            try {
                assert pgpSec != null;
//                Security.addProvider(new BouncyCastleProvider());
//                InputStream inP = new ByteArrayInputStream(privateKeyBank1.getBytes());
//                inP = PGPUtil.getDecoderStream(inP);

//                PGPPrivateKey pgpPrivKey = PGPUtils.findSecretKey(inP, pubKey.getKeyID(), privateKeyPassword.toCharArray());
//                PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
//                        pgpSec.getPublicKey().getAlgorithm(), "BC", PGPUtil.SHA512, "BC");
//                signatureGenerator.initSign(PGPSignature.DIRECT_KEY, pgpPrivKey);
//
//                PGPSignature signature = signatureGenerator.generateCertification(pubKey);


                messageSignature = PartnerProcess.signaturePgp(message, pgpSec,
                        privateKeyPassword.toCharArray());
            } catch (NoSuchAlgorithmException | NoSuchProviderException
                    | SignatureException | IOException | PGPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.info("{}| messageSignature: {}", logId, messageSignature);

            return messageSignature;
        } catch (Exception ex) {
//            logger.error("{}| Request transfer catch exception: ", logId, ex);
//            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(),null);
//            ResponseEntity<String> responseEntity = new ResponseEntity<>(
//                    response.toString(),
//                    HttpStatus.BAD_REQUEST);
            return "responseEntity";
        }
    }

    public static void main(String[] args) throws PGPException, IOException, NoSuchProviderException {
        BASE64Encoder base64Encoder = new BASE64Encoder();
        Security.addProvider(new BouncyCastleProvider());

        PGPPublicKey pgpPublicKey = PartnerProcess.readPublicKey(publicKeyBank1);
        PGPSecretKey pgpSecretKey = PartnerProcess.readSecretKey(privateKeyBank1, pgpPublicKey.getKeyID());
        PGPPrivateKey pgpPrivKey = pgpSecretKey.extractPrivateKey(pass.toCharArray(), "BC");

        String publicKey = base64Encoder.encode(pgpPublicKey.getEncoded()).replaceAll("\\r\\n|\\r|\\n|", "");
        logger.info("publicKey: {}", publicKey);

        String secretKey = base64Encoder.encode(pgpSecretKey.getEncoded()).replaceAll("\\r\\n|\\r|\\n|", "");
        logger.info("secretKey: {}", secretKey);

        String privateKey = base64Encoder.encode(pgpPrivKey.getKey().getEncoded()).replaceAll("\\r\\n|\\r|\\n|", "");
        logger.info("privateKey: {}", privateKey);
    }
}
