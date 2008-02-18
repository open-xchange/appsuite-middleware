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

package com.openexchange.groupware.contact;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * ContactInterface
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public interface ContactInterface {
	
	/**
	 * Determines the number of contacts a certain
	 * private or public folder.
	 * 
	 * @param folderId -
	 *            The Folder ID
	 * @param readCon -
	 *            The Readable Connection To DB
	 * @return Amount of contacts as an <code>int</code>
	 * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
	 */
	public int getNumberOfContacts(int folderId) throws OXException;
	
	/**
	 * List contacts in a folder
	 * @param folderId
	 * The Folder ID
	 * @param from
	 * Start position in list
	 * @param to 
	 * End position in list
	 * @param orderBy
	 * Column id to sort. 0 if no order by is used
	 * @param orderDir
	 * Order direction (asc or desc)
	 * @param cols
	 * The columns filled to the dataobject
	 * @param readcon 
	 * The readable Database Connection
	 * @return A SearchIterator contains Task objects
	 * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
	 */	 
	public SearchIterator getContactsInFolder(int folderId, int from, int to, int orderBy, String orderDir, int[] cols) throws OXException;

	/**
	 * Loads one contact by the given ID
	 * @param objectId
	 * The Object ID
	 * @return 
	 * return the ContactObject
	 * @throws OXException, OXPermissionException
	 */
	public ContactObject getObjectById(int objectId, int inFolder) throws OXException;

	/**
	 * Loads a range of contacts by the given IDs
	 * @param objectIdAndInFolder[]
	 * array with two dimensions. First dimension contains a seond array with two values.
	 * 1. value is object_id
	 * 2. value if folder_id
	 * @param cols
	 * The columns filled to the dataobject
	 * @return A SearchIterator contains ContactObjects
	 * @throws OXException
	 */	
	public SearchIterator getObjectsById(int[][] objectIdAndInFolder, int cols[]) throws OXException, Exception;
	
	public int getFolderId();
}
