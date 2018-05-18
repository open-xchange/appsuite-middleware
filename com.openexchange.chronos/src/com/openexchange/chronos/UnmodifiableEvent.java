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

package com.openexchange.chronos;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.SortedSet;
import org.dmfs.rfc5545.DateTime;

/**
 * {@link UnmodifiableEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UnmodifiableEvent extends DelegatingEvent {

    /**
     * Initializes a new {@link UnmodifiableEvent}.
     *
     * @param delegate The underlying event delegate
     */
    public UnmodifiableEvent(Event delegate) {
        super(delegate);
    }

    @Override
    public void setId(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFolderId(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeFolderId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUid(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeUid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRelatedTo(RelatedTo value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRelatedTo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSequence(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeSequence() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimestamp(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCreated(Date value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeCreated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCreatedBy(CalendarUser value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeCreatedBy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLastModified(Date value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeLastModified() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setModifiedBy(CalendarUser value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeModifiedBy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCalendarUser(CalendarUser value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeCalendarUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSummary(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeSummary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocation(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDescription(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCategories(List<String> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeCategories() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClassification(Classification value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeClassification() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setColor(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeColor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUrl(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGeo(double[] value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeGeo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStartDate(DateTime value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeStartDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEndDate(DateTime value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeEndDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTransp(Transp value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeTransp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSeriesId(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeSeriesId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRecurrenceRule(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRecurrenceRule() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRecurrenceId(RecurrenceId value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRecurrenceId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRecurrenceDates(SortedSet<RecurrenceId> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRecurrenceDates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChangeExceptionDates(SortedSet<RecurrenceId> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeChangeExceptionDates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDeleteExceptionDates(SortedSet<RecurrenceId> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeDeleteExceptionDates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatus(EventStatus value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeStatus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOrganizer(Organizer value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeOrganizer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttendees(List<Attendee> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttendees() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttachments(List<Attachment> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttachments() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAlarms(List<Alarm> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAlarms() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExtendedProperties(ExtendedProperties value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeExtendedProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFlags(EnumSet<EventFlag> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeFlags() {
        throw new UnsupportedOperationException();
    }

}
