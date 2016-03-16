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

import com.openexchange.exception.OXException;

/**
 * This switcher can translate all kinds of objects given to a boolean value.
 *
 * This is necessary for CSV files - mostly for the PRIVATE flag, which
 * may be -depending on the Outlook version- either "true", "yes",
 * "private" or whatever... so, rather than calling it "BooleanFinder",
 * one might call it "TruthSeeks", because it finds out whether a certain
 * value might be translated to <code>true</code>, considering everything
 * else <code>false</code>.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ContactSwitcherForBooleans extends	AbstractContactSwitcherWithDelegate {

	public String[] trueValues = {"y", "yes", "true" , "privat", "private", "priv\u00e9", "1"};

	public Object[] determineBooleanValue(final Object[] objects){
		final Object obj = objects[1];

		boolean boolValue = false;
		//check strings
		try {
			final String comp = (String) obj;
			for(final String trueVal : trueValues){
				if(trueVal.equals(comp.toLowerCase())){
					boolValue = true;
				}
			}
		} catch(final ClassCastException e){
			//do nothing, keep on trying
		}

		//check boolean object
		try {
			boolValue = (Boolean) obj;
		} catch(final ClassCastException e){
			//do nothing, keep on trying
		}
		// check Integer object
		try {
			final Integer comp = (Integer) obj;
			if(comp.compareTo(0) > 0 ){
				boolValue = true;
			}
		} catch(final ClassCastException e){
			//do nothing, keep on trying
		}

		objects[1] = boolValue;
		return objects;

	}

	@Override
	public Object privateflag(final Object... objects) throws OXException {
		return delegate.privateflag( determineBooleanValue(objects) );
	}

	@Override
	public Object markasdistributionlist(Object[] objects) throws OXException {
		return delegate.markasdistributionlist( determineBooleanValue(objects));
	}

}
