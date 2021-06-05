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

package com.openexchange.mail.config;

import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailAccountProperties} - Mail properties read from mail account with fallback to properties read from properties file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountProperties extends AbstractMailAccountProperties implements IMailProperties {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailAccountProperties.class);

    /**
     * Initializes a new {@link MailAccountProperties}.
     *
     * @param mailAccount The mail account
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public MailAccountProperties(MailAccount mailAccount, int userId, int contextId) {
        super(mailAccount, userId, contextId);
    }

    @Override
    public int getMailFetchLimit() {
        return lookUpIntProperty("com.openexchange.mail.mailFetchLimit", MailProperties.getInstance().getMailFetchLimit());
    }

    @Override
    public boolean hideInlineImages() {
        return lookUpBoolProperty("com.openexchange.mail.hideInlineImages", MailProperties.getInstance().hideInlineImages());
    }

    @Override
    public boolean isAllowNestedDefaultFolderOnAltNamespace() {
        return lookUpBoolProperty("com.openexchange.mail.allowNestedDefaultFolderOnAltNamespace", MailProperties.getInstance().isAllowNestedDefaultFolderOnAltNamespace());
    }

    @Override
    public boolean isIgnoreSubscription() {
        return lookUpBoolProperty("com.openexchange.mail.ignoreSubscription", MailProperties.getInstance().isIgnoreSubscription());
    }

    @Override
    public boolean isSupportSubscription() {
        return lookUpBoolProperty("com.openexchange.mail.supportSubscription", MailProperties.getInstance().isSupportSubscription());
    }

    @Override
    public boolean isUserFlagsEnabled() {
        return lookUpBoolProperty("com.openexchange.mail.userFlagsEnabled", MailProperties.getInstance().isUserFlagsEnabled());
    }

    @Override
    public void waitForLoading() throws InterruptedException {
        MailProperties.getInstance().waitForLoading();
    }

}
