/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
        for(Parameter p: parameters) {
            buffy.append('&').append(p.toString());
        }

        buffy.replace(0, 1, "?");
        return buffy.toString();
    }
}
