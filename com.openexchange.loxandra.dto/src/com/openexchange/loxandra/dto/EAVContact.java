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
package com.openexchange.loxandra.dto;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.openexchange.groupware.container.Contact;

/**
 * Extends the functionality of the {@link com.openexchange.groupware.container.Contact }
 * by supporting the EAV model. Contains a HashMap where all the unnamed properties are stored.
 * Use the addUnnamedProperty method, in order to set an unnamed property for the contact.
 * The getter/setter methods for userfield[1-20] in {@link com.openexchange.groupware.container.Contact}
 * should be deprecated in order to use the new EAV model.
 * <br/> <br/>
 * Additionally, provides UUID fields for the CID, ID and FID MySQL fields (codename Kiffer :)
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class EAVContact extends Contact implements Comparable<EAVContact> {

	private static final long serialVersionUID = -839484638375498364L;

	/** UUID is the primary unique key for each contact aka ID **/
	private UUID uuid;

	/** The context UUID aka CID **/
	private UUID contextUUID;

	/** The folder UUID aka FID **//*
	private UUID folderUUID;*/

	/** The folder UUID list aka FID **/
	private final List<UUID> folderUUIDs;

	/** The time UUID for this contact aka time of creation **/
	private UUID timeUUID;

	/** a hash map to hold the unnamed properties **/
	private final HashMap<String, String> unnamedProperties;

	/** Holds all the named properties for an object. Used only by the {@link #CassandraContactDAO.populateDTO */
	private final HashMap<String, ByteBuffer> namedProperties = new HashMap<String, ByteBuffer>();

	/**
	 * Default constructor
	 */
	public EAVContact() {
		unnamedProperties = new HashMap<String, String>();
		folderUUIDs = new ArrayList<UUID>();
	}

	/**
	 * Add a 'named property' to the contact
	 *
	 * @param key name of the property
	 * @param value of the property
	 */
	public void addNamedProperty(String key, ByteBuffer value) {
		namedProperties.put(key, value);
	}

	/**
	 * Get a 'named property' from the contact
	 * @param key property
	 * @return value
	 */
	public ByteBuffer getNamedProperty(String key) {
		return namedProperties.get(key);
	}

	/**
	 * Returns true if the hash map contains the specified
	 * 'named property'
	 *
	 * @param key
	 * @return
	 */
	public boolean containsNamedProperty(String key) {
		return namedProperties.containsKey(key);
	}

	/**
	 * Clears the named properties.
	 */
	public void clearNamedProperties() {
		namedProperties.clear();
	}

	/**
	 * Add an 'unnamed property' to the contact
	 *
	 * @param key name of the property
	 * @param value value of the property
	 */
	public void addUnnamedProperty(String key, String value) {
		unnamedProperties.put(key, value);
	}

	/**
	 * Get an unnamed property from the contact
	 *
	 * @param key property
	 * @return the value
	 */
	public String getUnnamedProperty(String key) {
		return unnamedProperties.get(key);
	}

	/**
	 * Get all names of the unnamed properties *-)
	 *
	 * @return a list with all names of the unnamed properties
	 */
	public List<String> getUnnamedPropertyNames() {
		List<String> keys = new ArrayList<String>();
		Iterator<String> iteratorKeys = unnamedProperties.keySet().iterator();

		while (iteratorKeys.hasNext()) {
			keys.add(iteratorKeys.next());
		}

		return keys;
	}

	/**
	 * Get all values of the unnamed properties
	 *
	 * @return a list with all values of the unnamed properties
	 */
	public List<String> getUnnamedPropertyValues() {
		List<String> values = new ArrayList<String>();

		Iterator<String> iteratorKeys = unnamedProperties.keySet().iterator();

		while (iteratorKeys.hasNext()) {
			values.add(unnamedProperties.get(iteratorKeys.next()));
		}

		return values;
	}

	/**
	 * @return keys iterator
	 */
	public Iterator<String> getKeysIterator() {
		return unnamedProperties.keySet().iterator();
	}

	/**
	 * @return the uuid
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the contextUUID
	 */
	public UUID getContextUUID() {
		return contextUUID;
	}

	/**
	 * @param contextUUID the contextUUID to set
	 */
	public void setContextUUID(UUID contextUUID) {
		this.contextUUID = contextUUID;
	}

	/**
	 * @return the folderUUID
	 */
	/*public UUID getFolderUUID() {
		return folderUUID;
	}

	*//**
	 * @param folderUUID the folderUUID to set
	 *//*
	public void setFolderUUID(UUID folderUUID) {
		this.folderUUID = folderUUID;
	}*/

	/**
	 * Compares the surname of a contact, if the same, then compares the given name.
	 */
	@Override
	public int compareTo(EAVContact o) {
		if (!yomiLastName.isEmpty()) {
			int yomiLastCompare = yomiLastName.compareTo(o.yomiLastName);
			return (yomiLastCompare != 0 ? yomiLastCompare : yomiFirstName.compareTo(o.yomiFirstName));
		} else {
			int latinLastCompare = sur_name.compareTo(o.sur_name);
			return (latinLastCompare != 0 ? latinLastCompare : given_name.compareTo(o.given_name));
		}
	}

	/**
	 * @return the timeUUID
	 */
	public UUID getTimeUUID() {
		return timeUUID;
	}

	/**
	 * @param timeUUID the timeUUID to set
	 */
	public void setTimeUUID(UUID timeUUID) {
		this.timeUUID = timeUUID;
	}

	/**
	 * Add contact to the specified folder
	 * @param uuid
	 */
	public void addFolderUUID(UUID uuid) {
		folderUUIDs.add(uuid);
	}

	/**
	 * Remove contact from the specified folder
	 * @param uuid
	 */
	public void removeFolderUUID(UUID uuid) {
		folderUUIDs.remove(uuid);
	}

	/**
	 * Get a list with all folder UUIDs in which the contact resides
	 *
	 * @return list with folders
	 */
	public List<UUID> getFolderUUIDs() {
		return folderUUIDs;
	}
}