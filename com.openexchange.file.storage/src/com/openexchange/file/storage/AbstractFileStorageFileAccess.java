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

package com.openexchange.file.storage;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link AbstractFileStorageFileAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AbstractFileStorageFileAccess implements FileStorageFileAccess {

    static final String CAPABILITY_SEQUENCE_NUMBERS = "com.openexchange.file.storage.sequenceNumbers";
    static final String CAPABILITY_ETAGS = "com.openexchange.file.storage.eTags";
    static final String CAPABILITY_ETAGS_RECURSIVE = "com.openexchange.file.storage.eTagsRecursive";
    static final String CAPABILITY_TRANSACTIONAL = "com.openexchange.file.storage.transactional";

    /**
     * A list of all known file fields.
     */
    static final List<File.Field> ALL_FIELDS = Arrays.asList(File.Field.values());


    public Map<String, Object> getCapabilities() {

        return null;
    }




    @Override
    public void startTransaction() throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void commit() throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback() throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void finish() throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTransactional(boolean transactional) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRequestTransactional(boolean transactional) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean exists(String folderId, String id, String version) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public File getFileMetadata(String folderId, String id, String version) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        return saveFileMetadata(file, sequenceNumber, Arrays.asList(File.Field.values()));
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFile, List<Field> modifiedFields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<Field> modifiedFields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getDocument(String folderId, String id, String version) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        return saveDocument(file, data, sequenceNumber, ALL_FIELDS);
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber, boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void touch(String folderId, String id) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        return getDocuments(folderId, ALL_FIELDS);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields) throws OXException {
        return getDocuments(folderId, fields, null, SortDirection.DEFAULT);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return getDelta(folderId, updateSince, fields, null, SortDirection.DEFAULT, ignoreDeleted);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        // TODO Auto-generated method stub
        return null;
    }

}
