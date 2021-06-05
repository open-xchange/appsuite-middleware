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
import com.openexchange.chronos.scheduling.RecipientSettings;
import com.openexchange.regional.RegionalSettings;

/**
 * {@link DefaultMessageContext}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.3
 */
public class DefaultMessageContext implements MessageContext {

    private final TypeWrapper wrapper;
    private final Locale locale;
    private final TimeZone timeZone;
    private final RegionalSettings regionalSettings;

    /**
     * Initializes a new {@link DefaultMessageContext}.
     * 
     * @param wrapper The underlying type wrapper
     * @param recipientSettings Recipient-specific settings to use for rendering
     */
    public DefaultMessageContext(TypeWrapper wrapper, RecipientSettings recipientSettings) {
        this(wrapper, recipientSettings.getLocale(), recipientSettings.getTimeZone(), recipientSettings.getRegionalSettings());
    }

    /**
     * Initializes a new {@link DefaultMessageContext}.
     * 
     * @param wrapper The underlying type wrapper
     * @param locale The target locale to use
     * @param timeZone The timezone to consider when formatting date-/time-related properties.
     * @param regionalSettings The preferred regional settings, or <code>null</code> if not configured
     */
    public DefaultMessageContext(TypeWrapper wrapper, Locale locale, TimeZone timeZone, RegionalSettings regionalSettings) {
        super();
        this.wrapper = wrapper;
        this.locale = locale;
        this.timeZone = timeZone;
        this.regionalSettings = regionalSettings;
    }

    @Override
    public TypeWrapper getWrapper() {
        return wrapper;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public RegionalSettings getRegionalSettings() {
        return regionalSettings;
    }

}
