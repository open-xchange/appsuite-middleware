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

package com.openexchange.messaging.rss;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.generic.DefaultMessagingAccountManager;
import com.openexchange.rss.utils.RssProperties;
import com.openexchange.session.Session;

/**
 * {@link ConfigurationCheckingAccountManager} is a {@link DefaultMessagingAccountManager} implementation which checks that the rss account configuration is valid.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class ConfigurationCheckingAccountManager extends DefaultMessagingAccountManager {

    /**
     * Initializes a new {@link ConfigurationCheckingAccountManager}.
     *
     * @param service The {@link MessagingService}
     */
    public ConfigurationCheckingAccountManager(MessagingService service) {
        super(service);
    }

    @Override
    public void updateAccount(MessagingAccount account, Session session) throws OXException {
        checkAccount(account);
        super.updateAccount(account, session);
    }

    @Override
    public int addAccount(MessagingAccount account, Session session) throws OXException {
        checkAccount(account);
        return super.addAccount(account, session);
    }

    /**
     * Checks whether the account has a valid account url or not.
     *
     * @param account The {@link MessagingAccount}
     * @throws OXException in case the account is invalid
     */
    private void checkAccount(MessagingAccount account) throws OXException {
        Object urlObj = account.getConfiguration().get(FormStrings.FORM_LABEL_URL);
        if (urlObj == null || RssProperties.isDenied(urlObj.toString())) {
            throw MessagingExceptionCodes.INVALID_ACCOUNT_CONFIGURATION.create("Invalid account url");
        }
    }

}
