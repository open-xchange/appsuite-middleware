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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.internet.MimeUtility;

import com.openexchange.ajax.Infostore;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart.ComposedPartType;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.session.Session;

/**
 * {@link InfostoreDocumentMailPart} - A {@link MailPart} implementation that
 * provides the input stream to an infostore document
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class InfostoreDocumentMailPart extends MailPart implements ComposedMailPart {

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(InfostoreDocumentMailPart.class);

	private transient DataSource dataSource;

	private transient InputStream docInputSream;

	private transient Object cachedContent;

	/**
	 * Constructor
	 * 
	 * @param documentId
	 *            The document's unique ID
	 * @param session
	 *            The session providing needed user data
	 * @throws MailException
	 *             If infostore document cannot be read
	 */
	public InfostoreDocumentMailPart(final int documentId, final Session session) throws MailException {
		super();
		try {
			final InfostoreFacade db = Infostore.FACADE;
			final Context ctx;
			try {
				ctx = ContextStorage.getStorageContext(session.getContextId());
			} catch (final ContextException e1) {
				throw new MailException(e1);
			}
			final User u = UserStorage.getStorageUser(session.getUserId(), ctx);
			final DocumentMetadata docMeta = db.getDocumentMetadata(documentId, InfostoreFacade.CURRENT_VERSION, ctx,
					u, UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx));
			setSize(docMeta.getFileSize());
			final String docMIMEType = docMeta.getFileMIMEType();
			setContentType(docMIMEType == null || docMIMEType.length() == 0 ? MIMETypes.MIME_APPL_OCTET : docMeta
					.getFileMIMEType());
			try {
				setFileName(MimeUtility.encodeText(docMeta.getFileName(), MailConfig.getDefaultMimeCharset(), "Q"));
			} catch (final UnsupportedEncodingException e) {
				setFileName(docMeta.getFileName());
			}
			docInputSream = db.getDocument(documentId, InfostoreFacade.CURRENT_VERSION, ctx, u,
					UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx));
		} catch (final OXException e) {
			throw new MailException(e);
		}
	}

	private DataSource getDataSource() throws MailException {
		/*
		 * Lazy creation
		 */
		if (null == dataSource) {
			try {
				dataSource = new MessageDataSource(docInputSream, getContentType());
			} catch (final UnsupportedEncodingException e) {
				throw new MailException(MailException.Code.ENCODING_ERROR, e, e.getMessage());
			} catch (final IOException e) {
				throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
			} finally {
				try {
					docInputSream.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
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
		if (getContentType().isMimeType("text/*")) {
			String charset = getContentType().getCharsetParameter();
			if (charset == null) {
				charset = "US-ASCII";
			}
			try {
				cachedContent = readStream(docInputSream, charset);
			} catch (final FileNotFoundException e) {
				throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
			} catch (final IOException e) {
				throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
			} finally {
				if (docInputSream != null) {
					try {
						docInputSream.close();
					} catch (final IOException e) {
						LOG.error(e.getLocalizedMessage(), e);
					}
					docInputSream = null;
				}
			}
			return cachedContent;
		}
		return null;
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
		return docInputSream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailPart#prepareForCaching()
	 */
	@Override
	public void prepareForCaching() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.transport.smtp.dataobjects.SMTPMailPart#getType()
	 */
	public ComposedPartType getType() {
		return ComposedMailPart.ComposedPartType.DOCUMENT;
	}
}
