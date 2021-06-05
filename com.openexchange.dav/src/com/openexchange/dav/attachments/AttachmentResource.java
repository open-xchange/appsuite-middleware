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

package com.openexchange.dav.attachments;

import java.io.InputStream;
import java.util.Date;
import com.openexchange.dav.AttachmentUtils;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link AttachmentResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class AttachmentResource extends DAVResource {

    private final AttachmentMetadata attachment;

    /**
     * Initializes a new {@link AttachmentResource}.
     *
     * @param factory The factory
     * @param attachment The attachment
     * @param url The WebDAV path of the resource
     */
    public AttachmentResource(DAVFactory factory, AttachmentMetadata attachment, WebdavPath url) {
        super(factory, url);
        this.attachment = attachment;
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return attachment.getFilename();
    }

    @Override
    public String getETag() throws WebdavProtocolException {
        if (false == exists() || null == attachment || null == attachment.getCreationDate()) {
            return "";
        }
        return String.format("%d-%d-%d", Integer.valueOf(getFactory().getSession().getContextId()), Integer.valueOf(attachment.getId()), Long.valueOf(attachment.getCreationDate().getTime()));
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return new Date(0);
    }

    @Override
    public Long getLength() throws WebdavProtocolException {
        return Long.valueOf(attachment.getFilesize());
    }

    @Override
    public String getContentType() throws WebdavProtocolException {
        return attachment.getFileMIMEType();
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return attachment.getCreationDate();
    }

    @Override
    public boolean hasBody() throws WebdavProtocolException {
        return true;
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        AttachmentBase attachments = Attachments.getInstance();
        try {
            return attachments.getAttachedFile(factory.getSession(), attachment.getFolderId(), attachment.getAttachedId(),
                attachment.getModuleId(), attachment.getId(), factory.getContext(), factory.getUser(), factory.getUserConfiguration());
        } catch (OXException e) {
            throw AttachmentUtils.protocolException(e, getUrl());
        }
    }

}
