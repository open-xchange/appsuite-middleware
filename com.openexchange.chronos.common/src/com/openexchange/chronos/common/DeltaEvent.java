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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.common;

import java.util.Set;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;

/**
 * {@link DeltaEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DeltaEvent extends DelegatingEvent {

    private final Set<EventField> setFields;

    /**
     * Initializes a new {@link DeltaEvent}.
     *
     * @param delegate The event delegate
     * @param setFields The <i>set</i> fields to indicate via <code>contains...</code>-methods
     */
    public DeltaEvent(Event delegate, Set<EventField> setFields) {
        super(delegate);
        this.setFields = setFields;
    }

    @Override
    public boolean isSet(EventField field) {
        return setFields.contains(field);
    }

    @Override
    public boolean containsAlarms() {
        return setFields.contains(EventField.ALARMS);
    }

    @Override
    public boolean containsAttachments() {
        return setFields.contains(EventField.ATTACHMENTS);
    }

    @Override
    public boolean containsAttendees() {
        return setFields.contains(EventField.ATTENDEES);
    }

    @Override
    public boolean containsCalendarUser() {
        return setFields.contains(EventField.CALENDAR_USER);
    }

    @Override
    public boolean containsCategories() {
        return setFields.contains(EventField.CATEGORIES);
    }

    @Override
    public boolean containsChangeExceptionDates() {
        return setFields.contains(EventField.CHANGE_EXCEPTION_DATES);
    }

    @Override
    public boolean containsClassification() {
        return setFields.contains(EventField.CLASSIFICATION);
    }

    @Override
    public boolean containsColor() {
        return setFields.contains(EventField.COLOR);
    }

    @Override
    public boolean containsCreated() {
        return setFields.contains(EventField.CREATED);
    }

    @Override
    public boolean containsCreatedBy() {
        return setFields.contains(EventField.CREATED_BY);
    }

    @Override
    public boolean containsDeleteExceptionDates() {
        return setFields.contains(EventField.DELETE_EXCEPTION_DATES);
    }

    @Override
    public boolean containsDescription() {
        return setFields.contains(EventField.DESCRIPTION);
    }

    @Override
    public boolean containsEndDate() {
        return setFields.contains(EventField.END_DATE);
    }

    @Override
    public boolean containsExtendedProperties() {
        return setFields.contains(EventField.EXTENDED_PROPERTIES);
    }

    @Override
    public boolean containsFilename() {
        return setFields.contains(EventField.FILENAME);
    }

    @Override
    public boolean containsFlags() {
        return setFields.contains(EventField.FLAGS);
    }

    @Override
    public boolean containsFolderId() {
        return setFields.contains(EventField.FOLDER_ID);
    }

    @Override
    public boolean containsGeo() {
        return setFields.contains(EventField.GEO);
    }
    
    @Override
    public boolean containsAttendeePrivileges() {
        return setFields.contains(EventField.ATTENDEE_PRIVILEGES);
    }

    @Override
    public boolean containsId() {
        return setFields.contains(EventField.ID);
    }

    @Override
    public boolean containsLastModified() {
        return setFields.contains(EventField.LAST_MODIFIED);
    }

    @Override
    public boolean containsLocation() {
        return setFields.contains(EventField.LOCATION);
    }

    @Override
    public boolean containsModifiedBy() {
        return setFields.contains(EventField.MODIFIED_BY);
    }

    @Override
    public boolean containsOrganizer() {
        return setFields.contains(EventField.ORGANIZER);
    }

    @Override
    public boolean containsRecurrenceId() {
        return setFields.contains(EventField.RECURRENCE_ID);
    }

    @Override
    public boolean containsRecurrenceRule() {
        return setFields.contains(EventField.RECURRENCE_RULE);
    }

    @Override
    public boolean containsRelatedTo() {
        return setFields.contains(EventField.RELATED_TO);
    }

    @Override
    public boolean containsSequence() {
        return setFields.contains(EventField.SEQUENCE);
    }

    @Override
    public boolean containsSeriesId() {
        return setFields.contains(EventField.SERIES_ID);
    }

    @Override
    public boolean containsStartDate() {
        return setFields.contains(EventField.START_DATE);
    }

    @Override
    public boolean containsStatus() {
        return setFields.contains(EventField.STATUS);
    }

    @Override
    public boolean containsSummary() {
        return setFields.contains(EventField.SUMMARY);
    }

    @Override
    public boolean containsTimestamp() {
        return setFields.contains(EventField.TIMESTAMP);
    }

    @Override
    public boolean containsTransp() {
        return setFields.contains(EventField.TRANSP);
    }

    @Override
    public boolean containsUid() {
        return setFields.contains(EventField.UID);
    }

    @Override
    public boolean containsUrl() {
        return setFields.contains(EventField.URL);
    }

}
