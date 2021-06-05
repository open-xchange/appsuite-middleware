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

package com.openexchange.find.basic.conf;

import com.openexchange.config.lean.Property;

/**
 * {@link FindBasicProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum FindBasicProperty implements Property {

    /**
     * Some mail backends provide a virtual folder that contains all messages of
     * a user to enable cross-folder mail search. Open-Xchange can make use of
     * this feature to improve the search experience.
     *
     * Set the value to the name of the virtual mail folder containing all messages.
     * Leave blank if no such folder exists.
     */
    allMessageFolder(""),

    /**
     * Denotes if mail search queries should be matched against mail bodies.
     * This improves the search experience within the mail module, if your mail
     * backend supports fast full text search. Otherwise it can slow down the
     * search requests significantly.
     *
     * Change the value to 'true', if fast full text search is supported. Default
     * is 'false'.
     */
    searchmailbody(Boolean.FALSE);

    private static final String PREFIX = "com.openexchange.find.basic.mail.";

    private final Object defaultValue;

    /**
     * Initializes a new {@link FindBasicProperty}.
     */
    private FindBasicProperty(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return PREFIX + name();
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
