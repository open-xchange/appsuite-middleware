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

package com.openexchange.contact;

import com.openexchange.exception.OXException.Truncated;
import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * {@link TruncatedContactAttribute} - {@link Truncated} implementation using 
 * contact fields to identify the truncated attribute. 
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TruncatedContactAttribute implements Truncated {
	
	private final ContactField field;
	private final int maxSize;
	private final int length;
	
	/**
	 * Initializes a new {@link TruncatedContactAttribute}.
	 * 
	 * @param field the field where the data truncation occurred
	 * @param maxSize the maximum size for the attribute
	 * @param length the actual length of the value
	 */
	public TruncatedContactAttribute(final ContactField field, final int maxSize, final int length) {
		this.field = field;
		this.maxSize = maxSize;
		this.length = length;		 
	}
	
	/**
	 * Gets the field representing the contact attribute where the data 
	 * truncation occurred.
	 * 
	 * @return the field
	 */
	public ContactField getField() {
		return this.field;
	}

	/**
	 * {@link Deprecated} - use <code>getField()</code> directly and determine 
	 * field ID in ajax layer internally afterwards.
	 */
	@Deprecated 
	@Override
	public int getId() {
		return this.getField().getNumber();
	}

	@Override
	public int getMaxSize() {
		return this.maxSize;
	}

	@Override
	public int getLength() {
		return this.length;
	}
}
