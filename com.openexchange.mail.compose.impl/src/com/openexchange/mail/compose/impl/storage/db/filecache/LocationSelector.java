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

package com.openexchange.mail.compose.impl.storage.db.filecache;

import static com.openexchange.mail.compose.impl.storage.db.filecache.FileCache.FILE_NAME_PREFIX;
import static com.openexchange.mail.compose.impl.storage.db.filecache.FileCacheImpl.buildFileName;
import static com.openexchange.mail.compose.impl.storage.db.filecache.FileCacheImpl.deleteFileSafe;
import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.UserAndContext;
import com.openexchange.uploaddir.UploadDirService;

/**
 * {@link LocationSelector} - Selects the currently valid location for managing file-cached contents.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class LocationSelector {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(LocationSelector.class);
    }

    private static final String PROP_NAME = "com.openexchange.mail.compose.rdbstorage.content.fileCacheDir";

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;
    private final ConcurrentMap<UserAndContext, Directories> user2directories;

    /**
     * Initializes a new {@link LocationSelector}.
     */
    public LocationSelector(ServiceLookup services) {
        super();
        this.services = services;
        user2directories = new ConcurrentHashMap<>(256, 0.9F, 1);
    }

    /**
     * Gets the currently active location for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The location or empty
     * @throws OXException If determining location fails
     */
    public Optional<File> getLocation(int userId, int contextId) throws OXException {
        // Get the configured directory path
        Optional<String> optionalConfiguredDirectoryPath = getConfiguredDirectoryPath(userId, contextId);

        // Determine location
        Directories directories = getDirectoriesFor(userId, contextId, true);
        synchronized (directories) {
            if (directories.isDeprecated()) {
                // Another thread marked it as deprecated
                return getLocation(userId, contextId);
            }

            String effectiveDirectoryPath = optionalConfiguredDirectoryPath.orElse(null);
            if (effectiveDirectoryPath == null) {
                // Not configured. Fall-back to standard upload directory.
                File fallbackLocation = requireFallbackLocation();
                effectiveDirectoryPath = fallbackLocation.getAbsolutePath();
            }

            LocationAndFile dir = directories.get(effectiveDirectoryPath);
            if (dir == null) {
                File newDirectory = new File(effectiveDirectoryPath);
                if (!newDirectory.isDirectory()) {
                    LoggerHolder.LOG.error("File cache directory does either not exist or is not a directory: {}. Using fall-back directory instead.", effectiveDirectoryPath);
                    return Optional.empty();
                }
                if (!newDirectory.canWrite()) {
                    LoggerHolder.LOG.error("File cache directory tdoes not grant write permission: {}. Using fall-back directory instead.", effectiveDirectoryPath);
                    return Optional.empty();
                }
                LocationAndFile newDir = new LocationAndFile(effectiveDirectoryPath, newDirectory);
                dir = directories.putIfAbsent(effectiveDirectoryPath, newDir);
                if (dir == null) {
                    dir = newDir;
                }
            }
            return Optional.of(dir.directory);
        }
    }

    private Optional<String> getConfiguredDirectoryPath(int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (viewFactory == null) {
            return Optional.empty();
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        String configuredDirectoryPath = ConfigViews.getDefinedStringPropertyFrom(PROP_NAME, "", view).trim();
        return Strings.isEmpty(configuredDirectoryPath) ? Optional.empty() : Optional.of(configuredDirectoryPath);
    }

    private Directories getDirectoriesFor(int userId, int contextId, boolean createIfAbsent) {
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        Directories directories = user2directories.get(key);
        if (createIfAbsent && directories == null) {
            Directories newDirectories = new Directories();
            directories = user2directories.putIfAbsent(key, newDirectories);
            if (directories == null) {
                directories = newDirectories;
            }
        }
        return directories;
    }

    private File requireFallbackLocation() throws OXException {
        UploadDirService uploadDirService = services.getOptionalService(UploadDirService.class);
        if (uploadDirService == null) {
            throw ServiceExceptionCode.absentService(UploadDirService.class);
        }

        File uploadDir = uploadDirService.getUploadDir();
        if (uploadDir == null) {
            throw OXException.general("Missing upload directory.");
        }
        return uploadDir;
    }

    /**
     * Gets the file for given arguments.
     *
     * @param compositionSpaceId The composition space identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The file or empty
     */
    public Optional<File> getFileFor(UUID compositionSpaceId, int userId, int contextId) {
        Directories directories = getDirectoriesFor(userId, contextId, false);
        if (directories == null) {
            return Optional.empty();
        }

        synchronized (directories) {
            if (directories.isDeprecated()) {
                // Another thread marked it as deprecated
                return getFileFor(compositionSpaceId, userId, contextId);
            }

            int size = directories.size();
            if (size <= 0) {
                user2directories.remove(UserAndContext.newInstance(userId, contextId));
                directories.markDeprecated();
                return Optional.empty();
            }

            File retval = null;
            boolean anyRemoved = false;
            for (Iterator<LocationAndFile> it = directories.values().iterator(); it.hasNext();) {
                LocationAndFile location = it.next();
                File[] files = getUserFiles(location, userId, contextId);
                if (files == null || files.length <= 0) {
                    // Contains no user files at all
                    it.remove();
                    anyRemoved = true;
                } else {
                    if (retval == null) {
                        String expectedFileName = buildFileName(compositionSpaceId, userId, contextId);
                        for (int i = files.length; retval == null && i-- > 0;) {
                            File file = files[i];
                            if (expectedFileName.equals(file.getName())) {
                                retval = file;
                            }
                        }
                    }
                }
            }
            if (anyRemoved && directories.size() <= 0) {
                user2directories.remove(UserAndContext.newInstance(userId, contextId));
                directories.markDeprecated();
            }
            return Optional.ofNullable(retval);
        }
    }

    /**
     * Deletes the file for given arguments.
     *
     * @param compositionSpaceId The composition space identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void deleteFileFor(UUID compositionSpaceId, int userId, int contextId) {
        Directories directories = getDirectoriesFor(userId, contextId, false);
        if (directories == null) {
            return;
        }

        synchronized (directories) {
            if (directories.isDeprecated()) {
                // Another thread marked it as deprecated
                deleteFileFor(compositionSpaceId, userId, contextId);
                return;
            }

            int size = directories.size();
            if (size <= 0) {
                user2directories.remove(UserAndContext.newInstance(userId, contextId));
                directories.markDeprecated();
                return;
            }

            boolean deleted = false;
            boolean anyRemoved = false;
            for (LocationAndFile location : directories.values()) {
                File[] files = getUserFiles(location, userId, contextId);
                if (files == null || files.length <= 0) {
                    // Contains no user files at all
                    directories.remove(location.location);
                    anyRemoved = true;
                } else {
                    if (!deleted) {
                        String expectedFileName = buildFileName(compositionSpaceId, userId, contextId);
                        for (int i = files.length; !deleted &&  i-- > 0;) {
                            File file = files[i];
                            if (expectedFileName.equals(file.getName())) {
                                deleteFileSafe(file);
                                deleted = true;
                            }
                        }
                        if (deleted && files.length == 1) {
                            // Deleted last file in location
                            directories.remove(location.location);
                            anyRemoved = true;
                        }
                    }
                }
            }
            if (anyRemoved && directories.size() <= 0) {
                user2directories.remove(UserAndContext.newInstance(userId, contextId));
                directories.markDeprecated();
            }
        }
    }

    private File[] getUserFiles(LocationAndFile location, int userId, int contextId) {
        return location.directory.listFiles(new UserFileFilter(userId, contextId));
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class LocationAndFile {

        final String location;
        final File directory;

        LocationAndFile(String location, File directory) {
            super();
            this.location = location;
            this.directory = directory;
        }
    }

    private static class Directories {

        private final Map<String, LocationAndFile> directories;
        private boolean deprecated;

        Directories() {
            super();
            directories = new HashMap<>(4);
        }

        LocationAndFile get(String location) {
            return directories.get(location);
        }

        LocationAndFile putIfAbsent(String location, LocationAndFile newDir) {
            return directories.putIfAbsent(location, newDir);
        }

        void remove(String location) {
            directories.remove(location);
        }

        Collection<LocationAndFile> values() {
            return directories.values();
        }

        int size() {
            return directories.size();
        }

        boolean isDeprecated() {
            return deprecated;
        }

        void markDeprecated() {
            this.deprecated = true;
        }
    }

    private static class UserFileFilter implements FileFilter {

        private final String appendix;

        UserFileFilter(int userId, int contextId) {
            super();
            this.appendix = new StringBuilder(16).append('-').append(userId).append('-').append(contextId).append(".tmp").toString();
        }

        @Override
        public boolean accept(File file) {
            String name = file.getName();
            return name.startsWith(FILE_NAME_PREFIX) && name.endsWith(appendix);
        }
    }

}
