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
 * {@link DelegatingEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class DelegatingEvent extends Event {

    protected final Event delegate;

    /**
     * Initializes a new {@link DelegatingEvent}.
     *
     * @param delegate The underlying event delegate
     */
    protected DelegatingEvent(Event delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public boolean isSet(EventField field) {
        return delegate.isSet(field);
    }

    @Override
    public boolean areSet(EventField... fields) {
        return delegate.areSet(fields);
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void setId(String value) {
        delegate.setId(value);
    }

    @Override
    public void removeId() {
        delegate.removeId();
    }

    @Override
    public boolean containsId() {
        return delegate.containsId();
    }

    @Override
    public String getFolderId() {
        return delegate.getFolderId();
    }

    @Override
    public void setFolderId(String value) {
        delegate.setFolderId(value);
    }

    @Override
    public void removeFolderId() {
        delegate.removeFolderId();
    }

    @Override
    public boolean containsFolderId() {
        return delegate.containsFolderId();
    }

    @Override
    public String getUid() {
        return delegate.getUid();
    }

    @Override
    public void setUid(String value) {
        delegate.setUid(value);
    }

    @Override
    public void removeUid() {
        delegate.removeUid();
    }

    @Override
    public boolean containsUid() {
        return delegate.containsUid();
    }

    @Override
    public RelatedTo getRelatedTo() {
        return delegate.getRelatedTo();
    }

    @Override
    public void setRelatedTo(RelatedTo value) {
        delegate.setRelatedTo(value);
    }

    @Override
    public void removeRelatedTo() {
        delegate.removeRelatedTo();
    }

    @Override
    public boolean containsRelatedTo() {
        return delegate.containsRelatedTo();
    }

    @Override
    public int getSequence() {
        return delegate.getSequence();
    }

    @Override
    public void setSequence(int value) {
        delegate.setSequence(value);
    }

    @Override
    public void removeSequence() {
        delegate.removeSequence();
    }

    @Override
    public boolean containsSequence() {
        return delegate.containsSequence();
    }

    @Override
    public long getTimestamp() {
        return delegate.getTimestamp();
    }

    @Override
    public void setTimestamp(long value) {
        delegate.setTimestamp(value);
    }

    @Override
    public void removeTimestamp() {
        delegate.removeTimestamp();
    }

    @Override
    public boolean containsTimestamp() {
        return delegate.containsTimestamp();
    }

    @Override
    public Date getCreated() {
        return delegate.getCreated();
    }

    @Override
    public void setCreated(Date value) {
        delegate.setCreated(value);
    }

    @Override
    public void removeCreated() {
        delegate.removeCreated();
    }

    @Override
    public boolean containsCreated() {
        return delegate.containsCreated();
    }

    @Override
    public CalendarUser getCreatedBy() {
        return delegate.getCreatedBy();
    }

    @Override
    public void setCreatedBy(CalendarUser value) {
        delegate.setCreatedBy(value);
    }

    @Override
    public void removeCreatedBy() {
        delegate.removeCreatedBy();
    }

    @Override
    public boolean containsCreatedBy() {
        return delegate.containsCreatedBy();
    }

    @Override
    public Date getLastModified() {
        return delegate.getLastModified();
    }

    @Override
    public void setLastModified(Date value) {
        delegate.setLastModified(value);
    }

    @Override
    public void removeLastModified() {
        delegate.removeLastModified();
    }

    @Override
    public boolean containsLastModified() {
        return delegate.containsLastModified();
    }

    @Override
    public CalendarUser getModifiedBy() {
        return delegate.getModifiedBy();
    }

    @Override
    public void setModifiedBy(CalendarUser value) {
        delegate.setModifiedBy(value);
    }

    @Override
    public void removeModifiedBy() {
        delegate.removeModifiedBy();
    }

    @Override
    public boolean containsModifiedBy() {
        return delegate.containsModifiedBy();
    }

    @Override
    public CalendarUser getCalendarUser() {
        return delegate.getCalendarUser();
    }

    @Override
    public void setCalendarUser(CalendarUser value) {
        delegate.setCalendarUser(value);
    }

    @Override
    public void removeCalendarUser() {
        delegate.removeCalendarUser();
    }

    @Override
    public boolean containsCalendarUser() {
        return delegate.containsCalendarUser();
    }

    @Override
    public String getSummary() {
        return delegate.getSummary();
    }

    @Override
    public void setSummary(String value) {
        delegate.setSummary(value);
    }

    @Override
    public void removeSummary() {
        delegate.removeSummary();
    }

    @Override
    public boolean containsSummary() {
        return delegate.containsSummary();
    }

    @Override
    public String getLocation() {
        return delegate.getLocation();
    }

    @Override
    public void setLocation(String value) {
        delegate.setLocation(value);
    }

    @Override
    public void removeLocation() {
        delegate.removeLocation();
    }

    @Override
    public boolean containsLocation() {
        return delegate.containsLocation();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void setDescription(String value) {
        delegate.setDescription(value);
    }

    @Override
    public void removeDescription() {
        delegate.removeDescription();
    }

    @Override
    public boolean containsDescription() {
        return delegate.containsDescription();
    }

    @Override
    public List<String> getCategories() {
        return delegate.getCategories();
    }

    @Override
    public void setCategories(List<String> value) {
        delegate.setCategories(value);
    }

    @Override
    public void removeCategories() {
        delegate.removeCategories();
    }

    @Override
    public boolean containsCategories() {
        return delegate.containsCategories();
    }

    @Override
    public Classification getClassification() {
        return delegate.getClassification();
    }

    @Override
    public void setClassification(Classification value) {
        delegate.setClassification(value);
    }

    @Override
    public void removeClassification() {
        delegate.removeClassification();
    }

    @Override
    public boolean containsClassification() {
        return delegate.containsClassification();
    }

    @Override
    public String getColor() {
        return delegate.getColor();
    }

    @Override
    public void setColor(String value) {
        delegate.setColor(value);
    }

    @Override
    public void removeColor() {
        delegate.removeColor();
    }

    @Override
    public boolean containsColor() {
        return delegate.containsColor();
    }

    @Override
    public String getFilename() {
        return delegate.getFilename();
    }

    @Override
    public void setFilename(String value) {
        delegate.setFilename(value);
    }

    @Override
    public void removeFilename() {
        delegate.removeFilename();
    }

    @Override
    public boolean containsFilename() {
        return delegate.containsFilename();
    }

    @Override
    public String getUrl() {
        return delegate.getUrl();
    }

    @Override
    public void setUrl(String value) {
        delegate.setUrl(value);
    }

    @Override
    public void removeUrl() {
        delegate.removeUrl();
    }

    @Override
    public boolean containsUrl() {
        return delegate.containsUrl();
    }

    @Override
    public double[] getGeo() {
        return delegate.getGeo();
    }

    @Override
    public void setGeo(double[] value) {
        delegate.setGeo(value);
    }

    @Override
    public void removeGeo() {
        delegate.removeGeo();
    }

    @Override
    public boolean containsGeo() {
        return delegate.containsGeo();
    }
    
    @Override
    public AttendeePrivileges getAttendeePrivileges() {
        return delegate.getAttendeePrivileges();
    }

    @Override
    public void setAttendeePrivileges(AttendeePrivileges value) {
        delegate.setAttendeePrivileges(value);
    }

    @Override
    public void removeAttendeePrivileges() {
        delegate.removeAttendeePrivileges();
    }

    @Override
    public boolean containsAttendeePrivileges() {
        return delegate.containsAttendeePrivileges();
    }

    @Override
    public DateTime getStartDate() {
        return delegate.getStartDate();
    }

    @Override
    public void setStartDate(DateTime value) {
        delegate.setStartDate(value);
    }

    @Override
    public void removeStartDate() {
        delegate.removeStartDate();
    }

    @Override
    public boolean containsStartDate() {
        return delegate.containsStartDate();
    }

    @Override
    public DateTime getEndDate() {
        return delegate.getEndDate();
    }

    @Override
    public void setEndDate(DateTime value) {
        delegate.setEndDate(value);
    }

    @Override
    public void removeEndDate() {
        delegate.removeEndDate();
    }

    @Override
    public boolean containsEndDate() {
        return delegate.containsEndDate();
    }

    @Override
    public Transp getTransp() {
        return delegate.getTransp();
    }

    @Override
    public void setTransp(Transp value) {
        delegate.setTransp(value);
    }

    @Override
    public void removeTransp() {
        delegate.removeTransp();
    }

    @Override
    public boolean containsTransp() {
        return delegate.containsTransp();
    }

    @Override
    public String getSeriesId() {
        return delegate.getSeriesId();
    }

    @Override
    public void setSeriesId(String value) {
        delegate.setSeriesId(value);
    }

    @Override
    public void removeSeriesId() {
        delegate.removeSeriesId();
    }

    @Override
    public boolean containsSeriesId() {
        return delegate.containsSeriesId();
    }

    @Override
    public String getRecurrenceRule() {
        return delegate.getRecurrenceRule();
    }

    @Override
    public void setRecurrenceRule(String value) {
        delegate.setRecurrenceRule(value);
    }

    @Override
    public void removeRecurrenceRule() {
        delegate.removeRecurrenceRule();
    }

    @Override
    public boolean containsRecurrenceRule() {
        return delegate.containsRecurrenceRule();
    }

    @Override
    public RecurrenceId getRecurrenceId() {
        return delegate.getRecurrenceId();
    }

    @Override
    public void setRecurrenceId(RecurrenceId value) {
        delegate.setRecurrenceId(value);
    }

    @Override
    public void removeRecurrenceId() {
        delegate.removeRecurrenceId();
    }

    @Override
    public boolean containsRecurrenceId() {
        return delegate.containsRecurrenceId();
    }

    @Override
    public SortedSet<RecurrenceId> getRecurrenceDates() {
        return delegate.getRecurrenceDates();
    }

    @Override
    public void setRecurrenceDates(SortedSet<RecurrenceId> value) {
        delegate.setRecurrenceDates(value);
    }

    @Override
    public void removeRecurrenceDates() {
        delegate.removeRecurrenceDates();
    }

    @Override
    public boolean containsRecurrenceDates() {
        return delegate.containsRecurrenceDates();
    }

    @Override
    public SortedSet<RecurrenceId> getChangeExceptionDates() {
        return delegate.getChangeExceptionDates();
    }

    @Override
    public void setChangeExceptionDates(SortedSet<RecurrenceId> value) {
        delegate.setChangeExceptionDates(value);
    }

    @Override
    public void removeChangeExceptionDates() {
        delegate.removeChangeExceptionDates();
    }

    @Override
    public boolean containsChangeExceptionDates() {
        return delegate.containsChangeExceptionDates();
    }

    @Override
    public SortedSet<RecurrenceId> getDeleteExceptionDates() {
        return delegate.getDeleteExceptionDates();
    }

    @Override
    public void setDeleteExceptionDates(SortedSet<RecurrenceId> value) {
        delegate.setDeleteExceptionDates(value);
    }

    @Override
    public void removeDeleteExceptionDates() {
        delegate.removeDeleteExceptionDates();
    }

    @Override
    public boolean containsDeleteExceptionDates() {
        return delegate.containsDeleteExceptionDates();
    }

    @Override
    public EventStatus getStatus() {
        return delegate.getStatus();
    }

    @Override
    public void setStatus(EventStatus value) {
        delegate.setStatus(value);
    }

    @Override
    public void removeStatus() {
        delegate.removeStatus();
    }

    @Override
    public boolean containsStatus() {
        return delegate.containsStatus();
    }

    @Override
    public Organizer getOrganizer() {
        return delegate.getOrganizer();
    }

    @Override
    public void setOrganizer(Organizer value) {
        delegate.setOrganizer(value);
    }

    @Override
    public void removeOrganizer() {
        delegate.removeOrganizer();
    }

    @Override
    public boolean containsOrganizer() {
        return delegate.containsOrganizer();
    }

    @Override
    public List<Attendee> getAttendees() {
        return delegate.getAttendees();
    }

    @Override
    public void setAttendees(List<Attendee> value) {
        delegate.setAttendees(value);
    }

    @Override
    public void removeAttendees() {
        delegate.removeAttendees();
    }

    @Override
    public boolean containsAttendees() {
        return delegate.containsAttendees();
    }

    @Override
    public List<Attachment> getAttachments() {
        return delegate.getAttachments();
    }

    @Override
    public void setAttachments(List<Attachment> value) {
        delegate.setAttachments(value);
    }

    @Override
    public void removeAttachments() {
        delegate.removeAttachments();
    }

    @Override
    public boolean containsAttachments() {
        return delegate.containsAttachments();
    }

    @Override
    public List<Alarm> getAlarms() {
        return delegate.getAlarms();
    }

    @Override
    public void setAlarms(List<Alarm> value) {
        delegate.setAlarms(value);
    }

    @Override
    public void removeAlarms() {
        delegate.removeAlarms();
    }

    @Override
    public boolean containsAlarms() {
        return delegate.containsAlarms();
    }

    @Override
    public ExtendedProperties getExtendedProperties() {
        return delegate.getExtendedProperties();
    }

    @Override
    public void setExtendedProperties(ExtendedProperties value) {
        delegate.setExtendedProperties(value);
    }

    @Override
    public void removeExtendedProperties() {
        delegate.removeExtendedProperties();
    }

    @Override
    public boolean containsExtendedProperties() {
        return delegate.containsExtendedProperties();
    }

    @Override
    public EnumSet<EventFlag> getFlags() {
        return delegate.getFlags();
    }

    @Override
    public void setFlags(EnumSet<EventFlag> value) {
        delegate.setFlags(value);
    }

    @Override
    public void removeFlags() {
        delegate.removeFlags();
    }

    @Override
    public boolean containsFlags() {
        return delegate.containsFlags();
    }

}
