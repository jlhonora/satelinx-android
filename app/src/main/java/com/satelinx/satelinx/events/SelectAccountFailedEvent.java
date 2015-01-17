package com.satelinx.satelinx.events;

import com.satelinx.satelinx.models.Account;

/**
 * Created by jlh on 1/17/15.
 */
public class SelectAccountFailedEvent {

    public Account account;
    public Exception exception;

    public SelectAccountFailedEvent(Account a, Exception e) {
        this.account = a;
        this.exception = e;
    }
}
