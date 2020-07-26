package com.backend.process;

import com.backend.model.Partner;
import com.backend.model.request.bank.QueryAccountRequest;
import com.backend.model.request.transaction.TransferRequest;
import com.backend.service.IPartnerService;
import com.backend.util.DataUtil;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.springframework.beans.factory.annotation.Autowired;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.util.Date;
import java.util.Iterator;


public class PartnerProcess {
    private static final Logger logger = LogManager.getLogger(PartnerProcess.class);

    @Autowired
    IPartnerService partnerService;

    public static PGPPublicKey readPublicKey(String input)
            throws IOException, PGPException {
        InputStream in = new ByteArrayInputStream(input.getBytes());
        PGPPublicKey k = null;
        in = PGPUtil.getDecoderStream(in);
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in);
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

        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(in);
        in.close();
        PGPSecretKey key = pgpSec.getSecretKey(keyId);

        if (key == null) {
            throw new IllegalArgumentException("Can't find encryption key in key ring.");
        }
        return key;
    }

    public static String signaturePgp(String message, PGPSecretKey pgpSec, char[] pass) throws IOException,
            NoSuchAlgorithmException, NoSuchProviderException, PGPException,
            SignatureException {
        byte[] messageCharArray = message.getBytes();
        Security.addProvider(new BouncyCastleProvider());

        ByteArrayOutputStream encOut = new ByteArrayOutputStream();
        OutputStream out = encOut;
        out = new ArmoredOutputStream(out);


        // Unlock the private key using the password
        String sec = "";
        BASE64Encoder base64Encoder = new BASE64Encoder();
        sec = base64Encoder.encode(pgpSec.getEncoded()).replaceAll("\\r\\n|\\r|\\n|", "");
        logger.info("sec: {}", sec);
        PGPPrivateKey pgpPrivKey = pgpSec.extractPrivateKey(pass, "BC");

        if (pgpPrivKey == null)
            throw new IllegalArgumentException("Unsupported signing key"
                    + (pgpSec.getKeyEncryptionAlgorithm() == PGPPublicKey.RSA_SIGN ?
                    ": RSA (sign-only) is unsupported by BouncyCastle" : ""));
        PGPPublicKey publicKey = pgpSec.getPublicKey();

        // Signature generator, we can generate the public key from the private
        // key! Nifty!
        PGPSignatureGenerator sGen = new PGPSignatureGenerator(publicKey.getAlgorithm(), PGPUtil.SHA1, "BC");

        sGen.initSign(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);


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

    public static boolean verifySignaturePgp(String logId, byte[] signedMessage, String pub) throws PGPException {
        try
        {
            Security.addProvider(new BouncyCastleProvider());

            PGPPublicKey publicKey = readPublicKey(pub);
            InputStream in = PGPUtil.getDecoderStream( new ByteArrayInputStream( signedMessage ) );

            PGPObjectFactory  pgpFact = new PGPObjectFactory ( in );

            PGPCompressedData c1 = ( PGPCompressedData ) pgpFact.nextObject();

            pgpFact = new PGPObjectFactory ( c1.getDataStream() );

            PGPOnePassSignatureList p1 = ( PGPOnePassSignatureList ) pgpFact.nextObject();

            PGPOnePassSignature ops = p1.get( 0 );

            PGPLiteralData p2 = ( PGPLiteralData ) pgpFact.nextObject();

            InputStream dIn = p2.getInputStream();
            int ch;


            ops.initVerify(publicKey, "BC");

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
}
