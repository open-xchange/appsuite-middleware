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

package com.openexchange.admin.autocontextid.tools;

import java.sql.SQLException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.groupware.impl.IDGenerator.Implementations;

public class AdminCacheExtended extends AdminCache {

    private PropertyHandlerExtended prop = null;

    public AdminCacheExtended() {
        super();
    }

    public void initCacheExtended() {
        prop = new PropertyHandlerExtended(System.getProperties());
    }

    public void initIDGenerator() throws SQLException {
        Implementations.NODBFUNCTION.getImpl().registerType("sequence_context", -2);
        Implementations.MYSQLFUNCTION.getImpl().registerType("sequence_context", -2);
    }

    @Override
    public PropertyHandlerExtended getProperties() {
        return prop;
    }
}
