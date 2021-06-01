package com.openexchange.drive.impl.checksum.sim;
///*
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
//
//package com.openexchange.drive.checksum.sim;
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//import com.openexchange.drive.checksum.ChecksumStore;
//import com.openexchange.exception.OXException;
//import com.openexchange.file.storage.File;
//
///**
// * {@link SimChecksumStore}
// *
// * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
// */
//public class SimChecksumStore implements ChecksumStore {
//
//    private final Map<File, String> checksums;
//    private final Map<String, Set<File>> files;
//
//    /**
//     * Initializes a new {@link SimChecksumStore}.
//     */
//    public SimChecksumStore() {
//        super();
//        this.checksums = new HashMap<File, String>();
//        this.files = new HashMap<String, Set<File>>();
//    }
//
//    @Override
//    public String getChecksum(File file) {
//        return checksums.get(file);
//    }
//
//    @Override
//    public Collection<File> getFiles(String checksum) {
//        return files.get(checksum);
//    }
//
//    @Override
//    public synchronized void addChecksum(File file, String checksum) {
//        checksums.put(file, checksum);
//        Set<File> ids = files.get(checksum);
//        if (null == ids) {
//            ids = new HashSet<File>();
//            files.put(checksum, ids);
//        }
//        ids.add(file);
//    }
//
//    @Override
//    public synchronized void removeChecksums(File file) {
//        String checksum = checksums.remove(file);
//        Set<File> ids = files.get(checksum);
//        if (null != ids) {
//            ids.remove(checksum);
//        }
//        if (0 == ids.size()) {
//            files.remove(ids);
//        }
//    }
//
//    @Override
//    public Map<File, String> getFilesInFolder(String folderID) throws OXException {
//        Map<File, String> files = new HashMap<File, String>();
//        for (Entry<File, String> entry : this.checksums.entrySet()) {
//            if (folderID.equals(entry.getKey().getFolderId())) {
//                files.put(entry.getKey(), entry.getValue());
//            }
//        }
//        return files;
//    }
//
//    @Override
//    public void updateFolderIDs(String currentFolderID, String newFolderID) throws OXException {
//        for (Entry<File, String> entry : this.checksums.entrySet()) {
//            if (currentFolderID.equals(entry.getKey().getFolderId())) {
//                entry.getKey().setFolderId(newFolderID);
//            }
//        }
//
//    }
//
//    @Override
//    public void addFolder(String folderID, long sequenceNumber, String checksum) throws OXException {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void removeFolder(String folderID) throws OXException {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public Entry<String, Long> getFolder(String folderID) throws OXException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//}
//
