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

package com.openexchange.groupware.contact;


/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public enum ContactUnificationState {
    RED(1),        //user decided two contacts are not refering to the same person
    GREEN(2),      //user stated that two contacts represent the same person
    UNDEFINED(0);

    private int num;

    ContactUnificationState(int num){
        this.num = num;
    }

    public int getNumber(){
        return num;
    }

    public static ContactUnificationState getByNumber(int num){
        for(ContactUnificationState state: values()) {
            if (state.getNumber() == num) {
                return state;
            }
        }
        return null;
    }
}
