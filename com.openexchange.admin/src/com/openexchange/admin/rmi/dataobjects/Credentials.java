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
 * This object must be send with every method call in ox rmi interface!
 * @author cutmasta
 */
public class Credentials implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = -5716255479339902964L;

    private String login;

    private String password;

    /**
     * Creates a new instance of the object
     */
    public Credentials () {
        super();
        init();
    }

    /**
     * Creates a new instance of the object and sets login and password
     * @param login
     * @param password
     */
    public Credentials(String login, String password) {
        super();
        this.login = login;
        this.password = password;
    }

    /**
     * Returns the login of this credentials object
     * @return Returns the login of this credentials object
     */
    public String getLogin () {
        return login;
    }

    /**
     * Set the login attribute of this credentials object
     * @param login Set the login attribute of this credentials object
     */
    public void setLogin (String login) {
        this.login = login;
    }

    /**
     * Returns the password in clear text
     * @return Returns the password in cleartext
     */
    public String getPassword () {
        return password;
    }

    /**
     * Sets this password for this credentials object
     * @param passwd Sets this password for this credentials object
     */
    public void setPassword (String passwd) {
        this.password = passwd;
    }

    /**
     * Sets all members to default values
     */
    private void init() {
        this.login = null;
        this.password = null;
        
    }
    
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
}
