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

package com.openexchange.unifiedinbox.config;

import com.openexchange.java.Strings;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailProperties;

/**
 * {@link UnifiedInboxProperties}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxProperties extends AbstractProtocolProperties implements IUnifiedInboxProperties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UnifiedInboxProperties.class);

    private static final UnifiedInboxProperties instance = new UnifiedInboxProperties();

    /**
     * Gets the singleton instance of {@link UnifiedInboxProperties}
     *
     * @return The singleton instance of {@link UnifiedInboxProperties}
     */
    public static UnifiedInboxProperties getInstance() {
        return instance;
    }

    private final IMailProperties mailProperties;

    /**
     * Initializes a new {@link UnifiedInboxProperties}
     */
    private UnifiedInboxProperties() {
        super();
        mailProperties = MailProperties.getInstance();
    }

    @Override
    protected void loadProperties0() throws MailConfigException {
        LOG.info("{}Loading global Unified Mail properties...{}Global Unified Mail properties successfully loaded", Strings.getLineSeparator(), Strings.getLineSeparator());
    }

    @Override
    protected void resetFields() {
        // Nothing to do
    }

    @Override
    public int getMailFetchLimit() {
        return mailProperties.getMailFetchLimit();
    }

    @Override
    public boolean hideInlineImages() {
        return mailProperties.hideInlineImages();
    }

    @Override
    public boolean isAllowNestedDefaultFolderOnAltNamespace() {
        return mailProperties.isAllowNestedDefaultFolderOnAltNamespace();
    }

    @Override
    public boolean isIgnoreSubscription() {
        return mailProperties.isIgnoreSubscription();
    }

    @Override
    public boolean isSupportSubscription() {
        return mailProperties.isSupportSubscription();
    }

    @Override
    public boolean isUserFlagsEnabled() {
        return mailProperties.isUserFlagsEnabled();
    }

}
