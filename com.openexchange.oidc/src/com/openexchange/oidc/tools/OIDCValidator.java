package com.openexchange.oidc.tools;

import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.openexchange.exception.OXException;

public interface OIDCValidator {
    
    boolean validateIdToken(JWT idToken, String nonce) throws OXException;

}
