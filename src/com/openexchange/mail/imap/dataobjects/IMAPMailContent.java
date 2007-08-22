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

package com.openexchange.mail.imap.dataobjects;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailContent;
import com.openexchange.mail.imap.IMAPException;

/**
 * IMAPMailContent
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPMailContent extends MailContent {

	private static final String MIME_MULTIPART_ALL = "multipart/*";

	private Part part;

	private final boolean isMulti;

	/**
	 * Constructor
	 */
	public IMAPMailContent(final Part part) {
		if (null == part) {
			throw new IllegalArgumentException("Part must not be null");
		}
		this.part = part;
		isMulti = (getContentType().isMimeType(MIME_MULTIPART_ALL));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getContent()
	 */
	@Override
	public Object getContent() throws IMAPException {
		if (null == part) {
			throw new IllegalStateException("Underlying part is null");
		}
		try {
			return part.getContent();
		} catch (final IOException e) {
			throw new IMAPException(IMAPException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getDataHandler()
	 */
	@Override
	public DataHandler getDataHandler() throws IMAPException {
		if (null == part) {
			throw new IllegalStateException("Underlying part is null");
		}
		try {
			return part.getDataHandler();
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IMAPException {
		if (null == part) {
			throw new IllegalStateException("Underlying part is null");
		}
		try {
			return part.getInputStream();
		} catch (final IOException e) {
			throw new IMAPException(IMAPException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getEnclosedMailContent(int)
	 */
	@Override
	public MailContent getEnclosedMailContent(final int index) throws IMAPException {
		if (null == part) {
			throw new IllegalStateException("Underlying part is null");
		}
		if (isMulti) {
			try {
				return new IMAPMailContent(((Multipart) part.getContent()).getBodyPart(index));
			} catch (final MessagingException e) {
				throw IMAPException.handleMessagingException(e);
			} catch (final IOException e) {
				throw new IMAPException(IMAPException.Code.IO_ERROR, e, e.getLocalizedMessage());
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getEnclosedCount()
	 */
	@Override
	public int getEnclosedCount() throws IMAPException {
		if (null == part) {
			throw new IllegalStateException("Underlying part is null");
		}
		if (isMulti) {
			try {
				return ((Multipart) part.getContent()).getCount();
			} catch (final MessagingException e) {
				throw IMAPException.handleMessagingException(e);
			} catch (final IOException e) {
				throw new IMAPException(IMAPException.Code.IO_ERROR, e, e.getLocalizedMessage());
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#prepareForCaching()
	 */
	@Override
	public void prepareForCaching() throws MailException {
		/*
		 * Release part reference
		 */
		part = null;
	}

}
