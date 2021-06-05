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

package com.openexchange.groupware.infostore.paths.impl;

import com.openexchange.groupware.infostore.Resolved;
import com.openexchange.webdav.protocol.WebdavPath;

public class ResolvedImpl implements Resolved {

	private final WebdavPath path;
	private final int id;
	private final boolean document;

	public ResolvedImpl(final WebdavPath path, final int id, final boolean document) {
		this.path = path;
		this.id = id;
		this.document = document;
	}

	@Override
    public WebdavPath getPath() {
		return path;
	}

	@Override
    public boolean isDocument() {
		return document;
	}

	@Override
    public boolean isFolder() {
		return !document;
	}

	@Override
    public int getId() {
		return id;
	}

}
