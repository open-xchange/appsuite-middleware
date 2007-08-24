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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import com.openexchange.api2.OXException;
import com.openexchange.mail.MailException;
import com.openexchange.tools.mail.ContentType;

/**
 * MailContent
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailContent {

	protected static final class IgnoreCaseString {

		private final String s;

		public IgnoreCaseString(final String s) {
			this.s = s;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object cobj) {
			if (cobj == this) {
				return (true);
			} else if ((cobj instanceof IgnoreCaseString)) {
				return (s.equalsIgnoreCase(((IgnoreCaseString) cobj).s));
			} else if ((cobj instanceof String)) {
				return (s.equalsIgnoreCase((String) cobj));
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return s;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return s.toLowerCase().hashCode();
		}
	}

	private static final class MyIterator implements Iterator<Map.Entry<String, String>> {

		private final Iterator<Map.Entry<IgnoreCaseString, String>> iter;

		public MyIterator(final Iterator<Map.Entry<IgnoreCaseString, String>> iter) {
			this.iter = iter;
		}

		public boolean hasNext() {
			return iter.hasNext();
		}

		public Map.Entry<String, String> next() {
			final Map.Entry<IgnoreCaseString, String> e = iter.next();
			if (null == e) {
				return null;
			}
			return new MyEntry(e);
		}

		public void remove() {
			iter.remove();
		}
	}

	private static final class MyEntry implements Map.Entry<String, String> {

		private final Map.Entry<IgnoreCaseString, String> e;

		public MyEntry(final Map.Entry<IgnoreCaseString, String> e) {
			this.e = e;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Map$Entry#getKey()
		 */
		public String getKey() {
			return e.getKey().s;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Map$Entry#getValue()
		 */
		public String getValue() {
			return e.getValue();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Map$Entry#setValue(java.lang.Object)
		 */
		public String setValue(final String value) {
			return e.setValue(value);
		}

	}

	private static final Iterator<Map.Entry<String, String>> EMPTY_ITER = new Iterator<Map.Entry<String, String>>() {
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

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailContent.class);

	/**
	 * The <code>Content-Type</code> header
	 */
	private ContentType contentType;

	private boolean b_contentType;

	/**
	 * The disposition (either <code>null</code>, INLINE, or ATTACHMENT)
	 */
	private String disposition;

	private boolean b_disposition;

	/**
	 * The file name
	 */
	private String fileName;

	private boolean b_fileName;

	/**
	 * The headers (if not explicitely set in other fields)
	 */
	private Map<IgnoreCaseString, String> headers;

	private boolean b_headers;

	/**
	 * The size
	 */
	private int size = -1;

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
	protected MailContent() {
		super();
		try {
			contentType = new ContentType("text/plain; charset=us-ascii");
		} catch (final OXException e) {
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
		} catch (final OXException e) {
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
	 * @throws OXException
	 *             If content type is invalid or could not be parsed
	 */
	public void setContentType(final String contentType) throws OXException {
		this.contentType = new ContentType(contentType);
		b_contentType = true;
	}

	/**
	 * Gets the disposition
	 * 
	 * @return the disposition
	 */
	public String getDisposition() {
		return disposition;
	}

	/**
	 * @return <code>true</code> if disposition is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsDisposition() {
		return b_disposition;
	}

	/**
	 * Removes the disposition
	 */
	public void removeDisposition() {
		disposition = null;
		b_disposition = false;
	}

	/**
	 * Sets the disposition
	 * 
	 * @param disposition
	 *            the disposition to set
	 */
	public void setDisposition(final String disposition) {
		this.disposition = disposition;
		b_disposition = true;
	}

	/**
	 * Gets the fileName
	 * 
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
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
			headers = new HashMap<IgnoreCaseString, String>();
			b_headers = true;
		}
		headers.put(new IgnoreCaseString(name), value);
	}

	/**
	 * Adds a header
	 * 
	 * @param name
	 *            The header name
	 * @param value
	 *            The header value
	 */
	public void addHeaders(final Map<String, String> headers) {
		if (null == headers) {
			throw new IllegalArgumentException("Headers must not be null");
		}
		final int size = headers.size();
		if (size == 0) {
			return;
		} else if (null == this.headers) {
			this.headers = new HashMap<IgnoreCaseString, String>();
			b_headers = true;
		}
		final Iterator<Map.Entry<String, String>> iter = headers.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<String, String> e = iter.next();
			this.headers.put(new IgnoreCaseString(e.getKey()), e.getValue());
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
		return new MyIterator(headers.entrySet().iterator());
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
			return headers.get(new IgnoreCaseString(name));
		}
		return null;
	}

	/**
	 * Gets the size
	 * 
	 * @return the size
	 */
	public int getSize() {
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
	public void setSize(final int size) {
		this.size = size;
		b_size = true;
	}

	/**
	 * Gets the contentId
	 * 
	 * @return the contentId
	 */
	public String getContentId() {
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
	 * @param sequenceId the sequenceId to set
	 */
	public void setSequenceId(final String sequenceId) {
		this.sequenceId = sequenceId;
		b_sequenceId = true;
	}

	/**
	 * Gets the number of enclosed mail contents
	 * 
	 * @return The number of enclosed mail contents
	 */
	public abstract int getEnclosedCount() throws MailException;

	/**
	 * Gets the mail content located at given index or <code>null</code>
	 * 
	 * @param index
	 *            The index of desired mail content or <code>null</code>
	 * @return The mail content
	 */
	public abstract MailContent getEnclosedMailContent(final int index) throws MailException;

	/**
	 * Returns the content as a Java object dependent on underlying
	 * implementation.
	 * 
	 * @return The content as a Java object
	 * @throws MailException
	 *             If content cannot be returned as a Java object
	 */
	public abstract Object getContent() throws MailException;

	/**
	 * Returns an appropiate {@link DataHandler} for this mail content
	 * 
	 * @return an appropiate {@link DataHandler}
	 * @throws MailException
	 *             If an appropiate {@link DataHandler} cannot be returned
	 */
	public abstract DataHandler getDataHandler() throws MailException;

	/**
	 * Returns an input stream for this content
	 * 
	 * @return An input stream for this content
	 * @throws MailException
	 *             If no input stream could be returned
	 */
	public abstract InputStream getInputStream() throws MailException;

	/**
	 * Prepares this mail content to be put into cache; meaning to release all
	 * kept resources
	 * 
	 * @throws MailException
	 *             If preparation fails
	 */
	public abstract void prepareForCaching() throws MailException;
}
