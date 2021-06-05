/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.contact.storage.rdb.fields;


/**
 * {@link DistListMemberField} -
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum DistListMemberField {
    /**
	 * ID of corresponding entry in prg_contacts table
	 */
	PARENT_CONTACT_ID,
	/**
	 * Object ID of the member's contact if the member is an existing contact
	 */
	CONTACT_ID,
	/**
	 * Which email field of an existing contact (if any) is used for the mail field. 0 independent contact 1 default email field (email1) 2 second email field (email2) 3 third email field (email3)
	 */
	MAIL_FIELD,
	/**
	 * Folder ID of the member's contact if the member is an existing contact
	 */
	CONTACT_FOLDER_ID,
	/**
	 * Display name
	 */
	DISPLAY_NAME,
	/**
	 * Last name
	 */
	LAST_NAME,
	/**
	 * First name
	 */
	FIRST_NAME,
	/**
	 * Mail address
	 */
	MAIL,
	/**
	 * Context id
	 */
	CONTEXT_ID,
	/**
	 * UUID
	 */
	UUID,
	;
}
