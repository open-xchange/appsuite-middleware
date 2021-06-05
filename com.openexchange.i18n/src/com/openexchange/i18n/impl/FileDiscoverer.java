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

package com.openexchange.i18n.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Locale;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class FileDiscoverer {

    private final File dir;

    /**
     * Initializes a new {@link FileDiscoverer}.
     *
     * @param dir The directory
     * @throws FileNotFoundException If directory could not be found
     */
    public FileDiscoverer(final File dir) throws FileNotFoundException {
        super();
        if (!dir.exists()) {
            throw new FileNotFoundException("Unable to load language files. Directory does not exist: " + dir);
        }
        if (dir.isFile()) {
            throw new FileNotFoundException("Unable to load language files." + dir + " is not a directory");
        }
        this.dir = dir;
    }

    /**
     * Gets the files with given file extension
     *
     * @param extension The file extension; e.g. <code>".po"</code>
     * @return The matching files
     */
    public String[] getFilesFromLanguageFolder(final String extension) {
        String[] files = dir.list(new FilenameFilter() {

            @Override
            public boolean accept(final File d, final String f) {
                return f.endsWith(extension);
            }
        });
        if (null == files) {
            return new String[0];
        }
        Arrays.sort(files);
        return files;
    }

    /**
     * Parses the locale from given file name; e.g. <code>"backend.<b>en_US</b>.po"</code>.
     *
     * @param fileName The file name
     * @return The parsed locale or <code>null</code>
     */
    public Locale getLocale(final String fileName) {
        final int indexOfUnderscore = fileName.indexOf('_');
        if (indexOfUnderscore < 0) {
            return null;
        }
        final int indexOfLastDot = fileName.lastIndexOf('.');
        if (indexOfLastDot < indexOfUnderscore) {
            return null;
        }
        final int indexOfDotBeforeUnderscore = fileName.lastIndexOf('.', indexOfUnderscore);

        final String language = fileName.substring(indexOfDotBeforeUnderscore + 1, indexOfUnderscore);
        final String country = fileName.substring(indexOfUnderscore + 1, indexOfLastDot);
        return new Locale(language, country);
    }

    /**
     * Gets the directory.
     *
     * @return The directory
     */
    public File getDirectory() {
        return dir;
    }
}
