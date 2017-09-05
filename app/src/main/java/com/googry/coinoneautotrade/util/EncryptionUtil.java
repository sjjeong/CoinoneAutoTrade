package com.googry.coinoneautotrade.util;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by seokjunjeong on 2017. 8. 28..
 */

public class EncryptionUtil {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String getJsonLimitOrders(String accessToken,
                                            String currency,
                                            long nonce) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("access_token", accessToken);
            jsonObject.put("currency", currency);
            jsonObject.put("nonce", nonce);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static String getJsonBalance(String accessToken, long nonce) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("access_token", accessToken);
            jsonObject.put("nonce", nonce);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static String getJsonOrderBuy(String accessToken,
                                         long price,
                                         double qty,
                                         String currency,
                                         long nonce) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("access_token", accessToken);
            jsonObject.put("price", price);
            jsonObject.put("qty", qty);
            jsonObject.put("currency", currency);
            jsonObject.put("nonce", nonce);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static String getEncyptPayload(String payload) {

        String base64Payload = Base64.encodeToString(payload.getBytes(), 0);
        base64Payload = base64Payload.replace("\n", "");
        return base64Payload;
    }

    public static String getSignature(String secretKey, String encrypPayload) {
        try {
            secretKey = secretKey.toUpperCase();
            Mac sha512_HMAC = null;
            byte[] byteKey = secretKey.getBytes("UTF-8");
            final String HMAC_SHA256 = "HmacSHA512";
            sha512_HMAC = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA256);
            sha512_HMAC.init(keySpec);
            byte[] mac_data = sha512_HMAC.doFinal(encrypPayload.getBytes("UTF-8"));
            String result = null;
            result = bytesToHex(mac_data);
            return result;
        } catch (Exception e) {
        }
        return null;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }
}
