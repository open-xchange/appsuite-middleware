/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id for this server object
     * 
     * @param id An {@link Integer} containing the id
     */
    public void setId(final Integer id) {
        this.id = id;
        this.idset = true;
    }

    /**
     * Returns the name of this server object
     * 
     * @return A {@link String} containing the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name for this server object
     * 
     * @param name A {@link String} containing the name
     */
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
            } catch (final IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (final IllegalAccessException e) {
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
}
