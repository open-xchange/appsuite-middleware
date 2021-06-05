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

package com.openexchange.imap.protection.impl;

import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.imap.protection.IMAPSelfProtection;
import com.openexchange.imap.services.Services;
import com.openexchange.server.ServiceExceptionCode;


/**
 * {@link ConfigUsingIMAPSelfProtection} - The IMAP self-protection fetching values from configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ConfigUsingIMAPSelfProtection implements IMAPSelfProtection {

    private final int maxNumberOfMessages;

    /**
     * Initializes a new {@link ConfigUsingIMAPSelfProtection}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If initialization fails
     */
    public ConfigUsingIMAPSelfProtection(int userId, int contextId) throws OXException {
        super();

        ConfigViewFactory viewFactory = Services.optService(ConfigViewFactory.class);
        if (viewFactory == null) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        maxNumberOfMessages = ConfigViews.getDefinedIntPropertyFrom("com.openexchange.imap.protection.maxNumberOfMessages", 100000, view);
    }

    @Override
    public int getMaxNumberOfMessages() {
        return maxNumberOfMessages;
    }

}
