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

package com.openexchange.groupware.dataRetrieval.attachments;

import java.io.InputStream;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.dataRetrieval.DataProvider;
import com.openexchange.groupware.dataRetrieval.FileMetadata;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link PIMAttachmentDataProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PIMAttachmentDataProvider implements DataProvider<PIMAttachmentState> {

    private static final String FOLDER = "folder";
    private static final String OBJECT = "attached";
    private static final String MODULE = "module";
    private static final String ID = "id";

    private AttachmentBase attachments = null;

    public PIMAttachmentDataProvider(final AttachmentBase attachments) {
        this.attachments = attachments;
    }

    @Override
    public String getId() {
        return "attachments";
    }

    @Override
    public InputStream retrieve(final PIMAttachmentState state, final Map<String, Object> specification, final ServerSession session) throws OXException {
        final int folderId = tolerantInt(specification.get(FOLDER));
        final int objectId = tolerantInt(specification.get(OBJECT));
        final int moduleId = tolerantInt(specification.get(MODULE));
        final int id = tolerantInt(specification.get(ID));

        return attachments.getAttachedFile(session, folderId, objectId, moduleId, id, session.getContext(), session.getUser(), session.getUserConfiguration());
    }

    @Override
    public FileMetadata retrieveMetadata(final PIMAttachmentState state, final Map<String, Object> specification, final ServerSession session) throws OXException {

        final int folderId = tolerantInt(specification.get(FOLDER));
        final int objectId = tolerantInt(specification.get(OBJECT));
        final int moduleId = tolerantInt(specification.get(MODULE));
        final int id = tolerantInt(specification.get(ID));

        final AttachmentMetadata attachment = attachments.getAttachment(session, folderId, objectId, moduleId, id, session.getContext(), session.getUser(), session.getUserConfiguration());

        return new AttachmentFileMetadata(attachment);
    }

    private int tolerantInt(final Object intable) {
        return Integer.parseInt(intable.toString());
    }

    @Override
    public PIMAttachmentState start() {
        return null;
    }

    @Override
    public void close(final PIMAttachmentState state) {

    }

    private static class AttachmentFileMetadata implements FileMetadata {

        private AttachmentMetadata attachment = null;

        public AttachmentFileMetadata(final AttachmentMetadata attachment) {
            this.attachment = attachment;
        }

        @Override
        public String getFilename() {
            return attachment.getFilename();
        }

        @Override
        public long getSize() {
            return attachment.getFilesize();
        }

        @Override
        public String getType() {
            return attachment.getFileMIMEType();
        }

    }

}
