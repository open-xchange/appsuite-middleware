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

package com.openexchange.utils.propertyhandling.internal;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link ConfigurationExceptionMessages}
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public class ConfigurationExceptionMessages implements LocalizableStrings {

    // The value given in the property %1$s is no integer value
    public static final String NO_INTEGER_VALUE_MSG = "The value given in the property %1$s is no integer value.";

    // Property %1$s not set but required.
    public static final String REQUIRED_PROPERTIY_NOT_SET_MSG = "Property %1$s not set but required.";

    // Property %1$s claims to have condition but condition not set.
    public static final String CONDITION_NOT_SET_MSG = "Property %1$s claims to have condition but condition not set.";

    // Property %1$s must be set if %2$s is set to %3$s
    public static final String MUST_BE_SET_TO_MSG = "Property %1$s must be set if %2$s is set to %3$s";

    // The class %1$s cannot be used as a property type at the moment
    public static final String UNKNOWN_TYPE_CLASS_MSG = "The %1$s cannot be used as a property type in the property %2$s";
}
