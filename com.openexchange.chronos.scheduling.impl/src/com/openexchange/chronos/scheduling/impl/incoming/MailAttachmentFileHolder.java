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

package com.openexchange.chronos.scheduling.impl.incoming;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;

/**
 * {@link MailAttachmentFileHolder} - {@link IFileHolder} for an calendar attachment received through an incoming mail
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class MailAttachmentFileHolder implements IFileHolder {

    private final ShortLivingMailAccess access;
    private final String contentId;
    private final String fileName;
    private final String contentType;
    private final long size;

    private List<Runnable> tasks;

    /**
     * Initializes a new {@link MailAttachmentFileHolder}.
     * 
     * @param access The access to gain the part from
     * @param contentId The sequence identifier of the attachment
     * @param fileName The name of the attachment
     * @param contentType The content type of the attachment
     * @param size The size or rather length of the attachment
     */
    public MailAttachmentFileHolder(ShortLivingMailAccess access, String contentId, String fileName, String contentType, long size) {
        super();
        this.access = access;
        this.contentId = contentId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
    }

    @Override
    public boolean repetitive() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // No-op
    }

    @Override
    public InputStream getStream() throws OXException {
        return access.getMailPart(contentId).getInputStream();
    }

    @Override
    public RandomAccess getRandomAccess() throws OXException {
        return null;
    }

    @Override
    public long getLength() {
        return size;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getDisposition() {
        return null;
    }

    @Override
    public String getDelivery() {
        return null;
    }

    @Override
    public List<Runnable> getPostProcessingTasks() {
        return tasks;
    }

    @Override
    public void addPostProcessingTask(Runnable task) {
        if (null == tasks) {
            tasks = new ArrayList<>(2);
        }
        tasks.add(task);
    }

}
