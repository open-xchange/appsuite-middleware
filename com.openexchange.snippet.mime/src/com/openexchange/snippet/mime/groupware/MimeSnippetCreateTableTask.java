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

package com.openexchange.snippet.mime.groupware;

import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.snippet.db.Tables;

/**
 * {@link MimeSnippetCreateTableTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeSnippetCreateTableTask extends AbstractCreateTableImpl implements UpdateTaskV2 {

    /**
     * Initializes a new {@link MimeSnippetCreateTableTask}.
     */
    public MimeSnippetCreateTableTask() {
        super();
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { Tables.getSnippetTable() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    public String[] getDependencies() {
        return NO_TABLES;
    }

    @Override
    public void perform(final PerformParameters params) throws com.openexchange.exception.OXException {
        createTable(Tables.getSnippetName(), Tables.getSnippetTable(), params.getConnectionProvider().getConnection());
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MimeSnippetCreateTableTask.class);
        logger.info("UpdateTask ''{}'' successfully performed!", MimeSnippetCreateTableTask.class.getSimpleName());
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { Tables.getSnippetTable() };
    }
}
