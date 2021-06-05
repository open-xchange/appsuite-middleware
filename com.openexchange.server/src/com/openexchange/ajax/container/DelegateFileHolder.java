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

package com.openexchange.ajax.container;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.openexchange.ajax.fileholder.IFileHolder;
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
            } catch (IOException e) {
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
