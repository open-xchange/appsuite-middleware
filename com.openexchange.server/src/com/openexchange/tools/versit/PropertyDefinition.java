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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.versit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author Viktor Pracht
 * @author Tobias Prinz (handling markInvalid() for empty properties)
 */
public class PropertyDefinition {

    public final ValueDefinition value;

    private final HashMap<String, ValueDefinition> Values = new HashMap<String, ValueDefinition>();

    private final HashMap<String, ParameterDefinition> Parameters = new HashMap<String, ParameterDefinition>();

    public static final PropertyDefinition Default = new PropertyDefinition(new ValueDefinition());

    public PropertyDefinition(final ValueDefinition value) {
        this.value = value;
    }

    public PropertyDefinition(final ValueDefinition value, final String[] valueNames, final ValueDefinition[] values, final String[] paramNames, final ParameterDefinition[] parameters) {
        this.value = value;
        for (int i = 0; i < values.length; i++) {
            addValue(valueNames[i], values[i]);
        }
        for (int i = 0; i < parameters.length; i++) {
            addParameter(paramNames[i], parameters[i]);
        }
    }

    public ParameterDefinition getParameter(final String name) {
        final ParameterDefinition param = Parameters.get(name.toUpperCase(Locale.ENGLISH));
        if (param == null) {
            return ParameterDefinition.Default;
        }
        return param;
    }

    public final void addParameter(final String name, final ParameterDefinition parameter) {
        Parameters.put(name.toUpperCase(Locale.ENGLISH), parameter);
    }

    public ValueDefinition getValue(final String name) {
        final ValueDefinition value = Values.get(name.toUpperCase(Locale.ENGLISH));
        if (value == null) {
            return this.value;
        }
        return value;
    }

    public final void addValue(final String name, final ValueDefinition value) {
        Values.put(name.toUpperCase(Locale.ENGLISH), value);
    }

    public Property parse(final Scanner s, final String propertyName) throws IOException {
        final Property property = new Property(propertyName);
        while (s.peek == ';') {
            s.read();
            final String paramName = s.parseName();
            if (paramName.length() == 0) {
                return null;
            }
            if( paramName.equalsIgnoreCase("BASE64")){
                //ugly work-around for Apple devs who consider BASE64 to be equal to ENCODING=b
                Parameter encoding = new Parameter("ENCODING");
                encoding.addValue(new ParameterValue("b"));
                property.addParameter(encoding);
                continue;
            }
            Parameter param = getParameter(paramName).parse(s, paramName);
            if (param == null) {
                Parameter flagParam = new Parameter(paramName);
                flagParam.addValue(new ParameterValue(""));
                param = flagParam;
            }
            property.addParameter(param);
        }
        if (s.peek != ':') {
            return null;
        }
        s.read();
        ValueDefinition valueDefinition = value;
        final Parameter valueParam = property.getParameter("VALUE");
        if (valueParam != null) {
            valueDefinition = getValue(valueParam.getValue(0).getText());
        }
        final Object value = valueDefinition.parse(s, property);
        if (value == null) {
            property.markInvalid();
        } else {
            property.setValue(value);
        }
        return property;
    }

    public void write(final FoldingWriter fw, final Property property) throws IOException {
        fw.write(property.name);
        final int count = property.getParameterCount();
        for (int i = 0; i < count; i++) {
            final Parameter parameter = property.getParameter(i);
            final ParameterDefinition definition = getParameter(parameter.name);
            definition.write(fw, parameter);
        }
        fw.write(":");
        ValueDefinition definition = value;
        final Parameter valueParameter = property.getParameter("VALUE");
        if (valueParameter != null) {
            definition = getValue(valueParameter.getValue(0).getText());
        }
        definition.write(fw, property);
    }

}
