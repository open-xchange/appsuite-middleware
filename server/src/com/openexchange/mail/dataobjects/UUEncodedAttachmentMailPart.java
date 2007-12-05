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

package com.openexchange.mail.dataobjects;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;

import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link UUEncodedAttachmentMailPart}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class UUEncodedAttachmentMailPart extends MailPart {

	private static final long serialVersionUID = 8980473176008331679L;

	private final transient UUEncodedPart uuencPart;

	/**
	 * @param part
	 *            The uuencoded part
	 */
	public UUEncodedAttachmentMailPart(final UUEncodedPart uuencPart) {
		super();
		this.uuencPart = uuencPart;
	}

	@Override
	public Object getContent() throws MailException {
		return null;
	}

	@Override
	public DataHandler getDataHandler() throws MailException {
		try {
			final ContentType contentType;
			if (!containsContentType()) {
				contentType = getContentType();
			} else {
				String ct = MIMEType2ExtMap.getContentType(uuencPart.getFileName());
				if (ct == null || ct.length() == 0) {
					ct = MIMETypes.MIME_APPL_OCTET;
				}
				contentType = new ContentType(ct);
			}
			return new DataHandler(new MessageDataSource(uuencPart.getInputStream(), contentType.toString()));
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, new Object[0]);
		}
	}

	@Override
	public int getEnclosedCount() throws MailException {
		return NO_ENCLOSED_PARTS;
	}

	@Override
	public MailPart getEnclosedMailPart(final int index) throws MailException {
		return null;
	}

	@Override
	public InputStream getInputStream() throws MailException {
		return uuencPart.getInputStream();
	}

	@Override
	public void prepareForCaching() {
	}

}
