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

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.fileholder.IFileHolder;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link DelegateFileHolder} - A delegating file holder providing possibility to set separate stream/length.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DelegateFileHolder implements IFileHolder {

    private final IFileHolder fileHolder;
    private InputStream stream;
    private InputStreamClosure streamProvider;
    private Long length;
    private int repetitive = -1;

    /**
     * Initializes a new {@link DelegateFileHolder}.
     *
     * @param fileHolder The delegate file holder
     */
    public DelegateFileHolder(final IFileHolder fileHolder) {
        super();
        this.fileHolder = fileHolder;
        length = null;
    }

    @Override
    public List<Runnable> getPostProcessingTasks() {
        return fileHolder.getPostProcessingTasks();
    }


    @Override
    public void addPostProcessingTask(Runnable task) {
        fileHolder.addPostProcessingTask(task);
    }

    @Override
    public boolean repetitive() {
        final int repetitive = this.repetitive;
        if (repetitive < 0) {
            return fileHolder.repetitive();
        }
        return repetitive > 0;
    }

    @Override
    public void close() throws IOException {
        Streams.close(stream, fileHolder);
    }

    @Override
    public RandomAccess getRandomAccess() throws OXException {
        return fileHolder.getRandomAccess();
    }

    @Override
    public InputStream getStream() throws OXException {
        final InputStream stream = this.stream;
        if (null != stream) {
            return stream;
        }
        final InputStreamClosure streamProvider = this.streamProvider;
        if (null != streamProvider) {
            try {
                return streamProvider.newStream();
            } catch (final IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
        return fileHolder.getStream();
    }

    /**
     * Sets the stream
     *
     * @param stream The stream to set
     * @param length The length to set
     * @return This file holder with arguments applied
     */
    public DelegateFileHolder setStream(final InputStream stream, final long length) {
        this.stream = stream;
        this.length = Long.valueOf(length);
        repetitive = 0;
        return this;
    }

    /**
     * Sets the stream
     *
     * @param streamProvider The stream to set
     * @param length The length to set
     * @return This file holder with arguments applied
     */
    public DelegateFileHolder setStream(final IFileHolder.InputStreamClosure streamProvider, final long length) {
        this.streamProvider = streamProvider;
        this.length = Long.valueOf(length);
        repetitive = 1;
        return this;
    }

    /**
     * Sets the length
     *
     * @param length The length to set
     * @return This file holder with length applied
     */
    public DelegateFileHolder setLength(final long length) {
        this.length = Long.valueOf(length);
        return this;
    }

    @Override
    public long getLength() {
        final Long length = this.length;
        if (length != null) {
            return length.longValue();
        }
        return fileHolder.getLength();
    }

    @Override
    public String getContentType() {
        return fileHolder.getContentType();
    }

    @Override
    public String getName() {
        return fileHolder.getName();
    }

    @Override
    public String getDisposition() {
        return fileHolder.getDisposition();
    }

    @Override
    public String getDelivery() {
        return fileHolder.getDelivery();
    }

}
