/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.oauth.provider.impl.jwt;

import java.text.ParseException;
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

    public static final String SCOPE_CLAIM_NAME = "scope";
    public static final String AUTHORIZED_PARTY_CLAIM_NAME = "azp";

    private List<String> issuer;

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
        super.verify(claimsSet, context);
        verify(claimsSet);
    }


    @Override
    public void verify(JWTClaimsSet claimsSet) throws BadJWTException {
        try {
            //Verify that the JWT issuer matches the configured issuer and therefore is allowed.
            if (!issuer.get(0).isEmpty() && !issuer.contains(claimsSet.getIssuer())) {
                throw new BadJWTException("JWT validation failed because of invalid issuer: " + claimsSet.getIssuer());
            }

            //Verify that the clientname claim is not empty.
            if (Strings.isEmpty(claimsSet.getStringClaim(AUTHORIZED_PARTY_CLAIM_NAME))) {
                throw new BadJWTException("Clientname claim is empty");
            }

            //Verify that the scope claim is not empty.
            if (Strings.isEmpty(claimsSet.getStringClaim(OAuthJWTClaimVerifier.SCOPE_CLAIM_NAME))) {
                throw new BadJWTException("Scope is null or empty");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
