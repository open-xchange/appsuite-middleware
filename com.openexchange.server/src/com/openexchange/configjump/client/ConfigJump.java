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

package com.openexchange.configjump.client;

import java.net.URL;
import com.openexchange.configjump.ConfigJumpExceptionCode;
import com.openexchange.configjump.ConfigJumpService;
import com.openexchange.configjump.Replacements;
import com.openexchange.exception.OXException;

/**
 * Provides a static method for the servlet to generate the ConfigJump URL based on the service.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ConfigJump {

    private static final ConfigJumpHolder holder = new ConfigJumpHolder();

    /**
     * Prevent instantiation.
     */
    private ConfigJump() {
        super();
    }

    public static URL getLink(final Replacements replacements) throws OXException {
        final ConfigJumpService service = holder.getService();
        if (null == service) {
            throw ConfigJumpExceptionCode.NOT_IMPLEMENTED.create();
        }
        try {
            return service.getLink(replacements);
        } finally {
            holder.ungetService(service);
        }
    }

    public static ConfigJumpHolder getHolder() {
        return holder;
    }
}
