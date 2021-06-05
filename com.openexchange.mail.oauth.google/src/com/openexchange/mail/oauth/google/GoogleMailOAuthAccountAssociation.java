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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.gmail.Gmail;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.association.AbstractOAuthAccountAssociation;
import com.openexchange.oauth.association.Module;
import com.openexchange.oauth.association.Status;
import com.openexchange.oauth.google.GoogleOAuthScope;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.session.Session;

/**
 * {@link GoogleMailOAuthAccountAssociation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class GoogleMailOAuthAccountAssociation extends AbstractOAuthAccountAssociation {

    /**
     * The identifier for Google Mail service.
     */
    private static final String serviceId = "googlemail";
    private final MailAccount mailAccount;

    /**
     * Initialises a new {@link GoogleMailOAuthAccountAssociation}.
     * 
     * @param accountId
     * @param userId
     * @param contextId
     */
    public GoogleMailOAuthAccountAssociation(int accountId, int userId, int contextId, MailAccount mailAccount) {
        super(accountId, userId, contextId);
        this.mailAccount = mailAccount;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getId() {
        return Integer.toString(mailAccount.getId());
    }

    @Override
    public String getDisplayName() {
        return mailAccount.getName();
    }

    @Override
    public String getModule() {
        return Module.MAIL.getModuleName();
    }

    @Override
    public String getFolder() {
        return mailAccount.getRootFolder();
    }

    @Override
    public List<OAuthScope> getScopes() {
        return Collections.singletonList(GoogleOAuthScope.mail);
    }

    @Override
    public Status getStatus(Session session) throws OXException {
        try {
            OAuthAccount oauthAccount = GoogleApiClients.getGoogleAccount(getOAuthAccountId(), session);
            GoogleCredential credentials = GoogleApiClients.getCredentials(oauthAccount, session);
            if (credentials == null) {
                throw new IllegalArgumentException("Cannot get credentials. Neither the oauth account nor the credentials can be 'null'.");
            }
            Gmail gmail = new Gmail.Builder(credentials.getTransport(), credentials.getJsonFactory(), credentials).setApplicationName(GoogleApiClients.getGoogleProductName(session)).build();
            gmail.users().getProfile("me").execute();
            return Status.OK;
        } catch (OXException | IOException e) {
            return Status.RECREATION_NEEDED;
        }
    }

    @Override
    protected AbstractOAuthAccess newAccess(Session session) throws OXException {
        throw new UnsupportedOperationException("No OAuthAccess for Google Mail.");
    }
}
