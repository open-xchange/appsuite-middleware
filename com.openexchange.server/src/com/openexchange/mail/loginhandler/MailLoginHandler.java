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

package com.openexchange.mail.loginhandler;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.dataretention.DataRetentionService;
import com.openexchange.dataretention.RetentionData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link MailLoginHandler} - The login handler delivering mailbox access event
 * to data retention.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailLoginHandler implements LoginHandlerService {

    private static final Logger LOG = LoggerFactory.getLogger(MailLoginHandler.class);

	/**
	 * Initializes a new {@link MailLoginHandler}.
	 */
	public MailLoginHandler() {
		super();
	}

	@Override
    public void handleLogin(LoginResult login) throws OXException {
        /*
         * Track mail login in data retention service
         */
        final DataRetentionService retentionService = ServerServiceRegistry.getInstance().getService(DataRetentionService.class);
        final Context ctx = login.getContext();
        final Session session = login.getSession();
        if (null != retentionService && UserConfigurationStorage.getInstance().getUserConfiguration(session.getUserId(), ctx).hasWebMail()) {
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                RetentionData retentionData = retentionService.newInstance();
                retentionData.setStartTime(new Date(System.currentTimeMillis()));
                mailAccess = MailAccess.getInstance(session);
                retentionData.setIdentifier(mailAccess.getMailConfig().getLogin());
                retentionData.setIPAddress(session.getLocalIp());
                retentionData.setLogin(session.getLogin());
                /*
                 * Finally store it
                 */
                retentionService.storeOnAccess(retentionData);
            } catch (OXException e) {
                // Skipp mail login in case the mail is not accessible from oauth. E.g. because master auth is not enabled
                if (MailExceptionCode.MISSING_CONNECT_PARAM.equals(e) && session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN) != null && e.getArgument(MailConfig.MISSING_SESSION_PASSWORD) != null) {
                    LOG.debug("Mail access with oauth impossible. Skipping mail login.");
                    return;
                }
                throw e;
            } finally {
                MailAccess.closeInstance(mailAccess);
            }
        }
    }

	@Override
    public void handleLogout(LoginResult logout) throws OXException {
	    // Nothing
	}
}
