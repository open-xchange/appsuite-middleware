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

package com.openexchange.groupware.contact.helpers;

import java.util.Comparator;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ContactFieldDocumentor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Formatter formatter = new Formatter();
		ContactField[] fields = ContactField.values();
		Arrays.sort(fields, new Comparator<ContactField>() {
			@Override
			public int compare(ContactField o1, ContactField o2) {
				return o1.getNumber() - o2.getNumber();
			}
		});
		System.out.println(formatter.format("%3s %38s %38s\n", "#", "Ajax name", "OXMF name"));
		for(ContactField field: fields){
			System.out.println(formatter.format("%3s %38s %38s\n", field.getNumber(), field.getAjaxName(), oxmf(field.getAjaxName())));

		}

	}

	private static String oxmf(String ajaxName) {
		Pattern p = Pattern.compile("([a-zA-Z0-9]+)_(\\w)([a-zA-Z0-9]+)");
		Matcher matcher = p.matcher(ajaxName);
		StringBuilder sb = new StringBuilder();
		boolean found = false;
		while(matcher.find()){
			found = true;
			sb.append(matcher.group(1));
			sb.append(matcher.group(2).toUpperCase());
			sb.append(matcher.group(3));
		}
		if(!found) {
            return ajaxName;
        }
		return sb.toString();
	}

}
