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
import java.lang.reflect.Field;

/**
 *
 * This class represents a server.
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class Server implements Serializable, NameAndIdObject {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -1752789033012449187L;

    private Integer id;

    private boolean idset;

    private String name;

    private boolean nameset;

    /**
     * Initiates an empty server object
     */
    public Server() {
    }

    /**
     * Returns the id of this server object
     *
     * @return An {@link Integer} containing the id
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id for this server object
     *
     * @param id An {@link Integer} containing the id
     */
    @Override
    public void setId(final Integer id) {
        this.id = id;
        this.idset = true;
    }

    /**
     * Returns the name of this server object
     *
     * @return A {@link String} containing the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name for this server object
     *
     * @param name A {@link String} containing the name
     */
    @Override
    public void setName(final String name) {
        this.name = name;
        this.nameset = true;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                Object ob = f.get(this);
                String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID")) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (IllegalAccessException e) {
                ret.append("IllegalAccessException\n");
            }
        }
        ret.append("]");
        return ret.toString();
    }

    /**
     * Used to check if the id field of this server object has been changed
     *
     * @return true if set; false if not
     **/
    public boolean isIdset() {
        return idset;
    }

    /**
     * Used to check if the name field of this server object has been changed
     *
     * @return true if set; false if not
     **/
    public boolean isNameset() {
        return nameset;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (idset ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nameset ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Server)) {
            return false;
        }
        final Server other = (Server) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (idset != other.idset) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (nameset != other.nameset) {
            return false;
        }
        return true;
    }
}
