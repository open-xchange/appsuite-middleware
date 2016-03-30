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

package com.openexchange.user.copy.internal.attachment;

import java.util.Date;

public class Attachment {

    private int id;

	private Date creationDate;

	private int createdBy;

	private int attachedId;

	private int moduleId;

	private String filename;

	private String fileMIMEType;

	private long filesize;

	private int rtfFlag;

	private String comment;

	private String fileId;


	public Attachment() {
	    super();
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(final int createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(final Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getFileMIMEType() {
		return fileMIMEType;
	}

	public void setFileMIMEType(final String fileMIMEType) {
		this.fileMIMEType = fileMIMEType;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public long getFilesize() {
		return filesize;
	}

	public void setFilesize(final long filesize) {
		this.filesize = filesize;
	}

	public int getAttachedId() {
		return attachedId;
	}

	public void setAttachedId(final int attachedId) {
		this.attachedId = attachedId;
	}

	public int getRtfFlag() {
		return rtfFlag;
	}

	public void setRtfFlag(final int rtfFlag) {
		this.rtfFlag = rtfFlag;
	}

	public int getModuleId() {
		return moduleId;
	}

	public void setModuleId(final int moduleId) {
		this.moduleId = moduleId;
	}

	public int getId(){
		return id;
	}

	public void setId(final int id){
		this.id=id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(final String fileId) {
		this.fileId = fileId;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + attachedId;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + createdBy;
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
        result = prime * result + ((fileMIMEType == null) ? 0 : fileMIMEType.hashCode());
        result = prime * result + ((filename == null) ? 0 : filename.hashCode());
        result = prime * result + (int) (filesize ^ (filesize >>> 32));
        result = prime * result + id;
        result = prime * result + moduleId;
        result = prime * result + rtfFlag;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Attachment other = (Attachment) obj;
        if (attachedId != other.attachedId) {
            return false;
        }
        if (comment == null) {
            if (other.comment != null) {
                return false;
            }
        } else if (!comment.equals(other.comment)) {
            return false;
        }
        if (createdBy != other.createdBy) {
            return false;
        }
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (fileId == null) {
            if (other.fileId != null) {
                return false;
            }
        } else if (!fileId.equals(other.fileId)) {
            return false;
        }
        if (fileMIMEType == null) {
            if (other.fileMIMEType != null) {
                return false;
            }
        } else if (!fileMIMEType.equals(other.fileMIMEType)) {
            return false;
        }
        if (filename == null) {
            if (other.filename != null) {
                return false;
            }
        } else if (!filename.equals(other.filename)) {
            return false;
        }
        if (filesize != other.filesize) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (moduleId != other.moduleId) {
            return false;
        }
        if (rtfFlag != other.rtfFlag) {
            return false;
        }
        return true;
    }

}
