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

import javax.mail.internet.MimeMessage;
import com.openexchange.mail.api.MailConfig;

/**
 * {@link ConverterConfig} that contains setting considered while converting {@link MimeMessage}s.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public interface ConverterConfig {

    /**
     * Checks the {@link MailConfig} for the user
     *
     * @return The {@link MailConfig}
     */
    MailConfig getMailConfig();

    /**
     * Checks if the folder should be considered while adding data to the mail
     *
     * @return <code>true</code> if the folder should be considered; otherwise <code>false</code>
     */
    boolean isConsiderFolder();

    /**
     * Checks if the body should be included while adding data to the mail
     *
     * @return <code>true</code> if the body should be included; otherwise <code>false</code>
     */
    boolean isIncludeBody();
}
