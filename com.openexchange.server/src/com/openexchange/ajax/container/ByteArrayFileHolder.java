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

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.fileholder.ByteArrayRandomAccess;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link ByteArrayFileHolder} - A {@link IFileHolder} implementation backed by a <code>byte</code> array.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ByteArrayFileHolder implements IFileHolder {

    private final byte[] bytes;
    private String name;
    private String contentType;
    private String disposition;
    private String delivery;
    private final List<Runnable> tasks;

    /**
     * Initializes a new {@link ByteArrayFileHolder}.
     */
    public ByteArrayFileHolder(final byte[] bytes) {
        super();
        this.bytes = bytes;
        contentType = "application/octet-stream";
        tasks = new LinkedList<Runnable>();
    }

    /**
     * Gets the bytes
     *
     * @return The bytes
     */
    public byte[] getBytes() {
        return bytes;
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
    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    @Override
    public String getDelivery() {
        return delivery;
    }
}
