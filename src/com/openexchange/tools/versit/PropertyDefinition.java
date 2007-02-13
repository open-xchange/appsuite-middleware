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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

/**
 * @author Viktor Pracht
 */
public class PropertyDefinition {

	public final ValueDefinition value;

	private HashMap Values = new HashMap();

	private HashMap Parameters = new HashMap();

	public static final PropertyDefinition Default = new PropertyDefinition(
			new ValueDefinition());

	public PropertyDefinition(ValueDefinition value) {
		this.value = value;
	}

	public PropertyDefinition(ValueDefinition value, String[] valueNames,
			ValueDefinition[] values, String[] paramNames,
			ParameterDefinition[] parameters) {
		this.value = value;
		for (int i = 0; i < values.length; i++)
			addValue(valueNames[i], values[i]);
		for (int i = 0; i < parameters.length; i++)
			addParameter(paramNames[i], parameters[i]);
	}

	public ParameterDefinition getParameter(String name) {
		ParameterDefinition param = (ParameterDefinition) Parameters.get(name
				.toUpperCase());
		if (param == null)
			return ParameterDefinition.Default;
		return param;
	}

	public void addParameter(String name, ParameterDefinition parameter) {
		Parameters.put(name.toUpperCase(), parameter);
	}

	public ValueDefinition getValue(String name) {
		ValueDefinition value = (ValueDefinition) Values
				.get(name.toUpperCase());
		if (value == null)
			return this.value;
		return value;
	}

	public void addValue(String name, ValueDefinition value) {
		Values.put(name.toUpperCase(), value);
	}

	public Property parse(Scanner s, String propertyName) throws IOException {
		Property property = new Property(propertyName);
		while (s.peek == ';') {
			s.read();
			String paramName = s.parseName();
			if (paramName.length() == 0)
				return null;
			Parameter param = getParameter(paramName).parse(s, paramName);
			if (param == null)
				return null;
			property.addParameter(param);
		}
		if (s.peek != ':')
			return null;
		s.read();
		ValueDefinition valueDefinition = value;
		Parameter valueParam = property.getParameter("VALUE");
		if (valueParam != null)
			valueDefinition = getValue(valueParam.getValue(0).getText());
		Object value = valueDefinition.parse(s, property);
		if (value == null)
			return null;
		property.setValue(value);
		return property;
	}

	public void write(FoldingWriter fw, Property property) throws IOException {
		fw.write(property.name);
		int count = property.getParameterCount();
		for (int i = 0; i < count; i++) {
			Parameter parameter = property.getParameter(i);
			ParameterDefinition definition = getParameter(parameter.name);
			definition.write(fw, parameter);
		}
		fw.write(":");
		ValueDefinition definition = value;
		Parameter valueParameter = property.getParameter("VALUE");
		if (valueParameter != null)
			definition = getValue(valueParameter.getValue(0).getText());
		definition.write(fw, property);
	}

}
