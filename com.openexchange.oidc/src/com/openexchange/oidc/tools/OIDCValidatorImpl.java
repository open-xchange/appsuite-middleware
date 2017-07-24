
package com.openexchange.oidc.tools;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.spi.OIDCBackend;;

public abstract class OIDCValidatorImpl implements OIDCValidator {

    private OIDCBackend backend;

    public OIDCValidatorImpl(OIDCBackend backend) {
        super();
        this.backend = backend;
    }

    @Override
    public boolean validateIdToken(JWT idToken, String nonce) throws OXException {
        JWKSet jwkSet = backend.getJwkSet();
        JWSAlgorithm expectedJWSAlg = backend.getJWSAlgorithm();

        IDTokenValidator idTokenValidator = new IDTokenValidator(new Issuer(""), new ClientID(this.backend.getBackendConfig().getClientID()), expectedJWSAlg, jwkSet);
        try {
            idTokenValidator.validate(idToken, new Nonce(nonce));
        } catch (BadJOSEException e) {
            throw OIDCExceptionCode.IDTOKEN_VALIDATON_FAILED_CONTENT.create(e, "");
        } catch (JOSEException e) {
            throw OIDCExceptionCode.IDTOKEN_VALIDATON_FAILED.create(e, "");
        }
        return true;
    }

    protected boolean validateClaims(JWTClaimsSet tokenClaims, String nonce) {
        return false;
    }

    // TODO QS-VS: WICHTIG
    // Access und refresh muss nicht validiert werden
    // Oauth Tokens müssen mit in die Session, sind allerdings optional
    // IDToken muss ebenfalls an die Session gehangen werden
}
