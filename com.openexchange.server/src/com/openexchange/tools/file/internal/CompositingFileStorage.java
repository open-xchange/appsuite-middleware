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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.file.internal;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import com.openexchange.exception.OXException;
import com.openexchange.tools.file.external.FileStorage;

/**
 * {@link CompositingFileStorage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositingFileStorage implements FileStorage {

    private final Map<String, FileStorage> prefixedStores = new HashMap<String, FileStorage>();

    private FileStorage standardFS;

    private String savePrefix;

    @Override
    public boolean deleteFile(String identifier) throws OXException {
        PreparedName prepared = prepareName(identifier);
        return prepared.fs.deleteFile(prepared.name);
    }

    @Override
    public Set<String> deleteFiles(String[] identifiers) throws OXException {
        Map<FileStorage, List<String>> partitions = new HashMap<FileStorage, List<String>>();
        Map<FileStorage, String> prefixes = new HashMap<FileStorage, String>();

        for (String name : identifiers) {
            PreparedName preparedName = prepareName(name);

            List<String> list = partitions.get(preparedName.fs);
            if(list == null) {
                list = new LinkedList<String>();
                partitions.put(preparedName.fs, list);
            }

            list.add(preparedName.name);
            if(preparedName.prefix != null) {
                prefixes.put(preparedName.fs, preparedName.prefix);
            }
        }
        Set<String> notDeleted = new HashSet<String>();
        for(Map.Entry<FileStorage, List<String>> entry: partitions.entrySet()) {
            FileStorage fileStorage = entry.getKey();
            List<String> ids = entry.getValue();

            Set<String> files = fileStorage.deleteFiles(ids.toArray(new String[ids.size()]));
            String prefix = prefixes.get(fileStorage);
            if(prefix == null) {
                notDeleted.addAll(files);
            } else {
                for(String file: files) {
                    notDeleted.add(prefix+"/"+file);
                }
            }
        }

        return notDeleted;
    }

    @Override
    public InputStream getFile(String name) throws OXException {
        PreparedName prepared = prepareName(name);
        return prepared.fs.getFile(prepared.name);
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        SortedSet<String> fileList = standardFS.getFileList();
        for(Map.Entry<String, FileStorage> entry: prefixedStores.entrySet()) {
            String prefix = entry.getKey();
            FileStorage fileStorage = entry.getValue();

            SortedSet<String> files = fileStorage.getFileList();
            for (String file : files) {
                fileList.add(prefix+"/"+file);
            }
        }
        return fileList;
    }

    @Override
    public long getFileSize(String name) throws OXException {
        PreparedName preparedName = prepareName(name);
        return preparedName.fs.getFileSize(preparedName.name);
    }

    @Override
    public String getMimeType(String name) throws OXException {
        return standardFS.getMimeType(name);
    }

    @Override
    public void recreateStateFile() throws OXException {
        standardFS.recreateStateFile();
        for(FileStorage fs: prefixedStores.values()) {
            fs.recreateStateFile();
        }
    }

    @Override
    public void remove() throws OXException {
        standardFS.remove();
        for(FileStorage fs: prefixedStores.values()) {
            fs.remove();
        }
    }

    @Override
    public String saveNewFile(InputStream file) throws OXException {
        if (savePrefix != null) {
            return saveNewFileInPrefixedSto(savePrefix, file);
        }
        return standardFS.saveNewFile(file);
    }

    protected String saveNewFileInPrefixedSto(String prefix, InputStream file) throws OXException {
        FileStorage fileStorage = prefixedStores.get(prefix);
        return prefix + "/" + fileStorage.saveNewFile(file);
    }

    @Override
    public boolean stateFileIsCorrect() throws OXException {
        boolean stateFileIsCorrect = standardFS.stateFileIsCorrect();
        if(!stateFileIsCorrect) {
            return false;
        }
        for(FileStorage fs: prefixedStores.values()) {
            boolean isCorrect = fs.stateFileIsCorrect();
            if(!isCorrect) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long appendToFile(InputStream file, String name, long offset) throws OXException {
        PreparedName prepared = prepareName(name);
        return prepared.fs.appendToFile(file, prepared.name, offset);
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        PreparedName prepared = prepareName(name);
        prepared.fs.setFileLength(length, prepared.name);
    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        PreparedName prepared = prepareName(name);
        return prepared.fs.getFile(prepared.name, offset, length);
    }

    public void addStore(FileStorage fs) {
        standardFS = fs;
    }

    public void addStore(String prefix, FileStorage fs) {
        prefixedStores.put(prefix, fs);
    }

    protected PreparedName prepareName(String canonicalName) {
        int idx = canonicalName.indexOf('/');
        if (idx < 0) {
            return new PreparedName(standardFS, canonicalName, null);
        }

        String prefix = canonicalName.substring(0, idx);
        String rest = canonicalName.substring(idx + 1);

        FileStorage fileStorage = prefixedStores.get(prefix);
        if (fileStorage != null) {
            return new PreparedName(fileStorage, rest, prefix);
        }

        return new PreparedName(standardFS, canonicalName, null);
    }

    protected static final class PreparedName {

        public FileStorage fs;
        public String name;
        public String prefix;

        public PreparedName(FileStorage fs, String name, String prefix) {
            super();
            this.fs = fs;
            this.name = name;
            this.prefix = prefix;
        }

    }

    public void setSavePrefix(String savePrefix) {
        this.savePrefix = savePrefix;
    }

}
