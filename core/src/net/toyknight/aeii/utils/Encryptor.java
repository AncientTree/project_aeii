package net.toyknight.aeii.utils;

import com.badlogic.gdx.Gdx;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author toyknight 10/19/2015.
 */
public class Encryptor {

    private static final String TAG = "Encryptor";

    private final static String[] HEX_DIGITS =
            {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public String encryptString(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] origin = str.getBytes("UTF-8");
            byte[] encrypted = md.digest(origin);
            return byteArrayToHexString(encrypted);
        } catch (UnsupportedEncodingException ex) {
            Gdx.app.log(TAG, str);
            return str;
        } catch (NoSuchAlgorithmException ex) {
            Gdx.app.log(TAG, str);
            return str;
        }
    }

    private String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (byte aB : b) {
            resultSb.append(byteToHexString(aB));
        }
        return resultSb.toString();
    }

    private String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return HEX_DIGITS[d1] + HEX_DIGITS[d2];
    }

}
