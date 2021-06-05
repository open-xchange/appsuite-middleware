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

package com.openexchange.mobile.configuration.generator.configuration;


public class Required {

    public static Required TRUE = new Required(Value.TRUE);

    public static Required FALSE = new Required(Value.FALSE);

    @SuppressWarnings("hiding")
    public enum Value {
        TRUE,
        FALSE,
        CONDITION;
    }

    private final Required.Value value;

    private Condition[] condition;

    public Required(Required.Value value, Condition[] condition) {
        super();
        this.value = value;
        this.condition = condition;
    }

    public Required(Required.Value value) {
        super();
        this.value = value;
    }


    public Required.Value getValue() {
        return value;
    }


    public Condition[] getCondition() {
        return condition;
    }


}
