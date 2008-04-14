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

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.datasource.MessageDataSource;
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
	 * Indicates whether content has been loaded via {@link #loadContent()} or
	 * not
	 */
	private boolean contentLoaded;

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
		if (null == part) {
			throw new IllegalStateException(ERR_NULL_PART);
		}
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
		if (!contentLoaded) {
			multipart = null;
			part = null;
		}
	}

	@Override
	public void loadContent() throws MailException {
		if (null == part) {
			throw new IllegalStateException(ERR_NULL_PART);
		}
		try {
			if (part instanceof MimeBodyPart) {
				final ContentType contentType = new ContentType(part.getContentType());
				if (contentType.isMimeType(MIMETypes.MIME_MESSAGE_RFC822)) {
					/*
					 * Compose a new body part with message/rfc822 data
					 */
					final MimeBodyPart newPart = new MimeBodyPart();
					newPart.setContent(new MimeMessage(MIMEDefaultSession.getDefaultSession(),
							getInputStreamFromPart((Message) part.getContent())), MIMETypes.MIME_MESSAGE_RFC822);
					part = newPart;
					contentLoaded = true;
				} else if (contentType.isMimeType(MIMETypes.MIME_MULTIPART_ALL)) {
					/*
					 * Compose a new body part with multipart/* data
					 */
					final MimeBodyPart newPart = new MimeBodyPart();
					newPart.setContent(new MimeMultipart(new MessageDataSource(getBytesFromMultipart((Multipart) part
							.getContent()), contentType.toString())));
					part = newPart;
					multipart = null;
					contentLoaded = true;
				} else {
					part = new MimeBodyPart(getInputStreamFromPart(part));
					contentLoaded = true;
				}
			} else if (part instanceof MimeMessage) {
				part = new MimeMessage(MIMEDefaultSession.getDefaultSession(), getInputStreamFromPart(part));
				contentLoaded = true;
			}
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	/**
	 * Gets the input stream of specified part's raw data.
	 * 
	 * @param part
	 *            Either a message or a body part
	 * @return The input stream of specified part's raw data (with the optional
	 *         empty starting line omitted)
	 * @throws IOException
	 *             If an I/O error occurs
	 * @throws MessagingException
	 *             If a messaging error occurs
	 */
	private static InputStream getInputStreamFromPart(final Part part) throws IOException, MessagingException {
		return new UnsynchronizedByteArrayInputStream(getBytesFromPart(part));
	}

	/**
	 * Gets the bytes of specified part's raw data.
	 * 
	 * @param part
	 *            Either a message or a body part
	 * @return The bytes of specified part's raw data (with the optional empty
	 *         starting line omitted)
	 * @throws IOException
	 *             If an I/O error occurs
	 * @throws MessagingException
	 *             If a messaging error occurs
	 */
	private static byte[] getBytesFromPart(final Part part) throws IOException, MessagingException {
		byte[] data;
		{
			final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(4096);
			part.writeTo(out);
			data = out.toByteArray();
		}
		return stripEmptyStartingLine(data);
	}

	/**
	 * Gets the bytes of specified multipart's raw data.
	 * 
	 * @param multipart
	 *            A multipart object
	 * @return The bytes of specified multipart's raw data (with the optional
	 *         empty starting line omitted)
	 * @throws IOException
	 *             If an I/O error occurs
	 * @throws MessagingException
	 *             If a messaging error occurs
	 */
	private static byte[] getBytesFromMultipart(final Multipart multipart) throws IOException, MessagingException {
		byte[] data;
		{
			final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(4096);
			multipart.writeTo(out);
			data = out.toByteArray();
		}
		return stripEmptyStartingLine(data);
	}

	/**
	 * Strips the possible empty starting line from specified byte array
	 * 
	 * @param data
	 *            The byte array
	 * @return The stripped byte array
	 */
	private static byte[] stripEmptyStartingLine(final byte[] data) {
		/*
		 * Starts with an empty line?
		 */
		int start = 0;
		if (data[start] == '\r') {
			start++;
		}
		if (data[start] == '\n') {
			start++;
		}
		if (start > 0) {
			final byte[] data0 = new byte[data.length - start];
			System.arraycopy(data, start, data0, 0, data0.length);
			return data0;
		}
		return data;
	}
}
