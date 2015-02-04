package com.satelinx.satelinx.models;

import com.google.gson.JsonObject;
import com.satelinx.satelinx.helpers.Serialization;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by jlh on 1/2/15.
 */
public class User {

    long id;

    String username;

    String name;

    String salt;

    String authorization_hash;

    List<Account> accounts;

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getAuthorizationHash() {
        return authorization_hash;
    }

    public void setAuthorizationHash(String authorizationHash) {
        this.authorization_hash = authorizationHash;
    }

    public void buildAuthorizationHash(String password) {
        String input = this.salt + "--" + password.trim();
        MessageDigest digester = null;
        try {
            digester = MessageDigest.getInstance("SHA-256");
            digester.reset();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }
        try {
            digester.update(input.getBytes("UTF-8"));
            byte[] digest = digester.digest();
            this.authorization_hash = bin2hex(digest);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data)).toLowerCase();
    }

    public JsonObject toJson() {

        return Serialization.getGsonInstance().toJsonTree(this).getAsJsonObject();
    }


    public List<Account> getAccounts() {
        return this.accounts;
    }

    public Account getAccount(int index) {
        if (this.accounts == null || this.accounts.isEmpty()) {
            return null;
        }
        if (index < 0 || index >= this.accounts.size()) {
            return null;
        }
        return this.accounts.get(index);
    }

}
