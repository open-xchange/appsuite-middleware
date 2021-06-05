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

package com.openexchange.oauth.impl.internal.groupware;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link OAuthCreateTableTask} must be executed a second time because it was released with a wrong definition for the table.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class OAuthCreateTableTask2 extends AbstractOAuthUpdateTask {

    public OAuthCreateTableTask2() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { OAuthCreateTableTask.class.getName() };
    }

    @Override
    void innerPerform(Connection connection, PerformParameters performParameters) throws OXException, SQLException {
        final List<Column> toChange = new ArrayList<Column>();
        if (Tools.isVARCHAR(connection, CreateOAuthAccountTable.TABLE_NAME, "accessToken")) {
            toChange.add(new Column("accessToken", "TEXT CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL"));
        }
        if (Tools.isVARCHAR(connection, CreateOAuthAccountTable.TABLE_NAME, "accessSecret")) {
            toChange.add(new Column("accessSecret", "TEXT CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL"));
        }
        Tools.modifyColumns(connection, CreateOAuthAccountTable.TABLE_NAME, toChange);

    }
}
