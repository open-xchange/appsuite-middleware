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

package com.openexchange.admin.diff.file.domain;

import org.apache.commons.io.FilenameUtils;

/**
 * Domain object that reflects a file marked as configuration file
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ConfigurationFile {

    private String name;

    private String extension;

    private final String rootDirectory;

    private final String pathBelowRootDirectory;

    private final String content;

    private final boolean isOriginal;

    /**
     * Initializes a new {@link ConfigurationFile}.
     *
     * @param name - the name of the file (includes possible file extensions)
     * @param rootDirectory - root directory of the files
     * @param pathBelowRootDirectory - location of the file below the root directory
     * @param content - content of the file
     * @param isOriginal - marker if the file is an original configuration file (true) or from the installation (false)
     */
    public ConfigurationFile(final String name, final String rootDirectory, final String pathBelowRootDirectory, final String content, final boolean isOriginal) {
        this.name = name;
        this.extension = FilenameUtils.getExtension(name);
        this.rootDirectory = rootDirectory;
        this.pathBelowRootDirectory = pathBelowRootDirectory;
        this.content = content;
        this.isOriginal = isOriginal;
    }

    /**
     * Gets the name. This contains also the file name extension
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the extension
     *
     * @return The extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Gets the content
     *
     * @return The content
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the isOriginal
     *
     * @return The isOriginal
     */
    public boolean isOriginal() {
        return isOriginal;
    }

    /**
     * Gets the rootDirectory
     *
     * @return The rootDirectory
     */
    protected String getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Gets the pathBelowRootDirectory
     *
     * @return The pathBelowRootDirectory
     */
    public String getPathBelowRootDirectory() {
        return pathBelowRootDirectory;
    }

    /**
     * Returns the full file name (incl. extension) but no path
     *
     * @return String - full file name (incl. extension) but no path
     */
    public String getFileNameWithExtension() {
        if (this.extension.isEmpty()) {
            return this.getName() + "." + this.getExtension();
        }
        return this.getName();
    }

    /**
     * Returns the full file name (incl. extension) and the path the file is located in
     *
     * @return String - full file name (incl. extension) and the path the file is located in
     */
    public String getFullFilePathWithExtension() {
        return new StringBuilder().append(this.rootDirectory).append(this.pathBelowRootDirectory).append(this.name).toString().replaceAll("//", "/");
    }

    /**
     * Sets the name
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the extension
     *
     * @param extension The extension to set
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder().append(this.getFullFilePathWithExtension()).append("\n").toString();
    }
}
