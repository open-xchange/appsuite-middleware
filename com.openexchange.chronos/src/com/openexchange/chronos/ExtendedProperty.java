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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos;

import java.util.List;

/**
 * {@link ExtendedProperty}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ExtendedProperty {

    private final String name;
    private final String value;
    private final List<ExtendedPropertyParameter> parameters;

    /**
     * Initializes a new {@link ExtendedProperty}.
     *
     * @param name The property name
     * @param value The value
     * @param parameters The parameters, or <code>null</code> if there are none
     */
    public ExtendedProperty(String name, String value, List<ExtendedPropertyParameter> parameters) {
        super();
        this.name = name;
        this.value = value;
        this.parameters = parameters;
    }

    /**
     * Initializes a new {@link ExtendedProperty}, without further parameters.
     *
     * @param name The property name
     * @param value The value
     */
    public ExtendedProperty(String name, String value) {
        this(name, value, null);
    }

    /**
     * Gets the property name.
     *
     * @return The property name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the property value.
     *
     * @return The property value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets a map of additional property parameters.
     *
     * @return The property parameters, or <code>null</code> if not set
     */
    public List<ExtendedPropertyParameter> getParameters() {
        return parameters;
    }

    /**
     * Gets the (first) property parameter matching the supplied name.
     *
     * @param name The name of the parameter to get
     * @return The parameter, or <code>null</code> if not found
     */
    public ExtendedPropertyParameter getParameter(String name) {
        if (null != parameters) {
            for (ExtendedPropertyParameter parameter : parameters) {
                if (name.equals(parameter.getName())) {
                    return parameter;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        if (null != parameters && 0 < parameters.size()) {
            for (ExtendedPropertyParameter parameter : parameters) {
                stringBuilder.append(';').append(parameter.getName()).append('=').append(parameter.getValue());
            }
        }
        stringBuilder.append(':').append(value);
        return stringBuilder.toString();
    }

}
