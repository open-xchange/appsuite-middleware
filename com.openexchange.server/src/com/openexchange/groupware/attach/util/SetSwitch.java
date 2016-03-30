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
		if(value == null) {
			value = Integer.valueOf(-1);
		}
		attachment.setCreatedBy(((Integer)value).intValue());
		return null;
	}

	@Override
    public Object id() {
		if(value == null) {
			value = Integer.valueOf(-1);
		}
		attachment.setId(((Integer)value).intValue());
		return null;
	}

	@Override
    public Object moduleId() {
		if(value == null) {
			value = Integer.valueOf(-1);
		}
		attachment.setModuleId(((Integer)value).intValue());
		return null;
	}

	@Override
    public Object attachedId() {
		if(value == null) {
			value = Integer.valueOf(-1);
		}
		attachment.setAttachedId(((Integer)value).intValue());
		return null;
	}

	@Override
    public Object fileSize() {
		if(value == null) {
			value = Integer.valueOf(0);
		}
		attachment.setFilesize(((Long)value).longValue());
		return null;
	}

	@Override
    public Object fileMIMEType() {
        if(value != null) {
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
		if(value == null) {
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
		if(value == null) {
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
}
