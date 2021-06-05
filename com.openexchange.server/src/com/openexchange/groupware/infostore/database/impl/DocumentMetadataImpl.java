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

package com.openexchange.groupware.infostore.database.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import com.openexchange.groupware.infostore.DefaultDocumentMetadata;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.SetSwitch;

public class DocumentMetadataImpl extends DefaultDocumentMetadata {

	private static final long serialVersionUID = 954199261404066624L;
    private static final String DEFAULT_TYPE = "application/octet-stream";

    /**
     * Initializes a new {@link DocumentMetadataImpl}.
     */
    public DocumentMetadataImpl() {
		this(InfostoreFacade.NEW);
	}

	/**
	 * Initializes a new {@link DocumentMetadataImpl}.
	 *
	 * @param id The document ID
	 */
	public DocumentMetadataImpl(final int id){
	    super();
		this.id = id;
		properties = new HashMap<String,String>();
		meta = new LinkedHashMap<String, Object>();
	}

	public DocumentMetadataImpl(final DocumentMetadata dm) {
        this(dm, Metadata.VALUES_ARRAY);
	}

	public DocumentMetadataImpl(final DocumentMetadata dm, Metadata[] values) {
	    this();
		final SetSwitch setSwitch = new SetSwitch(this);
		final GetSwitch getSwitch = new GetSwitch(dm);
		for(final Metadata attr : values) {
			setSwitch.setValue(attr.doSwitch(getSwitch));
			attr.doSwitch(setSwitch);
		}
        setOriginalFolderId(dm.getOriginalFolderId());
        setOriginalId(dm.getOriginalId());
	}

	@Override
	public int hashCode(){
		return getId();
	}

	@Override
	public boolean equals(final Object o){
		if (o instanceof DocumentMetadata) {
			final DocumentMetadata other = (DocumentMetadata) o;
			return id == other.getId();
		}
		return false;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(final Map<String,String> properties) {
		this.properties = properties;
	}

	@Override
    public String getFileMIMEType() {
	    return null == fileMIMEType ? DEFAULT_TYPE : fileMIMEType;
    }

	@Override
    public long getSequenceNumber() {
	    if (null != sequenceNumber) {
            return sequenceNumber;
        }
        Date lastModDate = getLastModified();
        if (null != lastModDate) {
            return lastModDate.getTime();
        }
        return 0;
	}

	@Override
    public void setSequenceNumber(final long sequenceNumber) {
	    this.sequenceNumber = sequenceNumber;
	}

    @Override
    public void setMeta(Map<String, Object> properties) {
        if (null == properties || properties.isEmpty()) {
            this.meta = null;
        } else {
            this.meta = new LinkedHashMap<String, Object>(properties);
        }
    }

}
