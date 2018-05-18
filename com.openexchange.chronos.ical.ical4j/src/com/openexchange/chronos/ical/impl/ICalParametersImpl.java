/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.ical.impl;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.osgi.Services;
import com.openexchange.config.ConfigurationService;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryImpl;

/**
 * {@link ICalParametersImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICalParametersImpl implements ICalParameters {

    /**
     * {@link TimeZoneRegistry}
     * <p/>
     * Holds a reference to the underlying timezone registry.
     */
    public static final String TIMEZONE_REGISTRY = TimeZoneRegistry.class.getName();

    /** The default timezone registry to use */
    private static final TimeZoneRegistry DEFAULT_TIMEZONE_REGISTRY = new TimeZoneRegistryImpl("zoneinfo-outlook/");

    private final Map<String, Object> parameters;

    /**
     * Initializes a new, empty {@link ICalParametersImpl}.
     */
    public ICalParametersImpl() {
        super();
        this.parameters = new HashMap<String, Object>();
        applyDefaults();
    }

    private void applyDefaults() {
        set(TIMEZONE_REGISTRY, DEFAULT_TIMEZONE_REGISTRY);
        ConfigurationService configService = Services.optService(ConfigurationService.class);
        if (null != configService) {
            set(IMPORT_LIMIT, configService.getIntProperty("com.openexchange.import.ical.limit", -1));
        }
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz) {
        return get(parameter, clazz, null);
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz, T defaultValue) {
        Object value = parameters.get(parameter);
        return null == value ? defaultValue : clazz.cast(value);
    }

    @Override
    public <T> ICalParameters set(String name, T value) {
        if (null != name) {
            parameters.put(name, value);
        } else {
            parameters.remove(name);
        }
        return this;
    }

}
