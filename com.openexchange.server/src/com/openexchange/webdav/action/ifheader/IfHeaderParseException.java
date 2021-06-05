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

package com.openexchange.webdav.action.ifheader;


/**
 * TODO Error codes
 */
public class IfHeaderParseException extends Exception {

	private static final long serialVersionUID = 1L;
	private final int col;

	public IfHeaderParseException(final int col) {
		super();
		this.col = col;
	}

	public IfHeaderParseException(final String arg0, final Throwable arg1, final int col) {
		super(arg0, arg1);
		this.col = col;
	}

	public IfHeaderParseException(final String arg0, final int col) {
		super(arg0);
		this.col = col;
	}

	public IfHeaderParseException(final Throwable arg0, final int col) {
		super(arg0);
		this.col = col;
	}

	public int getColumn() {
		return col;
	}



}
