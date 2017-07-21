package com.openexchange.oidc.tools;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.spi.OIDCBackend;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;;

public abstract class OIDCValidatorImpl implements OIDCValidator{
    
    private OIDCBackend backend;
    
    
    public OIDCValidatorImpl(OIDCBackend backend) {
        super();
        this.backend = backend;
    }

    @Override
    public boolean validateIdToken(AccessToken accessToken) throws OXException {
        JWTClaimsSet tokenClaims = getTokenClaims(accessToken.getValue());
        return validateClaims(tokenClaims);
    }
    
    public boolean validateClaims(JWTClaimsSet tokenClaims) {
        // TODO Auto-generated method stub
        return false;
    }

    public JWTClaimsSet getTokenClaims(String accessTokenString) {
        JWTClaimsSet claimsSet = null;
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        try {
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(this.backend.getBackendConfig().getPublicRSAKeys()));
            //TODO QS-VS: Algorithm from config?
            JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);
            SecurityContext ctx = null; // optional context parameter, not required here
            claimsSet = jwtProcessor.process(accessTokenString, ctx);
        } catch (MalformedURLException e) {
            //TODO QS-VS: Exception
            e.printStackTrace();
        } catch (ParseException e) {
          //TODO QS-VS: Exception
            e.printStackTrace();
        } catch (BadJOSEException e) {
          //TODO QS-VS: Exception
            e.printStackTrace();
        } catch (JOSEException e) {
          //TODO QS-VS: Exception
            e.printStackTrace();
        }
        return claimsSet;
    }

}
