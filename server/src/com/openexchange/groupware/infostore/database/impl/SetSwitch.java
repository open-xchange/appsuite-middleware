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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

/**
 * 
 */
package com.openexchange.groupware.infostore.database.impl;

import java.util.Date;

import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.MetadataSwitcher;

public class SetSwitch implements MetadataSwitcher{

	public static void copy(final DocumentMetadata source, final DocumentMetadata dest) {
		final SetSwitch sw = new SetSwitch(dest);
		final GetSwitch gw = new GetSwitch(source);
		for(final Metadata metadata : Metadata.VALUES) {
			sw.setValue(metadata.doSwitch(gw));
			metadata.doSwitch(sw);
		}
	}
	
	private Object value;
	private DocumentMetadata impl;
	
	public SetSwitch(final DocumentMetadata impl) {
		this.impl = impl;
	}

	public void setValue(final Object value) {
		this.value = value;
	}
	
	public Object lastModified() {
		impl.setLastModified((Date)value);
		return null;
	}

	public Object creationDate() {
		impl.setCreationDate((Date)value);
		return null;
	}

	public Object modifiedBy() {
		nullNumber();
		impl.setModifiedBy(((Integer)value).intValue());
		return null;
	}

	public Object folderId() {
		nullNumberAsLong();
		impl.setFolderId(((Long)value).longValue());
		return null;
	}

	public Object title() {
		impl.setTitle((String)value);
		return null;
	}

	public Object version() {
		impl.setVersion(((Integer)value).intValue());
		return null;
	}

	public Object content() {
		//impl.setContent((String)value);
		return null;
	}

	public Object id() {
		impl.setId(((Integer)value).intValue());
		return null;
	}

	public Object fileSize() {
		nullNumberAsLong();
		impl.setFileSize(((Long)value).longValue());
		return null;
	}

	public Object description() {
		impl.setDescription((String)value);
		return null;
	}

	public Object url() {
		impl.setURL((String)value);
		return null;
	}

	public Object createdBy() {
		nullNumber();
		impl.setCreatedBy(((Integer)value).intValue());
		return null;
	}

	public Object fileName() {
		impl.setFileName((String)value);
		return null;
	}

	public Object fileMIMEType() {
		impl.setFileMIMEType((String)value);
		return null;
	}

	public Object sequenceNumber() {
		//impl.setSequenceNumber((Long)value);
		return null;
	}
	
	public Object categories(){
		impl.setCategories((String)value);
		return null;
	}
	
	public Object lockedUntil(){
		impl.setLockedUntil((Date)value);
		return null;
	}
	
	public Object fileMD5Sum(){
		impl.setFileMD5Sum((String)value);
		return null;
	}

	public Object versionComment() {
		impl.setVersionComment((String)value);
		return null;
	}

	public Object currentVersion() {
		impl.setIsCurrentVersion(((Boolean)value).booleanValue());
		return null;
	}

	public Object colorLabel() {
		nullNumber();
		impl.setColorLabel(((Integer)value).intValue());
		return null;
	}

	private void nullNumber() {
		if(value == null) {
			value = Integer.valueOf(0);
		}
	}
	
	private void nullNumberAsLong() {
		if(value == null) {
			value = Long.valueOf(0);
		}
	}

	public Object filestoreLocation() {
		impl.setFilestoreLocation((String)value);
		return null;
	}
	
}
