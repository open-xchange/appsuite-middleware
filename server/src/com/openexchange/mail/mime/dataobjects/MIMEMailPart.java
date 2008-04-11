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

package com.openexchange.mail.mime.dataobjects;

import static com.openexchange.mail.mime.ContentType.isMimeType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MIMEMailPart} - Represents a MIME part as per RFC 822.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MIMEMailPart extends MailPart {

	private static final long serialVersionUID = -1142595512657302179L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MIMEMailPart.class);

	private static final String ERR_NULL_PART = "Underlying part is null";

	/**
	 * The delegate {@link Part} object
	 */
	private transient Part part;

	/**
	 * Cached instance of {@link Multipart}
	 */
	private transient Multipart multipart;

	/**
	 * Whether this part's content is of MIME type <code>multipart/*</code>
	 */
	private final boolean isMulti;

	/**
	 * Constructor - Only applies specified part, but does not set any
	 * attributes
	 */
	public MIMEMailPart(final Part part) {
		this.part = part;
		if (null != part) {
			boolean tmp = false;
			try {
				tmp = isMimeType(part.getContentType(), MIMETypes.MIME_MULTIPART_ALL);
			} catch (final MailException e) {
				LOG.error(e.getMessage(), e);
			} catch (final MessagingException e) {
				LOG.error(e.getMessage(), e);
			}
			isMulti = tmp;
		} else {
			isMulti = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getContent()
	 */
	@Override
	public Object getContent() throws MailException {
		if (null == part) {
			throw new IllegalStateException(ERR_NULL_PART);
		} else if (isMulti) {
			return null;
		}
		try {
			final Object obj = part.getContent();
			if (obj instanceof MimeMessage) {
				return MIMEMessageConverter.convertMessage((MimeMessage) obj);
			} else if (obj instanceof Part) {
				return MIMEMessageConverter.convertPart((Part) obj);
			} else {
				return obj;
			}
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getDataHandler()
	 */
	@Override
	public DataHandler getDataHandler() throws MailException {
		if (null == part) {
			throw new IllegalStateException(ERR_NULL_PART);
		} else if (isMulti) {
			return null;
		}
		try {
			return part.getDataHandler();
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws MailException {
		if (null == part) {
			throw new IllegalStateException(ERR_NULL_PART);
		} else if (isMulti) {
			return null;
		}
		try {
			try {
				return part.getInputStream();
			} catch (final IOException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder(256).append("Part's input stream could not be obtained: ").append(
							e.getMessage() == null ? "<no error message given>" : e.getMessage()).append(
							". Trying to read from part's raw input stream instead").toString(), e);
				}
				if (part instanceof MimeBodyPart) {
					return ((MimeBodyPart) part).getRawInputStream();
				} else if (part instanceof MimeMessage) {
					return ((MimeMessage) part).getRawInputStream();
				}
				throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
			} catch (final MessagingException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder(256).append("Part's input stream could not be obtained: ").append(
							e.getMessage() == null ? "<no error message given>" : e.getMessage()).append(
							". Trying to read from part's raw input stream instead").toString(), e);
				}
				if (part instanceof MimeBodyPart) {
					return ((MimeBodyPart) part).getRawInputStream();
				} else if (part instanceof MimeMessage) {
					return ((MimeMessage) part).getRawInputStream();
				}
				throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
			}
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getEnclosedMailContent(int)
	 */
	@Override
	public MailPart getEnclosedMailPart(final int index) throws MailException {
		if (null == part) {
			throw new IllegalStateException(ERR_NULL_PART);
		} else if (isMulti) {
			try {
				if (null == multipart) {
					multipart = (Multipart) part.getContent();
				}
				return MIMEMessageConverter.convertPart(multipart.getBodyPart(index));
			} catch (final MessagingException e) {
				throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
			} catch (final IOException e) {
				throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
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
	public int getEnclosedCount() throws MailException {
		if (null == part) {
			throw new IllegalStateException(ERR_NULL_PART);
		} else if (isMulti) {
			try {
				if (null == multipart) {
					multipart = (Multipart) part.getContent();
				}
				return multipart.getCount();
			} catch (final MessagingException e) {
				throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
			} catch (final IOException e) {
				throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
			}
		}
		return NO_ENCLOSED_PARTS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailPart#writeTo(java.io.OutputStream)
	 */
	@Override
	public void writeTo(final OutputStream out) throws MailException {
		try {
			part.writeTo(out);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#prepareForCaching()
	 */
	@Override
	public void prepareForCaching() {
		/*
		 * Release references
		 */
		multipart = null;
		part = null;
	}

	@Override
	public void loadContent() throws MailException {
		if (null == part) {
			throw new IllegalStateException(ERR_NULL_PART);
		}
		try {
			if (part instanceof MimeBodyPart) {
				final Object content = part.getContent();
				if (content instanceof MimeMessage) {
					/*
					 * Special treatment for message/rfc822 data
					 */
					final MimeBodyPart newPart = new MimeBodyPart();
					final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(4096);
					((MimeMessage) content).writeTo(out);
					newPart.setContent(new MimeMessage(MIMEDefaultSession.getDefaultSession(),
							new UnsynchronizedByteArrayInputStream(out.toByteArray())), MIMETypes.MIME_MESSAGE_RFC822);
					part = newPart;
				} else if (content instanceof Multipart) {
					/*
					 * Force to call MimeMultipart.parse()
					 */
					((Multipart) content).getCount();
				} else {
					final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(4096);
					((MimeBodyPart) part).writeTo(out);
					part = new MimeBodyPart(new UnsynchronizedByteArrayInputStream(out.toByteArray()));
				}
			} else if (part instanceof MimeMessage) {
				part = new MimeMessage(MIMEDefaultSession.getDefaultSession(), ((MimeMessage) part).getRawInputStream());
			}
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}
}
