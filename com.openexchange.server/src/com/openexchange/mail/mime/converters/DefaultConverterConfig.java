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

package com.openexchange.mail.mime.converters;

import com.openexchange.mail.api.MailConfig;

/**
 * {@link DefaultConverterConfig} Default implementation of {@link ConverterConfig}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class DefaultConverterConfig implements ConverterConfig {

    private final boolean considerFolder;
    private final MailConfig mailConfig;
    private final boolean includeBody;

    /**
     * Initializes a new {@link DefaultConverterConfig}.
     *
     * @param mailConfig The mail config
     */
    public DefaultConverterConfig(MailConfig mailConfig) {
        this(mailConfig, true, false);
    }

    /**
     * Initializes a new {@link DefaultConverterConfig}.
     *
     * @param mailConfig The mail config
     * @param considerFolder Whether the folder is supposed to be considered during conversion
     * @param includeBody Whether the body should be included during conversion
     */
    public DefaultConverterConfig(MailConfig mailConfig, boolean considerFolder, boolean includeBody) {
        super();
        this.considerFolder = considerFolder;
        this.mailConfig = mailConfig;
        this.includeBody = includeBody;
    }

    @Override
    public boolean isConsiderFolder() {
        return considerFolder;
    }

    @Override
    public MailConfig getMailConfig() {
        return mailConfig;
    }

    @Override
    public boolean isIncludeBody() {
        return includeBody;
    }
}
