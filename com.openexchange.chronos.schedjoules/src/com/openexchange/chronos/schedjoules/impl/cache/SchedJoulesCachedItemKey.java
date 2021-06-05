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

package com.openexchange.chronos.schedjoules.impl.cache;

/**
 * {@link SchedJoulesCachedItemKey}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesCachedItemKey {

    private final int contextId;
    private final int itemId;
    private final String locale;

    /**
     * Initialises a new {@link SchedJoulesCachedItemKey}.
     * 
     * @param itemId
     * @param locale
     */
    public SchedJoulesCachedItemKey(final int contextId, final int itemId, final String locale) {
        super();
        this.contextId = contextId;
        this.itemId = itemId;
        this.locale = locale;
    }

    /**
     * Gets the itemId
     *
     * @return The itemId
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Gets the locale
     *
     * @return The locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Gets the contextId
     *
     * @return The contextId
     */
    public int getContextId() {
        return contextId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + itemId;
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchedJoulesCachedItemKey other = (SchedJoulesCachedItemKey) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (itemId != other.itemId) {
            return false;
        }
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SchedJoulesCachedItemKey [contextId=").append(contextId).append(", itemId=").append(itemId).append(", locale=").append(locale).append("]");
        return builder.toString();
    }

}
