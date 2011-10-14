package com.openexchange.oauth;

import java.sql.Connection;
import java.util.Map;

import com.openexchange.exception.OXException;

public interface OAuthAccountInvalidationListener {
	/**
     * Triggered when an oauth account was invalidated.
     */
    public void onAfterOAuthAccountInvalidation(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException;
}
