package com.satelinx.satelinx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jlh on 1/2/15.
 */
public class User {

    long id;
    String username;
    String salt;

    public transient String authorizationHash;

    public void buildAuthorizationHash(String password) {
        String input = this.salt + "--" + password;
        MessageDigest digester = null;
        try {
            digester = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }
        try {
            byte[] bytes = new byte[1024];
            int byteCount;
            InputStream stream = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));
            while ((byteCount = stream.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }
            byte[] digest = digester.digest();
            this.authorizationHash = digest.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
