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

package com.openexchange.groupware.infostore.database.impl;

import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.MetadataSwitcher;

public class GetSwitch implements MetadataSwitcher {

	private DocumentMetadata metadata;

	public GetSwitch(DocumentMetadata metadata){
		this.metadata = metadata;
	}
	
	public Object lastModified() {
		return metadata.getLastModified();
	}

	public Object creationDate() {
		return metadata.getCreationDate();
	}

	public Object modifiedBy() {
		return metadata.getModifiedBy();
	}

	public Object folderId() {
		return metadata.getFolderId();
	}

	public Object title() {
		return metadata.getTitle();
	}

	public Object version() {
		return metadata.getVersion();
	}

	public Object content() {
		return metadata.getContent();
	}

	public Object id() {
		return metadata.getId();
	}

	public Object fileSize() {
		return metadata.getFileSize();
	}

	public Object description() {
		return metadata.getDescription();
	}

	public Object url() {
		return metadata.getURL();
	}

	public Object createdBy() {
		return metadata.getCreatedBy();
	}

	public Object fileName() {
		return metadata.getFileName();
	}

	public Object fileMIMEType() {
		return metadata.getFileMIMEType();
	}

	public Object sequenceNumber() {
		return metadata.getSequenceNumber();
	}

	public Object categories() {
		return metadata.getCategories();
	}

	public Object lockedUntil() {
		return metadata.getLockedUntil();
	}

	public Object fileMD5Sum() {
		return metadata.getFileMD5Sum();
	}

	public Object versionComment() {
		return metadata.getVersionComment();
	}

	public Object currentVersion() {
		return metadata.isCurrentVersion();
	}

	public Object colorLabel() {
		return metadata.getColorLabel();
	}

	public Object filestoreLocation() {
		return metadata.getFilestoreLocation();
	}

}
