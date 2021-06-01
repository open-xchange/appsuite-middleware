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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;

/**
 * {@link ComplexDisplayItem}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ComplexDisplayItem implements DisplayItem {

    private static final long serialVersionUID = -1049783444161691882L;
    
    private final String displayName;
    private final String detail;
    private Contact contact;

    /**
     * Initializes a new {@link ComplexDisplayItem}.
     *
     * @param displayName
     * @param detail
     */
    public ComplexDisplayItem(String displayName, String detail) {
        super();
        this.displayName = displayName;
        this.detail = detail;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public String getDetail() {
        return detail;
    }

    public String getImageUrl(Session session) {
        if (contact == null) {
            return null;
        }
        try {
            return ContactUtil.generateImageUrl(session, contact);
        } catch (OXException e) {
            return null;
        }
    }

    public boolean hasImageData() {
        return contact != null;
    }

    public void setContactForImageData(Contact con) {
        this.contact = con;
    }

    @Override
    public void accept(DisplayItemVisitor visitor) {
        visitor.visit(this);
    }

}
