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

package com.openexchange.groupware.update.tasks;

import com.google.common.collect.ImmutableList;
import com.openexchange.groupware.update.SimpleConvertUtf8ToUtf8mb4UpdateTask;

/**
 * {@link InfostoreConvertUtf8ToUtf8mb4Task} - Converts infostore tables to utf8mb4.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class InfostoreConvertUtf8ToUtf8mb4Task extends SimpleConvertUtf8ToUtf8mb4UpdateTask {

    /**
     * Initializes a new {@link InfostoreConvertUtf8ToUtf8mb4Task}.
     */
    public InfostoreConvertUtf8ToUtf8mb4Task() {
        //@formatter:off
        super(ImmutableList.of("infostore", "infostore_document", "del_infostore",
            "del_infostore_document", "infostore_property",
            "infostore_lock", "lock_null", "lock_null_lock"), 
            com.openexchange.groupware.update.tasks.AddMD5SumIndexForInfostoreDocumentTable.class.getName());
        //@formatter:on
    }
}
