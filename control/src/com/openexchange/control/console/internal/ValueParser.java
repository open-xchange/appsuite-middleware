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

package com.openexchange.control.console.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * {@link ValueParser}
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * 
 */
public class ValueParser {
	
	final List<ValuePairObject> valuePairObjectList = new ArrayList<ValuePairObject>();

	final List<ValueObject> valueList = new ArrayList<ValueObject>();
	
	public ValueParser(final String[] args, String[] parameter) throws ConsoleException {
		final HashSet<String> parameterSet = new HashSet<String>();
		
		for (int a = 0; a < parameter.length; a++) {
			parameterSet.add(parameter[a]);
		}
		
		for (int a = 0; a < args.length; a++) {
			final String param = args[a];
			if (parameterSet.contains(param)) {
				final ValuePairObject valuePairObject = parseValuePair(param, args, a);
				valuePairObjectList.add(valuePairObject);
				a++;
			} else {
				final ValueObject valueObject = new ValueObject(param);
				valueList.add(valueObject);
			}
		}
	}
	
	protected ValuePairObject parseValuePair(final String name, String[] args, int pos) throws ConsoleException {
		if (pos < args.length-1) {
			String value = args[pos+1];
			ValuePairObject valuePairObject = new ValuePairObject(name, value);
			return valuePairObject;
		} else {
			throw new ConsoleException("missing value for parameter: " + name);
		}
	}

	public ValuePairObject[] getValuePairObjects() {
		return (ValuePairObject[])valuePairObjectList.toArray(new ValuePairObject[valuePairObjectList.size()]);
	}

	public ValueObject[] getValueObjects() {
		return (ValueObject[])valueList.toArray(new ValueObject[valueList.size()]);
	}
}
