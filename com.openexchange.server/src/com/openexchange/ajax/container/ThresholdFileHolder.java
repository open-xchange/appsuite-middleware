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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.container;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link ThresholdFileHolder} - A {@link IFileHolder} that backs data in a <code>byte</code> array as long as specified threshold is not
 * exceeded, but streams data to a temporary file otherwise.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThresholdFileHolder implements IFileHolder {

    /** The in-memory buffer where data is stored */
    private byte buf[];

    /** The number of valid bytes that were already written */
    private int count;

    /** The temporary file to stream to if threshold exceeded */
    private File tempFile;

    /** The file name */
    private String name;

    /** The <code>Content-Type</code> value */
    private String contentType;

    /** The disposition */
    private String disposition;

    /** The delivery model */
    private String delivery;

    /** The in-memory threshold */
    private final int threshold;

    /** The initial capacity */
    private final int initalCapacity;

    /**
     * Initializes a new {@link ThresholdFileHolder}.
     */
    public ThresholdFileHolder(final int threshold) {
        this(threshold, -1);
    }

    /**
     * Initializes a new {@link ThresholdFileHolder}.
     */
    public ThresholdFileHolder(final int threshold, final int initalCapacity) {
        super();
        count = 0;
        this.threshold = threshold;
        contentType = "application/octet-stream";
        this.initalCapacity = initalCapacity > 0 ? initalCapacity : 2048;
    }

    /**
     * Writes the specified content to this file holder.
     * 
     * @param bytes The content to be written.
     * @throws OXException If write attempt fails
     */
    public void write(final byte[] bytes) throws OXException {
        if (0 == count && bytes.length > threshold) {
            // Nothing written & content does exceed threshold
            final File tempFile = TmpFileFileHolder.newTempFile();
            this.tempFile = tempFile;
            OutputStream out = null;
            try {
                out = new FileOutputStream(tempFile);
                out.write(bytes, 0, bytes.length);
                out.flush();
            } catch (final IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } finally {
                Streams.close(out);
            }
            return;
        }
        // Deal with possible available content
        write(Streams.newByteArrayInputStream(bytes));
    }

    /**
     * Writes the specified content to this file holder.
     * 
     * @param in The content to be written.
     * @throws OXException If write attempt fails
     */
    public void write(final InputStream in) throws OXException {
        ByteArrayOutputStream baos = Streams.newByteArrayOutputStream(initalCapacity);
        OutputStream out = baos;
        try {
            int count = this.count;
            File tempFile = this.tempFile;
            final int inMemoryThreshold = threshold;
            final int buflen = 0xFFFF; // 64KB
            final byte[] buffer = new byte[buflen];
            for (int len; (len = in.read(buffer, 0, buflen)) > 0;) {
                if (null == tempFile) {
                    // Count bytes to stream to file if threshold is exceeded
                    count += len;
                    if (count > inMemoryThreshold) {
                        tempFile = TmpFileFileHolder.newTempFile();
                        this.tempFile = tempFile;
                        out = new FileOutputStream(tempFile);
                        out.write(baos.toByteArray());
                        baos = null;
                    }
                }
                out.write(buffer, 0, len);
            }
            out.flush();
            if (null == tempFile) {
                this.count = count;
            }
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in);
            Streams.close(out);
        }
    }

    @Override
    public boolean repetitive() {
        return true;
    }

    @Override
    public void close() {
        final File tempFile = this.tempFile;
        if (null != tempFile) {
            tempFile.delete();
            this.tempFile = null;
        }
        this.buf = null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            close();
        } catch (final Exception ignore) {
            // Ignore
        }
    }

    @Override
    public InputStream getStream() throws OXException {
        final byte[] buf = this.buf;
        if (null != buf) {
            return new UnsynchronizedByteArrayInputStream(buf);
        }
        final File tempFile = this.tempFile;
        if (null == tempFile) {
            final IOException e = new IOException("Already closed.");
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
        try {
            return new FileInputStream(tempFile);
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public long getLength() {
        final byte[] buf = this.buf;
        if (null != buf) {
            return buf.length;
        }
        final File tempFile = this.tempFile;
        if (null == tempFile) {
            throw new IllegalStateException("Already closed.");
        }
        return tempFile.length();
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisposition() {
        return disposition;
    }

    @Override
    public String getDelivery() {
        return delivery;
    }

    /**
     * Sets the disposition.
     * 
     * @param disposition The disposition
     */
    public void setDisposition(final String disposition) {
        this.disposition = disposition;
    }

    /**
     * Sets the content type; e.g. "application/octet-stream"
     * 
     * @param contentType The content type
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the (file) name.
     * 
     * @param name The name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the delivery
     * 
     * @param delivery The delivery to set
     */
    public void setDelivery(final String delivery) {
        this.delivery = delivery;
    }

}
