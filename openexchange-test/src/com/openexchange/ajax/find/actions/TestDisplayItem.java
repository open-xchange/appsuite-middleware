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

package com.openexchange.ajax.find.actions;

import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.DisplayItemVisitor;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class TestDisplayItem implements DisplayItem {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7081613085203483974L;

    private final String name;

    private final String detail;

    private final String imageUrl;

    /**
     * Initializes a new {@link TestDisplayItem}.
     *
     * @param name
     * @param detail
     * @param imageUrl
     */
    public TestDisplayItem(String name, String detail, String imageUrl) {
        super();
        this.name = name;
        this.detail = detail;
        this.imageUrl = imageUrl;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    public String getDetail() {
        return detail;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public void accept(DisplayItemVisitor visitor) {
        // no op
    }

}
