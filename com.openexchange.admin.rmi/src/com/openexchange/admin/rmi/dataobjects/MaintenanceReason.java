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
 * This class represents a maintenance reason.
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class MaintenanceReason implements Serializable{
    /**
     * For serialization
     */
    private static final long serialVersionUID = -7581806972771279403L;

    private Integer id;

    private boolean idset;

    private String text;

    private boolean textset;

    public MaintenanceReason () {
        super();
        this.id = null;
        this.text = null;
    }

    /**
     * @param id
     */
    public MaintenanceReason(final Integer id) {
        super();
        this.id = id;
        this.text = null;
    }

    /**
     * @param id
     * @param text
     */
    public MaintenanceReason(final Integer id, final String text) {
        super();
        this.id = id;
        this.text = text;
    }

    /**
     * @param id
     * @param text
     */
    public MaintenanceReason(final String text) {
        super();
        this.id = null;
        this.text = text;
    }

    public Integer getId () {
        return id;
    }

    public void setId (final Integer val) {
        this.id = val;
        this.idset = true;
    }

    public String getText () {
        return text;
    }

    public void setText (final String val) {
        this.text = val;
        this.textset = true;
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
     * @return the idset
     */
    public boolean isIdset() {
        return idset;
    }

    /**
     * @return the textset
     */
    public boolean isTextset() {
        return textset;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (idset ? 1231 : 1237);
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + (textset ? 1231 : 1237);
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
        if (!(obj instanceof MaintenanceReason)) {
            return false;
        }
        final MaintenanceReason other = (MaintenanceReason) obj;
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
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        if (textset != other.textset) {
            return false;
        }
        return true;
    }
}
