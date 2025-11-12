package com.anas.springblog;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

import javax.crypto.SecretKey;

public class Base64KeyGenerator {
    public static void main(String[] args) {
        SecretKey secretKey = Jwts.SIG.HS256.key().build();
//        System.out.println(secretKey.toString());
        String key = Encoders.BASE64.encode(secretKey.getEncoded());
        System.out.println(key);
    }
}
