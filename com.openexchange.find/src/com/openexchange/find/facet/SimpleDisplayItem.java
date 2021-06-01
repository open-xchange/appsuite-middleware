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

package com.openexchange.find.facet;

import com.openexchange.i18n.I18nService;

/**
 * A {@link DisplayItem} containing only a (possibly localizable) default value.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class SimpleDisplayItem implements DisplayItem {

    private static final long serialVersionUID = 4905996948698710854L;

    private final String displayName;
    private final boolean isLocalizable;

    /**
     * Initializes a new {@link SimpleDisplayItem} that
     * is not localizable.
     *
     * @param displayName The default value
     */
    public SimpleDisplayItem(final String displayName) {
        this(displayName, false);
    }

    /**
     * Initializes a new {@link SimpleDisplayItem}.
     *
     * @param displayName The default value
     * @param isLocalizable If the default value can be localized via {@link I18nService}.
     */
    public SimpleDisplayItem(final String displayName, final boolean isLocalizable) {
        super();
        this.displayName = displayName;
        this.isLocalizable = isLocalizable;
    }

    public boolean isLocalizable() {
        return isLocalizable;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void accept(DisplayItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + ((isLocalizable) ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleDisplayItem other = (SimpleDisplayItem) obj;
        if (displayName == null) {
            if (other.displayName != null)
                return false;
        } else if (!displayName.equals(other.displayName))
            return false;
        if (isLocalizable != other.isLocalizable)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SimpleDisplayItem [defaultValue=" + displayName + ", isLocalizable=" + isLocalizable + "]";
    }

}
