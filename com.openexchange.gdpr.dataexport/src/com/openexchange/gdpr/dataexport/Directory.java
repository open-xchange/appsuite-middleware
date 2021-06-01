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

package com.openexchange.gdpr.dataexport;

/**
 * {@link Directory} - Represents a directory (or any other adequate hierarchy delimiter) that is supposed to be exported.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class Directory {

    private final String parentPath;
    private final String name;

    /**
     * Initializes a new {@link Directory}.
     *
     * @param parentPath The path of the parent directory; e.g. <code>"INBOX/"</code>
     * @param name The directory's name (typically a unique identifier)
     */
    public Directory(String parentPath, String name) {
        super();
        this.parentPath = parentPath == null ? "" : parentPath;
        this.name = name;
    }

    /**
     * Gets the path of the parent directory; e.g. <code>"INBOX/"</code>
     *
     * @return The path of the parent directory
     */
    public String getParentPath() {
        return parentPath;
    }

    /**
     * Gets the directory name
     *
     * @return The directory name
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parentPath == null) ? 0 : parentPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Directory)) {
            return false;
        }
        Directory other = (Directory) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (parentPath == null) {
            if (other.parentPath != null) {
                return false;
            }
        } else if (!parentPath.equals(other.parentPath)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Directory [");
        if (parentPath != null) {
            builder.append("pathPrefix=").append(parentPath).append(", ");
        }
        if (name != null) {
            builder.append("name=").append(name);
        }
        builder.append("]");
        return builder.toString();
    }

}
