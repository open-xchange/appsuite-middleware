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

package com.openexchange.multifactor;

import com.openexchange.config.lean.Property;

/**
 * {@link MultifactorProperties}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public enum MultifactorProperties implements Property {

    /**
     * WARNING: This puts the multifactor framework into demo mode.
     * This is for testing only!
     * DO NOT SET TO TRUE IN A PRODUCTIVE ENVIRONMENT!
     */
    demo(false),

    /**
     * List of urls that require a recent multifactor authentication. This means that the use must have
     * authenticated within the recentAuthenticationTime.
     */
    recentAuthRequired("multifactor/device?action=delete, multifactor/device?action=startRegistration"),

    /**
     * The time, in minutes, that a multifactor authentication is considered "recent".
     *
     * Some actions (defined in {@link MultifactorProperties#recentAuthRequired}) require that the client performed
     * multifactor authentication recently. If the multifactor authentication happened prior the configured amount of
     * minutes, the requests defined in recentAuthRequired will be denied.
     */
    recentAuthenticationTime(10);

    public static final String PREFIX = "com.openexchange.multifactor.";
    private Object             defaultValue;

    MultifactorProperties(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.config.lean.Property#getFQPropertyName()
     */
    @Override
    public String getFQPropertyName() {
        return PREFIX + name();
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
