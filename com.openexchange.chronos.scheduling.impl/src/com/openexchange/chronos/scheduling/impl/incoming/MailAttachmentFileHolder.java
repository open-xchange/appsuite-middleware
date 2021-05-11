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
 *    trademarks of the OX Software GmbH. group of companies.
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
