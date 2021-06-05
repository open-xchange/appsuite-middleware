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

package com.openexchange.database.migration.internal;

import java.util.ArrayList;
import java.util.List;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;

/**
 * {@link DBMigrationListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DBMigrationListener implements ChangeExecListener {

    private final List<ChangeSet> executed;
    private final List<ChangeSet> rolledBack;

    /**
     * Initializes a new {@link DBMigrationListener}.
     */
    public DBMigrationListener() {
        super();
        executed = new ArrayList<ChangeSet>();
        rolledBack = new ArrayList<ChangeSet>();
    }

    /**
     * Gets the executed changesets.
     *
     * @return A list of changesets that have been executed during the migration, or an empty list if there are none
     */
    public List<ChangeSet> getExecuted() {
        return executed;
    }

    /**
     * Gets the rolled back changesets.
     *
     * @return A list of changesets that have been rolled back during the migration, or an empty list if there are none
     */
    public List<ChangeSet> getRolledBack() {
        return rolledBack;
    }

    @Override
    public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, RunStatus runStatus) {
        // ignore
    }

    @Override
    public void ran(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ExecType execType) {
        executed.add(changeSet);
    }

    @Override
    public void rolledBack(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        rolledBack.add(changeSet);
    }

}
