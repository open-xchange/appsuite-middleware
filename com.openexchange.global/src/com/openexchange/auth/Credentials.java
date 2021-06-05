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

package com.openexchange.auth;

import java.io.Serializable;

/**
 * {@link Credentials} - Credentials providing login and password for administrative authentication.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public class Credentials implements Serializable {

    private static final long serialVersionUID = 5770217423790230093L;

    private String login;
    private String password;

    /**
     * Creates a new instance of the object
     */
    public Credentials() {
        super();
        this.login = null;
        this.password = null;
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
    public String getLogin() {
        return this.login;
    }

    /**
     * Set the login attribute of this credentials object
     *
     * @param login Set the login attribute of this credentials object
     */
    public void setLogin(final String login) {
        this.login = login;
    }

    /**
     * Returns the password in clear text
     *
     * @return Returns the password in clear text
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets this password for this credentials object
     *
     * @param passwd Sets this password for this credentials object
     */
    public void setPassword(final String passwd) {
        this.password = passwd;
    }

    /**
     * Constructs a <code>String</code> with all attributes in name = value format.
     *
     * @return a <code>String</code> representation of this object.
     */
    @Override
    public String toString() {
        final String TAB = "\n  ";
        return new StringBuilder(256).append("Credentials ( ").append(super.toString()).append(TAB).append("login = ").append(this.login).append(TAB).append(" )").toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Credentials)) {
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
        return true;
    }

}
