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

package com.openexchange.drive.client.windows.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.drive.client.windows.service.UpdaterExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link FileSystemResourceLoader}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class FileSystemResourceLoader extends AbstractResourceLoader {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FileSystemResourceLoader.class);

    private final File parentDir;

    public FileSystemResourceLoader(String path) {
        this.parentDir = new File(path);
    }

    @Override
    public synchronized InputStream get(String name) throws IOException {
        File f = new File(parentDir, name).getAbsoluteFile();
        checkAccessible(f);
        checkAbsoluteInSubpath(f);
        return new FileInputStream(f);
    }

    @Override
    public synchronized File getFile(String name) throws IOException {
        File f = new File(parentDir, name).getAbsoluteFile();
        checkAccessible(f);
        checkAbsoluteInSubpath(f);
        return f;
    }

    @Override
    public synchronized long getSize(String name) throws IOException {
        File f = new File(parentDir, name).getAbsoluteFile();
        checkAccessible(f);
        checkAbsoluteInSubpath(f);
        return f.length();
    }

    @Override
    public synchronized boolean exists(String name) {
        File f = new File(parentDir, name).getAbsoluteFile();
        if (f.exists()) {
            return true;
        }
        return false;
    }

    @Override
    public synchronized Set<String> getAvailableFiles() throws OXException {
        String[] filenames = parentDir.list();

        if (filenames == null) {
            throw UpdaterExceptionCodes.NO_FILES_ERROR.create(parentDir.getAbsolutePath());
        }

        Set<String> retval = new HashSet<String>(Arrays.asList(filenames));
        return retval;
    }

    private synchronized void checkAbsoluteInSubpath(File f) throws FileNotFoundException {
        File current = f;

        while (current != null) {
            current = current.getParentFile();
            if (current.equals(parentDir)) {
                return;
            }
        }
        LOG.error("Trying to leave designated directory with a relative path. Denying request.");
        throw new FileNotFoundException();
    }

    private synchronized void checkAccessible(File f) throws FileNotFoundException {
        if (!f.exists()) {
            LOG.info("Trying to load {} but it doesn''t exist", f);
            throw new FileNotFoundException();
        }
        if (!f.canRead()) {
            LOG.error("Trying to load {} but the groupware can not read it", f);
            throw new FileNotFoundException();
        }
        if (!f.isFile()) {
            LOG.error("Trying to load {} but it''s not a file", f);
            throw new FileNotFoundException();
        }
    }
}
