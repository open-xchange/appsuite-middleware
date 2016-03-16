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

package com.openexchange.groupware.infostore.database.impl;

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
	    this();
		final SetSwitch setSwitch = new SetSwitch(this);
		final GetSwitch getSwitch = new GetSwitch(dm);
		for(final Metadata attr : Metadata.VALUES) {
			setSwitch.setValue(attr.doSwitch(getSwitch));
			attr.doSwitch(setSwitch);
		}
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
	    return null == lastModified ? 0 : lastModified.getTime();
	}

	@Override
    public void setSequenceNumber(final long sequenceNumber) {
	    // Nothing to do, yet
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
