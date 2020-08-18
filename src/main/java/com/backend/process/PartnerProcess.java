package com.backend.process;

import com.backend.model.Partner;
import com.backend.model.request.bank.QueryAccountRequest;
import com.backend.model.request.transaction.TransactionRequest;
import com.backend.model.request.transaction.TransferRequest;
import com.backend.service.IPartnerService;
import com.backend.util.DataUtil;
import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.bouncycastle.util.io.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.util.Collection;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Date;
import java.util.Iterator;


public class PartnerProcess {
    private static final Logger logger = LogManager.getLogger(PartnerProcess.class);
    private static final BouncyCastleProvider provider = new BouncyCastleProvider();

    @Autowired
    IPartnerService partnerService;

    public static PGPPublicKey readPublicKey(String input)
            throws IOException, PGPException {
        InputStream in = new ByteArrayInputStream(input.getBytes());
        PGPPublicKey k = null;
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream( in ), new JcaKeyFingerprintCalculator());
        in.close();
        Iterator rIt = pgpPub.getKeyRings();
        while (rIt.hasNext()) {
            PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
            Iterator kIt = kRing.getPublicKeys();
            while (kIt.hasNext()) {
                k = (PGPPublicKey) kIt.next();
                if (k.isEncryptionKey()) {
                    break;
                }
            }
        }
        return k;
    }

    public static PGPSecretKey readSecretKey(String input, long keyId)
            throws IOException, PGPException {
        InputStream in = new ByteArrayInputStream(input.getBytes());
        in = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(in);

        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(in, new BcKeyFingerprintCalculator());
        in.close();
        PGPSecretKey key = pgpSec.getSecretKey(keyId);

        if (key == null) {
            throw new IllegalArgumentException("Can't find encryption key in key ring.");
        }
        return key;
    }

    public static String signaturePgp(String message, PGPSecretKey pgpSec, char[] pass) throws IOException,
            PGPException {
        byte[] messageCharArray = message.getBytes();
        Security.addProvider(provider);

        ByteArrayOutputStream encOut = new ByteArrayOutputStream();
        OutputStream out = encOut;
        out = new ArmoredOutputStream(out);


        // Unlock the private key using the password
        String sec = "";
        BASE64Encoder base64Encoder = new BASE64Encoder();
        sec = base64Encoder.encode(pgpSec.getEncoded()).replaceAll("\\r\\n|\\r|\\n|", "");
        logger.info("sec: {}", sec);

        PBESecretKeyDecryptor secretKeyDecrypt =
                new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider())
                        .build(pass);
        PGPPrivateKey pgpPrivKey = pgpSec.extractPrivateKey(secretKeyDecrypt);

        if (pgpPrivKey == null)
            throw new IllegalArgumentException("Unsupported signing key"
                    + (pgpSec.getKeyEncryptionAlgorithm() == PGPPublicKey.RSA_SIGN ?
                    ": RSA (sign-only) is unsupported by BouncyCastle" : ""));
        PGPPublicKey publicKey = pgpSec.getPublicKey();

        // Signature generator, we can generate the public key from the private
        // key! Nifty!

        PGPSignatureGenerator sGen = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(publicKey
                        .getAlgorithm(), PGPUtil.SHA512));

        sGen.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey); //PGPSignature.DIRECT_KEY


        Iterator it = pgpSec.getPublicKey().getUserIDs();
        if (it.hasNext()) {
            PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
            spGen.setSignerUserID(false, (String) it.next());
            sGen.setHashedSubpackets(spGen.generate());
        }

        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(
                PGPCompressedData.ZLIB);

        BCPGOutputStream bOut = new BCPGOutputStream(comData.open(out));

        sGen.generateOnePassVersion(false).encode(bOut);

        PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
        OutputStream lOut = lGen.open(bOut, PGPLiteralData.BINARY,
                PGPLiteralData.CONSOLE, messageCharArray.length, new Date());

        for (byte c : messageCharArray) {
            lOut.write(c);
            sGen.update(c);
        }

        lOut.close();
        /*
         * while ((ch = message.toCharArray().read()) >= 0) { lOut.write(ch);
         * sGen.update((byte) ch); }
         */
        lGen.close();

        sGen.generate().encode(bOut);

        comData.close();

        out.close();

        return encOut.toString();
    }

    public static boolean verifySignaturePgp(String logId, byte[] signedMessage, String pub) {
        try
        {
            Security.addProvider(provider);

            PGPPublicKey publicKey = readPublicKey(pub);
            InputStream in = PGPUtil.getDecoderStream( new ByteArrayInputStream( signedMessage ) );

            PGPObjectFactory  pgpFact = new PGPObjectFactory ( in, new BcKeyFingerprintCalculator() );

            PGPCompressedData c1 = ( PGPCompressedData ) pgpFact.nextObject();

            pgpFact = new PGPObjectFactory ( c1.getDataStream(),  new BcKeyFingerprintCalculator() );

            PGPOnePassSignatureList p1 = ( PGPOnePassSignatureList ) pgpFact.nextObject();

            PGPOnePassSignature ops = p1.get( 0 );

            PGPLiteralData p2 = ( PGPLiteralData ) pgpFact.nextObject();

            InputStream dIn = p2.getInputStream();
            int ch;

            ops.init( new JcaPGPContentVerifierBuilderProvider().setProvider(Security.getProvider("BC")), publicKey);

            while ( ( ch = dIn.read() ) >= 0 )
            {
                ops.update( ( byte ) ch );
            }

            PGPSignatureList p3 = ( PGPSignatureList ) pgpFact.nextObject();

            if ( ops.verify( p3.get( 0 ) ) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch ( Exception e ) {
            logger.error("{}| Verify signature catch exception: ", logId, e);
            return false;
        }
    }


    public static Boolean validateQueryAccountHash(String logId, Partner partner, QueryAccountRequest request)
            throws IOException, PGPException {
        JsonObject dataToHash = new JsonObject();
        dataToHash.put("cardNumber", request.getCardNumber())
                .put("partnerCode", request.getPartnerCode())
                .put("requestId", request.getRequestId())
                .put("requestTime", request.getRequestTime());
        PGPPublicKey publicKey = readPublicKey(partner.getPublicKey());
        String secretKey = DataUtil.pgpSecretKeyToString(readSecretKey(partner.getSecretKey(), publicKey.getKeyID()));
        String hashGen = DataUtil.createHash(dataToHash, secretKey, logId);
        String hash    = request.getHash();
        logger.info("{}| LHBank hash {} - Partner hash: {}", logId, hashGen, hash);
        if (!hashGen.equalsIgnoreCase(hash)) {
            logger.warn("{}| Valid signature: Fail!", logId);
            return false;
        }
        logger.info("{}| Validate hash: Success!", logId);
        return true;
    }

    public static Boolean validateTransferHash(String logId, Partner partner, TransferRequest request)
            throws IOException, PGPException {
        JsonObject dataToHash = new JsonObject()
                .put("cardName", request.getCardName())
                .put("bankCode", request.getBankCode())
                .put("from", request.getFrom())
                .put("isTransfer", request.getIsTransfer())
                .put("merchantCode", request.getPartnerCode())
                .put("requestId", request.getRequestId())
                .put("requestTime", request.getRequestTime())
                .put("to", request.getTo())
                .put("typeFee", request.getTypeFee())
                .put("value", request.getValue());
        PGPPublicKey publicKey = readPublicKey(partner.getPublicKey());
        String secretKey = DataUtil.pgpSecretKeyToString(readSecretKey(partner.getSecretKey(), publicKey.getKeyID()));
        String hashGen = DataUtil.createHash(dataToHash, secretKey, logId);
        String hash    = request.getHash();
        logger.info("{}| LHBank hash {} - Partner hash: {}", logId, hashGen, hash);
        if (!hashGen.equalsIgnoreCase(hash)) {
            logger.warn("{}| Valid signature: Fail!", logId);
            return false;
        }
        logger.info("{}| Validate hash: Success!", logId);
        return true;
    }

    public static String callPartnerTransfer(int bankId, TransactionRequest request) {

        return null;
    }

    public static String encrypt(byte[] data, String publicKeyFile) {
        try
        {
//            // ----- Read in the public key
            PGPPublicKey key = readPublicKey(StringEscapeUtils.unescapeJava(publicKeyFile));
            System.out.println("Creating a temp file...");
            // create a file and write the string to it
            File tempFile = File.createTempFile("pgp", null);
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(data);
            fos.close();
            System.out.println("Temp file created at ");
            System.out.println(tempFile.getAbsolutePath());
            System.out.println("Reading the temp file to make sure that the bits were written\n--------------");
            BufferedReader isr = new BufferedReader(new FileReader(tempFile));
            String line = "";
            while ( (line = isr.readLine()) != null )
            {
                System.out.println(line + "\n");
            }
            // find out a little about the keys in the public key ring
            System.out.println("Key Strength = " + key.getBitStrength());
            System.out.println("Algorithm = " + key.getAlgorithm());
            System.out.println("Bit strength = " + key.getBitStrength());
            System.out.println("Version = " + key.getVersion());
            System.out.println("Encryption key = " + key.isEncryptionKey()+ ", Master key = " + key.isMasterKey());
            int count = 0;
            for ( java.util.Iterator iterator = key.getUserIDs(); iterator.hasNext(); )
            {
                count++;
                System.out.println((String) iterator.next());
            }
            System.out.println("Key Count = " + count);
            // create an armored ascii file
            // FileOutputStream out = new FileOutputStream(outputfile);
            // encrypt the file
            // encryptFile(tempfile.getAbsolutePath(), out, key);
            // Encrypt the data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.out.println("encrypted text length before=" + baos.size());
            _encrypt(tempFile.getAbsolutePath(), baos, key);
            System.out.println("encrypted text length=" + baos.size());
            tempFile.delete();

//            KeyPair pair = generateKeyPair();
//            PrivateKey privateKey = pair.getPrivate();
//
//            Cipher cipher = Cipher.getInstance("RSA");
//            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
//
//            byte[] encryptedMessage = cipher.doFinal(data);
//
//            cipher.init(Cipher.ENCRYPT_MODE, (Key) key);
//
//            byte[] encryptedPublicKey = cipher.doFinal(pair.getPublic().getEncoded());
//
//            ByteBuffer buffer = ByteBuffer.allocate((encryptedPublicKey.length + encryptedMessage.length) + 4);
//            buffer.putInt(encryptedPublicKey.length);
//            buffer.put(encryptedPublicKey);
//            buffer.put(encryptedMessage);
//            return new BASE64Encoder().encode(buffer.array());
            return new BASE64Encoder().encode(baos.toByteArray());
        }
        catch (PGPException e)
        {
            // System.out.println(e.toString());
            System.out.println(e.getUnderlyingException().toString());
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    protected static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024, SecureRandom.getInstance("SHA1PRNG"));
        return keyPairGenerator.generateKeyPair();
    }
    private static void _encrypt(String fileName, OutputStream out, PGPPublicKey encKey)
            throws IOException, PGPException
    {
        out = new DataOutputStream(out);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        System.out.println("creating comData...");
        // get the data from the original file
        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedDataGenerator.ZIP);
        PGPUtil.writeFileToLiteralData(comData.open(bOut), PGPLiteralData.BINARY, new File(fileName));
        comData.close();
        System.out.println("comData created...");
        System.out.println("using PGPEncryptedDataGenerator...");
        // object that encrypts the data
        JcePGPDataEncryptorBuilder builder = new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256)
                .setWithIntegrityPacket(true)
                .setSecureRandom(new SecureRandom()).setProvider("BC");
        PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(builder);
        cPk.addMethod( new JcePublicKeyKeyEncryptionMethodGenerator( encKey ).setProvider( "BC" ));
        System.out.println("used PGPEncryptedDataGenerator...");
        // take the outputstream of the original file and turn it into a byte
        // array
        byte[] bytes = bOut.toByteArray();
        System.out.println("wrote bOut to byte array...");
        // write the plain text bytes to the armored outputstream
        OutputStream cOut = cPk.open(out, bytes.length);
        cOut.write(bytes);
        cPk.close();
        out.close();
    }

    public static String createEncryptedData(
            byte[] data,
            String pubKey)
            throws Exception {
//        encryptionKey

        JcePGPDataEncryptorBuilder builder = new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256)
                        .setWithIntegrityPacket(true)
                        .setSecureRandom(new SecureRandom()).setProvider("BC");
        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(builder);
//        encGen.addMethod(readPublicKey(StringEscapeUtils.unescapeJava(pubKey)));

//        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
//                new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256)
//                        .setWithIntegrityPacket(true)
//                        .setSecureRandom(new SecureRandom()).setProvider("BC"));
//        encGen.addMethod(
//                new JcePublicKeyKeyEncryptionMethodGenerator(encryptionKey)
//                        .setProvider("BC"));
        ByteArrayOutputStream encOut = new ByteArrayOutputStream();
        // create an indefinite length encrypted stream
        OutputStream cOut = encGen.open(encOut, new byte[4096]);
        // write out the literal data
        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
        OutputStream pOut = lData.open(
                cOut, PGPLiteralData.BINARY,
                PGPLiteralData.CONSOLE, data.length, new Date());
        pOut.write(data);
        pOut.close();
        // finish the encryption
        cOut.close();
        return new BASE64Encoder().encode(encOut.toByteArray()) ;
    }
}
