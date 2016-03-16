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

package com.openexchange.file.storage.fs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageEfficientRetrieval;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link FSFileAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FSFileAccess implements FileStorageFileAccess, FileStorageEfficientRetrieval {

    private final java.io.File directory;

    private final Session session;

    private final FileStorageAccountAccess accountAccess;

    /**
     * Initialises a new {@link FSFileAccess}.
     *
     * @param file
     * @param session
     */
    public FSFileAccess(java.io.File file, Session session, FileStorageAccountAccess accountAccess) {
        super();
        this.directory = file;
        this.session = session;
        this.accountAccess = accountAccess;
    }

    private java.io.File toFile(String folderId, String id) throws OXException {
        java.io.File file = new java.io.File(toDirectory(folderId), id);

        return file;
    }

    private java.io.File toDirectory(String folderId) throws OXException {
        if (folderId.equals(FileStorageFolder.ROOT_FULLNAME)) {
            return directory;
        }
        java.io.File dir = new java.io.File(directory, folderId);
        if (!dir.getParentFile().equals(directory)) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create("No directory traversal, please");
        }

        return dir;
    }

    private void save(java.io.File fsFile, InputStream in) throws OXException {
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(fsFile));

            int data = -1;
            while ((data = in.read()) != -1) {
                out.write(data);
            }
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void startTransaction() throws OXException {

    }

    @Override
    public void commit() throws OXException {

    }

    @Override
    public void rollback() throws OXException {

    }

    @Override
    public void finish() throws OXException {

    }

    @Override
    public void setTransactional(boolean transactional) {

    }

    @Override
    public void setRequestTransactional(boolean transactional) {

    }

    @Override
    public void setCommitsTransaction(boolean commits) {

    }

    @Override
    public boolean exists(String folderId, String id, String version) throws OXException {
        java.io.File file = toFile(folderId, id);
        return file.exists() && file.canRead();
    }

    @Override
    public File getFileMetadata(String folderId, String id, String version) throws OXException {
        DefaultFile file = new DefaultFile();
        java.io.File fsFile = toFile(folderId, id);

        initFile(folderId, id, file, fsFile);

        return file;
    }

    private void initFile(String folderId, String id, DefaultFile file, java.io.File fsFile) {
        file.setCreated(new Date(0));
        file.setCreatedBy(session.getUserId());
        file.setDescription(fsFile.getName());
        file.setFileSize(fsFile.length());
        file.setFolderId(folderId);
        file.setId(id);
        file.setFileName(fsFile.getName());
        file.setIsCurrentVersion(true);
        file.setLastModified(new Date(fsFile.lastModified()));
        file.setLockedUntil(null);
        file.setModifiedBy(session.getUserId());
        file.setTitle(fsFile.getName());
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        return saveFileMetadata(file, sequenceNumber, Arrays.asList(Field.values()));
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        if (modifiedFields.contains(Field.FILENAME) || modifiedFields.contains(Field.TITLE)) {
            String origName = file.getId();
            String title = file.getTitle();
            String fileName = file.getFileName();

            String renameTo = null;
            if (title != null && !origName.equals(title)) {
                renameTo = title;
            }

            if (fileName != null && !origName.equals(fileName)) {
                renameTo = fileName;
            }

            if (renameTo != null) {
                toFile(file.getFolderId(), origName).renameTo(toFile(file.getFolderId(), renameTo));
                file.setId(renameTo);
            }
            return new IDTuple(file.getFolderId(), file.getId());
        }
        return new IDTuple(file.getFolderId(), file.getId());
    }

    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFile, List<Field> modifiedFields) throws OXException {
        if (version != CURRENT_VERSION) {
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create("File System");
        }

        java.io.File file = toFile(source.getFolder(), source.getId());

        String name = file.getName();
        if (update.getTitle() != null) {
            name = update.getTitle();
        }

        if (update.getFileName() != null) {
            name = update.getFileName();
        }

        if (newFile == null) {
            try {
                newFile = new BufferedInputStream(new FileInputStream(file), 65536);
            } catch (FileNotFoundException e) {
                throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(source.getId(), source.getFolder());
            }
        }

        save(toFile(destFolder, name), newFile);

        return new IDTuple(destFolder, name);
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<Field> modifiedFields) throws OXException {
        java.io.File file = toFile(source.getFolder(), source.getId());

        String name = file.getName();
        if (update.getTitle() != null) {
            name = update.getTitle();
        }

        if (update.getFileName() != null) {
            name = update.getFileName();
        }

        file.renameTo(toFile(destFolder, name));

        return new IDTuple(destFolder, name);
    }

    @Override
    public InputStream getDocument(String folderId, String id, String version) throws OXException {
        try {
            return new FileInputStream(toFile(folderId, id));
        } catch (FileNotFoundException e) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
        }
    }

    @Override
    public Document getDocumentAndMetadata(String folderId, String fileId, String version) throws OXException {
        java.io.File fsFile = toFile(folderId, fileId);

        return toDocument(fsFile);
    }

    private static final String UNKNOWN_CS = "application/octet-stream";

    private Document toDocument(final java.io.File fsFile) {
        String contentType = MimeType2ExtMap.getContentType(fsFile);
        return new Document() {

            @Override
            public InputStream getData() throws OXException {
                try {
                    return new FileInputStream(fsFile);
                } catch (FileNotFoundException e) {
                    throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(fsFile.getAbsolutePath(), "");
                }
            }

        }.setSize(fsFile.length()).setMimeType(UNKNOWN_CS.equals(contentType) ? null : contentType).setEtag("fs://" + fsFile.getAbsolutePath() + "/" + fsFile.lastModified()).setLastModified(fsFile.lastModified());
    }

    @Override
    public Document getDocumentAndMetadata(String folderId, String fileId, String version, String clientETag) throws OXException {
        return getDocumentAndMetadata(folderId, fileId, version);
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        save(toFile(file.getFolderId(), file.getFileName()), data);
        return new IDTuple(file.getFolderId(), file.getId());
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        saveFileMetadata(file, sequenceNumber);
        return saveDocument(file, data, sequenceNumber);
    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        java.io.File directory = toDirectory(folderId);
        java.io.File[] files = directory.listFiles(new OnlyFiles());
        if (files != null) {
            for (java.io.File file : files) {
                file.delete();
            }
        }
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber, boolean hardDelete) throws OXException {
        for (IDTuple idTuple : ids) {
            toFile(idTuple.getFolder(), idTuple.getId()).delete();
        }
        return Collections.emptyList();
    }

    @Override
    public void touch(String folderId, String id) throws OXException {

    }

    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        List<File> list = list(folderId);
        return new FileTimedResult(list);
    }

    private List<File> list(String folderId) throws OXException {
        java.io.File dir = toDirectory(folderId);
        java.io.File[] files = dir.listFiles(new OnlyFiles());
        if (files == null) {
            return Collections.emptyList();
        }
        List<File> list = new ArrayList<File>(files.length);

        for (java.io.File fsFile : files) {
            DefaultFile file = new DefaultFile();
            initFile(folderId, fsFile.getName(), file, fsFile);
            list.add(file);
        }
        return list;
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields) throws OXException {
        return getDocuments(folderId);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        List<File> files = list(folderId);
        Collections.sort(files, order.comparatorBy(sort));
        return new FileTimedResult(files);
    }

    @Override
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        List<File> files = new ArrayList<File>(ids.size());
        for (IDTuple idTuple : ids) {
            java.io.File fsFile = toFile(idTuple.getFolder(), idTuple.getId());
            DefaultFile file = new DefaultFile();
            initFile(idTuple.getFolder(), idTuple.getId(), file, fsFile);
            files.add(file);
        }
        return new FileTimedResult(files);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        java.io.File dir = toDirectory(folderId);
        java.io.File[] files = dir.listFiles(new OnlyFiles());
        if (files == null) {
            return new FileDelta(
                Collections.<File> emptyList(),
                Collections.<File> emptyList(),
                Collections.<File> emptyList(),
                UNDEFINED_SEQUENCE_NUMBER);
        }
        List<File> f = new ArrayList<File>();
        for (java.io.File fsFile : files) {
            if (fsFile.lastModified() > updateSince) {
                DefaultFile file = new DefaultFile();
                initFile(folderId, fsFile.getName(), file, fsFile);
                f.add(file);
            }
        }
        return new FileDelta(
            f,
            Collections.<File> emptyList(),
            Collections.<File> emptyList(),
            UNDEFINED_SEQUENCE_NUMBER);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        java.io.File dir = toDirectory(folderId);
        java.io.File[] files = dir.listFiles(new OnlyFiles());
        if (files == null) {
            return new FileDelta(
                Collections.<File> emptyList(),
                Collections.<File> emptyList(),
                Collections.<File> emptyList(),
                UNDEFINED_SEQUENCE_NUMBER);
        }
        List<File> f = new ArrayList<File>();
        for (java.io.File fsFile : files) {
            if (fsFile.lastModified() > updateSince) {
                DefaultFile file = new DefaultFile();
                initFile(folderId, fsFile.getName(), file, fsFile);
                f.add(file);
            }
        }
        return new FileDelta(
            f,
            Collections.<File> emptyList(),
            Collections.<File> emptyList(),
            UNDEFINED_SEQUENCE_NUMBER);
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        return SearchIteratorAdapter.emptyIterator();
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        return SearchIteratorAdapter.emptyIterator();
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

}
