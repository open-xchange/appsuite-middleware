/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
    public DataContentHandlerDataSource(Object object, String objectMimeType, DataContentHandler dch) {
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
                } catch (Exception e) {
                    pin.setException(e);
                } finally {
                    try {
                        pos.close();
                    } catch (IOException ie) {
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
