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

package com.openexchange.mail.json;

import java.io.Closeable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailServletInterface;

/**
 * {@link MailActionConstants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MailActionConstants {

    /**
     * The property name for {@link MailServletInterface} instance.
     */
    public static final String PROPERTY_MAIL_IFACE = "com.openexchange.mail.json.mailInterface";

    /**
     * The property name for the collection of {@link Closeable} instances.
     */
    public static final String PROPERTY_CLOSEABLES = "com.openexchange.mail.json.closeables";

    /**
     * A set containing all log property names.
     */
    public static final Set<LogProperties.Name> ALL_LOG_PROPERTIES = Collections.unmodifiableSet(EnumSet.of(
        LogProperties.Name.MAIL_ACCOUNT_ID,
        LogProperties.Name.MAIL_FULL_NAME,
        LogProperties.Name.MAIL_HOST,
        LogProperties.Name.MAIL_MAIL_ID,
        LogProperties.Name.MAIL_LOGIN));

}
