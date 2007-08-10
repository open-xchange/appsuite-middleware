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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author Viktor Pracht (design)
 * @author Tobias Prinz (added invalid flag to fix bug 8527)
 */
public class Property {

	public final String name;

	private final ArrayList<Parameter> Parameters = new ArrayList<Parameter>();
	
	private final HashMap<String, Parameter> Index = new HashMap<String, Parameter>();

	private Object value = null;
	
	private boolean invalid;

	public Property(String name) {
		this.name = name;
		this.invalid = false;
	}

	public Parameter getParameter(final String name) {
		return Index.get(name.toUpperCase(Locale.ENGLISH));
	}
	
	public Parameter getParameter(final int index) {
		return Parameters.get(index);
	}
	
	public int getParameterCount() {
		return Parameters.size();
	}
	
	public void addParameter(final Parameter parameter) {
		final String Name = parameter.name.toUpperCase(Locale.ENGLISH);
		final Parameter existingParam = Index.get(Name);
		if (existingParam != null) {
			final int count = parameter.getValueCount();
			for (int i = 0; i < count; i++) {
				existingParam.addValue(parameter.getValue(i));
			}
		} else {
			Parameters.add(parameter);
			Index.put(parameter.name.toUpperCase(Locale.ENGLISH), parameter);
		}
	}

	public Object getValue() {
		return value;
	}

	public Property setValue(final Object value) {
		this.value = value;
		return this;
	}
	
	public void markInvalid(){
		this.invalid = true;
	}
	
	public boolean isInvalid(){
		return this.invalid;
	}

}
