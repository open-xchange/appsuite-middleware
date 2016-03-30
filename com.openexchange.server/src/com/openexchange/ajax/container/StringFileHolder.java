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

package com.openexchange.ajax.container;

import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.fileholder.ByteArrayRandomAccess;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link StringFileHolder} - A {@link IFileHolder} implementation backed by a {@link String}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StringFileHolder implements IFileHolder {

    private final byte[] bytes;
    private String name;
    private String contentType;
    private String disposition;
    private String delivery;
    private final List<Runnable> tasks;

    /**
     * Initializes a new {@link StringFileHolder} with default encoding (UTF-8).
     *
     * @param string The string
     */
    public StringFileHolder(final String string) {
        this(string, "UTF-8");
    }

    /**
     * Initializes a new {@link StringFileHolder}.
     *
     * @param string The string
     * @param encoding The encoding; e.g. "UTF-8"
     * @throws IllegalStateException If encoding is not supported
     */
    public StringFileHolder(final String string, final String encoding) {
        super();
        try {
            this.bytes = string.getBytes(Charsets.forName(encoding));
            contentType = "application/octet-stream";
            tasks = new LinkedList<Runnable>();
        } catch (final UnsupportedCharsetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<Runnable> getPostProcessingTasks() {
        return tasks;
    }

    @Override
    public void addPostProcessingTask(Runnable task) {
        if (null != task) {
            tasks.add(task);
        }
    }

    @Override
    public boolean repetitive() {
        return true;
    }

    @Override
    public void close() {
        // Nope
    }

    @Override
    public InputStream getStream() {
        return new UnsynchronizedByteArrayInputStream(bytes);
    }

    @Override
    public RandomAccess getRandomAccess() throws OXException {
        return new ByteArrayRandomAccess(bytes);
    }

    @Override
    public long getLength() {
        return bytes.length;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
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

    @Override
    public String getDisposition() {
        return disposition;
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
     * Sets the delivery
     *
     * @param delivery The delivery to set
     */
    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    @Override
    public String getDelivery() {
        return delivery;
    }

}
