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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail;

import java.util.Date;
import com.openexchange.authentication.LoginException;
import com.openexchange.dataretention.DataRetentionException;
import com.openexchange.dataretention.DataRetentionService;
import com.openexchange.dataretention.RetentionData;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.login.Login;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.cache.MailAccessCache;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link MailLoginHandler} - The login handler delivering mailbox access event to data retention.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailLoginHandler implements LoginHandlerService {

    /**
     * Initializes a new {@link MailLoginHandler}.
     */
    public MailLoginHandler() {
        super();
    }

    public void handleLogin(final Login login) throws LoginException {
        /*
         * Email successfully sent, trigger data retention
         */
        final DataRetentionService retentionService = ServerServiceRegistry.getInstance().getService(DataRetentionService.class);
        try {
            final Session session = login.getSession();
            if (null != retentionService && UserConfigurationStorage.getInstance().getUserConfiguration(
                session.getUserId(),
                login.getContext()).hasWebMail()) {
                final RetentionData retentionData = retentionService.newInstance();
                retentionData.setStartTime(new Date(System.currentTimeMillis()));
                retentionData.setIdentifier(MailAccess.getInstance(session).getMailConfig().getLogin());
                retentionData.setIPAddress(session.getLocalIp());
                retentionData.setLogin(session.getLogin());
                /*
                 * Finally store it
                 */
                retentionService.storeOnAccess(retentionData);
            }
        } catch (final UserConfigurationException e) {
            throw new LoginException(e);
        } catch (final MailException e) {
            throw new LoginException(e);
        } catch (final DataRetentionException e) {
            throw new LoginException(e);
        }
    }

    public void handleLogout(final Login logout) throws LoginException {
        // Time-out mail access cache
        try {
            MailAccessCache.getInstance().clear();
        } catch (final MailException e) {
            throw new LoginException(e);
        }
    }
}
