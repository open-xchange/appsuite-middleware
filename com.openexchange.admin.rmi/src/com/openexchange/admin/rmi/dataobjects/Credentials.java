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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

/**
 * This object must be send with every method call in ox rmi interface!
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public class Credentials implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5716255479339902964L;

    private String login;

    private String password;

    private String passwordMech;

    /**
     * Creates a new instance of the object
     */
    public Credentials() {
        super();
        init();
    }

    /**
     * Creates a new instance of the object and sets login and password
     * 
     * @param login
     * @param password
     */
    public Credentials(final String login, final String password) {
        super();
        this.login = login;
        this.password = password;
    }

    /**
     * Returns the login of this credentials object
     * 
     * @return Returns the login of this credentials object
     */
    public final String getLogin() {
        return this.login;
    }

    /**
     * Set the login attribute of this credentials object
     * 
     * @param login Set the login attribute of this credentials object
     */
    public final void setLogin(final String login) {
        this.login = login;
    }

    /**
     * Returns the password in clear text
     * 
     * @return Returns the password in cleartext
     */
    public final String getPassword() {
        return this.password;
    }

    /**
     * Sets this password for this credentials object
     * 
     * @param passwd Sets this password for this credentials object
     */
    public final void setPassword(final String passwd) {
        this.password = passwd;
    }

    /**
     * Gets the passwordMech
     *
     * @return The passwordMech
     */
    public String getPasswordMech() {
        return passwordMech;
    }

    /**
     * Sets the passwordMech
     *
     * @param passwordMech The passwordMech to set
     */
    public void setPasswordMech(String passwordMech) {
        this.passwordMech = passwordMech;
    }

    /**
     * Sets all members to default values
     */
    private void init() {
        this.login = null;
        this.password = null;
    }

    /**
     * Constructs a <code>String</code> with all attributes in name = value format.
     *
     * @return a <code>String</code> representation of this object.
     */
    @Override
    public String toString() {
        final String TAB = "\n  ";

        final StringBuilder retValue = new StringBuilder();

        retValue.append("Credentials ( ").append(super.toString()).append(TAB).append("login = ").append(this.login).append(TAB).append(
            "passwordMech = ").append(this.passwordMech).append(" )");

        return retValue.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((passwordMech == null) ? 0 : passwordMech.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Credentials other = (Credentials) obj;
        if (login == null) {
            if (other.login != null) {
                return false;
            }
        } else if (!login.equals(other.login)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (passwordMech == null) {
            if (other.passwordMech != null) {
                return false;
            }
        } else if (!passwordMech.equals(other.passwordMech)) {
            return false;
        }
        return true;
    }
}
