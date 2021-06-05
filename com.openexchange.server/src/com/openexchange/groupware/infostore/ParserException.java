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

package com.openexchange.groupware.infostore;

/**
 * TODO Error codes
 */
public class ParserException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -8332582839285408682L;

	public ParserException() {
		super();
	}

	public ParserException(final String arg0) {
		super(arg0);
	}

	public ParserException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	public ParserException(final Throwable arg0) {
		super(arg0);
	}

}
