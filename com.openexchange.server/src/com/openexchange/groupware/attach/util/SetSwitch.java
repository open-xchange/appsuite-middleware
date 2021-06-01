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

package com.openexchange.groupware.attach.util;

import java.util.Date;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;

public class SetSwitch implements AttachmentField.AttachmentSwitch{

	private final AttachmentMetadata attachment;
	private Object value;

	public SetSwitch(final AttachmentMetadata attachment) {
		this.attachment = attachment;
	}

	public void setValue(final Object value) {
		this.value = value;
	}

	@Override
    public Object createdBy() {
		if (value == null) {
			value = Integer.valueOf(-1);
		}
		attachment.setCreatedBy(((Integer)value).intValue());
		return null;
	}

	@Override
    public Object id() {
		if (value == null) {
			value = Integer.valueOf(-1);
		}
		attachment.setId(((Integer)value).intValue());
		return null;
	}

	@Override
    public Object moduleId() {
		if (value == null) {
			value = Integer.valueOf(-1);
		}
		attachment.setModuleId(((Integer)value).intValue());
		return null;
	}

	@Override
    public Object attachedId() {
		if (value == null) {
			value = Integer.valueOf(-1);
		}
		attachment.setAttachedId(((Integer)value).intValue());
		return null;
	}

	@Override
    public Object fileSize() {
		if (value == null) {
			value = Integer.valueOf(0);
		}
		attachment.setFilesize(((Long)value).longValue());
		return null;
	}

	@Override
    public Object fileMIMEType() {
        if (value != null) {
            attachment.setFileMIMEType(value.toString());
        }
        return null;
	}

	@Override
    public Object creationDate() {
		attachment.setCreationDate((Date)value);
		return null;
	}

	@Override
    public Object folderId() {
		if (value == null) {
			value = Integer.valueOf(-1);
		}
		attachment.setFolderId(((Integer)value).intValue());
		return null;
	}

	@Override
    public Object fileName(){
		attachment.setFilename((String)value);
		return null;
	}

	@Override
    public Object rtfFlag(){
		if (value == null) {
			value = Boolean.FALSE;
		}
		attachment.setRtfFlag(((Boolean)value).booleanValue());
		return null;
	}

	@Override
    public Object comment() {
		attachment.setComment((String)value);
		return null;
	}

	@Override
    public Object fileId() {
		attachment.setFileId((String)value);
		return null;
	}

	@Override
	public Object checksum() {
	    attachment.setChecksum((String) value);
	    return null;
	}
}
