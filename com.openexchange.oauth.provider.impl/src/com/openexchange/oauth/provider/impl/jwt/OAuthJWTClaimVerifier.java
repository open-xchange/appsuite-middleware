/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.oauth.provider.impl.jwt;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.openexchange.java.Strings;

/**
 * {@link OAuthJWTClaimVerifier}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since v7.10.5
 */
public class OAuthJWTClaimVerifier<T extends SecurityContext> extends DefaultJWTClaimsVerifier<T> {

    public static final OAuthJWTClaimVerifier<SecurityContext> DEFAULT_VERIFIER = new OAuthJWTClaimVerifier<SecurityContext>(Collections.emptyList());

    public static final String SCOPE_CLAIM_NAME = "scope";
    public static final String AUTHORIZED_PARTY_CLAIM_NAME = "azp";

    private final List<String> issuer;

    /**
     * Initializes a new {@link OAuthJWTClaimVerifier}.
     *
     * @param issuer Allowed JWT issuer
     */
    public OAuthJWTClaimVerifier(List<String> issuer) {
        super();
        this.issuer = issuer;
    }

    @Override
    public void verify(JWTClaimsSet claimsSet, T context) throws BadJWTException {
        //Check if the token is expired
        super.verify(claimsSet, context);
        verify(claimsSet);
    }

    @Override
    public void verify(JWTClaimsSet claimsSet) throws BadJWTException {
        try {
            //Verify that the JWT issuer matches the configured issuer and therefore is allowed.
            if (issuer.isEmpty() == false && (issuer.size() > 1 || Strings.isNotEmpty(issuer.get(0))) && !issuer.contains(claimsSet.getIssuer())) {
                throw new BadJWTException("JWT validation failed because of an invalid issuer: " + claimsSet.getIssuer());
            }

            //Verify that the clientname claim is not empty.
            if (Strings.isEmpty(claimsSet.getStringClaim(AUTHORIZED_PARTY_CLAIM_NAME))) {
                throw new BadJWTException("The clientname claim of the received token is empty");
            }

            //Verify that the scope claim is not empty.
            if (Strings.isEmpty(claimsSet.getStringClaim(OAuthJWTClaimVerifier.SCOPE_CLAIM_NAME))) {
                throw new BadJWTException("The scope claim of the received token is empty");
            }

        } catch (ParseException e) {
            throw new BadJWTException("Unable to parse claim", e);
        }
    }

}
