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

package com.openexchange.groupware.filestore;

import java.net.URI;

public class FilestoreImpl implements Filestore {
	/**
	 *
	 */
	private static final long serialVersionUID = -912578396776397210L;
	private int id;
	private URI uri;
	private long size;
	private long maxContext;

	@Override
    public int getId() {
		return id;
	}
	@Override
    public long getMaxContext() {
		return maxContext;
	}
	@Override
    public long getSize() {
		return size;
	}
	@Override
    public URI getUri() {
		return uri;
	}
	public void setId(final int id) {
		this.id = id;
	}
	public void setMaxContext(final long maxContext) {
		this.maxContext = maxContext;
	}
	public void setSize(final long size) {
		this.size = size;
	}
	public void setUri(final URI uri) {
		this.uri = uri;
	}


}
