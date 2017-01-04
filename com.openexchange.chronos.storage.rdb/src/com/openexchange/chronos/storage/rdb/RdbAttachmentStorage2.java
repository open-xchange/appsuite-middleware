///*
// *
// *    OPEN-XCHANGE legal information
// *
// *    All intellectual property rights in the Software are protected by
// *    international copyright laws.
// *
// *
// *    In some countries OX, OX Open-Xchange, open xchange and OXtender
// *    as well as the corresponding Logos OX Open-Xchange and OX are registered
// *    trademarks of the Open-Xchange, Inc. group of companies.
// *    The use of the Logos is not covered by the GNU General Public License.
// *    Instead, you are allowed to use these Logos according to the terms and
// *    conditions of the Creative Commons License, Version 2.5, Attribution,
// *    Non-commercial, ShareAlike, and the interpretation of the term
// *    Non-commercial applicable to the aforementioned license is published
// *    on the web site http://www.open-xchange.com/EN/legal/index.html.
// *
// *    Please make sure that third-party modules and libraries are used
// *    according to their respective licenses.
// *
// *    Any modifications to this package must retain all copyright notices
// *    of the original copyright holder(s) for the original code used.
// *
// *    After any such modifications, the original and derivative code shall remain
// *    under the copyright of the copyright holder(s) and/or original author(s)per
// *    the Attribution and Assignment Agreement that can be located at
// *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
// *    given Attribution for the derivative code and a license granting use.
// *
// *     Copyright (C) 2004-2020 Open-Xchange, Inc.
// *     Mail: info@open-xchange.com
// *
// *
// *     This program is free software; you can redistribute it and/or modify it
// *     under the terms of the GNU General Public License, Version 2 as published
// *     by the Free Software Foundation.
// *
// *     This program is distributed in the hope that it will be useful, but
// *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *     for more details.
// *
// *     You should have received a copy of the GNU General Public License along
// *     with this program; if not, write to the Free Software Foundation, Inc., 59
// *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// */
//
//package com.openexchange.chronos.storage.rdb;
//
//import static com.openexchange.java.Autoboxing.I;
//import static com.openexchange.java.Autoboxing.I2i;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import com.google.common.io.BaseEncoding;
//import com.openexchange.chronos.Attachment;
//import com.openexchange.chronos.service.EntityResolver;
//import com.openexchange.chronos.storage.AttachmentStorage;
//import com.openexchange.database.provider.DBProvider;
//import com.openexchange.database.provider.DBTransactionPolicy;
//import com.openexchange.exception.OXException;
//import com.openexchange.groupware.attach.AttachmentBase;
//import com.openexchange.groupware.attach.AttachmentField;
//import com.openexchange.groupware.attach.AttachmentMetadata;
//import com.openexchange.groupware.attach.AttachmentMetadataFactory;
//import com.openexchange.groupware.attach.Attachments;
//import com.openexchange.groupware.ldap.User;
//import com.openexchange.groupware.results.TimedResult;
//import com.openexchange.groupware.userconfiguration.UserConfiguration;
//import com.openexchange.java.Charsets;
//import com.openexchange.java.Streams;
//import com.openexchange.java.Strings;
//import com.openexchange.tools.iterator.SearchIterator;
//import com.openexchange.tools.iterator.SearchIterators;
//import com.openexchange.tools.session.ServerSession;
//
///**
// * {@link RdbAttachmentStorage}
// *
// * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
// * @since v7.10.0
// */
//public class RdbAttachmentStorage extends RdbStorage implements AttachmentStorage {
//
//    private static final int MODULE_ID = com.openexchange.groupware.Types.APPOINTMENT;
//    private static final AttachmentMetadataFactory METADATA_FACTORY = new AttachmentMetadataFactory();
//
//    private final ServerSession session;
//    private final UserConfiguration userConfiguration;
//    private final User user;
//
//    /**
//     * Initializes a new {@link RdbAttachmentStorage}.
//     *
//     * @param session The server session
//     * @param entityResolver The entity resolver to use
//     * @param dbProvider The database provider to use
//     * @param txPolicy The transaction policy
//     */
//    public RdbAttachmentStorage(ServerSession session, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
//        super(session.getContext(), entityResolver, dbProvider, txPolicy);
//        this.session = session;
//        this.userConfiguration = session.getUserConfiguration();
//        this.user = session.getUser();
//    }
//
//    @Override
//    public List<Attachment> loadAttachments(int folderID, int eventID) throws OXException {
//        return loadAttachments(folderID, new int[] { eventID }).get(I(eventID));
//    }
//
//    @Override
//    public Map<Integer, List<Attachment>> loadAttachments(int folderID, int[] eventIDs) throws OXException {
//        AttachmentBase attachmentBase = Attachments.getInstance(dbProvider);
//        Map<Integer, List<Attachment>> attachmentsById = new HashMap<Integer, List<Attachment>>();
//        for (int eventID : eventIDs) {
//            List<Attachment> attachments = getAttachments(attachmentBase.getAttachments(session, folderID, eventID, MODULE_ID, context, user, userConfiguration));
//            if (null != attachments && 0 < attachments.size()) {
//                attachmentsById.put(I(eventID), attachments);
//            }
//        }
//        return attachmentsById;
//    }
//
//    public void deleteAttachments(int folderID, int eventID) throws OXException {
//        AttachmentBase attachmentBase = Attachments.getInstance(dbProvider);
//        List<Integer> attachmentIDs = new ArrayList<Integer>();
//        TimedResult<AttachmentMetadata> timedResult = attachmentBase.getAttachments(
//            session, folderID, eventID, MODULE_ID, new AttachmentField[] { AttachmentField.ID_LITERAL }, null, 0, context, user, userConfiguration);
//        SearchIterator<AttachmentMetadata> iterator = null;
//        try {
//            iterator = timedResult.results();
//            while (iterator.hasNext()) {
//                attachmentIDs.add(I(iterator.next().getId()));
//            }
//        } finally {
//            SearchIterators.close(iterator);
//        }
//        if (0 < attachmentIDs.size()) {
//            attachmentBase.detachFromObject(folderID, eventID, MODULE_ID, I2i(attachmentIDs), session, context, user, userConfiguration);
//        }
//    }
//
//    public void insertAttachments(int folderID, int eventID, List<Attachment> attachments) throws OXException {
//        AttachmentBase attachmentBase = Attachments.getInstance(dbProvider);
//        /*
//         * store new binary attachments
//         */
//        for (Attachment binaryAttachment : filterBinary(attachments)) {
//            AttachmentMetadata metadata = getMetadata(binaryAttachment, folderID, eventID);
//            InputStream inputStream = null;
//            try {
//                inputStream = binaryAttachment.getData().getStream();
//                attachmentBase.attachToObject(metadata, inputStream, session, context, user, userConfiguration);
//            } finally {
//                Streams.close(inputStream);
//            }
//        }
//        /*
//         * copy over referenced managed attachments
//         */
//        for (Attachment managedAttachment : filterManaged(attachments)) {
//            AttachmentMetadata referencedMetadata = decodeManagedId(managedAttachment.getManagedId());
//            copyAttachment(attachmentBase, referencedMetadata, folderID, eventID);
//        }
//    }
//
//    /**
//     * Copies an existing attachment to another target event.
//     *
//     * @param attachmentBase The underlying attachment service
//     * @param folderId The parent folder identifier of the targeted event
//     * @param eventId The identifier of the targeted event
//     * @return The copied attachment metadata
//     */
//    private AttachmentMetadata copyAttachment(AttachmentBase attachmentBase, AttachmentMetadata originalMetadata, int folderId, int eventId) throws OXException {
//        AttachmentMetadata metadata = METADATA_FACTORY.newAttachmentMetadata(originalMetadata);
//        metadata.setId(AttachmentBase.NEW);
//        metadata.setAttachedId(eventId);
//        metadata.setFolderId(folderId);
//        InputStream inputStream = null;
//        try {
//            inputStream = attachmentBase.getAttachedFile(
//                session, originalMetadata.getFolderId(), originalMetadata.getAttachedId(), originalMetadata.getModuleId(), originalMetadata.getId(), context, user, userConfiguration);
//            attachmentBase.attachToObject(metadata, inputStream, session, context, user, userConfiguration);
//            return metadata;
//        } finally {
//            Streams.close(inputStream);
//        }
//    }
//
//    private static List<Attachment> getAttachments(TimedResult<AttachmentMetadata> timedResult) throws OXException {
//        List<Attachment> attachments = new ArrayList<Attachment>();
//        SearchIterator<AttachmentMetadata> iterator = null;
//        try {
//            iterator = timedResult.results();
//            while (iterator.hasNext()) {
//                attachments.add(getAttachment(iterator.next()));
//            }
//        } finally {
//            SearchIterators.close(iterator);
//        }
//        return attachments;
//    }
//
//    private static Attachment getAttachment(AttachmentMetadata metadata) {
//        Attachment attachment = new Attachment();
//        attachment.setFilename(metadata.getFilename());
//        attachment.setFormatType(metadata.getFileMIMEType());
//        attachment.setLastModified(metadata.getCreationDate());
//        attachment.setManagedId(getManagedId(metadata.getFolderId(), metadata.getAttachedId(), metadata.getId()));
//        attachment.setSize(metadata.getFilesize());
//        return attachment;
//    }
//
//    private static List<Attachment> filterBinary(List<Attachment> attachments) {
//        List<Attachment> binaryAttachments = new ArrayList<Attachment>();
//        for (Attachment attachment : attachments) {
//            if (null != attachment.getData()) {
//                binaryAttachments.add(attachment);
//            }
//        }
//        return binaryAttachments;
//    }
//
//    private static List<Attachment> filterManaged(List<Attachment> attachments) {
//        List<Attachment> managedAttachments = new ArrayList<Attachment>();
//        for (Attachment attachment : attachments) {
//            if (null != attachment.getManagedId()) {
//                managedAttachments.add(attachment);
//            }
//        }
//        return managedAttachments;
//    }
//
//    private static AttachmentMetadata getMetadata(Attachment attachment, int folderID, int objectID) {
//        AttachmentMetadata metadata = METADATA_FACTORY.newAttachmentMetadata();
//        metadata.setModuleId(MODULE_ID);
//        metadata.setFolderId(folderID);
//        metadata.setAttachedId(objectID);
//        if (null != attachment.getFormatType()) {
//            metadata.setFileMIMEType(attachment.getFormatType());
//        } else if (null != attachment.getData()) {
//            metadata.setFileMIMEType(attachment.getData().getContentType());
//        }
//        if (null != attachment.getFilename()) {
//            metadata.setFilename(attachment.getFilename());
//        } else if (null != attachment.getData()) {
//            metadata.setFilename(attachment.getData().getName());
//        }
//        if (0 < attachment.getSize()) {
//            metadata.setFilesize(attachment.getSize());
//        } else if (null != attachment.getData()) {
//            metadata.setFilesize(attachment.getData().getLength());
//        }
//        return metadata;
//    }
//
//
//    private static String getManagedId(int folderId, int eventId, int attachmentId) {
//        String name = MODULE_ID + "-" + folderId + "-" + eventId + "-" + attachmentId;
//        return BaseEncoding.base64Url().omitPadding().encode(name.getBytes(Charsets.UTF_8));
//    }
//
//    private static AttachmentMetadata decodeManagedId(String name) throws IllegalArgumentException {
//        String decodedName = new String(BaseEncoding.base64Url().omitPadding().decode(name), Charsets.UTF_8);
//        String[] splitted = Strings.splitByDelimNotInQuotes(decodedName, '-');
//        if (null == splitted || 4 != splitted.length) {
//            throw new IllegalArgumentException(name);
//        }
//        int moduleId, folderId, attachedId, id;
//        try {
//            moduleId = Integer.parseInt(splitted[0]);
//            folderId = Integer.parseInt(splitted[1]);
//            attachedId = Integer.parseInt(splitted[2]);
//            id = Integer.parseInt(splitted[3]);
//        } catch (NumberFormatException e) {
//            throw new IllegalArgumentException(name, e);
//        }
//        AttachmentMetadata metdata = METADATA_FACTORY.newAttachmentMetadata();
//        metdata.setModuleId(moduleId);
//        metdata.setFolderId(folderId);
//        metdata.setAttachedId(attachedId);
//        metdata.setId(id);
//        return metdata;
//    }
//
//}
