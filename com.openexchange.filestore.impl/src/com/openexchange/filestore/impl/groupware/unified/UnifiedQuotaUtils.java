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

package com.openexchange.filestore.impl.groupware.unified;

import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.impl.osgi.Services;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link UnifiedQuotaUtils} - Utility class for Unified Quota.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class UnifiedQuotaUtils {

    /**
     * Initializes a new {@link UnifiedQuotaUtils}.
     */
    private UnifiedQuotaUtils() {
        super();
    }

    /**
     * Checks if Unified Quota is enabled for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public static boolean isUnifiedQuotaEnabledFor(int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = Services.optService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        ComposedConfigProperty<String> property = view.property("com.openexchange.unifiedquota.enabled", String.class);
        if (null == property || !property.isDefined()) {
            // Not enabled by default
            return false;
        }

        String value = property.get();
        boolean defaultValue = false;
        return Strings.isEmpty(value) ? defaultValue : ("true".equalsIgnoreCase(value.trim()) ? true : ("false".equalsIgnoreCase(value.trim()) ? false : defaultValue));
    }

}
