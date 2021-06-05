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

package com.openexchange.admin.rmi.dataobjects;

import java.io.Serializable;

/**
 * {@link Quota} - A quota limit representation for a certain module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class Quota implements Serializable {

    private static final long serialVersionUID = -1371977751244817135L;

    private long limit;
    private String module;

    /**
     * Initializes a new {@link Quota}.
     */
    public Quota() {
        super();
    }


    /**
     * Initializes a new {@link Quota}.
     *
     * @param limit The limit
     * @param module The module identifier
     */
    public Quota(long limit, String module) {
        super();
        this.limit = limit;
        this.module = module;
    }


    /**
     * Gets the limit
     *
     * @return The limit
     */
    public long getLimit() {
        return limit;
    }

    /**
     * Sets the limit
     *
     * @param limit The limit to set
     */
    public void setLimit(long limit) {
        this.limit = limit;
    }

    /**
     * Gets the module
     *
     * @return The module
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets the module
     *
     * @param module The module to set
     */
    public void setModule(String module) {
        this.module = module;
    }

}
