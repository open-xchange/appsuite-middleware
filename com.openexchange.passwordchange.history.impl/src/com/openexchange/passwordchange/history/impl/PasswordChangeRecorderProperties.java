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

package com.openexchange.passwordchange.history.impl;

import com.openexchange.config.lean.Property;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;

/**
 * {@link PasswordChangeRecorderProperties}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public enum PasswordChangeRecorderProperties implements Property {

    /**
     * The property that indicates if a password change should be recorded into a history.
     * <p>
     * If set to <code>true</code> a history is saved.<br>
     * If set to <code>false</code> no history is saved.
     * <p>
     * Default is <code>false</code>.
     */
    ENABLE("enabled", Boolean.FALSE),

    /**
     * The recorder that takes care of the password change history.<br>
     * It has to be registered as {@link PasswordChangeRecorder} before usage.
     * <p>
     * Default is <code>"default"</code> for the shipped version.
     */
    RECORDER("recorder", "default"),

    /**
     * The number of entries to be saved within the {@link PasswordChangeRecorder} for a certain user.
     * <p>
     * Default is <code>10</code>.
     */
    LIMIT("limit", Integer.valueOf(10))

    ;

    private final String fqn;
    private final Object defaultValue;

    private PasswordChangeRecorderProperties(String name, Object defaultValue) {
        this.defaultValue = defaultValue;
        this.fqn = "com.openexchange.passwordchange.history." + name;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public <T> T getDefaultValue(Class<T> clazz) {
        if (defaultValue.getClass().isAssignableFrom(clazz)) {
            return clazz.cast(defaultValue);
        }
        throw new IllegalArgumentException("The object cannot be converted to the specified type '" + clazz.getCanonicalName() + "'");
    }
}
