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

package com.openexchange.authentication.application.storage.rdb.passwords;

import java.util.UUID;

/**
 * {@link PasswordChange}
 * Convenience class for managing passwords during change
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class PasswordChange {

    private String encrLogin, newEncrPass, newEncrLogin;
    private UUID guid;

    /**
     * Initializes a new {@link PasswordChange}.
     * 
     * @param guid The guid
     * @param encrLogin the encrypted login
     */
    public PasswordChange(UUID guid, String encrLogin) {
        this.guid = guid;
        this.encrLogin = encrLogin;
    }

    /**
     * Set new encrypted password
     *
     * @param newEncrPass the new encrypted password
     */
    public void setNewEncrPass(String newEncrPass) {
        this.newEncrPass = newEncrPass;
    }

    /**
     * Set new encrypted login
     *
     * @param newEncrLogin The new encrypted login
     */
    public void setNewEncrLogin(String newEncrLogin) {
        this.newEncrLogin = newEncrLogin;
    }

    /**
     * Return the guid associated with the passwords
     *
     * @return the guid associated with the passwords
     */
    public UUID getGUID() {
        return this.guid;
    }

    /**
     * Returns old encrypted login
     *
     * @return old encrypted login
     */
    public String getEncrLogin() {
        return this.encrLogin;
    }

    /**
     * Get the new encrypted password
     *
     * @return the new encrypted password
     */
    public String getNewEncrPass() {
        return this.newEncrPass;
    }

    /**
     * Get the new encrypted login
     *
     * @return the new encrypted login
     */
    public String getNewEncrLogin() {
        return this.newEncrLogin;
    }
}
