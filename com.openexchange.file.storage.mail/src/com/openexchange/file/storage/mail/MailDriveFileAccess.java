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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

package com.openexchange.file.storage.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.search.SubjectTerm;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageMailAttachments;
import com.openexchange.file.storage.FileStorageReadOnly;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.imap.IMAPMessageStorage;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.mime.utils.MimeStorageUtility;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link MailDriveFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class MailDriveFileAccess extends AbstractMailDriveResourceAccess implements FileStorageFileAccess, FileStorageSequenceNumberProvider, FileStorageReadOnly, FileStorageMailAttachments {

    /** The fetch profile for a virtual folder */
    public static final FetchProfile FETCH_PROFILE_VIRTUAL = new FetchProfile() {
        // Unnamed block
        {
            add(IMAPFolder.FetchProfileItem.INTERNALDATE);
            add(FetchProfile.Item.SIZE);
            add(FetchProfile.Item.CONTENT_INFO);
            add(IMAPFolder.FetchProfileItem.HEADERS);
            add(UIDFolder.FetchProfileItem.UID);
            add(MimeStorageUtility.ORIGINAL_MAILBOX);
            add(MimeStorageUtility.ORIGINAL_UID);
        }
    };

    // -----------------------------------------------------------------------------------------------------------------------------------

    private final MailDriveAccountAccess accountAccess;
    final int userId;

    /**
     * Initializes a new {@link MailDriveFileAccess}.
     *
     * @param fullNameCollection The full name collection
     * @param session The session The account access
     * @param accountAccess The account access
     */
    public MailDriveFileAccess(FullNameCollection fullNameCollection, Session session, MailDriveAccountAccess accountAccess) {
        super(fullNameCollection, session);
        this.accountAccess = accountAccess;
        this.userId = session.getUserId();
    }

    @Override
    public void startTransaction() throws OXException {
        // Nope
    }

    @Override
    public void commit() throws OXException {
        // Nope
    }

    @Override
    public void rollback() throws OXException {
        // Nope
    }

    @Override
    public void finish() throws OXException {
        // Nope
    }

    @Override
    public void setTransactional(final boolean transactional) {
        // Nope
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        // Nope
    }

    @Override
    public void setCommitsTransaction(final boolean commits) {
        // Nope
    }

    @Override
    public boolean exists(String folderId, final String id, String version) throws OXException {
        if (FileStorageFileAccess.CURRENT_VERSION != version) {
            return false;
        }

        final FullName fullName = checkFolderId(folderId);

        return perform(new MailDriveClosure<Boolean>() {

            @Override
            protected Boolean doPerform(IMAPStore imapStore, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, MessagingException, IOException {
                IMAPFolder folder = getIMAPFolderFor(fullName, imapStore);
                folder.open(Folder.READ_ONLY);
                try {
                    long uid = parseUnsignedLong(id);
                    return uid < 0 ? Boolean.FALSE : Boolean.valueOf(null != folder.getMessageByUID(uid));
                } finally {
                    folder.close(false);
                }
            }
        }).booleanValue();
    }

    @Override
    public File getFileMetadata(final String folderId, final String id, final String version) throws OXException {
        if (CURRENT_VERSION != version) {
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(MailDriveConstants.ID);
        }

        final FullName fullName = checkFolderId(folderId);

        return perform(new MailDriveClosure<File>() {

            @Override
            protected File doPerform(IMAPStore imapStore, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, MessagingException, IOException {
                IMAPFolder folder = getIMAPFolderFor(fullName, imapStore);
                folder.open(Folder.READ_ONLY);
                try {
                    Message message = folder.getMessageByUID(parseUnsignedLong(id));
                    if (null == message) {
                        throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
                    }

                    return new MailDriveFile(folderId, id, userId, getRootFolderId()).parseMessage((IMAPMessage) message);
                } finally {
                    folder.close(false);
                }
            }
        });
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        return saveFileMetadata(file, sequenceNumber, null);
    }

    @Override
    public IDTuple saveFileMetadata(final File file, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public IDTuple copy(final IDTuple source, String version, final String destFolder, final File update, final InputStream newFile, final List<Field> modifiedFields) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public IDTuple move(final IDTuple source, final String destFolder, long sequenceNumber, final File update, final List<File.Field> modifiedFields) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        if (CURRENT_VERSION != version) {
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(MailDriveConstants.ID);
        }

        FullName fullName = checkFolderId(folderId);

        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        IMAPFolder imapFolder = null;
        boolean error = true;
        try {
            mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();

            imapFolder = (IMAPFolder) getIMAPStore(mailAccess).getFolder(fullName.getFullName());
            imapFolder.open(Folder.READ_ONLY);

            IMAPMessage message = (IMAPMessage) imapFolder.getMessageByUID(parseUnsignedLong(id));
            if (null == message) {
                throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
            }

            InputStream in = new ResourceReleasingInputStream(message.getInputStream(), imapFolder, mailAccess);
            error = false;
            return in;
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw getImapMessageStorageFrom(mailAccess).handleMessagingException(fullName.getFullName(), e);
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (error) {
                closeSafe(imapFolder);
                MailAccess.closeInstance(mailAccess);
            }
        }
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        return saveDocument(file, data, sequenceNumber, null);
    }

    @Override
    public IDTuple saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public void removeDocument(final String folderId, long sequenceNumber) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, long sequenceNumber, final boolean hardDelete) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public void touch(String folderId, String id) throws OXException {
        exists(folderId, id, CURRENT_VERSION);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId) throws OXException {
        final FullName fullName = checkFolderId(folderId);

        if (fullName.isDefaultFolder()) {
            return new FileTimedResult(Collections.<File> emptyList());
        }

        List<File> files = perform(new MailDriveClosure<List<File>>() {

            @Override
            protected List<File> doPerform(IMAPStore imapStore, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, MessagingException, IOException {
                IMAPFolder folder = getIMAPFolderFor(fullName, imapStore);
                folder.open(Folder.READ_ONLY);
                try {
                    int messageCount = folder.getMessageCount();
                    if (messageCount <= 0) {
                        return Collections.<File> emptyList();
                    }

                    List<File> files = new LinkedList<File>();
                    int limit = 100;
                    int offset = 1;

                    do {
                        int end = offset + limit;
                        if (end > messageCount) {
                            end = messageCount;
                        }

                        // Get & fetch messages
                        Message[] messages = folder.getMessages(offset, end);
                        folder.fetch(messages, FETCH_PROFILE_VIRTUAL);

                        // Iterate messages
                        int i = 0;
                        for (int k = messages.length; k-- > 0;) {
                            IMAPMessage message = (IMAPMessage) messages[i++];
                            long uid = message.getUID();
                            if (uid < 0) {
                                uid = folder.getUID(message);
                            }
                            files.add(new MailDriveFile(folderId, Long.toString(uid), userId, getRootFolderId()).parseMessage(message));
                        }

                        // Clear folder's message cache
                        IMAPMessageStorage.clearCache(folder);

                        offset = end + 1;
                    } while (offset <= messageCount);

                    return files;
                } finally {
                    folder.close(false);
                }
            }
        });

        return new FileTimedResult(files);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        return getDocuments(folderId);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        final FullName fullName = checkFolderId(folderId);

        if (fullName.isDefaultFolder()) {
            return new FileTimedResult(Collections.<File> emptyList());
        }

        List<File> files = perform(new MailDriveClosure<List<File>>() {

            @Override
            protected List<File> doPerform(IMAPStore imapStore, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, MessagingException, IOException {
                IMAPFolder folder = getIMAPFolderFor(fullName, imapStore);
                folder.open(Folder.READ_ONLY);
                try {
                    int messageCount = folder.getMessageCount();
                    if (messageCount <= 0) {
                        return Collections.<File> emptyList();
                    }

                    List<File> files = new LinkedList<File>();
                    int limit = 100;
                    int offset = 1;

                    do {
                        int end = offset + limit;
                        if (end > messageCount) {
                            end = messageCount;
                        }

                        // Get & fetch messages
                        Message[] messages = folder.getMessages(offset, end);
                        folder.fetch(messages, FETCH_PROFILE_VIRTUAL);

                        // Iterate messages
                        int i = 0;
                        for (int k = messages.length; k-- > 0;) {
                            IMAPMessage message = (IMAPMessage) messages[i++];
                            long uid = message.getUID();
                            if (uid < 0) {
                                uid = folder.getUID(message);
                            }
                            files.add(new MailDriveFile(folderId, Long.toString(uid), userId, getRootFolderId()).parseMessage(message, fields));
                        }

                        // Clear folder's message cache
                        IMAPMessageStorage.clearCache(folder);

                        offset = end + 1;
                    } while (offset <= messageCount);

                    return files;
                } finally {
                    folder.close(false);
                }
            }
        });

        // Sort collection if needed
        sort(files, sort, order);

        return new FileTimedResult(files);
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, final List<Field> fields) throws OXException {
        if (null == ids) {
            return new FileTimedResult(Collections.<File> emptyList());
        }

        final int size = ids.size();
        if (size <= 0) {
            return new FileTimedResult(Collections.<File> emptyList());
        }

        final Map<FullName, List<UidAndIndex>> uids = new HashMap<FullName, List<UidAndIndex>>(6, 0.9f);
        {
            Map<String, FullName> checkedFolders = new HashMap<String, FullName>(6, 0.9f);
            int i = 0;
            for (IDTuple id : ids) {
                String folderId = id.getFolder();

                FullName fullName = checkedFolders.get(folderId);
                if (null == fullName) {
                    fullName = checkFolderId(folderId);
                    checkedFolders.put(folderId, fullName);
                }

                List<UidAndIndex> l = uids.get(fullName);
                if (null == l) {
                    l = new ArrayList<UidAndIndex>();
                    uids.put(fullName, l);
                }

                long uid = parseUnsignedLong(id.getId());
                if (uid >= 0) {
                    l.add(new UidAndIndex(uid, i));
                }
                i++;
            }
        }

        return perform(new MailDriveClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(IMAPStore imapStore, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, MessagingException, IOException {
                File[] files = new File[size];

                int limit = 100;
                for (Map.Entry<FullName, List<UidAndIndex>> toFetch : uids.entrySet()) {
                    FullName fullName = toFetch.getKey();
                    IMAPFolder folder = getIMAPFolderFor(fullName, imapStore);
                    folder.open(Folder.READ_ONLY);
                    try {
                        if (folder.getMessageCount() <= 0) {
                            // Folder is empty
                            for (UidAndIndex uid : toFetch.getValue()) {
                                files[uid.index] = null;
                            }
                        } else {
                            List<UidAndIndex> uids = toFetch.getValue();
                            int numUids = uids.size();
                            int offset = 0;

                            do {
                                int end = offset + limit;
                                int cSize;
                                if (end > numUids) {
                                    end = numUids;
                                    cSize = end - offset;
                                } else {
                                    cSize = limit;
                                }

                                // Get & fetch messages
                                Message[] messages;
                                Map<Long, Integer> indexes;
                                {
                                    long[] grabMe = new long[cSize];
                                    indexes = new HashMap<Long, Integer>(cSize, 0.9f);
                                    for (int k = offset; k < end; k++) {
                                        UidAndIndex uidi = uids.get(k);
                                        grabMe[k] = uidi.uid;
                                        indexes.put(Long.valueOf(uidi.uid), Integer.valueOf(uidi.index));
                                    }
                                    messages = folder.getMessagesByUID(grabMe);
                                    folder.fetch(messages, FETCH_PROFILE_VIRTUAL);
                                }

                                // Iterate messages
                                int i = 0;
                                for (int k = messages.length; k-- > 0;) {
                                    IMAPMessage message = (IMAPMessage) messages[i++];
                                    if (null != message) {
                                        long uid = message.getUID();
                                        if (uid < 0) {
                                            uid = folder.getUID(message);
                                        }

                                        Integer index = indexes.get(Long.valueOf(uid));
                                        if (null != index) {
                                            files[index.intValue()] = new MailDriveFile(fullName.getFolderId(), Long.toString(uid), userId, getRootFolderId()).parseMessage(message, fields);
                                        }
                                    }
                                }

                                // Clear folder's message cache
                                IMAPMessageStorage.clearCache(folder);

                                offset = end;
                            } while (offset < numUids);
                        }
                    } finally {
                        folder.close(false);
                    }
                }

                return new FileTimedResult(files);
            }
        });
    }

    private static final SearchIterator<File> EMPTY_ITER = SearchIteratorAdapter.emptyIterator();

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public SearchIterator<File> search(final String pattern, List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        return search(pattern, fields, folderId, false, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(final String pattern, final List<Field> fields, final String folderId, final boolean includeSubfolders, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        final List<FullName> fullNames;
        {
            if (null == folderId) {
                fullNames = fullNameCollection.asList();
            } else {
                fullNames = Collections.singletonList(checkFolderId(folderId));
            }
        }

        List<File> files = perform(new MailDriveClosure<List<File>>() {

            @Override
            protected List<File> doPerform(IMAPStore imapStore, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, MessagingException, IOException {
                List<File> files = new LinkedList<File>();

                for (FullName fullName : fullNames) {
                    if (fullName.isNotDefaultFolder()) {
                        IMAPFolder folder = getIMAPFolderFor(fullName, imapStore);
                        folder.open(Folder.READ_ONLY);
                        try {
                            if (folder.getMessageCount() > 0) {
                                Message[] messages = folder.search(new SubjectTerm(pattern));
                                folder.fetch(messages, FETCH_PROFILE_VIRTUAL);

                                int i = 0;
                                for (int k = messages.length; k-- > 0;) {
                                    IMAPMessage message = (IMAPMessage) messages[i++];
                                    long uid = message.getUID();
                                    if (uid < 0) {
                                        uid = folder.getUID(message);
                                    }
                                    files.add(new MailDriveFile(fullName.getFolderId(), Long.toString(uid), userId, getRootFolderId()).parseMessage(message, fields));
                                }
                            }
                        } finally {
                            folder.close(false);
                        }
                    }
                }

                return files;
            }
        });

        // Sort collection
        sort(files, sort, order);

        // Slice...
        if ((start != NOT_SET) && (end != NOT_SET)) {
            int size = files.size();
            if ((start) > size) {
                /*
                 * Return empty iterator if start is out of range
                 */
                return SearchIteratorAdapter.emptyIterator();
            }
            /*
             * Reset end index if out of range
             */
            int toIndex = end;
            if (toIndex >= size) {
                toIndex = size;
            }
            files = files.subList(start, toIndex);
        }

        return new SearchIteratorAdapter<File>(files.iterator(), files.size());
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        if (null == folderIds || 0 == folderIds.size()) {
            return Collections.emptyMap();
        }
        FileStorageFolderAccess folderAccess = getAccountAccess().getFolderAccess();
        Map<String, Long> sequenceNumbers = new HashMap<String, Long>(folderIds.size());
        for (String folderId : folderIds) {
            Date lastModifiedDate = folderAccess.getFolder(folderId).getLastModifiedDate();
            sequenceNumbers.put(folderId, null != lastModifiedDate ? Long.valueOf(lastModifiedDate.getTime()) : null);
        }
        return sequenceNumbers;
    }

    /**
     * Sorts the supplied list of files if needed.
     *
     * @param files The files to sort
     * @param sort The sort order, or <code>null</code> if not specified
     * @param order The sort direction
     */
    public static void sort(List<File> files, Field sort, SortDirection order) {
        if (null != sort && 1 < files.size()) {
            Collections.sort(files, order.comparatorBy(sort));
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------

    /**
     * An <code>InputStream</code> that takes care of releasing/closing resources.
     */
    private static class ResourceReleasingInputStream extends InputStream {

        private final InputStream in;
        private final IMAPFolder imapFolder;
        private final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess;

        ResourceReleasingInputStream(InputStream in,IMAPFolder imapFolder, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
            super();
            this.in = in;
            this.imapFolder = imapFolder;
            this.mailAccess = mailAccess;
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return in.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return in.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return in.skip(n);
        }

        @Override
        public String toString() {
            return in.toString();
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public void close() throws IOException {
            try {
                in.close();
            } finally {
                closeSafe(imapFolder);
                MailAccess.closeInstance(mailAccess);
            }
        }

        @Override
        public void mark(int readlimit) {
            in.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            in.reset();
        }

        @Override
        public boolean markSupported() {
            return in.markSupported();
        }
    }

    private static final class UidAndIndex {

        final long uid;
        final int index;

        UidAndIndex(long uid, int index) {
            super();
            this.uid = uid;
            this.index = index;
        }
    }

}
