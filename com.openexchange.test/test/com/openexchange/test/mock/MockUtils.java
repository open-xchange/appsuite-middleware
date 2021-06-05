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

package com.openexchange.test.mock;

import org.powermock.reflect.Whitebox;

/**
 * This class offers some nice options to change behavior in the classes to test, for verification or other helpful stuff.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class MockUtils {

    /**
     * Inject a value you would like to have for your test case into the field name of the provided object
     * 
     * @param objectToModify - the object the value should be changed for
     * @param fieldNameInClass - the name of the field that should be changed
     * @param newValue - the new value for the field of the object
     */
    public static void injectValueIntoPrivateField(Object objectToModify, String fieldNameInClass, Object newValue) {
        Whitebox.setInternalState(objectToModify, fieldNameInClass, newValue);
    }

    /**
     * Get the value of a private field e. g. if it is not visible and does not have a getter.
     * 
     * @param objectToGetState - the object you would like to get the value from
     * @param fieldNameInClass - the name of the field you would like to get the value for
     * @return Object with the value of the field from the given object
     */
    public static Object getValueFromField(Object objectToGetState, String fieldNameInClass) {
        return Whitebox.getInternalState(objectToGetState, fieldNameInClass);
    }

}
