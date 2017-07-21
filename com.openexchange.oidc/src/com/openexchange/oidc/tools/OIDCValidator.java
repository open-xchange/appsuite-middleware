package com.openexchange.oidc.tools;

import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.openexchange.exception.OXException;

public interface OIDCValidator {
    
    boolean validateIdToken(AccessToken accessToken) throws OXException;

}
