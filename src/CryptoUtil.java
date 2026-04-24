import javax.crypto.Cipher;
import java.security.*;
import java.util.Base64;

public class CryptoUtil {

    public static KeyPair generateKeyPair(int keySize) {
        KeyPair keys = null;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keySize);
            keys = keyGen.genKeyPair();
        } catch (Exception ex) {
            System.err.println("Error generando las claves: " + ex);
        }
        return keys;
    }

    public static String encrypt(byte[] data, PublicKey publicKey) {
        byte[] encryptedData = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            encryptedData = cipher.doFinal(data);
        } catch (Exception ex) {
            System.err.println("Error encriptando: " + ex);
        }
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public static String decrypt(String encryptedData, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedBytes);
            return new String(decryptedData);
        } catch (Exception ex) {
            System.err.println("Error desencriptando: " + ex);
            return null;
        }
    }

    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static PublicKey stringToPublicKey(String publicKeyStr) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            java.security.spec.X509EncodedKeySpec spec =
                    new java.security.spec.X509EncodedKeySpec(decodedKey);
            return keyFactory.generatePublic(spec);
        } catch (Exception ex) {
            System.err.println("Error convirtiendo String a PublicKey: " + ex);
            return null;
        }
    }
}