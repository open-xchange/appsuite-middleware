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

package com.openexchange.messaging;

import java.io.Serializable;

/**
 * {@link StringContent} - A string content.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class StringContent implements SimpleContent<String>, Serializable {

    private static final long serialVersionUID = 5708086498526356536L;
    
    private String data;

    /**
     * Initializes a new {@link StringContent}.
     */
    public StringContent() {
        this(null);
    }

    /**
     * Initializes a new {@link StringContent}.
     *
     * @param data The string data
     */
    public StringContent(final String data) {
        super();
        this.data = data;
    }

    @Override
    public String getData() {
        return data;
    }

    /**
     * Sets the string data.
     *
     * @param data The string data
     */
    public void setData(final String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

}
