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

package com.openexchange.userfeedback.mail.internal;

import static com.openexchange.java.Autoboxing.I;
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
    port(I(587), UserFeedbackMailProperty.PREFIX + "smtp."),
    timeout(I(50000), UserFeedbackMailProperty.PREFIX + "smtp."),
    connectionTimeout(I(10000), UserFeedbackMailProperty.PREFIX + "smtp."),
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
    public Object getDefaultValue() {
        return defaultValue;
    }

}
