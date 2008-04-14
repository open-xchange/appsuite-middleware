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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MailPart} - Abstract super class for all {@link MailPart} subclasses.
 * <p>
 * It's main purpose is to provide access to common part headers and part's
 * content.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailPart implements Serializable, Cloneable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 4688498122773765967L;

	/**
	 * The integer constant returned by {@link #getEnclosedCount()} if mail
	 * part's content type does not match <code>multipart/*</code> and
	 * therefore does not hold any enclosed parts.
	 */
	public static final int NO_ENCLOSED_PARTS = -1;

	/**
	 * {@link HeaderIterator} - Converts an instance of
	 * <code>java.util.Iterator&lt;HeaderName, String&gt;</code> to
	 * <code>java.util.Iterator&lt;String, String&gt;</code>
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 * 
	 */
	private static final class HeaderIterator implements Iterator<Map.Entry<String, String>> {

		private final Iterator<Map.Entry<HeaderName, String>> iter;

		public HeaderIterator(final Iterator<Map.Entry<HeaderName, String>> iter) {
			this.iter = iter;
		}

		public boolean hasNext() {
			return iter.hasNext();
		}

		public Map.Entry<String, String> next() {
			final Map.Entry<HeaderName, String> e = iter.next();
			if (null == e) {
				return null;
			}
			return new HeaderEntry(e);
		}

		public void remove() {
			iter.remove();
		}
	}

	/**
	 * {@link HeaderEntry} - Converts an instance of
	 * <code>java.util.Map.Entry&lt;HeaderName, String&gt;</code> to
	 * <code>java.util.Map.Entry&lt;String, String&gt;</code>
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 * 
	 */
	private static final class HeaderEntry implements Map.Entry<String, String> {

		private final Map.Entry<HeaderName, String> headerEntry;

		public HeaderEntry(final Map.Entry<HeaderName, String> headerEntry) {
			this.headerEntry = headerEntry;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Map$Entry#getKey()
		 */
		public String getKey() {
			return headerEntry.getKey().toString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Map$Entry#getValue()
		 */
		public String getValue() {
			return headerEntry.getValue();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Map$Entry#setValue(java.lang.Object)
		 */
		public String setValue(final String value) {
			return headerEntry.setValue(value);
		}

	}

	private static final transient Iterator<Map.Entry<String, String>> EMPTY_ITER = new Iterator<Map.Entry<String, String>>() {
		/**
		 * @return <tt>true</tt> if the iterator has more elements.
		 */
		public boolean hasNext() {
			return false;
		}

		/**
		 * @return The next element in the iteration.
		 */
		public Entry<String, String> next() {
			return null;
		}

		/**
		 * Removes from the underlying collection the last element returned by
		 * the iterator (optional operation).
		 */
		public void remove() {
		}

	};

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailPart.class);

	/**
	 * The <code>Content-Type</code> header
	 */
	private ContentType contentType;

	private boolean b_contentType;

	/**
	 * The disposition (either <code>null</code>, INLINE, or ATTACHMENT)
	 */
	private ContentDisposition contentDisposition;

	private boolean b_disposition;

	/**
	 * The file name
	 */
	private String fileName;

	private boolean b_fileName;

	/**
	 * The headers (if not explicitly set in other fields)
	 */
	private Map<HeaderName, String> headers;

	private boolean b_headers;

	/**
	 * The size
	 */
	private long size = -1;

	private boolean b_size;

	/**
	 * The <code>Content-ID</code> header used for inline images in HTML
	 * content
	 */
	private String contentId;

	private boolean b_contentId;

	/**
	 * The content's sequence ID inside message (something like <code>1.2</code>)
	 */
	private String sequenceId;

	private boolean b_sequenceId;

	/**
	 * Default constructor
	 */
	protected MailPart() {
		super();
		try {
			contentType = new ContentType("text/plain; charset=us-ascii");
			contentDisposition = new ContentDisposition();
		} catch (final MailException e) {
			/*
			 * Cannot occur
			 */
			LOG.error(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Gets the contentType
	 * 
	 * @return the contentType
	 */
	public ContentType getContentType() {
		if (!b_contentType) {
			final String ct = getHeader(MessageHeaders.HDR_CONTENT_TYPE);
			if (ct != null) {
				try {
					setContentType(new ContentType(ct));
				} catch (final MailException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		return contentType;
	}

	/**
	 * @return <code>true</code> if contentType is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsContentType() {
		return b_contentType;
	}

	/**
	 * Removes the contentType
	 */
	public void removeContentType() {
		try {
			contentType = new ContentType("text/plain; charset=us-ascii");
		} catch (final MailException e) {
			/*
			 * Cannot occur
			 */
			LOG.error(e.getLocalizedMessage(), e);
		}
		b_contentType = false;
	}

	/**
	 * Sets the contentType
	 * 
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(final ContentType contentType) {
		this.contentType = contentType;
		b_contentType = true;
	}

	/**
	 * Parses and sets the contentType
	 * 
	 * @param contentType
	 *            the contentType to parse
	 * @throws MailException
	 *             If content type is invalid or could not be parsed
	 */
	public void setContentType(final String contentType) throws MailException {
		this.contentType = new ContentType(contentType);
		b_contentType = true;
	}

	/**
	 * Gets the disposition
	 * 
	 * @return the disposition
	 */
	public ContentDisposition getContentDisposition() {
		if (!b_disposition) {
			final String disp = getHeader(MessageHeaders.HDR_DISPOSITION);
			if (disp != null) {
				try {
					setContentDisposition(new ContentDisposition(disp));
				} catch (final MailException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		return contentDisposition;
	}

	/**
	 * @return <code>true</code> if disposition is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsContentDisposition() {
		return b_disposition;
	}

	/**
	 * Removes the disposition
	 */
	public void removeContentDisposition() {
		contentDisposition = null;
		b_disposition = false;
	}

	/**
	 * Sets the disposition
	 * 
	 * @param disposition
	 *            the disposition to set
	 * @throws MailException
	 *             If content disposition is invalid or could not be parsed
	 */
	public void setContentDisposition(final String disposition) throws MailException {
		this.contentDisposition = new ContentDisposition(disposition);
		b_disposition = true;
	}

	/**
	 * Sets the disposition
	 * 
	 * @param disposition
	 *            the disposition to set
	 */
	public void setContentDisposition(final ContentDisposition disposition) {
		this.contentDisposition = disposition;
		b_disposition = true;
	}

	/**
	 * Gets the fileName
	 * 
	 * @return the fileName
	 */
	public String getFileName() {
		if (b_fileName) {
			return fileName;
		}
		String fn = contentDisposition.getFilenameParameter();
		if (fn == null) {
			fn = contentType.getParameter("name");
		}
		return fn;
	}

	/**
	 * @return <code>true</code> if fileName is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsFileName() {
		return b_fileName;
	}

	/**
	 * Removes the fileName
	 */
	public void removeFileName() {
		fileName = null;
		b_fileName = false;
	}

	/**
	 * Sets the fileName
	 * 
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(final String fileName) {
		this.fileName = fileName;
		if (null != this.fileName) {
			contentType.setParameter("name", fileName);
			contentDisposition.setFilenameParameter(fileName);
		}
		b_fileName = true;
	}

	/**
	 * Adds a header
	 * 
	 * @param name
	 *            The header name
	 * @param value
	 *            The header value
	 */
	public void addHeader(final String name, final String value) {
		if (null == name) {
			throw new IllegalArgumentException("Header name must not be null");
		} else if (value == null) {
			/*
			 * Don't need to put a null value
			 */
			return;
		} else if (null == headers) {
			headers = new HashMap<HeaderName, String>();
			b_headers = true;
		}
		headers.put(HeaderName.valueOf(name), value);
	}

	/**
	 * Adds a header map
	 * 
	 * @param headers
	 *            The header map
	 */
	public void addHeaders(final Map<String, String> headers) {
		if (null == headers) {
			throw new IllegalArgumentException("Headers must not be null");
		}
		final int size = headers.size();
		if (size == 0) {
			return;
		} else if (null == this.headers) {
			this.headers = new HashMap<HeaderName, String>();
			b_headers = true;
		}
		final Iterator<Map.Entry<String, String>> iter = headers.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<String, String> e = iter.next();
			this.headers.put(HeaderName.valueOf(e.getKey()), e.getValue());
		}
	}

	/**
	 * Adds a header map
	 * 
	 * @param headers
	 *            The header map
	 */
	public void addHeadersMap(final Map<HeaderName, String> headers) {
		if (null == headers) {
			throw new IllegalArgumentException("Headers must not be null");
		}
		final int size = headers.size();
		if (size == 0) {
			return;
		} else if (null == this.headers) {
			this.headers = new HashMap<HeaderName, String>();
			b_headers = true;
		}
		final Iterator<Map.Entry<HeaderName, String>> iter = headers.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<HeaderName, String> e = iter.next();
			this.headers.put(e.getKey(), e.getValue());
		}
	}

	/**
	 * @return <code>true</code> if headers is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsHeaders() {
		return b_headers;
	}

	/**
	 * Removes the headers
	 */
	public void removeHeaders() {
		headers = null;
		b_headers = false;
	}

	/**
	 * Gets the number of headers
	 * 
	 * @return The number of headers
	 */
	public int getHeadersSize() {
		if (null == headers) {
			return 0;
		}
		return headers.size();
	}

	/**
	 * Gets an instance of {@link Iterator} to iterate all headers
	 * 
	 * @return An instance of {@link Iterator} to iterate all headers
	 * @see #getHeadersSize()
	 */
	public Iterator<Map.Entry<String, String>> getHeadersIterator() {
		if (null == headers) {
			return EMPTY_ITER;
		}
		return new HeaderIterator(headers.entrySet().iterator());
	}

	/**
	 * Gets the header's value or <code>null</code>
	 * 
	 * @param name
	 *            The header name
	 * @return The header's value or <code>null</code>
	 */
	public String getHeader(final String name) {
		if (containsHeaders() && null != headers) {
			return headers.get(HeaderName.valueOf(name));
		}
		return null;
	}

	/**
	 * Gets the headers as a map
	 * 
	 * @return The header's value or <code>null</code>
	 */
	public Map<HeaderName, String> getHeaders() {
		if (containsHeaders() && null != headers) {
			return headers;
		}
		return null;
	}

	/**
	 * Gets the non-matching headers as a map
	 * 
	 * @param nonMatchingHeaders
	 *            The non-matching headers
	 * @return The non-matching headers as a map
	 */
	public Map<HeaderName, String> getNonMatchingHeaders(final String[] nonMatchingHeaders) {
		if (containsHeaders() && null != headers) {
			final Set<HeaderName> set = new HashSet<HeaderName>(nonMatchingHeaders.length);
			for (int i = 0; i < nonMatchingHeaders.length; i++) {
				set.add(HeaderName.valueOf(nonMatchingHeaders[i]));
			}
			final Map<HeaderName, String> retval = new HashMap<HeaderName, String>();
			final int size = headers.size();
			final Iterator<Map.Entry<HeaderName, String>> iter = headers.entrySet().iterator();
			for (int i = 0; i < size; i++) {
				final Map.Entry<HeaderName, String> e = iter.next();
				if (!set.contains(e.getKey())) {
					retval.put(HeaderName.valueOf(e.getKey().toString()), e.getValue());
				}
			}
			return retval;
		}
		return null;
	}

	/**
	 * Gets the matching headers as a map
	 * 
	 * @param matchingHeaders
	 *            The matching headers
	 * @return The matching headers as a map
	 */
	public Map<HeaderName, String> getMatchingHeaders(final String[] matchingHeaders) {
		if (containsHeaders() && null != headers) {
			final Set<HeaderName> set = new HashSet<HeaderName>(matchingHeaders.length);
			for (int i = 0; i < matchingHeaders.length; i++) {
				set.add(HeaderName.valueOf(matchingHeaders[i]));
			}
			final Map<HeaderName, String> retval = new HashMap<HeaderName, String>();
			final int size = headers.size();
			final Iterator<Map.Entry<HeaderName, String>> iter = headers.entrySet().iterator();
			for (int i = 0; i < size; i++) {
				final Map.Entry<HeaderName, String> e = iter.next();
				if (set.contains(e.getKey())) {
					retval.put(HeaderName.valueOf(e.getKey().toString()), e.getValue());
				}
			}
			return retval;
		}
		return null;
	}

	/**
	 * Removes the header if present
	 * 
	 * @param name
	 *            The header name
	 * @return The header's former value or <code>null</code> if not found
	 */
	public String removeHeader(final String name) {
		if (containsHeaders() && null != headers) {
			return headers.remove(HeaderName.valueOf(name));
		}
		return null;
	}

	/**
	 * Gets the size
	 * 
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @return <code>true</code> if size is set; otherwise <code>false</code>
	 */
	public boolean containsSize() {
		return b_size;
	}

	/**
	 * Removes the size
	 */
	public void removeSize() {
		size = -1;
		b_size = false;
	}

	/**
	 * Sets the size
	 * 
	 * @param size
	 *            the size to set
	 */
	public void setSize(final long size) {
		this.size = size;
		b_size = true;
	}

	/**
	 * Gets the contentId
	 * 
	 * @return the contentId
	 */
	public String getContentId() {
		if (!b_contentId) {
			final String cid = getHeader(MessageHeaders.HDR_CONTENT_ID);
			if (cid != null) {
				setContentId(cid);
			}
		}
		return contentId;
	}

	/**
	 * @return <code>true</code> if contentId is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsContentId() {
		return b_contentId;
	}

	/**
	 * Removes the contentId
	 */
	public void removeContentId() {
		contentId = null;
		b_contentId = false;
	}

	/**
	 * Sets the contentId
	 * 
	 * @param contentId
	 *            the contentId to set
	 */
	public void setContentId(final String contentId) {
		this.contentId = contentId;
		b_contentId = true;
	}

	/**
	 * Gets the sequenceId
	 * 
	 * @return the sequenceId
	 */
	public String getSequenceId() {
		return sequenceId;
	}

	/**
	 * @return <code>true</code> if sequenceId is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsSequenceId() {
		return b_sequenceId;
	}

	/**
	 * Removes the sequenceId
	 */
	public void removeSequenceId() {
		sequenceId = null;
		b_sequenceId = false;
	}

	/**
	 * Sets the sequenceId
	 * 
	 * @param sequenceId
	 *            the sequenceId to set
	 */
	public void setSequenceId(final String sequenceId) {
		this.sequenceId = sequenceId;
		b_sequenceId = true;
	}

	@Override
	public Object clone() {
		try {
			final MailPart clone = (MailPart) super.clone();
			if (contentType != null) {
				clone.contentType = new ContentType(contentType.toString());
			}
			if (null != headers) {
				clone.headers = new HashMap<HeaderName, String>(headers);
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			LOG.error(e.getLocalizedMessage(), e);
			throw new InternalError(e.getLocalizedMessage());
		} catch (MailException e) {
			LOG.error(e.getLocalizedMessage(), e);
			throw new InternalError(e.getLocalizedMessage());
		}
	}

	/**
	 * Checks if part's MIME type is <code>multipart/*</code>
	 * 
	 * @return <code>true</code> if part holds enclosed parts; otherwise
	 *         <code>false</code>
	 * @throws MailException
	 *             If check fails
	 */
	public boolean hasEnclosedParts() throws MailException {
		return getEnclosedCount() != NO_ENCLOSED_PARTS;
	}

	/**
	 * Returns the part's content as a Java object dependent on underlying
	 * implementation. <br>
	 * This method is not applicable if part's MIME type is
	 * <code>multipart/*</code>
	 * 
	 * @return The content as a Java object or <code>null</code> if not
	 *         applicable
	 * @throws MailException
	 *             If content cannot be returned as a Java object
	 */
	public abstract Object getContent() throws MailException;

	/**
	 * Returns an appropriate {@link DataHandler} for this mail part. <br>
	 * This method is not applicable if part's MIME type is
	 * <code>multipart/*</code>
	 * 
	 * @return an appropriate {@link DataHandler} or <code>null</code> if not
	 *         applicable
	 * @throws MailException
	 *             If an appropriate {@link DataHandler} cannot be returned
	 */
	public abstract DataHandler getDataHandler() throws MailException;

	/**
	 * Returns an input stream for this part. <br>
	 * This method is not applicable if part's MIME type is
	 * <code>multipart/*</code>
	 * 
	 * @return An input stream for this part or <code>null</code> if not
	 *         applicable
	 * @throws MailException
	 *             If no input stream could be returned
	 */
	public abstract InputStream getInputStream() throws MailException;

	/**
	 * Gets the number of enclosed mail parts. <br>
	 * This method is only applicable if part's MIME type is
	 * <code>multipart/*</code>
	 * 
	 * @see #NO_ENCLOSED_PARTS
	 * @return The number of enclosed mail parts or {@link #NO_ENCLOSED_PARTS}
	 *         if not applicable
	 */
	public abstract int getEnclosedCount() throws MailException;

	/**
	 * Gets the mail part located at given index. <br>
	 * This method is only applicable if part's MIME type is
	 * <code>multipart/*</code>
	 * 
	 * @param index
	 *            The index of desired mail part or <code>null</code> if not
	 *            applicable
	 * @return The mail part
	 */
	public abstract MailPart getEnclosedMailPart(final int index) throws MailException;

	/**
	 * Ensures that the part's content is loaded, thus this part is independent
	 * of the original.
	 * <p>
	 * This method is intended for mailing systems that read the contents
	 * stepwise on demand. If dealing with such a mail part with its underlying
	 * connection closed, the part's content is no more accessible. Otherwise
	 * this method may be implemented with an empty body.
	 * <p>
	 * Moreover the loaded content is no more discarded when
	 * {@link #prepareForCaching()} is invoked.
	 * 
	 * @throws MailException
	 *             If loading part's content fails
	 */
	public abstract void loadContent() throws MailException;

	/**
	 * Writes complete part's data into given output stream
	 * 
	 * @param out
	 *            The output stream to write to
	 * @throws MailException
	 *             If writing to output stream fails
	 */
	public void writeTo(final OutputStream out) throws MailException {
		InputStream in = null;
		try {
			in = getInputStream();
			if (null == in) {
				throw new MailException(MailException.Code.NO_CONTENT);
			}
			final byte[] buf = new byte[8192];
			int count = -1;
			while ((count = in.read(buf, 0, buf.length)) != -1) {
				out.write(buf, 0, count);
			}
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	private static final String US_ASCII = "US-ASCII";

	/**
	 * Gets the mail part's source
	 * 
	 * @return The mail part's source
	 * @throws MailException
	 *             If mail part's source cannot be returned
	 */
	public String getSource() throws MailException {
		byte[] data;
		{
			final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(4096);
			writeTo(out);
			data = out.toByteArray();
		}
		try {
			return new String(data, US_ASCII);
		} catch (final UnsupportedEncodingException e) {
			/*
			 * Cannot occur
			 */
			throw new MailException(MailException.Code.ENCODING_ERROR, e, US_ASCII);
		}
	}

	/**
	 * Prepares this mail part to be put into cache; meaning to release all kept
	 * resources
	 */
	public abstract void prepareForCaching();

}
