/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.database.tombstone.cleanup.update;

import java.sql.Connection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.openexchange.database.tombstone.cleanup.SchemaTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.CalendarTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.FolderTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.GroupTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.ObjectPermissionTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.ResourceTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.TaskTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.TombstoneTableCleaner;
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
    protected Set<TombstoneTableCleaner> getTombstoneCleaner() {
        return Stream.of(new AttachmentTombstoneUpdateTaskCleaner(), new CalendarTombstoneCleaner(), new ContactTombstoneUpdateTaskCleaner(), new FolderTombstoneCleaner(), new GroupTombstoneCleaner(), new InfostoreTombstoneUpdateTaskCleaner(), new ObjectPermissionTombstoneCleaner(), new ResourceTombstoneCleaner(), new TaskTombstoneCleaner()).collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }
}
