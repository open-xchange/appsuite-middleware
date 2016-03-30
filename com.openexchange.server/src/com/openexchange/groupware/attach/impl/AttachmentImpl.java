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

package com.openexchange.groupware.attach.impl;

import java.util.Date;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.util.AttachmentToolkit;

public class AttachmentImpl implements AttachmentMetadata {

	private Date creationDate;
	private int createdBy;

	private int attachedId;
	private int moduleId;

	private String filename;
	private String fileMIMEType;
	private long filesize;

	private boolean rtfFlag;

	private int id;

	private int folderId;

	private String comment;

	private String fileId;

	public AttachmentImpl(final AttachmentMetadata a) {
		AttachmentToolkit.copy(a,this);
	}

	public AttachmentImpl() {
	}

	@Override
    public int getCreatedBy() {
		return createdBy;
	}

	@Override
    public void setCreatedBy(final int createdBy) {
		this.createdBy = createdBy;
	}

	@Override
    public Date getCreationDate() {
		return creationDate;
	}

	@Override
    public void setCreationDate(final Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
    public String getFileMIMEType() {
		return fileMIMEType;
	}

	@Override
    public void setFileMIMEType(final String fileMIMEType) {
		this.fileMIMEType = fileMIMEType;
	}

	@Override
    public String getFilename() {
		return filename;
	}

	@Override
    public void setFilename(final String filename) {
		this.filename = filename;
	}

	@Override
    public long getFilesize() {
		return filesize;
	}

	@Override
    public void setFilesize(final long filesize) {
		this.filesize = filesize;
	}

	@Override
    public int getAttachedId() {
		return attachedId;
	}

	@Override
    public void setAttachedId(final int attachedId) {
		this.attachedId = attachedId;
	}

	@Override
    public boolean getRtfFlag() {
		return rtfFlag;
	}

	@Override
    public void setRtfFlag(final boolean rtfFlag) {
		this.rtfFlag = rtfFlag;
	}

	@Override
    public int getModuleId() {
		return moduleId;
	}

	@Override
    public void setModuleId(final int moduleId) {
		this.moduleId = moduleId;
	}

	@Override
    public int getId(){
		return id;
	}

	@Override
    public void setId(final int id){
		this.id=id;
	}

	@Override
	public int hashCode(){
		return id%1000;
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof AttachmentMetadata && ((AttachmentMetadata)o).getId() == id;
	}

	@Override
    public int getFolderId() {
		return folderId;
	}

	@Override
    public void setFolderId(final int folderId) {
		this.folderId = folderId;
	}

	@Override
    public String getComment() {
		return comment;
	}

	@Override
    public void setComment(final String comment) {
		this.comment = comment;
	}

	@Override
    public String getFileId() {
		return fileId;
	}

	@Override
    public void setFileId(final String fileId) {
		this.fileId = fileId;
	}


}
