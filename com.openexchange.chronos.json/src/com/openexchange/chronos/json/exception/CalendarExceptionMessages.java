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

package com.openexchange.chronos.json.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link CalendarExceptionMessages}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class CalendarExceptionMessages implements LocalizableStrings {

    /**
     * The event %s couldn't be deleted: %s
     */
    public static final String ERROR_DELETING_EVENT_MSG = "The event %s couldn't be deleted: %s";
    /**
     * Multiple events couldn't be deleted.
     */
    public static final String ERROR_DELETING_EVENTS_MSG = "Multiple events couldn't be deleted.";
    /**
     * Unable to add alarms: %s
     */
    public static final String UNABLE_TO_ADD_ALARMS_MSG = "Unable to add alarms: %s";
    /**
     * The content-id '%1$s' refers to a non-existing attachment in the body part.
     */
    public static final String MISSING_BODY_PART_ATTACHMENT_REFERENCE_MSG = "The content-id '%1$s' refers to a non-existing attachment in the body part.";
    /**
     * The metadata of the attachment '%1$s' does not have a content-id.
     */
    public static final String MISSING_METADATA_ATTACHMENT_REFERENCE_MSG = "The metadata of the attachment '%1$s' does not have a content-id.";
}
