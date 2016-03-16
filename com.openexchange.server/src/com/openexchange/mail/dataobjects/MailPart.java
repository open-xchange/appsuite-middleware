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

package com.openexchange.mail.dataobjects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.activation.DataHandler;
import javax.mail.MessageRemovedException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MailPart} - Abstract super class for all {@link MailPart} subclasses.
 * <p>
 * It's main purpose is to provide access to common part headers and part's content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailPart implements Serializable, Cloneable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 4688498122773765967L;

    /**
     * The integer constant returned by {@link #getEnclosedCount()} if mail part's content type does not match <code>multipart/*</code> and
     * therefore does not hold any enclosed parts.
     */
    public static final int NO_ENCLOSED_PARTS = -1;

    private static final transient Iterator<Map.Entry<String, String>> EMPTY_ITER = new Iterator<Map.Entry<String, String>>() {

        /**
         * @return <tt>true</tt> if the iterator has more elements.
         */
        @Override
        public boolean hasNext() {
            return false;
        }

        /**
         * @return The next element in the iteration.
         */
        @Override
        public Entry<String, String> next() {
            return null;
        }

        /**
         * Removes from the underlying collection the last element returned by the iterator (optional operation).
         */
        @Override
        public void remove() {
            // Nothing to remove
        }

    };

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailPart.class);

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
    private HeaderCollection headers;

    private boolean b_headers;

    /**
     * The size
     */
    private long size = -1;

    private boolean b_size;

    /**
     * The <code>Content-ID</code> header used for inline images in HTML content
     */
    private String contentId;

    private boolean b_contentId;

    /**
     * The content's sequence ID inside message (something like <code>1.2</code> )
     */
    private String sequenceId;

    private boolean b_sequenceId;

    /**
     * The message reference (on reply or forward)
     */
    private MailPath msgref;

    private boolean b_msgref;

    /**
     * Default constructor
     */
    protected MailPart() {
        super();
        contentType = new ContentType();
        contentType.setPrimaryType("text");
        contentType.setSubType("plain");
        contentType.setCharsetParameter("us-ascii");
        contentDisposition = new ContentDisposition();
    }

    /**
     * Gets the content type
     *
     * @return the content type
     */
    public ContentType getContentType() {
        if (!b_contentType) {
            final String ct = getFirstHeader(MessageHeaders.HDR_CONTENT_TYPE);
            if (ct != null) {
                try {
                    setContentType(new ContentType(ct));
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
        }
        return contentType;
    }

    /**
     * @return <code>true</code> if content type is set; otherwise <code>false</code>
     */
    public boolean containsContentType() {
        return b_contentType || containsHeader(MessageHeaders.HDR_CONTENT_TYPE);
    }

    /**
     * Removes the content type
     */
    public void removeContentType() {
        try {
            contentType = new ContentType("text/plain; charset=us-ascii");
            if (headers != null) {
                headers.setHeader(MessageHeaders.HDR_CONTENT_TYPE, "text/plain; charset=us-ascii");
            }
        } catch (final OXException e) {
            /*
             * Cannot occur
             */
            LOG.error("", e);
        }
        b_contentType = false;
    }

    /**
     * Sets the content type
     *
     * @param contentType the contentType to set
     */
    public void setContentType(final ContentType contentType) {
        this.contentType = contentType;
        b_contentType = true;
    }

    /**
     * Parses and sets the content type
     *
     * @param contentType the content type to parse
     * @throws OXException If content type is invalid or could not be parsed
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
    public ContentDisposition getContentDisposition() {
        if (!b_disposition) {
            final String disp = getFirstHeader(MessageHeaders.HDR_DISPOSITION);
            if (disp != null) {
                try {
                    setContentDisposition(new ContentDisposition(disp));
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
        }
        return contentDisposition;
    }

    /**
     * @return <code>true</code> if disposition is set; otherwise <code>false</code>
     */
    public boolean containsContentDisposition() {
        return b_disposition || containsHeader(MessageHeaders.HDR_DISPOSITION);
    }

    /**
     * Removes the disposition
     */
    public void removeContentDisposition() {
        contentDisposition = null;
        removeHeader(MessageHeaders.HDR_DISPOSITION);
        b_disposition = false;
    }

    /**
     * Sets the disposition
     *
     * @param disposition the disposition to set
     * @throws OXException If content disposition is invalid or could not be parsed
     */
    public void setContentDisposition(final String disposition) throws OXException {
        contentDisposition = new ContentDisposition(disposition);
        b_disposition = true;
    }

    /**
     * Sets the disposition
     *
     * @param disposition the disposition to set
     */
    public void setContentDisposition(final ContentDisposition disposition) {
        contentDisposition = disposition;
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
            fn = contentType.getNameParameter();
        }
        return fn;
    }

    /**
     * @return <code>true</code> if fileName is set; otherwise <code>false</code>
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
     * @param fileName the fileName to set
     */
    public void setFileName(final String fileName) {
        this.fileName = fileName;
        if (null != this.fileName) {
            contentType.setNameParameter(fileName);
            contentDisposition.setFilenameParameter(fileName);
        }
        b_fileName = true;
    }

    /**
     * Adds a header
     *
     * @param name The header name
     * @param value The header value
     */
    public void addHeader(final String name, final String value) {
        if (null == value) {
            return;
        }
        if (null == headers) {
            headers = new HeaderCollection();
            b_headers = true;
        }
        headers.addHeader(name, value);
    }

    /**
     * Sets a header
     *
     * @param name The header name
     * @param value The header value
     */
    public void setHeader(final String name, final String value) {
        if (null == value) {
            return;
        }
        if (null == headers) {
            headers = new HeaderCollection();
            b_headers = true;
        }
        headers.setHeader(name, value);
    }

    /**
     * Adds a header collection
     *
     * @param headers The header collection
     */
    public void addHeaders(final HeaderCollection headers) {
        if (null == headers || headers.isEmpty()) {
            return;
        } else if (null == this.headers) {
            this.headers = new HeaderCollection();
            b_headers = true;
        }
        this.headers.addHeaders(headers);
    }

    /**
     * @return <code>true</code> if headers is set; otherwise <code>false</code>
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
        return headers.getAllHeaders();
    }

    /**
     * Checks for a header entry for specified header
     *
     * @param name The header name
     * @return <code>true</code> if a header entry exists for specified header; otherwise <code>false</code>
     */
    public boolean containsHeader(final String name) {
        if (null == headers) {
            return false;
        }
        return headers.containsHeader(name);
    }

    /**
     * Gets all the values for the specified header. Returns null if no headers with the specified name exist.
     *
     * @param name The header name
     * @return The header values or <code>null</code>
     */
    public String[] getHeader(final String name) {
        if (containsHeaders() && (null != headers)) {
            return headers.getHeader(name);
        }
        return null;
    }

    /**
     * Gets the first header for specified header name.
     * <p>
     * This is a convenience method that invokes {@link #getHeader(String, String)} with the latter parameter set to <code>null</code>.
     *
     * @param name The header name
     * @return The header's first value or <code>null</code>
     */
    public String getFirstHeader(final String name) {
        return getHeader(name, null);
    }

    /**
     * Gets all the headers for this header name, returned as a single String, with headers separated by the delimiter. If the delimiter is
     * <code>null</code>, only the first header is returned. Returns null if no headers with the specified name exist.
     *
     * @param name The header name
     * @param delimiter The delimiter
     * @return The header values as a single String or <code>null</code>
     */
    public String getHeader(final String name, final String delimiter) {
        if (containsHeaders() && (null != headers)) {
            return headers.getHeader(name, delimiter);
        }
        return null;
    }

    /**
     * Gets all the headers for this header name, returned as a single String, with headers separated by the delimiter. If the delimiter is
     * <code>'\0'</code>, only the first header is returned. Returns null if no headers with the specified name exist.
     *
     * @param name The header name
     * @param delimiter The delimiter character
     * @return The header values as a single String or <code>null</code>
     */
    public String getHeader(final String name, final char delimiter) {
        if (containsHeaders() && (null != headers)) {
            return headers.getHeader(name, delimiter);
        }
        return null;
    }

    /**
     * Gets a read-only version of this part's headers
     *
     * @return A read-only version of this part's headers
     */
    public HeaderCollection getHeaders() {
        if (containsHeaders() && (null != headers)) {
            return headers.getReadOnlyCollection();
        }
        return HeaderCollection.EMPTY_COLLECTION;
    }

    /**
     * Gets an iterator for non-matching headers
     *
     * @param nonMatchingHeaders The non-matching headers
     * @return An iterator for non-matching headers
     */
    public Iterator<Map.Entry<String, String>> getNonMatchingHeaders(final String[] nonMatchingHeaders) {
        if (containsHeaders() && (null != headers)) {
            return headers.getNonMatchingHeaders(nonMatchingHeaders);
        }
        return EMPTY_ITER;
    }

    /**
     * Gets an iterator for matching headers
     *
     * @param matchingHeaders The matching headers
     * @return An iterator for matching headers or <code>null</code> if not exists
     */
    public Iterator<Map.Entry<String, String>> getMatchingHeaders(final String[] matchingHeaders) {
        if (containsHeaders() && (null != headers)) {
            return headers.getMatchingHeaders(matchingHeaders);
        }
        return EMPTY_ITER;
    }

    /**
     * Removes the header if present
     *
     * @param name The header name
     */
    public void removeHeader(final String name) {
        if (containsHeaders() && (null != headers)) {
            headers.removeHeader(name);
        }
    }

    /**
     * Checks if this part contains all of specified headers.
     *
     * @param names The names of the headers to check
     * @return <code>true</code> if this part contains all of specified headers; otherwise <code>false</code>
     */
    public boolean hasHeaders(final String... names) {
        boolean ret = true;
        for (int i = 0; ret && i < names.length; i++) {
            ret = headers.containsHeader(names[i]);
        }
        return ret;
    }

    /**
     * Gets the size.
     * <p>
     * <div style="background-color:#FFDDDD; padding:6px; margin:0px;">
     * <b>Note</b>: The size returned reflects the value as given by associated mail storage, which might not be
     * the exact size, but only a "size estimation" as possible transfer-encodings (e.g. base64) are not considered.
     * </div>
     * <p>
     * For obtaining the exact you might want to use:
     * <pre>
     *   com.openexchange.java.Streams.countInputStream(mailPart.getInputStream());
     * <pre>
     *
     * @return The size or <code>-1</code> if unknown
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
     * @param size the size to set
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
            final String cid = getFirstHeader(MessageHeaders.HDR_CONTENT_ID);
            if (cid != null) {
                setContentId(cid);
            }
        }
        return contentId;
    }

    /**
     * @return <code>true</code> if contentId is set; otherwise <code>false</code>
     */
    public boolean containsContentId() {
        return b_contentId || containsHeader(MessageHeaders.HDR_CONTENT_ID);
    }

    /**
     * Removes the contentId
     */
    public void removeContentId() {
        contentId = null;
        removeHeader(MessageHeaders.HDR_CONTENT_ID);
        b_contentId = false;
    }

    /**
     * Sets the contentId
     *
     * @param contentId the contentId to set
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
     * @return <code>true</code> if sequenceId is set; otherwise <code>false</code>
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
     * Gets the message reference
     *
     * @return the message reference
     */
    public MailPath getMsgref() {
        if (b_msgref) {
            return msgref;
        }
        final String xMsgref = getFirstHeader(MessageHeaders.HDR_X_OXMSGREF);
        if (null != xMsgref) {
            removeHeader(MessageHeaders.HDR_X_OXMSGREF);
            b_msgref = true;
            try {
                msgref = new MailPath(xMsgref);
            } catch (final OXException e) {
                LOG.error("", e);
                msgref = null;
            }
        }
        return msgref;
    }

    /**
     * @return <code>true</code> if message reference is set; otherwise <code>false</code>
     */
    public boolean containsMsgref() {
        if (b_msgref) {
            return true;
        }
        return getHeader(MessageHeaders.HDR_X_OXMSGREF) != null;
    }

    /**
     * Removes the message reference
     */
    public void removeMsgref() {
        msgref = null;
        b_msgref = false;
        removeHeader(MessageHeaders.HDR_X_OXMSGREF);
    }

    /**
     * Sets the message reference
     *
     * @param msgref the message reference to set
     */
    public void setMsgref(final MailPath msgref) {
        this.msgref = msgref;
        b_msgref = true;
    }

    @Override
    public Object clone() {
        try {
            final MailPart clone = (MailPart) super.clone();
            if (contentType != null) {
                clone.contentType = new ContentType(contentType.toString());
            }
            if (null != headers) {
                clone.headers = new HeaderCollection(headers);
            }
            return clone;
        } catch (final CloneNotSupportedException e) {
            LOG.error("", e);
            throw new InternalError(e.getMessage());
        } catch (final OXException e) {
            LOG.error("", e);
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * Checks if part's MIME type is <code>multipart/*</code>
     *
     * @return <code>true</code> if part holds enclosed parts; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean hasEnclosedParts() throws OXException {
        return getEnclosedCount() != NO_ENCLOSED_PARTS;
    }

    /**
     * Returns the part's content as a Java object dependent on underlying implementation. <br>
     * This method is not applicable if part's MIME type is <code>multipart/*</code>
     *
     * @return The content as a Java object or <code>null</code> if not applicable
     * @throws OXException If content cannot be returned as a Java object
     */
    public abstract Object getContent() throws OXException;

    /**
     * Returns an appropriate {@link DataHandler} for this mail part. <br>
     * This method is not applicable if part's MIME type is <code>multipart/*</code>
     *
     * @return an appropriate {@link DataHandler} or <code>null</code> if not applicable
     * @throws OXException If an appropriate {@link DataHandler} cannot be returned
     */
    public abstract DataHandler getDataHandler() throws OXException;

    /**
     * Returns an input stream for this part. <br>
     * This method is not applicable if part's MIME type is <code>multipart/*</code>
     *
     * @return An input stream for this part or <code>null</code> if not applicable
     * @throws OXException If no input stream could be returned
     */
    public abstract InputStream getInputStream() throws OXException;

    /**
     * Gets the number of enclosed mail parts. <br>
     * This method is only applicable if part's MIME type is <code>multipart/*</code>
     *
     * @see #NO_ENCLOSED_PARTS
     * @return The number of enclosed mail parts or {@link #NO_ENCLOSED_PARTS} if not applicable
     */
    public abstract int getEnclosedCount() throws OXException;

    /**
     * Gets the mail part located at given index. <br>
     * This method is only applicable if part's MIME type is <code>multipart/*</code>
     *
     * @param index The index of desired mail part or <code>null</code> if not applicable
     * @return The mail part
     */
    public abstract MailPart getEnclosedMailPart(final int index) throws OXException;

    /**
     * Ensures that the part's content is loaded, thus this part is independent of the original.
     * <p>
     * This method is intended for mailing systems that read the contents stepwise on demand. If dealing with such a mail part with its
     * underlying connection closed, the part's content is no more accessible. Otherwise this method may be implemented with an empty body.
     * <p>
     * Moreover the loaded content is no more discarded when {@link #prepareForCaching()} is invoked.
     *
     * @throws OXException If loading part's content fails
     */
    public abstract void loadContent() throws OXException;

    /**
     * Writes complete part's data into given output stream
     *
     * @param out The output stream to write to
     * @throws OXException If writing to output stream fails
     */
    public void writeTo(final OutputStream out) throws OXException {
        final InputStream in = getInputStream();
        if (null == in) {
            throw MailExceptionCode.NO_CONTENT.create();
        }
        try {
            int buflen = 8192;
            byte[] buf = new byte[buflen];
            for (int count; (count = in.read(buf, 0, buflen)) > 0;) {
                out.write(buf, 0, count);
            }
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in);
        }
    }

    /**
     * Gets the mail part's source
     *
     * @return The mail part's source
     * @throws OXException If mail part's source cannot be returned
     */
    public String getSource() throws OXException {
        return new String(getSourceBytes(), Charsets.ISO_8859_1);
    }

    /**
     * Gets a newly allocated byte array containing the mail part's source bytes
     *
     * @return The mail part's source bytes
     * @throws OXException If mail part's source cannot be returned
     */
    public byte[] getSourceBytes() throws OXException {
        final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(4096);
        writeTo(out);
        return out.toByteArray();
    }

    /**
     * Prepares this mail part to be put into cache; meaning to release all kept resources
     */
    public abstract void prepareForCaching();

}
