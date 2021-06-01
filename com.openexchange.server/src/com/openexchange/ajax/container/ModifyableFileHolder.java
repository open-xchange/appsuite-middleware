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

/**
 * {@link ModifyableFileHolder} - A delegating file holder providing possibility to set MIME type, name, disposition and delivery
 * information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ModifyableFileHolder implements IFileHolder {

    private final IFileHolder fileHolder;
    private String name;
    private String disposition;
    private String contentType;
    private String delivery;

    /**
     * Initializes a new {@link ModifyableFileHolder}.
     *
     * @param fileHolder The delegate file holder
     */
    public ModifyableFileHolder(final IFileHolder fileHolder) {
        super();
        this.fileHolder = fileHolder;
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
        return fileHolder.repetitive();
    }

    @Override
    public void close() throws IOException {
        fileHolder.close();
    }

    @Override
    public InputStream getStream() throws OXException {
        return fileHolder.getStream();
    }

    @Override
    public RandomAccess getRandomAccess() throws OXException {
        return fileHolder.getRandomAccess();
    }

    @Override
    public long getLength() {
        return fileHolder.getLength();
    }

    @Override
    public String getContentType() {
        return null == contentType ? fileHolder.getContentType() : contentType;
    }

    @Override
    public String getName() {
        return null == name ? fileHolder.getName() : name;
    }

    @Override
    public String getDisposition() {
        return null == disposition ? fileHolder.getDisposition() : disposition;
    }

    @Override
    public String getDelivery() {
        return delivery == null ? fileHolder.getDelivery() : delivery;
    }

    /**
     * Sets the MIME type
     *
     * @param contentType The MIME type to set
     * @return This file holder with new argument applied
     */
    public ModifyableFileHolder setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Sets the delivery
     *
     * @param delivery The delivery to set
     * @return This file holder with new argument applied
     */
    public ModifyableFileHolder setDelivery(String delivery) {
        this.delivery = delivery;
        return this;
    }

    /**
     * Sets the disposition
     *
     * @param disposition The disposition to set
     * @return This file holder with new argument applied
     */
    public ModifyableFileHolder setDisposition(String disposition) {
        this.disposition = disposition;
        return this;
    }

    /**
     * Sets the name
     *
     * @param name The name to set
     * @return This file holder with new argument applied
     */
    public ModifyableFileHolder setName(String name) {
        this.name = name;
        return this;
    }

}
