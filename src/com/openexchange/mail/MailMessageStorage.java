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

package com.openexchange.mail;

import com.openexchange.mail.MailStorageUtils.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * MailMessageStorage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailMessageStorage {

	/**
	 * Default constructor
	 */
	protected MailMessageStorage() {
		super();
	}

	/**
	 * Gets the message located in given folder whose UID matches given UID.
	 * <p>
	 * The returned instance of {@link MailMessage} is completely pre-filled
	 * including content references.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param msgUID
	 *            The message UID
	 * @return Corresponding message
	 * @throws MailException
	 *             If message could not be returned
	 */
	public abstract MailMessage getMessage(String folder, long msgUID) throws MailException;

	/**
	 * Gets messages located in given folder. See parameter description to know
	 * which messages are going to be returned
	 * <p>
	 * In contrast to {@link #getMessage(String, long)} the returned instances
	 * of {@link MailMessage} are only pre-filled with the fields specified
	 * through parameter <code>fields</code>.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param fromToIndices
	 *            The indices range specifying the desired sub-list in sorted
	 *            list; may be <code>null</code> or must have a length of
	 *            <code>2</code>
	 * @param sortCol
	 *            The sort field
	 * @param order
	 *            Whether ascending or descending sort order
	 * @param searchCols
	 *            The search fields
	 * @param searchPatterns
	 *            The pattern for the search field; therefore this array's
	 *            length must be equal to length of parameter
	 *            <code>searchCols</code>
	 * @param linkSearchTermsWithOR
	 *            <code>true</code> to link search fields with a logical OR;
	 *            <code>false</code> to link with logical AND
	 * @param fields
	 *            The fields to pre-fill in returned instances of
	 *            {@link MailMessage}
	 * @return The desired, pre-filled instances of {@link MailMessage}
	 * @throws MailException
	 */
	public abstract MailMessage[] getMessages(String folder, final int[] fromToIndices, final MailListField sortCol,
			final OrderDirection order, final MailListField[] searchCols, final String[] searchPatterns,
			final boolean linkSearchTermsWithOR, final MailListField[] fields) throws MailException;

	/**
	 * Gets all unread messages located in folder whose fullname matches given
	 * parameter.
	 * 
	 * @param fullname
	 *            The fullname of the folder
	 * @return All unread messages contained in an array of {@link MailMessage}
	 */
	public abstract MailMessage[] getUnreadMessages(final String fullname);

	public abstract MailMessage[] searchMessages(String folder, MailListField[] searchCols, String[] searchPatterns,
			boolean linkWithOR, MailListField[] fields) throws MailException;

}
