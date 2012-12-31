package com.openexchange.xing.session;

/**
 * <p>
 * Holds your app's key and secret.
 * </p>
 */
public final class AppKeyPair extends TokenPair {

    private static final long serialVersionUID = -6101337006024285975L;

    public AppKeyPair(String key, String secret) {
        super(key, secret);
    }
}
