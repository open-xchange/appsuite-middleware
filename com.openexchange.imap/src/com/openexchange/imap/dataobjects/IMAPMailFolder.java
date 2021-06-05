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

package com.openexchange.imap.dataobjects;

import com.openexchange.imap.IMAPProvider;
import com.openexchange.mail.dataobjects.MailFolder;

/**
 * {@link IMAPMailFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class IMAPMailFolder extends MailFolder {

    private static final long serialVersionUID = -2181337892739317688L;

    private static final String PROTOCOL = IMAPProvider.PROTOCOL_IMAP.toString();

	private boolean nonExistent;
	private boolean b_nonExistent;

	/**
	 * Initializes a new {@link IMAPMailFolder}
	 */
	public IMAPMailFolder() {
		super();
		setProperty("protocol", PROTOCOL);
	}

	/**
	 * Checks if this folder is non-existent according to IMAP's "LIST-EXTENDED"
	 * extension
	 *
	 * @return <code>true</code> if non-existent; otherwise <code>false</code>
	 */
	public boolean isNonExistent() {
		return nonExistent;
	}

	/**
	 * @return <code>true</code> if non-existent is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsNonExistent() {
		return b_nonExistent;
	}

	/**
	 * Removes the non-existent status
	 */
	public void removeNonExistent() {
		nonExistent = false;
		b_nonExistent = false;
	}

	/**
	 * Sets if this folder is non-existent according to IMAP's "LIST-EXTENDED"
	 * extension
	 *
	 * @param nonExistent
	 *            <code>true</code> to set as non-existent; otherwise
	 *            <code>false</code>
	 */
	public void setNonExistent(boolean nonExistent) {
		this.nonExistent = nonExistent;
		b_nonExistent = true;
	}
}
