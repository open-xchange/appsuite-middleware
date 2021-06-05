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

package com.openexchange.userfeedback.internal;

import static com.openexchange.java.Autoboxing.B;
import com.openexchange.config.lean.Property;

/**
 * {@link UserFeedbackProperty}
 *
 * @author <a href="vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.4
 */
public enum UserFeedbackProperty implements Property{
    enabled(B(true), UserFeedbackProperty.PREFIX),
    mode("star-rating-v1", UserFeedbackProperty.PREFIX);

    private static final String EMPTY = "";
    private static final String PREFIX = "com.openexchange.userfeedback.";
    private final String fqn;
    private final Object defaultValue;

    /**
     * Initialises a new {@link UserFeedbackProperty}.
     */
    private UserFeedbackProperty() {
        this(EMPTY);
    }

    /**
     * Initialises a new {@link UserFeedbackProperty}.
     * 
     * @param defaultValue The default value of the property
     */
    private UserFeedbackProperty(Object defaultValue) {
        this(defaultValue, PREFIX);
    }

    /**
     * Initialises a new {@link UserFeedbackProperty}.
     * 
     * @param defaultValue The default value of the property
     * @param optional Whether the property is optional
     */
    private UserFeedbackProperty(Object defaultValue, String fqn) {
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
