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

package com.openexchange.advertisement;

import com.openexchange.advertisement.AdvertisementConfigService.ConfigResultType;
import com.openexchange.exception.OXException;

/**
 * {@link ConfigResult} wraps the result of a configuration attempt
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class ConfigResult {

    private final OXException exception;
    private final String message;
    private ConfigResultType type;

    public ConfigResult(ConfigResultType type, OXException exception) {
        this.message = type.name();
        this.exception = exception;
        this.type = type;
    }

    /**
     * Checks if the result contains an error
     * 
     * @return true if the result contains an error, false otherwise
     */
    public boolean hasError() {
        return this.exception != null;
    }

    /**
     * Returns the message
     * 
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the {@link OXException} or null
     * 
     * @return
     */
    public OXException getError() {
        return exception;
    }

    /**
     * Return the type of the result.
     * 
     * @return the {@link ConfigResultType}
     */
    public ConfigResultType getConfigResultType() {
        return type;
    }

}
