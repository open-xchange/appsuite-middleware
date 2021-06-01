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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;

public class GetSwitch implements AttachmentField.AttachmentSwitch {

	private final AttachmentMetadata attachment;

	public GetSwitch(final AttachmentMetadata attachment) {
		this.attachment = attachment;
	}

	@Override
    public Object createdBy() {
		return I(attachment.getCreatedBy());
	}

	@Override
    public Object id() {
		return I(attachment.getId());
	}

	@Override
    public Object moduleId() {
		return I(attachment.getModuleId());
	}

	@Override
    public Object attachedId() {
		return I(attachment.getAttachedId());
	}

	@Override
    public Object fileSize() {
		return L(attachment.getFilesize());
	}

	@Override
    public Object fileMIMEType() {
		return attachment.getFileMIMEType();
	}

	@Override
    public Object creationDate() {
		return attachment.getCreationDate();
	}

	@Override
    public Object folderId() {
		return I(attachment.getFolderId());
	}

	@Override
    public Object fileName(){
		return attachment.getFilename();
	}

	@Override
    public Object rtfFlag(){
		return attachment.getRtfFlag() ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
    public Object comment() {
		return attachment.getComment();
	}

	@Override
    public Object fileId() {
		return attachment.getFileId();
	}

	@Override
	public Object checksum() {
	    return attachment.getChecksum();
	}

}
