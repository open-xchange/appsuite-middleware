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

package com.openexchange.find.calendar;

import com.openexchange.find.Document;
import com.openexchange.find.DocumentVisitor;

/**
 * {@link CalendarDocument}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class CalendarDocument implements Document {

    private static final long serialVersionUID = 644937237827581918L;

    private Object object;
    private String format;

    /**
     * Initializes a new {@link CalendarDocument}.
     *
     * @param object The underling calendar object
     * @param format The object's format name
     */
    public CalendarDocument(Object object, String format) {
        super();
        this.object = object;
        this.format = format;
    }

    /**
     * Gets the underlying calendar object.
     *
     * @return The underlying calendar object
     */
    public Object getObject() {
        return object;
    }

    /**
     * Gets the object's format name.
     *
     * @return The format name
     */
    public String getFormat() {
        return format;
    }

    @Override
    public void accept(DocumentVisitor visitor) {
        visitor.visit(this);
    }

}
