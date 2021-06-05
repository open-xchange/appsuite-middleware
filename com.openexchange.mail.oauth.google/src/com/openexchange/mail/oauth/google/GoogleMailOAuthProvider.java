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

package com.openexchange.mail.oauth.google;

import static com.openexchange.google.api.client.GoogleApiClients.REFRESH_THRESHOLD;
import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Builder;
import com.google.api.services.gmail.model.Profile;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.ImmutableAutoconfig;
import com.openexchange.mail.oauth.DefaultTokenInfo;
import com.openexchange.mail.oauth.MailOAuthProvider;
import com.openexchange.mail.oauth.TokenInfo;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link GoogleMailOAuthProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class GoogleMailOAuthProvider implements MailOAuthProvider {

    /**
     * Initializes a new {@link GoogleMailOAuthProvider}.
     */
    public GoogleMailOAuthProvider() {
        super();
    }

    @Override
    public Autoconfig getAutoconfigFor(OAuthAccount oauthAccount, Session session) throws OXException {
        try {
            // Ensure not expired
            OAuthAccount oauthAccountToUse = oauthAccount;
            OAuthAccount newAccount = GoogleApiClients.ensureNonExpiredGoogleAccount(oauthAccountToUse, session);
            if (null != newAccount) {
                oauthAccountToUse = newAccount;
            }

            // Determine E-Mail address from "user/me" end-point
            GoogleCredential credentials = com.openexchange.google.api.client.GoogleApiClients.getCredentials(oauthAccountToUse, session);
            Gmail gmail = new Builder(credentials.getTransport(), credentials.getJsonFactory(), credentials).setApplicationName(GoogleApiClients.getGoogleProductName(session)).build();
            Profile profile = gmail.users().getProfile("me").execute();
            String email = profile.getEmailAddress();

            ImmutableAutoconfig.Builder builder = ImmutableAutoconfig.builder();
            builder.username(email);
            builder.mailOAuthId(oauthAccount.getId()).mailPort(I(993)).mailProtocol("imap").mailSecure(Boolean.TRUE).mailServer("imap.gmail.com").mailStartTls(false);
            builder.transportOAuthId(oauthAccount.getId()).transportPort(I(-1)).transportProtocol("gmailsend").transportSecure(Boolean.FALSE).transportServer("www.googleapis.com");
            return builder.build();
        } catch (IOException e) {
            throw OAuthExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TokenInfo getTokenFor(OAuthAccount oauthAccount, Session session) throws OXException {
        // Ensure not expired
        String key = "oauth.google.expiry." + oauthAccount.getId();

        // Query session parameters
        {
            Object object = session.getParameter(key);
            if (object instanceof Long) {
                Long stamp = (Long) object;
                if ((stamp.longValue() - System.currentTimeMillis()) >= REFRESH_THRESHOLD) {
                    // More than 1 minute to go
                    return DefaultTokenInfo.newXOAUTH2TokenInfoFor(oauthAccount.getToken());
                }

                // Expired... Drop cached value.
                session.setParameter(key, null);
            }
        }

        // Not cached... Query Google API
        {
            int expirySeconds = GoogleApiClients.getExpiryForGoogleAccount(oauthAccount, session);
            if (expirySeconds >= REFRESH_THRESHOLD) {
                // More than 1 minute to go
                session.setParameter(key, Long.valueOf(System.currentTimeMillis() + (expirySeconds * 1000)));
                return DefaultTokenInfo.newXOAUTH2TokenInfoFor(oauthAccount.getToken());
            }
        }

        // Expired...
        OAuthAccount oauthAccountToUse = oauthAccount;
        OAuthAccount newAccount = GoogleApiClients.ensureNonExpiredGoogleAccount(oauthAccountToUse, session);
        if (null != newAccount) {
            oauthAccountToUse = newAccount;
        }
        return DefaultTokenInfo.newXOAUTH2TokenInfoFor(oauthAccountToUse.getToken());
    }

    @Override
    public String getProviderId() {
        return KnownApi.GOOGLE.getServiceId();
    }
}
