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

package com.openexchange.filestore.utils;

import java.io.File;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import com.openexchange.filestore.utils.osgi.Services;
import com.openexchange.uploaddir.UploadDirService;

/**
 * {@link TempFileHelper}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class TempFileHelper {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(TempFileHelper.class);
    }

    private static final TempFileHelper INSTANCE = new TempFileHelper();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static TempFileHelper getInstance() {
        return INSTANCE;
    }

    /**
     * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
     *
     * @param tmpFile The file or directory to delete, can be {@code null}
     * @return {@code true} if the file or directory was deleted, otherwise {@code false}
     */
    public static boolean deleteQuietly(final File tmpFile) {
        return FileUtils.deleteQuietly(tmpFile);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link TempFileHelper}.
     */
    private TempFileHelper() {
        super();
    }

    /**
     * Creates a new empty file. If this method returns successfully then it is guaranteed that:
     * <ol>
     * <li>The file denoted by the returned abstract pathname did not exist before this method was invoked, and
     * <li>Neither this method nor any of its variants will return the same abstract pathname again in the current invocation of the virtual
     * machine.
     * </ol>
     *
     * @return The optional abstract pathname denoting a newly-created empty file
     */
    public Optional<File> newTempFile() {
        return newTempFile("open-xchange-spoolfile-");
    }

    /**
     * Creates a new empty file. If this method returns successfully then it is guaranteed that:
     * <ol>
     * <li>The file denoted by the returned abstract pathname did not exist before this method was invoked, and
     * <li>Neither this method nor any of its variants will return the same abstract pathname again in the current invocation of the virtual
     * machine.
     * </ol>
     *
     * @param prefix The prefix to use for generated file
     * @return The optional abstract pathname denoting a newly-created empty file
     */
    public Optional<File> newTempFile(String prefix) {
        try {
            UploadDirService service = Services.getService(UploadDirService.class);
            File tmpFile = File.createTempFile(null == prefix ? "open-xchange-spoolfile-" : prefix, ".tmp", service.getUploadDir());
            tmpFile.deleteOnExit();
            return Optional.of(tmpFile);
        } catch (Exception e) {
            LoggerHolder.LOG.warn("Failed to create new temporary file", e);
            return Optional.empty();
        }
    }

}
