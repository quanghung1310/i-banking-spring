package com.backend;

import com.backend.model.response.BaseResponse;
import com.backend.process.MerchantProcess;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.openpgp.*;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;


public class MerchantTest {
    private static final Logger logger = LogManager.getLogger(MerchantTest.class);

    private static final Gson PARSER = new Gson();

    private static final String publicKeyBank1 =
            "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                    "Version: Keybase OpenPGP v1.0.0\n" +
                    "Comment: https://keybase.io/crypto\n" +
                    "\n" +
                    "xo0EXxKvqgEEALGFwjDLRZksZST8RhfDaCxqxkG9cnfZoyvlirDQ9eIXO6IMKNio\n" +
                    "Fkna6YTAWhKMQTk+3JO6atVoUDnIgqia/x3UxVpZDgw94nUw599nfINklKWFAATn\n" +
                    "q4+VhIOAGjWJBZKU1fuETETAYtQ0Acj+5IEDUxpWtNL57ahvEKaXKZujABEBAAHN\n" +
                    "F2xhbmcxIDxsYW5nMUBnbWFpbC5jb20+wq0EEwEKABcFAl8Sr6oCGy8DCwkHAxUK\n" +
                    "CAIeAQIXgAAKCRBa8GGUfMO9JBl4BAChvyTzguDMEGzHlcJcx54ykBeIBJBmoma5\n" +
                    "tutvrf1JEUjJhqoz0rzH8EKMAgsSirhDMc7D24BLJi9G6maGpw4S+DCqoFr1wWdy\n" +
                    "HjRitTuUdLUJj7HKwomWMkjqn2p+CUrd6KuINPXc+ekPtBPR8clWsUjh6HA3HYWq\n" +
                    "yNF3Evwrac6NBF8Sr6oBBADb+1XDHzz+bIw7fbB5jZ8Xlbsye8h+6RPTgMM585LH\n" +
                    "a2FVK+SinIzhVQCdZg1DFN/535GYmPpFINEmsPLlPs4dkcu7tf1v9W9NotqTTlo7\n" +
                    "WjDrHJQOkuVTRm3GgSnQPy2fwu764nCm8ApLgxrV0EMknTd1DmQHqRN0kPvPstmx\n" +
                    "FQARAQABwsCDBBgBCgAPBQJfEq+qBQkPCZwAAhsuAKgJEFrwYZR8w70knSAEGQEK\n" +
                    "AAYFAl8Sr6oACgkQiQUS4kqATgRjPwP/W0kB6BWi7i3Kbsd5AQiTJZovBgCV1teE\n" +
                    "dG7a1mkqkNxPfjxFKEvFlP1kiGaC8EyJr0xw5lBC+udtlHwcTYd8/EN6AlbGKQWJ\n" +
                    "s5qnyRi381NCl0MrdiNxfq0b86TwEZPXS80xLDOI9nGXfM8ZBPKF+HupTLwstMQb\n" +
                    "vMNmzxGHEmsqnwP/TPMPWedPxznFvQZigqOTQP00kSWKdZ2UCyKie/7kBgYXN6mp\n" +
                    "J3DLvlN+9RE/quPqLYZaADig6zRg+ZDn3GwjJ1nxRw77I0dNAtFhdPE7/+mvawAH\n" +
                    "cyMW0xur0NWefK8IIbeY386mbzDANURcP+bRz3FIAgXOGxHTvVduajTbtc3OjQRf\n" +
                    "Eq+qAQQArxAaYLwzj/Pm+HB1YWzID4TFMDkH6UgRyqZugd7YAOn04AQ1OlyaUXRF\n" +
                    "VqNhG8I/vykfqmiXYVnWS+5z07F+eTK1wIcimeuYBmuCnlL8uAhXYZYUcU5jM885\n" +
                    "MR1rDu6YZ9qqQEGjZKuoZGx8xj+ZRproL7yi4JjzOsYT/cFmZG0AEQEAAcLAgwQY\n" +
                    "AQoADwUCXxKvqgUJDwmcAAIbLgCoCRBa8GGUfMO9JJ0gBBkBCgAGBQJfEq+qAAoJ\n" +
                    "EPah6QxerICQUNAD/2JZCHnBHk8h9QTfRpcxrqm823Ieb/3Tg0oka/Vw5CysEaga\n" +
                    "hS9Mp5SLfYvC+0tCsAiOHjxNeiceV7CLAKH+1MgPvGY+C0hwUMD3W3t5QmYyqQWN\n" +
                    "BzvBvBGxt5qRmOJg128h+krToqo510YWIysEFatLhPieXONoi2RuhNpPJW8MB+gD\n" +
                    "/jd2bjSkp5Aqu42jGaFhPWyApECHBKcr0k4yz98Al+76GsKwMRiN9jhUi2oPFUY6\n" +
                    "QAPLYO1QqBRjqUDdUXHD6mOM8HAUXUYFaJtllM8gggnBvIzQ4h8mwNOMHKaVZ0R1\n" +
                    "Tr1++i4pFDH8CaGJvZuWKKT0NV9FqgBct67U8046zNDX\n" +
                    "=gMPU\n" +
                    "-----END PGP PUBLIC KEY BLOCK-----";


    private static final String privateKeyBank1 =
            "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
                    "Version: Keybase OpenPGP v1.0.0\n" +
                    "Comment: https://keybase.io/crypto\n" +
                    "\n" +
                    "xcFGBF8Sr6oBBACxhcIwy0WZLGUk/EYXw2gsasZBvXJ32aMr5Yqw0PXiFzuiDCjY\n" +
                    "qBZJ2umEwFoSjEE5PtyTumrVaFA5yIKomv8d1MVaWQ4MPeJ1MOffZ3yDZJSlhQAE\n" +
                    "56uPlYSDgBo1iQWSlNX7hExEwGLUNAHI/uSBA1MaVrTS+e2obxCmlymbowARAQAB\n" +
                    "/gkDCD8onLmAuuB+YLRl5p9JSbhRRFbMaHkWy2H3swpM/3T+v9pWYxjXRTu329zu\n" +
                    "yfGnhVZ48XxJeVM8hrbgkF6zwtdxpoKCz9536oF4aHVRXR+AwQxmHb0fD/ZzPwYi\n" +
                    "3uIzuKw1+8ogDy4KU4CUa9qwvoTIG2i6dZ24DCK17JK9O9oTFD3TvViZh3Vf9wWF\n" +
                    "AkXwOrwb4q6HAsaa9aIyRsrz51U07iJG7Lpc0Xt/eTyOiOv6DB/VVgzZh6U1qssy\n" +
                    "82E0jVUFW51RP3t01iiAFYjcasqIsN6kwVlc5P6ZYFvP8k6sV42eHvFjGICzvV89\n" +
                    "hTgQEgRvJIrcCDRN8Se8aueSpMwDv/h/+EX266LrkSSpu6KmJwpq7idrVBtTABLL\n" +
                    "Kvo5vCdrYZ0CBZ4KuuxMz+sTwBPhtGnLG9iIii98UWrHp8vkMsz8eW5XLpK64Puq\n" +
                    "WN20PBqT8s1q9YI362zbHW3nvKOa320ayzsU8rVFLmqhErfVJsmj+RzNF2xhbmcx\n" +
                    "IDxsYW5nMUBnbWFpbC5jb20+wq0EEwEKABcFAl8Sr6oCGy8DCwkHAxUKCAIeAQIX\n" +
                    "gAAKCRBa8GGUfMO9JBl4BAChvyTzguDMEGzHlcJcx54ykBeIBJBmoma5tutvrf1J\n" +
                    "EUjJhqoz0rzH8EKMAgsSirhDMc7D24BLJi9G6maGpw4S+DCqoFr1wWdyHjRitTuU\n" +
                    "dLUJj7HKwomWMkjqn2p+CUrd6KuINPXc+ekPtBPR8clWsUjh6HA3HYWqyNF3Evwr\n" +
                    "acfBRgRfEq+qAQQA2/tVwx88/myMO32weY2fF5W7MnvIfukT04DDOfOSx2thVSvk\n" +
                    "opyM4VUAnWYNQxTf+d+RmJj6RSDRJrDy5T7OHZHLu7X9b/VvTaLak05aO1ow6xyU\n" +
                    "DpLlU0ZtxoEp0D8tn8Lu+uJwpvAKS4Ma1dBDJJ03dQ5kB6kTdJD7z7LZsRUAEQEA\n" +
                    "Af4JAwivfUAhyxX6cmAS62fC35i3z7JeQqX9KIZsDPH8WrSSSnMxWDYSps9yaVW7\n" +
                    "WPxXMjd4mnSwgBsZbZyAdBCpvnuye4Tz/uW3gblleCF6Lvm8nRLMCAV65M2Kl6b0\n" +
                    "xxDEvfF/4lvuqYCI5ugr9NRGrTECUZCw7UjmlbXEgCG403tUz3XdHfAJ/hEzQDta\n" +
                    "PkYO9zT7VXAjklOLmOKOXz1S2LJCp54uYt+Cf42k94LNKxrhI0UqJ6RhLFCndNnL\n" +
                    "P6OdybcbmmH3tpid0SwOYH57knBfkOJuobOikbyfJ23FmsVicNt7mFn+SaSSWjPB\n" +
                    "6kO6MkRVbO+wnUG38v6cG4VeYbpzXvmfs5ReIFWDvtPnh2hVwcYJK5JMYHmdzp/O\n" +
                    "mcGdya3qgSLmpx2wkazFJH6PQB+5HmFh1te7dgx0L9cpFiNfaJJqTz6z6NkEpbzH\n" +
                    "nInDaMs6Pbb3qwFslv4wguM4FEkipHBQj3db22lOYeBzSQXUXcDOJYeFwsCDBBgB\n" +
                    "CgAPBQJfEq+qBQkPCZwAAhsuAKgJEFrwYZR8w70knSAEGQEKAAYFAl8Sr6oACgkQ\n" +
                    "iQUS4kqATgRjPwP/W0kB6BWi7i3Kbsd5AQiTJZovBgCV1teEdG7a1mkqkNxPfjxF\n" +
                    "KEvFlP1kiGaC8EyJr0xw5lBC+udtlHwcTYd8/EN6AlbGKQWJs5qnyRi381NCl0Mr\n" +
                    "diNxfq0b86TwEZPXS80xLDOI9nGXfM8ZBPKF+HupTLwstMQbvMNmzxGHEmsqnwP/\n" +
                    "TPMPWedPxznFvQZigqOTQP00kSWKdZ2UCyKie/7kBgYXN6mpJ3DLvlN+9RE/quPq\n" +
                    "LYZaADig6zRg+ZDn3GwjJ1nxRw77I0dNAtFhdPE7/+mvawAHcyMW0xur0NWefK8I\n" +
                    "IbeY386mbzDANURcP+bRz3FIAgXOGxHTvVduajTbtc3HwUYEXxKvqgEEAK8QGmC8\n" +
                    "M4/z5vhwdWFsyA+ExTA5B+lIEcqmboHe2ADp9OAENTpcmlF0RVajYRvCP78pH6po\n" +
                    "l2FZ1kvuc9OxfnkytcCHIpnrmAZrgp5S/LgIV2GWFHFOYzPPOTEdaw7umGfaqkBB\n" +
                    "o2SrqGRsfMY/mUaa6C+8ouCY8zrGE/3BZmRtABEBAAH+CQMIXO5uufhKJAVg2nFk\n" +
                    "yYH/a2vZ1Ix+x2rfllH078s7soUN+O63fEPLQuT7DXNE1Zv689Kul3J5nEVN2dIa\n" +
                    "r2gnFhnpCCfn876x7kPDO8b85oB/Q6lI14aK/xlHfM1VXWaZwtCDYt2Sn13C0FNr\n" +
                    "pylC4hRV8oY44FP+8bdgKP7gTwV7E9mm14Y/EnBl1bRVMbSFV0i2zDS5Jexjdn6s\n" +
                    "krbC3qhIEIBKh3EKSi1EQaND5748dGruBZ+wTE2ViwZpw46C2IlhYMEOctOk/v+w\n" +
                    "dqEndScBPmuKVA6ZQ6/lpghDg7TqNyg+CWVRuywkOqy99B9QtOXMYlmADBdALMtm\n" +
                    "kVqPIhE2QPJOY52AzQ+qLpAjli+9jM1kk/yHx+lbTlPLvyRBVaSi7uptAnOvA9ep\n" +
                    "NuqbCpzbgMQHgKrDwzoCLKtBcy+Z9+gwDJ9yAimpvDKwAbcrw6akWg9d/jjitw/j\n" +
                    "j/5H4xJp42ai+3j5KxcHfE7wv2Mthydt9cLAgwQYAQoADwUCXxKvqgUJDwmcAAIb\n" +
                    "LgCoCRBa8GGUfMO9JJ0gBBkBCgAGBQJfEq+qAAoJEPah6QxerICQUNAD/2JZCHnB\n" +
                    "Hk8h9QTfRpcxrqm823Ieb/3Tg0oka/Vw5CysEagahS9Mp5SLfYvC+0tCsAiOHjxN\n" +
                    "eiceV7CLAKH+1MgPvGY+C0hwUMD3W3t5QmYyqQWNBzvBvBGxt5qRmOJg128h+krT\n" +
                    "oqo510YWIysEFatLhPieXONoi2RuhNpPJW8MB+gD/jd2bjSkp5Aqu42jGaFhPWyA\n" +
                    "pECHBKcr0k4yz98Al+76GsKwMRiN9jhUi2oPFUY6QAPLYO1QqBRjqUDdUXHD6mOM\n" +
                    "8HAUXUYFaJtllM8gggnBvIzQ4h8mwNOMHKaVZ0R1Tr1++i4pFDH8CaGJvZuWKKT0\n" +
                    "NV9FqgBct67U8046zNDX\n" +
                    "=yonD\n" +
                    "-----END PGP PRIVATE KEY BLOCK-----\n";

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
                pubKey = MerchantProcess.readPublicKey(publicKeyBank1);
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
                pgpSec = MerchantProcess.readSecretKey(privateKeyBank1, pubKey.getKeyID());
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


                messageSignature = MerchantProcess.signaturePgp(message, pgpSec,
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

    public static void main(String[] args) throws PGPException {
        boolean sig = MerchantProcess.verifySignaturePgp(signBank1.getBytes(StandardCharsets.UTF_8), publicKeyBank1);
        logger.info("sig: {}", sig);
    }
}
