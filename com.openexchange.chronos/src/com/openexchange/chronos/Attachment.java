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

package com.openexchange.chronos;

import java.util.Date;
import com.openexchange.ajax.fileholder.IFileHolder;

/**
 * {@link Attachment}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.1.1">RFC 5545, section 3.8.1.1</a>
 */
public class Attachment {

    private int managedId;
    private String uri;
    private IFileHolder data;
    private Date created;
    private String formatType;
    private long size;
	private String filename;
    private String checksum;

    /**
     * Initializes a new {@link Attachment}.
     */
	public Attachment() {
		super();
	}

    /**
     * Gets the attachment's format- / MIME-type.
     *
     * @return The format type
     */
	public String getFormatType() {
		return formatType;
	}

    /**
     * Sets the attachment's format- / MIME-type.
     *
     * @param formatType The format type
     */
	public void setFormatType(String formatType) {
		this.formatType = formatType;
	}

    public long getSize() {
		return size;
	}

    public void setSize(long size) {
		this.size = size;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

    public int getManagedId() {
		return managedId;
	}

    public void setManagedId(int managedId) {
		this.managedId = managedId;
	}

	public IFileHolder getData() {
		return data;
	}

	public void setData(IFileHolder data) {
		this.data = data;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Gets the checksum of the attachment's binary data, if available.
     *
     * @return The checksum, or <code>null</code> if not available
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Sets the checksum of the attachment's binary data
     *
     * @param checksum The checksum
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public String toString() {
        return "Attachment [managedId=" + managedId + ", uri=" + uri + ", filename=" + filename + "]";
    }

}
