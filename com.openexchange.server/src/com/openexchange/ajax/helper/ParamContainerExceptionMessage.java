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

package com.openexchange.ajax.helper;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ParamContainerExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class ParamContainerExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link ParamContainerExceptionMessage}.
     */
    private ParamContainerExceptionMessage() {
        super();
    }

    /**
     * The value you inserted for %2$s is not correct
     */
    public final static String BAD_PARAM_VALUE_MSG_DISPLAY = "The value inserted for %2$s is not correct.";

    /**
     * Please add a value for parameter %1$s
     */
    public final static String MISSING_PARAMETER_MSG_DISPLAY = "Please add a value for %1$s";

}
