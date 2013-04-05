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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.internal;

import com.openexchange.drive.checksum.ChecksumProvider;
import com.openexchange.drive.checksum.ChecksumStore;
import com.openexchange.drive.sim.checksum.SimChecksumStore;
import com.openexchange.drive.storage.DriveStorage;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DriveSession}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveSession {

    private final ServerSession session;

    private IDBasedFileAccess fileAccess;
    private ChecksumStore checksumStore;
    private ChecksumProvider checksumProvider;
//    private FolderService folderService;
//    private FolderHelper folderHelper;
    private final String rootFolderID;
    private DriveStorage storage;

    /**
     * Initializes a new {@link DriveSession}.
     */
    public DriveSession(ServerSession session, String rootFolderID, ChecksumStore checksumStore, ChecksumProvider checksumProvider) {
        super();
        this.session = session;
        this.rootFolderID = rootFolderID;
        this.checksumProvider = checksumProvider;
        this.checksumStore = checksumStore;
    }

    public File getFileByName(String folderID, final String fileName) throws OXException {
        TimedResult<File> documents = getFileAccess().getDocuments(folderID);
        return find(documents, new Predicate<File>() {

            @Override
            public boolean matches(File t) {
                return fileName.equals(t.getFileName());
            }
        });
    }

    public String getMD5(File file) throws OXException {
        return checksumProvider.getMD5(file, getStorage());
    }

    public ServerSession getServerSession() {
        return session;
    }

    public DriveStorage getStorage() {
        if (null == storage) {
            storage = new DriveStorage(this, rootFolderID);
        }
        return storage;
    }
//
//    public FolderHelper getFolderHelper() {
//        if (null == folderHelper) {
//            folderHelper = new FolderHelper(this, rootFolderID);
//        }
//        return folderHelper;
//    }

    /**
     * Gets the fileAccess
     *
     * @return The fileAccess
     * @throws OXException
     */
    public IDBasedFileAccess getFileAccess() throws OXException {
        if (null == fileAccess) {
            fileAccess = DriveServiceLookup.getService(IDBasedFileAccessFactory.class, true).createAccess(session);
        }
        return fileAccess;
    }

    /**
     * Gets the folder service
     *
     * @return The folder service
     * @throws OXException
     */
//    public FolderService getFolderService() throws OXException {
//        if (null == folderService) {
//            folderService = DriveServiceLookup.getService(FolderService.class, true);
//        }
//        return folderService;
//    }

    /**
     * Gets the checksumStore
     *
     * @return The checksumStore
     */
    public ChecksumStore getChecksumStore() {
        if (null == checksumStore) {
            checksumStore = new SimChecksumStore();
        }
        return checksumStore;
    }

    /**
     * Gets the checksumProvider
     *
     * @return The checksumProvider
     */
    public ChecksumProvider getChecksumProvider() {
        if (null == checksumProvider) {
            checksumProvider = new ChecksumProvider(getChecksumStore());
        }
        return checksumProvider;
    }

    private static <T> T find(TimedResult<T> ts, Predicate<T> predicate) throws OXException {
        SearchIterator<T> searchIterator = null;
        try {
            searchIterator = ts.results();
            while (searchIterator.hasNext()) {
                T t = searchIterator.next();
                if (predicate.matches(t)) {
                    return t;
                }
            }
        } finally {
            if (null != searchIterator) {
                searchIterator.close();
            }
        }
        return null;
    }

    private interface Predicate<T> {

        boolean matches(T t);
    }

}
