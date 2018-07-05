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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.schedjoules.impl;

import java.util.concurrent.TimeUnit;
import com.openexchange.config.lean.Property;

/**
 * {@link SchedJoulesProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum SchedJoulesProperty implements Property {
    /**
     * The API key that gives access to the complete API of SchedJoules.
     */
    apiKey,
    /**
     * The refresh interval of subscriptions in milliseconds.
     * Defaults to 86400000 (1 day)
     */
    refreshInterval(TimeUnit.DAYS.toMillis(1)),
    /**
     * The host to access schedjoules.
     * Defaults to 'api.schedjoules.com'<br>
     * <b>Note:</b> this property should explicitly not be published as the endpoint is fix
     */
    host("api.schedjoules.com"),
    /**
     * The scheme used to contact the SchedJoules API.
     * Defaults to 'https'<br>
     * <b>Note:</b> this property should explicitly not be published as the endpoint is fix
     */
    scheme("https"),
    /**
     * Defines a comma separated blacklist for itemIds of SchedJoules calendars and pages that should be hidden from
     * the end user.
     * Default: empty
     */
    itemBlacklist;

    private final String fqn;
    private final Object defaultValue;
    private static final String PREFIX = "com.openexchange.calendar.schedjoules.";

    /**
     * Initialises a new {@link UserFeedbackMailProperty}.
     *
     * @param defaultValue The default value of the property
     * @param optional Whether the property is optional
     */
    private SchedJoulesProperty() {
        this("", PREFIX);
    }

    /**
     * Initialises a new {@link UserFeedbackMailProperty}.
     *
     * @param defaultValue The default value of the property
     * @param optional Whether the property is optional
     */
    private SchedJoulesProperty(Object defaultValue) {
        this(defaultValue, PREFIX);
    }

    /**
     * Initialises a new {@link UserFeedbackMailProperty}.
     *
     * @param defaultValue The default value of the property
     * @param optional Whether the property is optional
     */
    private SchedJoulesProperty(Object defaultValue, String fqn) {
        this.defaultValue = defaultValue;
        this.fqn = fqn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.lean.Property#getFQPropertyName()
     */
    @Override
    public String getFQPropertyName() {
        return fqn + name();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.lean.Property#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
