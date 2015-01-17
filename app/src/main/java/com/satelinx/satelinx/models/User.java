package com.satelinx.satelinx.models;

import android.accounts.Account;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.satelinx.satelinx.models.typeAdapters.UserTypeAdapter;

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
    String salt;
    transient String authorizationHash;

    List<Account> accounts;

    public String getUsername() {
        return username;
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
        return authorizationHash;
    }

    public void setAuthorizationHash(String authorizationHash) {
        this.authorizationHash = authorizationHash;
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
            this.authorizationHash = bin2hex(digest);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data)).toLowerCase();
    }

    public JsonObject toJson() {

        return User.getJsonInstance().toJsonTree(this).getAsJsonObject();
    }

    public static Gson getJsonInstance() {
        return new GsonBuilder()
                .registerTypeAdapter(User.class, new UserTypeAdapter())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

}
