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

package com.openexchange.contact.internal.mapping;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Mapping} - Generic mapping operations for contact properties.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface Mapping<T> {
	
	/**
	 * Gets a value indicating whether the mapped property is set in the 
	 * supplied contact or not. This is usually done by passing the result
	 * of the contact instance's <code>containsXXX</code>-method.
	 * 
	 * @param contact the contact
	 * @return <code>true</code>, if the property is set, <code>false</code>,
	 * otherwise.
	 */
	boolean isSet(Contact contact);

	/**
	 * Sets the mapped property in the contact to the given value.
	 * 
	 * @param contact the contact to set the property for
	 * @param value the value to set
	 * @throws OXException
	 */
	void set(Contact contact, T value) throws OXException;
	
	/**
	 * Gets the mapped property's value from a contact.
	 * 
	 * @param contact the contact to get the property value from
	 * @return the value
	 */
	T get(Contact contact);

	/**
	 * Validates the property in a contact, throwing exceptions if validation
	 * fails.
	 * 
	 * @param contact the contact to validate the property for
	 * @throws OXException 
	 */
	void validate(Contact contact) throws OXException;

	/**
	 * Gets a value indicating whether a property's value is equal in two 
	 * contacts or not.
	 * 
	 * @param contact1 the first contact for comparison
	 * @param contact2 the second contact for comparison
	 * @return
	 */
	boolean equals(Contact contact1, Contact contact2);
	
	/**
	 * Copies the value of a property in one contact to another one.
	 * 
	 * @param from the contact to read the property value from
	 * @param to the contact to set the property
	 * @throws OXException
	 */
	void copy(Contact from, Contact to) throws OXException;
	
}
