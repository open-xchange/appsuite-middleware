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

    private final RssProperties rssProperties;

    /**
     * Initializes a new {@link ConfigurationCheckingAccountManager}.
     *
     * @param service The {@link MessagingService}
     */
    public ConfigurationCheckingAccountManager(MessagingService service, RssProperties rssProperties) {
        super(service);
        this.rssProperties = rssProperties;
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
     * Checks whether the account has a valid account URL or not.
     *
     * @param account The {@link MessagingAccount}
     * @throws OXException in case the account is invalid
     */
    private void checkAccount(MessagingAccount account) throws OXException {
        Object urlObj = account.getConfiguration().get(FormStrings.FORM_LABEL_URL);
        if (urlObj == null || rssProperties.isDenied(urlObj.toString())) {
            throw MessagingExceptionCodes.INVALID_ACCOUNT_CONFIGURATION.create("Invalid account url");
        }
    }

}
