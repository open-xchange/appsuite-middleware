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

package com.openexchange.tools.validate;

import org.json.JSONObject;
import com.openexchange.exception.OXException;

/**
 * {@link ParameterValidator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class ParameterValidator {

    public static void checkJSON(JSONObject object) throws OXException {
        if (object == null) {
            throw ParameterValidatorExceptionCodes.JSON_OBJECT_CANNOT_BE_NULL_ERROR.create();
        } else if (object.length() == 0) {
            throw ParameterValidatorExceptionCodes.JSON_OBJECT_CANNOT_BE_EMPTY_ERROR.create();
        }
    }

    public static void checkString(String object) throws OXException {
        if (object == null) {
            throw ParameterValidatorExceptionCodes.STRING_OBJECT_CANNOT_BE_NULL_ERROR.create();
        } else if (object.length() == 0) {
            throw ParameterValidatorExceptionCodes.STRING_OBJECT_CANNOT_BE_EMPTY_ERROR.create();
        }
    }

    public static void checkObject(Object object) throws OXException {
        if (object == null) {
            throw ParameterValidatorExceptionCodes.OBJECT_CANNOT_BE_NULL_ERROR.create();
        }
    }
}
