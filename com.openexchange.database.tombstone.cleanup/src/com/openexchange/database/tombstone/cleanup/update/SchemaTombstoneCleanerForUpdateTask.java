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

package com.openexchange.database.tombstone.cleanup.update;

import java.sql.Connection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.openexchange.database.tombstone.cleanup.SchemaTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.AbstractTombstoneTableCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.CalendarTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.FolderTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.GroupTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.ObjectPermissionTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.ResourceTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.TaskTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.update.cleaners.AttachmentTombstoneUpdateTaskCleaner;
import com.openexchange.database.tombstone.cleanup.update.cleaners.ContactTombstoneUpdateTaskCleaner;
import com.openexchange.database.tombstone.cleanup.update.cleaners.InfostoreTombstoneUpdateTaskCleaner;

/**
 * {@link SchemaTombstoneCleanerForUpdateTask}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class SchemaTombstoneCleanerForUpdateTask extends SchemaTombstoneCleaner {

    /**
     * Default constructor that should be used if you do not yet have a {@link Connection} to the target schema
     */
    public SchemaTombstoneCleanerForUpdateTask() {
        super();
    }

    @Override
    protected Set<AbstractTombstoneTableCleaner> getTombstoneCleaner() {
        return Stream.of(new AttachmentTombstoneUpdateTaskCleaner(), new CalendarTombstoneCleaner(), new ContactTombstoneUpdateTaskCleaner(), new FolderTombstoneCleaner(), new GroupTombstoneCleaner(), new InfostoreTombstoneUpdateTaskCleaner(), new ObjectPermissionTombstoneCleaner(), new ResourceTombstoneCleaner(), new TaskTombstoneCleaner()).collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }
}
