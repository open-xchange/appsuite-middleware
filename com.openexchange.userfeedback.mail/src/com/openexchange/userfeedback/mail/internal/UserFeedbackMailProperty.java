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

package com.openexchange.userfeedback.mail.internal;

import com.openexchange.config.lean.Property;

/**
 * {@link UserFeedbackMailProperty}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum UserFeedbackMailProperty implements Property {
    senderName(UserFeedbackMailProperty.EMPTY, UserFeedbackMailProperty.PREFIX + "mail."),
    senderAddress(UserFeedbackMailProperty.EMPTY, UserFeedbackMailProperty.PREFIX + "mail."),
    exportPrefix(UserFeedbackMailProperty.EXPORT_PREFIX, UserFeedbackMailProperty.PREFIX + "mail."),
    hostname(UserFeedbackMailProperty.EMPTY, UserFeedbackMailProperty.PREFIX + "smtp."),
    port(587, UserFeedbackMailProperty.PREFIX + "smtp."),
    timeout(50000, UserFeedbackMailProperty.PREFIX + "smtp."),
    connectionTimeout(10000, UserFeedbackMailProperty.PREFIX + "smtp."),
    username(UserFeedbackMailProperty.EMPTY, UserFeedbackMailProperty.PREFIX + "smtp."),
    password(UserFeedbackMailProperty.EMPTY, UserFeedbackMailProperty.PREFIX + "smtp."),
    signKeyFile(UserFeedbackMailProperty.EMPTY, UserFeedbackMailProperty.PREFIX + "pgp."),
    signKeyPassword(UserFeedbackMailProperty.EMPTY, UserFeedbackMailProperty.PREFIX + "pgp."),
    ;

    private static final String EMPTY = "";
    private static final String PREFIX = "com.openexchange.userfeedback.";
    private static final String EXPORT_PREFIX = "OX_App_Suite_Feedback_Report";
    private final String fqn;
    private final Object defaultValue;

    /**
     * Initialises a new {@link UserFeedbackMailProperty}.
     */
    private UserFeedbackMailProperty() {
        this(EMPTY);
    }

    /**
     * Initialises a new {@link UserFeedbackMailProperty}.
     *
     * @param defaultValue The default value of the property
     */
    private UserFeedbackMailProperty(Object defaultValue) {
        this(defaultValue, PREFIX);
    }

    /**
     * Initialises a new {@link UserFeedbackMailProperty}.
     *
     * @param defaultValue The default value of the property
     * @param optional Whether the property is optional
     */
    private UserFeedbackMailProperty(Object defaultValue, String fqn) {
        this.defaultValue = defaultValue;
        this.fqn = fqn;
    }

    /**
     * Returns the fully qualified name of the property
     *
     * @return the fully qualified name of the property
     */
    @Override
    public String getFQPropertyName() {
        return fqn + name();
    }

    /**
     * Returns the default value of this property
     *
     * @return the default value of this property
     */
    @Override
    public <T extends Object> T getDefaultValue(Class<T> cls) {
        if (defaultValue.getClass().isAssignableFrom(cls)) {
            return cls.cast(defaultValue);
        }
        throw new IllegalArgumentException("The object cannot be converted to the specified type '" + cls.getCanonicalName() + "'");
    }

}
