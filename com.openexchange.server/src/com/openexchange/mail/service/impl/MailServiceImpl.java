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

package com.openexchange.mail.service.impl;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link MailServiceImpl} - The mail service implementation
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailServiceImpl implements MailService {

    /**
     * Initializes a new {@link MailServiceImpl}
     */
    public MailServiceImpl() {
        super();
    }

    @Override
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess(Session session, int accountId) throws OXException {
        return MailAccess.getInstance(session, accountId);
    }

    @Override
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess(int userId, int contextId, int accountId) throws OXException {
        return MailAccess.getInstance(userId, contextId, accountId);
    }

    @Override
    public MailTransport getMailTransport(Session session, int accountId) throws OXException {
        return MailTransport.getInstance(session, accountId);
    }

    @Override
    public MailConfig getMailConfig(Session session, int accountId) throws OXException {
        return MailAccess.getInstance(session, accountId).getMailConfig();
    }

    @Override
    public TransportConfig getTransportConfig(Session session, int accountId) throws OXException {
        return MailTransport.getInstance(session, accountId).getTransportConfig();
    }

    @Override
    public String getMailLoginFor(int userId, int contextId, int accountId) throws OXException {
        // Get the user
        User user = UserStorage.getInstance().getUser(userId, contextId);

        // Get the mail account
        MailAccountStorageService service = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(MailAccountStorageService.class);
        }
        MailAccount mailAccount = service.getMailAccount(accountId, userId, contextId);

        // Return login
        return MailConfig.getMailLogin(mailAccount, user.getLoginInfo(), userId, contextId);
    }

}
