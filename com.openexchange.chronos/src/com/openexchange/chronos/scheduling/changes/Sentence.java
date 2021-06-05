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

package com.openexchange.chronos.scheduling.changes;

import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.regional.RegionalSettings;

/**
 * {@link Sentence}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public interface Sentence {

    /**
     * Get the message in a specific format
     *
     * @param format The format. For <code>null</code>, format <code>text</code> is assumed
     * @param locale The local to translate the sentence in
     * @param timeZone The time zone to use the sentence
     * @param regionalSettings The preferred regional settings, or <code>null</code> if not configured
     * @return The message in a specific format, localized
     */
    String getMessage(String format, Locale locale, TimeZone timeZone, RegionalSettings regionalSettings);

}
