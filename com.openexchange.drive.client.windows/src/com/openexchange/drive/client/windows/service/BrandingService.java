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

package com.openexchange.drive.client.windows.service;

import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.drive.client.windows.service.internal.Services;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link BrandingService} provides access to the branding configuration
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class BrandingService {

    /**
     * Retrieves the branding for the given session.
     * 
     * @param session
     * @return The branding name
     * @throws OXException if the branding couldn't be retrieved
     */
    public static String getBranding(Session session) throws OXException {
        return getBranding(session.getUserId(), session.getContextId());
    }

    /**
     * Retrieves the branding for the given user.
     * 
     * @param userId
     * @param contextId
     * @return The branding name
     * @throws OXException if the branding couldn't be retrieved
     */
    public static String getBranding(int userId, int contextId) throws OXException {
        ConfigViewFactory configFactory = Services.getService(ConfigViewFactory.class);
        ConfigView configView = configFactory.getView(userId, contextId);
        return configView.get(Constants.BRANDING_CONF, String.class);
    }

}
