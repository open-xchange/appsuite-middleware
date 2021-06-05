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

package com.openexchange.chronos.json.fields;

import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.UpdatesResult;

/**
 * {@link ChronosCalendarResultJsonFields} contains fields of the calendar results
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ChronosCalendarResultJsonFields {


    public static class Result {
        /**
         * The created events. See {@link CalendarResult#getCreations()}.
         */
        public static final String CREATED = "created";

        /**
         * The deleted events. See {@link CalendarResult#getDeletions()}.
         */
        public static final String DELETED = "deleted";

        /**
         * The updated events. See {@link CalendarResult#getUpdates()}.
         */
        public static final String UPDATED = "updated";

    }

    public static final class Updates {
        /**
         * The new and modified events. See {@link UpdatesResult#getNewAndModifiedEvents()}.
         */
        public static final String NEW = "newAndModified";

        /**
         * The deleted events. See {@link UpdatesResult#getDeletedEvents())}.
         */
        public static final String DELETED = "deleted";
    }

    public static final class ErrorAwareResult extends Result {
        /**
         * The id of the event. See {@link ErrorAwareCalendarResult#getId()}.
         */
        public static final String ID = "id";
        /**
         * The optional recurrence id of the event. See {@link ErrorAwareCalendarResult#getId()}.
         */
        public static final String RECURRENCE_ID = "recurrenceId";
        /**
         * The folder id. See {@link ErrorAwareCalendarResult#getFolderID()}.
         */
        public static final String FOLDER_ID = "folderId";
        /**
         * The error. See {@link ErrorAwareCalendarResult#getError()}.
         */
        public static final String ERROR = "error";
    }

}

