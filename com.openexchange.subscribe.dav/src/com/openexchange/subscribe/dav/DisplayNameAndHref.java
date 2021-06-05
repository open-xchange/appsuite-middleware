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

package com.openexchange.subscribe.dav;


/**
 * {@link DisplayNameAndHref} - A simple container for a display name and hyper reference (<code>href</code>) value.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DisplayNameAndHref {

    private final String displayName;
    private final String href;
    private int hash = 0;

    /**
     * Initializes a new {@link DisplayNameAndHref}.
     */
    public DisplayNameAndHref(String displayName, String href) {
        super();
        this.displayName = displayName;
        this.href = href;
    }

    /**
     * Gets the display name
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the hyper reference (<code>href</code>)
     *
     * @return The hyper reference (<code>href</code>)
     */
    public String getHref() {
        return href;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            int prime = 31;
            h = 1;
            h = prime * h + ((displayName == null) ? 0 : displayName.hashCode());
            h = prime * h + ((href == null) ? 0 : href.hashCode());
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DisplayNameAndHref)) {
            return false;
        }
        DisplayNameAndHref other = (DisplayNameAndHref) obj;
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (href == null) {
            if (other.href != null) {
                return false;
            }
        } else if (!href.equals(other.href)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DisplayNameAndHref [");
        if (displayName != null) {
            builder.append("displayName=").append(displayName).append(", ");
        }
        if (href != null) {
            builder.append("href=").append(href);
        }
        builder.append("]");
        return builder.toString();
    }

}
