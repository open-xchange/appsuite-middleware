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

