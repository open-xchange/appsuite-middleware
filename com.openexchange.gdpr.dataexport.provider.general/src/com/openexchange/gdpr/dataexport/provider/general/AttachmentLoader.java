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

package com.openexchange.gdpr.dataexport.provider.general;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.container.SystemObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AttachmentLoader} - Helper class to load binary attachments for a PIM object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class AttachmentLoader {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentLoader.class);
    }

    /**
     * Initializes a new {@link AttachmentLoader}.
     */
    private AttachmentLoader() {
        super();
    }

    /**
     * The special property for versit conversions (iCal/vCard) to include binary attachments
     *
     * @see SystemObject#setProperty(String, Object)
     */
    public static final String PROPERTY_BINARY_ATTACHMENTS = "com.openexchange.data.conversion.ical.attach.binaryAttachments";

    /**
     * Loads the attachment binaries (if any) for specified PIM object.
     *
     * @param objectId The object identifier
     * @param moduleId The module identifier
     * @param folderId The folder identifier
     * @param session The session
     * @return The optional attachment binaries (never <code>null</code>)
     * @throws OXException If attachment binaries cannot be loaded
     */
    public static Optional<List<IFileHolder>> loadAttachmentBinaries(int objectId, int moduleId, int folderId, Session session) throws OXException {
        List<IFileHolder> attachmentBinaries = null;
        try {
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);

            Context ctx = serverSession.getContext();
            User user = serverSession.getUser();
            UserConfiguration userConfig = serverSession.getUserConfiguration();

            List<AttachmentMetadata> attachments = determineAttachments(objectId, folderId, moduleId, serverSession, user, userConfig, ctx);
            if (attachments.isEmpty()) {
                return Optional.empty();
            }

            attachmentBinaries = new ArrayList<IFileHolder>(attachments.size());
            for (AttachmentMetadata attachment : attachments) {
                attachmentBinaries.add(getAttachmentBinary(attachment, objectId, folderId, moduleId, session, user, userConfig, ctx));
            }

            List<IFileHolder> retval = attachmentBinaries;
            attachmentBinaries = null; // Avoid premature closing
            return Optional.of(retval);
        } finally {
            Streams.close(attachmentBinaries);
        }
    }

    private static final AttachmentBase ATTACHMENT_BASE = Attachment.ATTACHMENT_BASE;
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private static ThresholdFileHolder getAttachmentBinary(AttachmentMetadata attachment, int contactId, int folderId, int moduleId, Session session, User user, UserConfiguration userConfig, Context ctx) throws OXException {
        ThresholdFileHolder fileHolder = null;
        boolean rollback = false;
        try {
            ATTACHMENT_BASE.startTransaction();
            rollback = true;

            String preferredContentType = attachment.getFileMIMEType();
            String contentTypeByFileName = MimeType2ExtMap.getContentType(attachment.getFilename(), null);
            if (null != contentTypeByFileName) {
                if (APPLICATION_OCTET_STREAM.equals(preferredContentType)) {
                    preferredContentType = contentTypeByFileName;
                } else {
                    final String primaryType1 = getPrimaryType(preferredContentType);
                    final String primaryType2 = getPrimaryType(contentTypeByFileName);
                    if (!Strings.toLowerCase(primaryType1).startsWith(Strings.toLowerCase(primaryType2))) {
                        preferredContentType = contentTypeByFileName;
                    }
                }
            }

            fileHolder = new ThresholdFileHolder();
            InputStream documentData = ATTACHMENT_BASE.getAttachedFile(session, folderId, contactId, moduleId, attachment.getId(), ctx, user, userConfig);
            try {
                fileHolder.write(documentData);
            } finally {
                Streams.close(documentData);
            }

            fileHolder.setContentType(preferredContentType);
            fileHolder.setName(attachment.getFilename());

            ATTACHMENT_BASE.commit();
            rollback = false;

            ThresholdFileHolder retval = fileHolder;
            fileHolder = null; // Avoid premature closing
            return retval;
        } finally {
            finish(rollback);
            Streams.close(fileHolder);
        }
    }

    private static List<AttachmentMetadata> determineAttachments(int contactId, int folderId, int moduleId, ServerSession serverSession, User user, UserConfiguration userConfig, Context ctx) throws OXException {
        SearchIterator<AttachmentMetadata> iter = null;
        boolean rollback = false;
        try {
            ATTACHMENT_BASE.startTransaction();
            rollback = true;

            iter = ATTACHMENT_BASE.getAttachments(serverSession, folderId, contactId, moduleId, ctx, user, userConfig).results();
            List<AttachmentMetadata> attachmentIds;
            if (iter.hasNext()) {
                attachmentIds = new ArrayList<>();
                do {
                    attachmentIds.add(iter.next());
                } while (iter.hasNext());
            } else {
                attachmentIds = Collections.emptyList();
            }

            ATTACHMENT_BASE.commit();
            rollback = false;

            return attachmentIds;
        } finally {
            finish(rollback);
            SearchIterators.close(iter);
        }
    }

    private static void finish(boolean rollback) {
        if (rollback) {
            try {
                ATTACHMENT_BASE.rollback();
            } catch (Exception e) {
                LoggerHolder.LOG.debug("Rollback failed.", e);
            }
        }
        try {
            ATTACHMENT_BASE.finish();
        } catch (Exception e) {
            LoggerHolder.LOG.debug("", e);
        }
    }

    private static String getPrimaryType(final String contentType) {
        if (Strings.isEmpty(contentType)) {
            return contentType;
        }
        final int pos = contentType.indexOf('/');
        return pos > 0 ? contentType.substring(0, pos) : contentType;
    }

}
