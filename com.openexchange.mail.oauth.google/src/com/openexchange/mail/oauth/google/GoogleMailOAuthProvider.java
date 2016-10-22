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

package com.openexchange.mail.oauth.google;

import java.io.IOException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Profile;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.ImmutableAutoconfig;
import com.openexchange.mail.oauth.MailOAuthProvider;
import com.openexchange.oauth.API;
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
            Gmail gmail = new Gmail.Builder(credentials.getTransport(), credentials.getJsonFactory(), credentials).setApplicationName(GoogleApiClients.getGoogleProductName(session)).build();
            Profile profile = gmail.users().getProfile("me").execute();
            String email = profile.getEmailAddress();

            ImmutableAutoconfig.Builder builder = ImmutableAutoconfig.builder();
            builder.username(email);
            builder.mailOAuthId(oauthAccount.getId()).mailPort(993).mailProtocol("imap").mailSecure(true).mailServer("imap.gmail.com").mailStartTls(false);
            builder.transportOAuthId(oauthAccount.getId()).transportPort(587).transportProtocol("smtp").transportSecure(false).transportServer("smtp.gmail.com").transportStartTls(true);
            return builder.build();
        } catch (IOException e) {
            throw OAuthExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getProviderId() {
        return API.GOOGLE.getFullName();
    }
}
