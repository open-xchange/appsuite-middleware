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
import java.util.Arrays;

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

    private byte[] salt;

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
     * Gets the salt
     *
     * @return The salt
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * Sets the salt
     *
     * @param salt The salt to set
     */
    public void setSalt(byte[] salt) {
        this.salt = salt;
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

        retValue.append("Credentials ( ").append(super.toString()).append(TAB).append("login = ").append(this.login).append(TAB).append("passwordMech = ").append(this.passwordMech).append(TAB).append(" )");

        return retValue.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((passwordMech == null) ? 0 : passwordMech.hashCode());
        result = prime * result + ((salt == null) ? 0 : Arrays.hashCode(salt));
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
        if (salt == null) {
            if (other.salt != null) {
                return false;
            }
        } else if (!Arrays.equals(salt, other.salt)) {
            return false;
        }
        return true;
    }
}
