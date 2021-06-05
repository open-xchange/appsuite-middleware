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

package com.openexchange.jsieve.commands;

import java.util.Arrays;
import java.util.Set;

/**
 * {@link Headers}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public enum Headers {
    from("from"),
    to("to"),
    cc("cc"),
    bcc("bcc"),
    sender("sender"),
    resent_from("resent-from"),
    resent_to("resent-to");

    String[] neededCapabilities=null;
    String name = name();

    /**
     * Initializes a new {@link Headers}.
     *
     * @param capabilities The needed capabilities
     */
    private Headers(String name, String... capabilities) {
       this.neededCapabilities = capabilities;
       this.name=name;
    }

    /**
     * Initializes a new {@link Headers}.
     */
    private Headers(String name) {
        this.name = name;
    }

    public String getHeaderName(){
        return name;
    }

    public boolean isValid(Set<String> capabilities){
        if (neededCapabilities==null){
            return true;
        }
        return Arrays.asList(neededCapabilities).containsAll(capabilities);
    }

}
