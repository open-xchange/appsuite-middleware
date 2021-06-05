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

package com.openexchange.regional.impl.storage;

/**
 * {@link SQLStatements}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
final class SQLStatements {

    public static final String SELECT = "SELECT * FROM regional_settings WHERE cid=? AND userId=?";
    //@formatter:off
    public static final String UPSERT = "INSERT INTO regional_settings (cid,userId,timeFormat,timeFormatLong,dateFormat,dateFormatShort,dateFormatMedium,dateFormatLong,dateFormatFull,numberFormat,firstDayOfWeek,firstDayOfYear) "
                                                 + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE timeFormat=?,timeFormatLong=?,dateFormat=?,dateFormatShort=?,dateFormatMedium=?,dateFormatLong=?,dateFormatFull=?,numberFormat=?,firstDayOfWeek=?,firstDayOfYear=?;";
    //@formatter:on
    public static final String DELETE_USER = "DELETE FROM regional_settings WHERE cid=? AND userId=?";
    public static final String DELETE_CONTEXT = "DELETE FROM regional_settings WHERE cid=?";
}
