package com.satelinx.satelinx.events;

import com.satelinx.satelinx.models.Account;

/**
 * Created by jlh on 1/17/15.
 */
public class SelectAccountEvent extends AuthenticatedEvent {

    public Account account;

    public SelectAccountEvent(Account a, String authorizationHash) {
        this.authorizationHash = authorizationHash;
        this.account = a;
    }
}
