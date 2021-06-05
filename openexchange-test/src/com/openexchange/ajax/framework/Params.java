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

package com.openexchange.ajax.framework;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;

/**
 * This is a shortcut to creating a parameter array with lots of parameter arguments, which usually uses a lot of boiler plate code, like
 * creating a list, creating new parameters, adding them, then converting them to an array.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class Params {

    private final List<Parameter> parameters;

    /*********************** Constructors *************************/

    public Params() {
        parameters = new LinkedList<Parameter>();
    }

    public Params(String key, String value) {
        this();
        add(key, value);
    }

    public Params(String... items) {
        this();
        add(items);
    }

    public Params(Parameter... params) {
        this();
        add(params);
    }

    /*********************** Add *************************/

    public void add(String key, String value) {
        parameters.add(new Parameter(key, value));
    }

    public void add(Parameter param) {
        parameters.add(param);
    }

    public void add(Parameter... params) {
        parameters.addAll(Arrays.asList(params));
    }

    public void add(Params params) {
        add(params.toArray());
    }

    public void add(String... items) {
        if (items.length % 2 == 1) {
            throw new IllegalArgumentException("The number of arguments should be even: key, value, key, value...");
        }
        for (int i = 0; i < items.length; i++) {
            if (i % 2 == 1) {
                parameters.add(new Parameter(items[i - 1], items[i]));
            }
        }
    }

    /*********************** Conversions *************************/

    public Parameter[] toArray() {
        return parameters.toArray(new Parameter[] {});
    }

    public List<Parameter> toList() {
        return parameters;
    }

    @Override
    public String toString() {
        StringBuffer buffy = new StringBuffer();
        for (Parameter p : parameters) {
            buffy.append('&').append(p.toString());
        }

        buffy.replace(0, 1, "?");
        return buffy.toString();
    }
}
