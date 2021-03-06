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

package com.openexchange.chronos.scheduling.changes.impl;

import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.regional.RegionalSettings;

/**
 * {@link MessageContext}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.3
 */
public interface MessageContext {

    /**
     * Gets the underlying wrapper.
     * 
     * @return The type wrapper
     */
    TypeWrapper getWrapper();

    /**
     * Gets the target locale to use.
     * 
     * @return The target locale
     */
    Locale getLocale();

    /**
     * Gets the timezone to consider when formatting date-/time-related properties.
     * 
     * @return The timezone
     */
    TimeZone getTimeZone();

    /**
     * Gets customized regional settings to use for the recipient, if configured.
     * 
     * @return The preferred regional settings, or <code>null</code> if not configured
     */
    RegionalSettings getRegionalSettings();

}
