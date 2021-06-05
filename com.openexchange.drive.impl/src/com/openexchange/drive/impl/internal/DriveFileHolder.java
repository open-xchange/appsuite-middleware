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

package com.openexchange.drive.impl.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.drive.impl.internal.throttle.BucketInputStream;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link DriveFileHolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveFileHolder implements IFileHolder {

    private final InputStream stream;
    private final String contentType;
    private final String name;
    private final List<Runnable> tasks;
    private final long length;

    /**
     * Initializes a new {@link DriveFileHolder}.
     *
     * @param session The sync session
     * @param stream The underlying stream
     * @param name The filename
     * @param contentType The content-type, or <code>null</code> if unknown
     * @param length The context length, or <code>-1</code> if unknown
     */
    public DriveFileHolder(SyncSession session, InputStream stream, String name, String contentType, long length) {
        this(session, stream, name, contentType, length, true);
    }

    /**
     * Initializes a new {@link DriveFileHolder}.
     *
     * @param session The sync session
     * @param stream The underlying stream
     * @param name The filename
     * @param contentType The content-type, or <code>null</code> if unknown
     * @param length The context length, or <code>-1</code> if unknown
     * @param throttled <code>true</code> to provide a (potentially) throttled stream, <code>false</code>, otherwise
     */
    public DriveFileHolder(SyncSession session, InputStream stream, String name, String contentType, long length, boolean throttled) {
        super();
        this.contentType = null != contentType ? contentType : "application/octet-stream";
        this.length = length;
        this.name = name;
        this.stream = throttled ? new BucketInputStream(stream, session.getServerSession()) : stream;
        tasks = new LinkedList<Runnable>();
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
        return false;
    }

    @Override
    public void close() throws IOException {
        Streams.close(stream);
    }

    @Override
    public InputStream getStream() throws OXException {
        return stream;
    }

    @Override
    public RandomAccess getRandomAccess() throws OXException {
        // No random access support
        return null;
    }

    @Override
    public long getLength() {
        return length;
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
        return null;
    }

    @Override
    public String getDelivery() {
        return "download";
    }

}
