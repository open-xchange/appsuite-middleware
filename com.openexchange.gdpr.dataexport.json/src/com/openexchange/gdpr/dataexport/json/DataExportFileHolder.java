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

package com.openexchange.gdpr.dataexport.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportDownload;

/**
 * {@link DataExportFileHolder} - A file holder backed by a data export download.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class DataExportFileHolder implements IFileHolder {

    private final DataExportDownload dataExportDownload;
    private String delivery;
    private final List<Runnable> tasks;

    /**
     * Initializes a new {@link DataExportFileHolder}.
     *
     * @param dataExportDownload The data export download
     */
    public DataExportFileHolder(DataExportDownload dataExportDownload) {
        super();
        this.dataExportDownload = dataExportDownload;
        tasks = new LinkedList<Runnable>();
    }

    /**
     * Sets the delivery
     *
     * @param delivery The delivery to set
     */
    public void setDelivery(final String delivery) {
        this.delivery = delivery;
    }

    @Override
    public boolean repetitive() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

    @Override
    public InputStream getStream() throws OXException {
        return dataExportDownload.getInputStream();
    }

    @Override
    public RandomAccess getRandomAccess() throws OXException {
        return null;
    }

    @Override
    public long getLength() {
        return -1;
    }

    @Override
    public String getContentType() {
        return dataExportDownload.getContentType();
    }

    @Override
    public String getName() {
        return dataExportDownload.getFileName();
    }

    @Override
    public String getDisposition() {
        return "attachment";
    }

    @Override
    public String getDelivery() {
        return delivery;
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

}