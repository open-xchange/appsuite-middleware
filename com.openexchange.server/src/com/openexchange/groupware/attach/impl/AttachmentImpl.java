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

package com.openexchange.groupware.attach.impl;

import java.util.Date;
import com.openexchange.groupware.attach.AttachmentBatch;
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

    private AttachmentBatch batch;

    private String checksum;

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

    @Override
    public void setAttachmentBatch(AttachmentBatch batch) {
        this.batch = batch;
    }

    @Override
    public AttachmentBatch getAttachmentBatch() {
        return batch;
    }

    @Override
    public String getChecksum() {
        return checksum;
    }

    @Override
    public void setChecksum(String checksum ) {
        this.checksum = checksum;
    }

}
