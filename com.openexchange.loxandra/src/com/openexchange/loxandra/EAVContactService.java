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
package com.openexchange.loxandra;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.openexchange.loxandra.dto.EAVContact;


/**
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface EAVContactService {

	/**
	 * Insert a new contact
	 * @return int code
	 */
	public void insertContact(EAVContact c);

	/**
	 * Delete a contact
	 * @param uuid for the contact
	 * @return int code
	 */
	public void deleteContact(UUID uuid);

	/**
	 * Update a contact
	 * @param c contact to update
	 * @return int code
	 */
	public void updateContact(EAVContact c);

	/**
	 * Copy the contact to another folder
	 * @param contact for copy
	 * @param the folder UUID for copy
	 * @return
	 */
	public void copyContactToFolder(EAVContact c, UUID newFolderUUID);

	/**
	 * Move contact to another folder
	 * @param the folder UUID for move
	 * @return
	 */
	public void moveContactToFolder(EAVContact c, UUID oldFolderUUID, UUID newFolderUUID);

	/**
	 * Remove the given contact from a specific folder
	 * @param c
	 * @param folderUUID
	 */
	public void removeContactFromFolder(EAVContact c, UUID folderUUID);

	/**
	 * Delete one or more unnamed properties (a.k.a. columns)
	 * @param o
	 */
	public void deleteProperties(UUID uuid, String... prop);

	/**
	 * Add one or more unnamed properties
	 *
	 * @param uuid
	 * @param props
	 */
	public void addProperties(UUID uuid, HashMap<String, String> props);

	/**
	 * Get one or more unnamed properties
	 *
	 * @param uuid
	 * @param props
	 * @return props
	 */
	public String getProperties(UUID uuid, String... props);

	/**
	 * Get a contact
	 * @param uuid
	 * @param limited boolean value indicated whether to retrieve the full details or the basic stuff
	 * @return
	 */
	public EAVContact getContact(UUID uuid, boolean limited);

	/**
	 * Get all contacts residing inside a folder
	 *
	 * @param folderUUID uuid of the folder
	 * @return a sorted list (by surname) of all contacts
	 */
	public List<EAVContact> getContactsFromFolder(UUID folderUUID);

	/**
	 * Get a range of contacts residing inside a folder.
	 *
	 * @param folderUUID The Folder UUID
     * @param from Start position in list inclusive
     * @param to End position in list exclusive
     *
     * @return A SearchIterator contains Task objects
	 */
	public List<EAVContact> getContactsFromFolder(UUID folderUUID, int from, int to);

	/**
	 * Return the number of contacts contained within the given folder
	 * @param folderUUID
	 * @return
	 */
	public int getNumberOfContactsInFolder(UUID folderUUID);
}
