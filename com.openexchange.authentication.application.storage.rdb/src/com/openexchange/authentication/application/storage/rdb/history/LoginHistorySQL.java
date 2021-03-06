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

package com.openexchange.authentication.application.storage.rdb.history;

/**
 * {@link LoginHistorySQL}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class LoginHistorySQL {

    public static final String UPDATE_LOGIN = "REPLACE INTO app_passwords_history (uuid, cid, user, timestamp, client, userAgent, ip) VALUES (?, ?, ?, ?, ?, ?, ?);";
    public static final String GET_LOGIN_HISTORY = "SELECT uuid, cid, user, timestamp, client, userAgent, ip from app_passwords_history WHERE user = ? and cid = ?";
    public static final String DELETE_FOR_USER = "DELETE FROM app_passwords_history WHERE cid = ? AND user = ?";
    public static final String DELETE_FOR_CONTEXT = "DELETE FROM app_passwords_history WHERE cid = ?";
    public static final String DELETE_FOR_UUID = "DELETE FROM app_passwords_history WHERE uuid = ?";
}
