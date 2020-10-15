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

package com.openexchange.file.storage.xox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link XOXFileConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class XOXFileConverter {

    private final EntityHelper entityHelper;

    /**
     * Initializes a new {@link XOXFileConverter}.
     * 
     * @param entityHelper The underlying entity helper to resolve & mangle remote entities
     */
    public XOXFileConverter(EntityHelper entityHelper) {
        super();
        this.entityHelper = entityHelper;
    }

    /**
     * Converts a remote file into its file storage equivalent.
     *
     * @param remoteFile The remote file to convert
     * @return The file storage folder
     */
    public XOXFile getStorageFile(DefaultFile remoteFile) {
        return getStorageFile(remoteFile, null);
    }

    /**
     * Converts a remote file into its file storage equivalent.
     *
     * @param remoteFile The remote file to convert
     * @param fields The fields as requested by the client, or <code>null</code> to consider all fields
     * @return The file storage file
     */
    private XOXFile getStorageFile(DefaultFile remoteFile, Set<Field> fields) {
        if (null == remoteFile) {
            return null;
        }
        /*
         * get file with entities under perspective of remote guest session in foreign context
         */
        XOXFile file = new XOXFile(remoteFile);
        /*
         * qualify remote entities for usage in local session in storage account's context & erase ambiguous numerical identifiers
         */
        if (null == fields || fields.contains(Field.CREATED_FROM)) {
            file.setCreatedFrom(entityHelper.mangleRemoteEntity(null == remoteFile.getCreatedFrom() && 0 < remoteFile.getCreatedBy() ? 
                entityHelper.optEntityInfo(remoteFile.getCreatedBy(), false) : remoteFile.getCreatedFrom()));
        }
        file.setCreatedBy(0);
        if (null == fields || fields.contains(Field.MODIFIED_FROM)) {
            file.setModifiedFrom(entityHelper.mangleRemoteEntity(null == remoteFile.getModifiedFrom() && 0 < remoteFile.getModifiedBy() ? 
                entityHelper.optEntityInfo(remoteFile.getModifiedBy(), false) : remoteFile.getModifiedFrom()));
        }
        file.setModifiedBy(0);
        /*
         * enhance & qualify remote entities in object permissions for usage in local session in storage account's context
         */
        //TODO: directly get from extended object permission field if possible
        file.setObjectPermissions(entityHelper.mangleRemoteObjectPermissions(entityHelper.addObjectPermissionEntityInfos(remoteFile.getObjectPermissions())));
        /*
         * assume not shareable by default
         */
        file.setShareable(false);
        return file;
    }

    /**
     * Converts a list of remote files into its file storage equivalent, and returns them as {@link SearchIterator}.
     *
     * @param remoteFile The remote file to convert
     * @param fields The fields as requested by the client, or <code>null</code> to consider all fields
     * @return The search iterator of file storage files
     */
    public SearchIterator<File> getStorageSearchIterator(List<DefaultFile> remoteFiles, List<Field> fields) {
        if (null == remoteFiles) {
            return null;
        }
        Set<Field> requestedFields = null == fields ? null : new HashSet<Field>(fields);
        List<File> files = new ArrayList<File>(remoteFiles.size());
        for (DefaultFile remoteFile : remoteFiles) {
            files.add(getStorageFile(remoteFile, requestedFields));
        }
        return new SearchIteratorAdapter<File>(files.iterator(), files.size());
    }

    /**
     * Converts a list of remote files into its file storage equivalent, and returns them as {@link TimedResult}.
     *
     * @param remoteFile The remote file to convert
     * @param fields The fields as requested by the client, or <code>null</code> to consider all fields
     * @return The timed result of file storage files
     */
    public TimedResult<File> getStorageTimedResult(List<DefaultFile> remoteFiles, List<Field> fields) {
        if (null == remoteFiles) {
            return null;
        }
        if (remoteFiles.isEmpty()) {
            return com.openexchange.groupware.results.Results.emptyTimedResult();
        }
        Set<Field> requestedFields = null == fields ? null : new HashSet<Field>(fields);
        long maxSequenceNumber = 0L;
        List<File> files = new ArrayList<File>(remoteFiles.size());
        for (DefaultFile remoteFile : remoteFiles) {
            maxSequenceNumber = Math.max(maxSequenceNumber, remoteFile.getSequenceNumber());
            files.add(getStorageFile(remoteFile, requestedFields));
        }
        long sequenceNumber = maxSequenceNumber;
        return new TimedResult<File>() {

            @Override
            public SearchIterator<File> results() throws OXException {
                return new SearchIteratorAdapter<File>(files.iterator(), files.size());
            }

            @Override
            public long sequenceNumber() throws OXException {
                return sequenceNumber;
            }
        };
    }

    /**
     * Converts a file storage file to its remote file equivalent.
     *
     * @param file The file storage file to convert
     * @return The remote file
     */
    public DefaultFile getRemoteFile(File file) {
        if (null == file) {
            return null;
        }
        DefaultFile remoteFile = new DefaultFile(file);

        //TODO: unmangle..
        return remoteFile;
    }

}
