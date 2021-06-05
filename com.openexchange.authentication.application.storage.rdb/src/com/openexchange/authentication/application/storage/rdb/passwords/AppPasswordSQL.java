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

/**
 * {@link AppPasswordSQL}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AppPasswordSQL {

    public static final String GET_AUTH = "SELECT uuid, user, cid, login, encrPass, passHash, mech, salt, appType, name FROM app_passwords WHERE login = ?";
    public static final String INSERT_AUTH = "INSERT INTO app_passwords (user, cid, uuid, login, appType, name, mech, passHash, salt, encrPass, encrLogin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String REMOVE_AUTH = "DELETE FROM app_passwords WHERE cid = ? AND user = ? AND uuid = ?";
    public static final String LIST_AUTH = "SELECT uuid, login, name, appType FROM app_passwords WHERE cid = ? AND user = ?";
    public static final String DELETE_FOR_USER = "DELETE FROM app_passwords WHERE cid = ? AND user = ?";
    public static final String DELETE_FOR_CONTEXT = "DELETE FROM app_passwords WHERE cid = ?";
    public static final String GET_PASSWORDS_FOR_UPDATE = "SELECT uuid, encrPass, encrLogin from app_passwords WHERE cid = ? and user = ?";
    public static final String UPDATE_PASSWORD = "UPDATE app_passwords SET encrPass = ?, encrLogin = ? WHERE cid = ? and uuid = ?";

}
