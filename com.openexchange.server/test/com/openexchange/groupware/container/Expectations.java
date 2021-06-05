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

package com.openexchange.groupware.container;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;


/**
 * A set of keys that are to be checked and a set of expected values for these keys.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class Expectations extends KeyValueHolder {

    public Expectations(Changes changes){
        setMap(changes.getMap());
    }

    public Expectations(){
        setMap(new HashMap<Integer, Object>());
    }

    public void verify(CommonObject actual){
        verify("", actual);
    }

    public void verify(String message, CommonObject actual){
        int successfulTests = 0;
        for(Integer key: getMap().keySet()){
            Object expectedValue = getMap().get(key);
            Object actualValue = actual.get(key.intValue());
            if (actual.contains(key.intValue())) {
                assertEquals(message + " Field "+key+" does not match expectation ("+successfulTests+" successful tests before this)", expectedValue, actualValue);
            } else {
                assertEquals(message + " Field "+key+" does not match expectation ("+successfulTests+" successful tests before this)", expectedValue, null);
            }
            successfulTests++;
        }
    }
}
