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

package com.openexchange.subscribe.database;

import com.openexchange.groupware.update.ExtendedColumnCreationTask;
import com.openexchange.tools.update.Column;

/**
 * Adds columns for storing the time stamp of the creation and the last modification for each subscription.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class SubscriptionsCreatedAndLastModifiedColumn extends ExtendedColumnCreationTask {

    public SubscriptionsCreatedAndLastModifiedColumn() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.groupware.update.tasks.CreateSubscribeTableTask" };
    }

    @Override
    protected String getTableName() {
        return "subscriptions";
    }

    @Override
    protected Column[] getColumns() {
        return new Column[] { new Column("created", "INT8 NOT NULL DEFAULT 0"), new Column("lastModified", "INT8 NOT NULL DEFAULT 0") };
    }
}
