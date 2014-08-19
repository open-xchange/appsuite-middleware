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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.mail.attachment.storage;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.processing.MimeProcessingUtility;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DefaultMailAttachmentStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class DefaultMailAttachmentStorage implements MailAttachmentStorage {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultMailAttachmentStorage.class);

    private static class PublicationRefs {

        final PublicationTarget target;
        final PublicationService publisher;

        PublicationRefs(PublicationTarget target, PublicationService publisher) {
            super();
            this.target = target;
            this.publisher = publisher;
        }

    }

    // ------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link DefaultMailAttachmentStorage}.
     */
    public DefaultMailAttachmentStorage() {
        super();
    }

    private PublicationRefs getPublicationRefs() throws OXException {
        PublicationTargetDiscoveryService discoveryService = ServerServiceRegistry.getInstance().getService(PublicationTargetDiscoveryService.class, true);

        PublicationTarget target = discoveryService.getTarget("com.openexchange.publish.online.infostore.document");
        if (null == target) {
            LOG.warn("Missing publication target for ID \"com.openexchange.publish.online.infostore.document\".\nThrowing quota-exceeded exception instead.");
            throw ServiceExceptionCode.absentService(PublicationTarget.class);
        }

        PublicationService publisher = target.getPublicationService();

        return new PublicationRefs(target, publisher);
    }

    private Context getContext(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return ContextStorage.getStorageContext(session.getContextId());
    }

    private User getSessionUser(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), getContext(session));
    }

    private Locale getSessionUserLocale(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        final Context context = ContextStorage.getStorageContext(session.getContextId());
        return UserStorage.getInstance().getUser(session.getUserId(), context).getLocale();
    }

    @Override
    public String storeAttachment(MailPart attachment, MessageInfo msgInfo, Session session) throws OXException {
        IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true);

        // Check for folder ID
        final String key = MailSessionParameterNames.getParamPublishingInfostoreFolderID();
        if (!session.containsParameter(key)) {
            final Throwable t = new Throwable("Missing folder ID of publishing infostore folder.");
            throw MailExceptionCode.SEND_FAILED_UNKNOWN.create(t, new Object[0]);
        }
        final int folderId = ((Integer) session.getParameter(key)).intValue();

        // Create document meta data for current attachment
        String name = attachment.getFileName();
        if (name == null) {
            name = "attachment";
        }
        final File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(String.valueOf(folderId));
        file.setFileName(name);
        file.setFileMIMEType(attachment.getContentType().getBaseType());
        file.setTitle(name);
        if (null != msgInfo) {
            // Description
            Locale locale = TransportProperties.getInstance().getExternalRecipientsLocale();
            if (null == locale) {
                locale = getSessionUserLocale(session);
            }
            final StringHelper stringHelper = StringHelper.valueOf(locale);
            String desc = stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENT_INFO);
            {
                final String subject = msgInfo.subject;
                desc = desc.replaceFirst("#SUBJECT#", com.openexchange.java.Strings.quoteReplacement(null == subject ? stringHelper.getString(MailStrings.DEFAULT_SUBJECT) : subject));
            }
            {
                final Date date = msgInfo.date;
                final String repl = date == null ? "" : com.openexchange.java.Strings.quoteReplacement(MimeProcessingUtility.getFormattedDate(date, DateFormat.LONG, locale, TimeZone.getDefault()));
                desc = desc.replaceFirst("#DATE#", repl);
            }
            {
                final InternetAddress[] to = msgInfo.to;
                desc = desc.replaceFirst("#TO#", com.openexchange.java.Strings.quoteReplacement(to == null || to.length == 0 ? "" : com.openexchange.java.Strings.quoteReplacement(MimeProcessingUtility.addrs2String(to))));
            }
            file.setDescription(desc);
        }
        /*
         * Put attachment's document to dedicated infostore folder
         */
        final IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
        boolean retry = true;
        int count = 1;
        final StringBuilder hlp = new StringBuilder(16);
        while (retry) {
            /*
             * Get attachment's input stream
             */
            final InputStream in = attachment.getInputStream();
            boolean rollbackNeeded = false;
            try {
                /*
                 * Start InfoStore transaction
                 */
                fileAccess.startTransaction();
                rollbackNeeded = true;
                try {
                    fileAccess.saveDocument(file, in, FileStorageFileAccess.DISTANT_FUTURE);
                    fileAccess.commit();
                    rollbackNeeded = false;
                    retry = false;
                } catch (final OXException x) {
                    fileAccess.rollback();
                    rollbackNeeded = false;
                    if (!x.isPrefix("IFO")) {
                        throw x;
                    }
                    if (441 != x.getCode()) {
                        throw x;
                    }
                    /*
                     * Duplicate document name, thus retry with a new name
                     */
                    hlp.setLength(0);
                    final int pos = name.lastIndexOf('.');
                    final String newName;
                    if (pos >= 0) {
                        newName =
                            hlp.append(name.substring(0, pos)).append("_(").append(++count).append(')').append(name.substring(pos)).toString();
                    } else {
                        newName = hlp.append(name).append("_(").append(++count).append(')').toString();
                    }
                    file.setFileName(newName);
                    file.setTitle(newName);
                } catch (final RuntimeException e) {
                    throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                } finally {
                    if (rollbackNeeded) {
                        fileAccess.rollback();
                    }
                    fileAccess.finish();
                }
            } finally {
                Streams.close(in);
            }
        }
        return file.getId();
    }

    @Override
    public MailPart getAttachment(String id, Session session) throws OXException {
        try {
            IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true);

            IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
            File fileMetadata = fileAccess.getFileMetadata(id, FileStorageFileAccess.CURRENT_VERSION);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setDataHandler(new DataHandler(new MessageDataSource(fileAccess.getDocument(id, FileStorageFileAccess.CURRENT_VERSION), fileMetadata.getFileMIMEType())));
            mimeBodyPart.setFileName(fileMetadata.getFileName());
            mimeBodyPart.setHeader("Content-Type", fileMetadata.getFileMIMEType());

            return MimeMessageConverter.convertPart(mimeBodyPart, false);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void removeAttachment(String id, Session session) throws OXException {
        IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true);
        IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
        long timestamp = System.currentTimeMillis();

        // Delete file
        fileAccess.startTransaction();
        try {
            fileAccess.removeDocument(Collections.singletonList(id), timestamp);
            fileAccess.commit();
        } catch (final OXException x) {
            fileAccess.rollback();
            throw x;
        } finally {
            fileAccess.finish();
        }
    }

    @Override
    public DownloadUri getDownloadUri(String id, Session session) throws OXException {
        PublicationRefs publicationRefs = getPublicationRefs();

        // Generate publication for current attachment
        final Publication publication = new Publication();
        publication.setModule("infostore/object");
        publication.setEntityId(String.valueOf(id));
        publication.setContext(getContext(session));
        publication.setUserId(session.getUserId());

        // Set target
        publication.setTarget(publicationRefs.target);

        // ... and publish
        publicationRefs.publisher.create(publication);

        // Get associated URL
        String url = (String) publication.getConfiguration().get("url");

        // Return
        return new DownloadUri(url, Integer.toString(publication.getId()));
    }

    @Override
    public void discard(String id, DownloadUri downloadUri, Session session) throws OXException {
        IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true);
        IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);

        PublicationRefs publicationRefs = getPublicationRefs();

        long timestamp = System.currentTimeMillis();

        // Delete publication
        try {
            publicationRefs.publisher.delete(getContext(session), Strings.parsePositiveInt(downloadUri.getDownloadInfo()));
        } catch (final OXException e) {
            LOG.error("Publication with ID \"{} could not be roll-backed.", downloadUri.getDownloadInfo(), e);
        }

        // Delete file
        try {
            fileAccess.startTransaction();
            try {
                fileAccess.removeDocument(Collections.singletonList(id), timestamp);
                fileAccess.commit();
            } catch (final OXException x) {
                fileAccess.rollback();
                throw x;
            } finally {
                fileAccess.finish();
            }
        } catch (final OXException e) {
            LOG.error("Transaction error while deleting file with ID \"{}\" failed.", id, e);
        }
    }

}
