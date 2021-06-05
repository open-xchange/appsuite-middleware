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

package com.openexchange.mail.compose.impl.storage;

import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.impl.NonCryptoCompositionSpaceStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AbstractCompositionSpaceStorageService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public abstract class AbstractCompositionSpaceStorageService implements NonCryptoCompositionSpaceStorageService {

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractCompositionSpaceStorageService}.
     */
    protected AbstractCompositionSpaceStorageService(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the max. number of allowed concurrent composition spaces.
     *
     * @param session The session
     * @return The max. number of allowed composition spaces
     * @throws OXException If number cannot be returned
     */
    protected int getMaxSpacesPerUser(Session session) throws OXException {
        int defaultValue = 20;

        ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (null == viewFactory) {
            return defaultValue;
        }

        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        return ConfigViews.getDefinedIntPropertyFrom("com.openexchange.mail.compose.maxSpacesPerUser", defaultValue, view);
    }

}
