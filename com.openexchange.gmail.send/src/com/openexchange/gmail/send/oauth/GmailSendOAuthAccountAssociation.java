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

package com.openexchange.gmail.send.oauth;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.gmail.send.GmailSendProvider;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.TransportAccount;
import com.openexchange.oauth.association.Module;
import com.openexchange.oauth.association.OAuthAccountAssociation;
import com.openexchange.oauth.association.Status;
import com.openexchange.oauth.google.GoogleOAuthScope;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.session.Session;


/**
 * {@link GmailSendOAuthAccountAssociation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class GmailSendOAuthAccountAssociation implements OAuthAccountAssociation {

    private final int accountId;
    private final TransportAccount account;
    private final int userId;
    private final int contextId;

    /**
     * Initializes a new {@link GmailSendOAuthAccountAssociation}.
     *
     * @param accountId The identifier of the OAuth account
     * @param account The associated transport account
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public GmailSendOAuthAccountAssociation(int accountId, TransportAccount account, int userId, int contextId) {
        super();
        this.accountId = accountId;
        this.account = account;
        this.userId = userId;
        this.contextId = contextId;
    }

    @Override
    public int getOAuthAccountId() {
        return accountId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public String getServiceId() {
        return GmailSendProvider.PROTOCOL_GMAIL_SEND.getName();
    }

    @Override
    public String getId() {
        return Integer.toString(account.getId());
    }

    @Override
    public String getDisplayName() {
        return account.getName();
    }

    @Override
    public String getModule() {
        return Module.MAIL.getModuleName();
    }

    @Override
    public String getFolder() {
        return MailFolderUtility.prepareFullname(account.getId(), MailFolder.ROOT_FOLDER_ID);
    }

    @Override
    public Status getStatus(Session session) throws OXException {
        try {
            MailTransport transport = MailTransport.getInstance(session, account.getId());
            transport.ping();
            return Status.OK;
        } catch (OXException e) {
            return Status.RECREATION_NEEDED;
        }
    }

    @Override
    public List<OAuthScope> getScopes() {
        return Collections.singletonList(GoogleOAuthScope.mail);
    }

}
