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

package com.openexchange.groupware.attach;

import java.util.Date;

public interface AttachmentMetadata {

    public abstract int getCreatedBy();

    public abstract void setCreatedBy(int createdBy);

    public abstract Date getCreationDate();

    public abstract void setCreationDate(Date creationDate);

    public abstract String getFileMIMEType();

    public abstract void setFileMIMEType(String fileMIMEType);

    public abstract String getFilename();

    public abstract void setFilename(String filename);

    public abstract long getFilesize();

    public abstract void setFilesize(long filesize);

    public abstract int getAttachedId();

    public abstract void setAttachedId(int objectId);

    public abstract boolean getRtfFlag();

    public abstract void setRtfFlag(boolean rtfFlag);

    public abstract int getModuleId();

    public abstract void setModuleId(int moduleId);

    public abstract int getId();

    public abstract void setId(int id);

    public abstract void setFolderId(int folderId);

    public abstract int getFolderId();

    public abstract void setComment(String string);

    public abstract String getComment();

    public abstract void setFileId(String string);

    public abstract String getFileId();

    public void setAttachmentBatch(AttachmentBatch batch);

    public AttachmentBatch getAttachmentBatch();

    public abstract String getChecksum();

    public abstract void setChecksum(String checksum);

}
