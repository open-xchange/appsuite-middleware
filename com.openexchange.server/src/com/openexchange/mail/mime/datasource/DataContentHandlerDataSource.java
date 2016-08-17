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

package com.openexchange.mail.mime.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import com.openexchange.conversion.DataHandler;
import com.openexchange.java.ExceptionAwarePipedInputStream;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.AbortBehavior;

/**
 * {@link DataContentHandlerDataSource} - A {@link DataSource} backed by a {@link DataContentHandler}.
 * <p>
 * This bypasses the need for {@link DataHandler} to look-up an appropriate {@link DataContentHandler}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DataContentHandlerDataSource implements DataSource {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataContentHandlerDataSource.class);

    private final DataContentHandler dch;
    private final Object object;
    private final String objectMimeType;

    /**
     * Initializes a new {@link DataContentHandlerDataSource}.
     */
    public DataContentHandlerDataSource(final Object object, final String objectMimeType, final DataContentHandler dch) {
        super();
        this.object = object;
        this.objectMimeType = objectMimeType;
        this.dch = dch;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        final PipedOutputStream pos = new PipedOutputStream();
        final ExceptionAwarePipedInputStream pin = new ExceptionAwarePipedInputStream(pos, 65536);

        final DataContentHandler dch = this.dch;
        final Object object = this.object;
        final String objectMimeType = this.objectMimeType;
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    dch.writeTo(object, objectMimeType, pos);
                } catch (final Exception e) {
                    pin.setException(e);
                } finally {
                    try {
                        pos.close();
                    } catch (final IOException ie) {
                        // Ignore
                    }
                }
            }
        };
        final ThreadPoolService threadPool = ThreadPools.getThreadPool();
        if (null == threadPool) {
            new Thread(r, "DataContentHandlerDataSource.getInputStream").start();
        } else {
            threadPool.submit(ThreadPools.task(r), AbortBehavior.getInstance());
        }
        return pin;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public String getContentType() {
        return objectMimeType;
    }

    @Override
    public String getName() {
        return null;
    }

}
