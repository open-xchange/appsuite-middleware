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

import java.util.Date;
import java.util.Optional;

/**
 * {@link Item} - Represents an item (mail, appointment, whatever) that is supposed to be exported.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class Item {

    private final String path;
    private final String name;
    private final Optional<Date> date;

    /**
     * Initializes a new {@link Item}.
     *
     * @param path The path prefix; e.g. <code>"INBOX/"</code>
     * @param name The item's name (typically a unique identifier)
     * @param date The optional date or <code>null</code>
     */
    public Item(String path, String name, Date date) {
        super();
        this.path = path == null ? "" : path;
        this.name = name;
        this.date = Optional.ofNullable(date);
    }

    /**
     * Gets the path prefix; e.g. <code>"INBOX/"</code>
     *
     * @return The path prefix
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the optional date.
     *
     * @return The optional date
     */
    public Optional<Date> getOptionalDate() {
        return date;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        if (!(obj instanceof Item)) {
            return false;
        }
        Item other = (Item) obj;
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Item [");
        if (path != null) {
            builder.append("path=").append(path).append(", ");
        }
        if (name != null) {
            builder.append("name=").append(name);
        }
        if (date != null && date.isPresent()) {
            builder.append("date=").append(date.get());
        }
        builder.append("]");
        return builder.toString();
    }

}
