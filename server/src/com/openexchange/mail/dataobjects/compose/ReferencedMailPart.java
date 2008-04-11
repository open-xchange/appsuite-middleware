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

package com.openexchange.mail.dataobjects.compose;

import static com.openexchange.mail.utils.MessageUtility.readStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import com.openexchange.configuration.ServerConfig;
import com.openexchange.groupware.upload.impl.AJAXUploadFile;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.MailPartHandler;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link ReferencedMailPart} - A {@link MailPart} implementation that points to
 * a referenced part in original mail.
 * <p>
 * Since a mail part causes troubles when its input stream is read multiple
 * times, corresponding data is either stored in an internal byte array or
 * copied as a temporary file to disk (depending on
 * {@link TransportConfig#getReferencedPartLimit()}). Therefore this part needs
 * special handling to ensure removal of temporary file when it is dispatched.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class ReferencedMailPart extends MailPart implements ComposedMailPart {

	private static final long serialVersionUID = 1097727980840011436L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ReferencedMailPart.class);

	protected static final int DEFAULT_BUF_SIZE = 0x2000;

	private static final int MB = 1048576;

	private transient DataSource dataSource;

	private transient Object cachedContent;

	private byte[] data;

	private File file;

	private String fileId;

	/**
	 * Initializes a new {@link ReferencedMailPart}
	 * 
	 * @param referencedPart
	 *            The referenced part
	 * @param session
	 *            The session
	 * @throws MailException
	 *             If a mail error occurs
	 */
	public ReferencedMailPart(final MailPart referencedPart, final Session session) throws MailException {
		try {
			handleReferencedPart(referencedPart, session);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
		setHeaders(referencedPart);
	}

	/**
	 * Constructor
	 * 
	 * @param sequenceId
	 *            The sequence ID
	 */
	public ReferencedMailPart(final String sequenceId) {
		setSequenceId(sequenceId);
	}

	/**
	 * Loads the referenced part<br>
	 * This is a convenience method that invokes
	 * {@link #loadReferencedPart(MailMessageParser, MailMessage, Session)} with
	 * the first parameter set to <code>null</code>
	 * 
	 * @param referencedMail
	 *            The original mail containing the referenced part
	 * @throws MailException
	 *             If referenced part cannot be loaded
	 */
	public String loadReferencedPart(final MailMessage referencedMail, final Session session) throws MailException {
		return loadReferencedPart(null, referencedMail, session);
	}

	/**
	 * Loads the referenced part
	 * 
	 * @param parserArg
	 *            The parser used to filter part
	 * @param referencedMail
	 *            The original mail containing the referenced part
	 * @throws MailException
	 *             If referenced part cannot be loaded
	 */
	public String loadReferencedPart(final MailMessageParser parserArg, final MailMessage referencedMail,
			final Session session) throws MailException {
		if (null != data || null != file) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Content already present. Loading aborted.");
			}
			return null;
		}
		try {
			MailMessageParser parser = parserArg;
			if (parser == null) {
				parser = new MailMessageParser();
			} else {
				parser.reset();
			}
			final MailPartHandler handler = new MailPartHandler(getSequenceId());
			parser.parseMailMessage(referencedMail, handler);
			if (handler.getMailPart() == null) {
				throw new MailException(MailException.Code.PART_NOT_FOUND, getSequenceId(), Long.valueOf(referencedMail
						.getMailId()), referencedMail.getFolder());
			}
			setHeaders(handler.getMailPart());
			return handleReferencedPart(handler.getMailPart(), session);
		} catch (final MailException e) {
			throw new MailException(e);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	/**
	 * Convenience method that loads all referenced mail parts contained in
	 * specified composed mail.
	 * 
	 * @param mail
	 *            The composed mail
	 * @param session
	 *            The session
	 * @return A list of {@link String} containing the IDs of loaded referenced
	 *         parts
	 * @throws MailException
	 *             If referenced parts cannot be loaded
	 */
	public static final List<String> loadReferencedParts(final ComposedMailMessage mail, final Session session)
			throws MailException {
		/*
		 * Load referenced parts
		 */
		final MailMessageParser parser = new MailMessageParser();
		final int count = mail.getEnclosedCount();
		final List<String> tempIds = new ArrayList<String>(4);
		for (int i = 0; i < count; i++) {
			final ComposedMailPart composedMailPart = (ComposedMailPart) mail.getEnclosedMailPart(i);
			if (ComposedMailPart.ComposedPartType.REFERENCE.equals(composedMailPart.getType())) {
				final String id = ((ReferencedMailPart) composedMailPart).loadReferencedPart(parser, mail
						.getReferencedMail(), session);
				parser.reset();
				if (id != null) {
					tempIds.add(id);
				}
			}
		}
		return tempIds;
	}

	private String handleReferencedPart(final MailPart referencedPart, final Session session) throws MailException,
			IOException {
		final long size = referencedPart.getSize();
		if (size <= TransportConfig.getReferencedPartLimit()) {
			copy2ByteArr(referencedPart.getInputStream());
			return null;
		}
		copy2File(referencedPart, session);
		if (LOG.isInfoEnabled()) {
			LOG.info(new StringBuilder("Referenced mail part exeeds ").append(
					Float.valueOf(TransportConfig.getReferencedPartLimit() / MB).floatValue()).append(
					"MB limit. A temporary disk copy has been created: ").append(file.getName()));
		}
		return fileId;
	}

	private static final File UPLOAD_DIR = new File(ServerConfig.getProperty(ServerConfig.Property.UploadDirectory));

	private static final String FILE_PREFIX = "openexchange";

	private int copy2File(final MailPart referencedPart, final Session session) throws MailException, IOException {
		int totalBytes = 0;
		{
			final BufferedInputStream in;
			{
				final InputStream inputStream = referencedPart.getInputStream();
				in = inputStream instanceof BufferedInputStream ? (BufferedInputStream) inputStream
						: new BufferedInputStream(inputStream);
			}
			final File tmpFile = File.createTempFile(FILE_PREFIX, null, UPLOAD_DIR);
			tmpFile.deleteOnExit();
			OutputStream out = null;
			try {
				out = new BufferedOutputStream(new FileOutputStream(tmpFile), DEFAULT_BUF_SIZE);
				final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
				int len;
				while ((len = in.read(bbuf)) != -1) {
					out.write(bbuf, 0, len);
					totalBytes += len;
				}
				out.flush();
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (final IOException e) {
						LOG.error(e.getLocalizedMessage(), e);
					}
				}
			}
			file = tmpFile;
		}
		final AJAXUploadFile uploadFile = new AJAXUploadFile(file, System.currentTimeMillis());
		fileId = plainStringToMD5(file.getName());
		session.putUploadedFile(fileId, uploadFile);
		return totalBytes;
	}

	private int copy2ByteArr(final InputStream inputStream) throws IOException {
		final UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_BUF_SIZE * 2);
		final BufferedInputStream in = inputStream instanceof BufferedInputStream ? (BufferedInputStream) inputStream
				: new BufferedInputStream(inputStream);
		final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
		int len;
		int totalBytes = 0;
		while ((len = in.read(bbuf)) != -1) {
			out.write(bbuf, 0, len);
			totalBytes += len;
		}
		out.flush();
		this.data = out.toByteArray();
		return totalBytes;
	}

	private void setHeaders(final MailPart referencedPart) {
		if (referencedPart.containsContentId()) {
			setContentId(referencedPart.getContentId());
		}
		setContentType(referencedPart.getContentType());
		setContentDisposition(referencedPart.getContentDisposition());
		setFileName(referencedPart.getFileName());
		setSize(referencedPart.getSize());
		final int count = referencedPart.getHeadersSize();
		final Iterator<Map.Entry<String, String>> iter = referencedPart.getHeadersIterator();
		for (int i = 0; i < count; i++) {
			final Map.Entry<String, String> e = iter.next();
			addHeader(e.getKey(), e.getValue());
		}
	}

	private DataSource getDataSource() throws MailException {
		/*
		 * Lazy creation
		 */
		if (null == dataSource) {
			try {
				if (data != null) {
					if (getContentType().isMimeType(MIMETypes.MIME_TEXT_ALL)
							&& getContentType().getCharsetParameter() == null) {
						/*
						 * Add default mail charset
						 */
						getContentType().setCharsetParameter(MailConfig.getDefaultMimeCharset());
					}
					return (dataSource = new MessageDataSource(data, getContentType().toString()));
				} else if (file != null) {
					if (getContentType().isMimeType(MIMETypes.MIME_TEXT_ALL)
							&& getContentType().getCharsetParameter() == null) {
						/*
						 * Add system charset
						 */
						getContentType().setCharsetParameter(
								System.getProperty("file.encoding", MailConfig.getDefaultMimeCharset()));
					}
					return (dataSource = new MessageDataSource(new FileInputStream(file), getContentType()));
				}
				throw new MailException(MailException.Code.NO_CONTENT);
			} catch (final MailConfigException e) {
				LOG.error(e.getLocalizedMessage(), e);
				dataSource = new MessageDataSource(new byte[0], "application/octet-stream");
			} catch (final IOException e) {
				LOG.error(e.getLocalizedMessage(), e);
				dataSource = new MessageDataSource(new byte[0], "application/octet-stream");
			}
		}
		return dataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailPart#getContent()
	 */
	@Override
	public Object getContent() throws MailException {
		if (cachedContent != null) {
			return cachedContent;
		}
		if (getContentType().isMimeType(MIMETypes.MIME_TEXT_ALL)) {
			if (data != null) {
				String charset = getContentType().getCharsetParameter();
				if (null == charset) {
					charset = MailConfig.getDefaultMimeCharset();
				}
				applyByteContent(charset);
				return cachedContent;
			} else if (file != null) {
				String charset = getContentType().getCharsetParameter();
				if (null == charset) {
					charset = System.getProperty("file.encoding", MailConfig.getDefaultMimeCharset());
				}
				applyFileContent(charset);
				return cachedContent;
			}
		}
		return null;
	}

	private void applyFileContent(final String charset) throws MailException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			cachedContent = readStream(fis, charset);
		} catch (final FileNotFoundException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	private void applyByteContent(final String charset) throws MailException {
		try {
			cachedContent = new String(data, charset);
		} catch (final UnsupportedEncodingException e) {
			throw new MailException(MailException.Code.ENCODING_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailPart#getDataHandler()
	 */
	@Override
	public DataHandler getDataHandler() throws MailException {
		return new DataHandler(getDataSource());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailPart#getEnclosedCount()
	 */
	@Override
	public int getEnclosedCount() throws MailException {
		return NO_ENCLOSED_PARTS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailPart#getEnclosedMailPart(int)
	 */
	@Override
	public MailPart getEnclosedMailPart(final int index) throws MailException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailPart#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws MailException {
		try {
			if (data != null) {
				return new ByteArrayInputStream(data);
			} else if (file != null) {
				return new FileInputStream(file);
			}
			throw new IllegalStateException("No content");
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailPart#loadContent()
	 */
	@Override
	public void loadContent() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailPart#prepareForCaching()
	 */
	@Override
	public void prepareForCaching() {
	}

	private static final String ALG_MD5 = "MD5";

	private static String plainStringToMD5(final String input) {
		final MessageDigest md;
		try {
			/*
			 * Choose MD5 (SHA1 is also possible)
			 */
			md = MessageDigest.getInstance(ALG_MD5);
		} catch (final NoSuchAlgorithmException e) {
			LOG.error("Unable to generate file ID", e);
			return input;
		}
		/*
		 * Reset
		 */
		md.reset();
		/*
		 * Update the digest
		 */
		try {
			md.update(input.getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			/*
			 * Should not occur since utf-8 is a known encoding in jsdk
			 */
			LOG.error("Unable to generate file ID", e);
			return input;
		}
		/*
		 * Here comes the hash
		 */
		final byte[] byteHash = md.digest();
		final StringBuilder resultString = new StringBuilder();
		for (int i = 0; i < byteHash.length; i++) {
			resultString.append(Integer.toHexString(0xF0 & byteHash[i]).charAt(0));
		}
		return resultString.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.transport.smtp.dataobjects.SMTPMailPart#getType()
	 */
	public ComposedPartType getType() {
		return ComposedMailPart.ComposedPartType.REFERENCE;
	}
}
