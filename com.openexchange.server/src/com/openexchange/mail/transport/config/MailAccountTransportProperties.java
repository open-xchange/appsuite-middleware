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

package com.openexchange.mail.transport.config;

import com.openexchange.mail.config.AbstractMailAccountProperties;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailAccountTransportProperties} - Transport properties read from mail account with fallback to properties read from properties
 * file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountTransportProperties extends AbstractMailAccountProperties implements ITransportProperties {

    /**
     * Initializes a new {@link MailAccountTransportProperties}.
     *
     * @param mailAccount The mail account providing the properties
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountTransportProperties(MailAccount mailAccount, int userId, int contextId) {
        super(mailAccount, userId, contextId);
        if (null == mailAccount) {
            throw new IllegalArgumentException("mail account is null.");
        }
    }

    /**
     * Initializes a new {@link MailAccountTransportProperties} with empty properties.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    protected MailAccountTransportProperties(int userId, int contextId) {
        super(null, userId, contextId);
    }

    @Override
    public int getReferencedPartLimit() {
        return lookUpIntProperty("com.openexchange.mail.transport.referencedPartLimit", TransportProperties.getInstance().getReferencedPartLimit());
    }

}
