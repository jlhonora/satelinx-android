package com.satelinx.satelinx.events;

import com.satelinx.satelinx.models.Account;

/**
 * Created by jlh on 1/17/15.
 */
public class SelectAccountReadyEvent {

    public Account account;

    public SelectAccountReadyEvent(Account a) {
        this.account = a;
    }
}
