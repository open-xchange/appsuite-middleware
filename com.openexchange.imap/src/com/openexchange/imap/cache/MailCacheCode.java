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

package com.openexchange.imap.cache;

/**
 * {@link MailCacheCode}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public enum MailCacheCode {

	/**
	 * Rights
	 */
	RIGHTS(1),
	/**
	 * User flags
	 */
	USER_FLAGS(2),
	/**
	 * Namespace folders
	 */
	NAMESPACE_FOLDERS(3),
	/**
	 * Root folder allows subfolders
	 */
	ROOT_SUBFOLDER(4),
	/**
	 * Capabilities
	 */
	CAPS(5),
	/**
	 * Folders
	 */
	FOLDERS(6),
	/**
	 * LIST/LSUB collection
	 */
	LIST_LSUB(7),
	;

	private final int code;

	private MailCacheCode(int code) {
		this.code = code;
	}

	/**
	 * @return The code as <code>int</code>
	 */
	public int getCode() {
		return code;
	}
}
