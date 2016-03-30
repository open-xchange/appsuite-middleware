package com.openexchange.drive.impl.checksum.sim;
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
// *    trademarks of the OX Software GmbH group of companies.
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
// *     Copyright (C) 2016-2020 OX Software GmbH
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
