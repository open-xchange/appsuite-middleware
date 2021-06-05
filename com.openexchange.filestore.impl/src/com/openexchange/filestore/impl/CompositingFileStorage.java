/*
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

package com.openexchange.filestore.impl;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import com.google.common.collect.ImmutableMap;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;

/**
 * {@link CompositingFileStorage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositingFileStorage implements FileStorage {

    private final Map<String, FileStorage> prefixedStores;
    private final FileStorage standardFS;
    private final String savePrefix;

    /**
     * Initializes a new {@link CompositingFileStorage}.
     *
     * @param standardFS The fall-back storage to use in case no prefix is given
     * @param savePrefix The standard prefix to assume when saving files
     * @param prefixedStores The storages associated with a certain prefix; may be <code>null</code>
     */
    public CompositingFileStorage(FileStorage standardFS, String savePrefix, Map<String, FileStorage> prefixedStores) {
        super();
        this.standardFS = standardFS;
        this.savePrefix = savePrefix;
        this.prefixedStores = null == prefixedStores ? Collections.<String, FileStorage> emptyMap() : ImmutableMap.<String, FileStorage> copyOf(prefixedStores);
    }

    @Override
    public URI getUri() {
        return standardFS.getUri();
    }

    @Override
    public boolean deleteFile(String identifier) throws OXException {
        ImmutablePreparedName prepared = prepareName(identifier);
        return prepared.fs.deleteFile(prepared.name);
    }

    @Override
    public Set<String> deleteFiles(String[] identifiers) throws OXException {
        Map<FileStorage, List<String>> partitions = new HashMap<FileStorage, List<String>>();
        Map<FileStorage, String> prefixes = new HashMap<FileStorage, String>();

        for (String name : identifiers) {
            ImmutablePreparedName preparedName = prepareName(name);

            List<String> list = partitions.get(preparedName.fs);
            if (list == null) {
                list = new LinkedList<String>();
                partitions.put(preparedName.fs, list);
            }

            list.add(preparedName.name);
            if (preparedName.prefix != null) {
                prefixes.put(preparedName.fs, preparedName.prefix);
            }
        }
        Set<String> notDeleted = new HashSet<String>();
        for(Map.Entry<FileStorage, List<String>> entry: partitions.entrySet()) {
            FileStorage fileStorage = entry.getKey();
            List<String> ids = entry.getValue();

            Set<String> files = fileStorage.deleteFiles(ids.toArray(new String[ids.size()]));
            String prefix = prefixes.get(fileStorage);
            if (prefix == null) {
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
        ImmutablePreparedName prepared = prepareName(name);
        return prepared.fs.getFile(prepared.name);
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        // Get the ones from standard file storage
        SortedSet<String> fileList = standardFS.getFileList();

        // Add the ones from prefixed file storages
        StringBuilder pathBuilder = null;
        for (Map.Entry<String, FileStorage> entry : prefixedStores.entrySet()) {
            String prefix = entry.getKey();
            FileStorage fileStorage = entry.getValue();

            SortedSet<String> files = fileStorage.getFileList();
            for (String file : files) {
                if (null == pathBuilder) {
                    pathBuilder = new StringBuilder(64);
                } else {
                    pathBuilder.setLength(0);
                }
                fileList.add(pathBuilder.append(prefix).append('/').append(file).toString());
            }
        }

        return fileList;
    }

    @Override
    public long getFileSize(String name) throws OXException {
        ImmutablePreparedName preparedName = prepareName(name);
        return preparedName.fs.getFileSize(preparedName.name);
    }

    @Override
    public String getMimeType(String name) throws OXException {
        return standardFS.getMimeType(name);
    }

    @Override
    public void recreateStateFile() throws OXException {
        standardFS.recreateStateFile();
        for (FileStorage fs : prefixedStores.values()) {
            fs.recreateStateFile();
        }
    }

    @Override
    public void remove() throws OXException {
        standardFS.remove();
        for (FileStorage fs : prefixedStores.values()) {
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
        return new StringBuilder(prefix).append('/').append(fileStorage.saveNewFile(file)).toString();
    }

    @Override
    public boolean stateFileIsCorrect() throws OXException {
        boolean stateFileIsCorrect = standardFS.stateFileIsCorrect();
        if (!stateFileIsCorrect) {
            return false;
        }
        for (FileStorage fs : prefixedStores.values()) {
            boolean isCorrect = fs.stateFileIsCorrect();
            if (!isCorrect) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long appendToFile(InputStream file, String name, long offset) throws OXException {
        ImmutablePreparedName prepared = prepareName(name);
        return prepared.fs.appendToFile(file, prepared.name, offset);
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        ImmutablePreparedName prepared = prepareName(name);
        prepared.fs.setFileLength(length, prepared.name);
    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        ImmutablePreparedName prepared = prepareName(name);
        return prepared.fs.getFile(prepared.name, offset, length);
    }

    /**
     * Gets the name to file storage associated for given canonical name; e.g. <code>"hashed/ab/cd/ef/334234234"</code>.
     *
     * @param canonicalName The canonical name to resolve
     * @return The name to file storage association
     */
    protected ImmutablePreparedName prepareName(String canonicalName) {
        int idx = canonicalName.indexOf('/');
        if (idx < 0) {
            return new ImmutablePreparedName(standardFS, canonicalName, null);
        }

        String prefix = canonicalName.substring(0, idx);
        String rest = canonicalName.substring(idx + 1);

        FileStorage fileStorage = prefixedStores.get(prefix);
        if (fileStorage != null) {
            return new ImmutablePreparedName(fileStorage, rest, prefix);
        }

        return new ImmutablePreparedName(standardFS, canonicalName, null);
    }

    protected static final class ImmutablePreparedName {

        final FileStorage fs;
        final String name;
        final String prefix;

        ImmutablePreparedName(FileStorage fs, String name, String prefix) {
            super();
            this.fs = fs;
            this.name = name;
            this.prefix = prefix;
        }

    }

}
