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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import java.util.List;

/**
 * {@link UserizedEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UserizedEvent extends Event {

    int folderId;
    List<Alarm> alarms;
    Event delegate;

    public UserizedEvent(Event delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * Gets the folderId
     *
     * @return The folderId
     */
    public int getFolderId() {
        return folderId;
    }

    /**
     * Sets the folderId
     *
     * @param folderId The folderId to set
     */
    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    /**
     * Gets the alarms
     *
     * @return The alarms
     */
    public List<Alarm> getAlarms() {
        return alarms;
    }

    /**
     * Sets the alarms
     *
     * @param alarms The alarms to set
     */
    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public int getId() {
        return delegate.getId();
    }

    public String getUid() {
        return delegate.getUid();
    }

    public String getFilename() {
        return delegate.getFilename();
    }

    public String getiCalId() {
        return delegate.getiCalId();
    }

    public Date getCreated() {
        return delegate.getCreated();
    }

    public Date getLastModified() {
        return delegate.getLastModified();
    }

    public int getCreatedBy() {
        return delegate.getCreatedBy();
    }

    public int getModifiedBy() {
        return delegate.getModifiedBy();
    }

    public String getSummary() {
        return delegate.getSummary();
    }

    public String getLocation() {
        return delegate.getLocation();
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public EventStatus getStatus() {
        return delegate.getStatus();
    }

    public Date getStartDate() {
        return delegate.getStartDate();
    }

    public String getStartTimezone() {
        return delegate.getStartTimezone();
    }

    public Date getEndDate() {
        return delegate.getEndDate();
    }

    public String getEndTimezone() {
        return delegate.getEndTimezone();
    }

    public boolean isAllDay() {
        return delegate.isAllDay();
    }

    public TimeTransparency getTransp() {
        return delegate.getTransp();
    }

    public int getRecurrenceId() {
        return delegate.getRecurrenceId();
    }

    public String getRecurrenceRule() {
        return delegate.getRecurrenceRule();
    }

    public List<Date> getDeleteExceptionDates() {
        return delegate.getDeleteExceptionDates();
    }

    public List<Date> getChangeExceptionDates() {
        return delegate.getChangeExceptionDates();
    }

    public Organizer getOrganizer() {
        return delegate.getOrganizer();
    }

    public List<Attendee> getAttendees() {
        return delegate.getAttendees();
    }

    public List<Attachment> getAttachments() {
        return delegate.getAttachments();
    }

    public Classification getClassification() {
        return delegate.getClassification();
    }

    public Integer getSequence() {
        return delegate.getSequence();
    }

    public List<String> getCategories() {
        return delegate.getCategories();
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public void setId(int id) {
        delegate.setId(id);
    }

    public void setUid(String uid) {
        delegate.setUid(uid);
    }

    public void setFilename(String filename) {
        delegate.setFilename(filename);
    }

    public void setiCalId(String iCalId) {
        delegate.setiCalId(iCalId);
    }

    public void setCreated(Date created) {
        delegate.setCreated(created);
    }

    public void setLastModified(Date lastModified) {
        delegate.setLastModified(lastModified);
    }

    public void setCreatedBy(int createdBy) {
        delegate.setCreatedBy(createdBy);
    }

    public void setModifiedBy(int modifiedBy) {
        delegate.setModifiedBy(modifiedBy);
    }

    public void setSummary(String summary) {
        delegate.setSummary(summary);
    }

    public void setLocation(String location) {
        delegate.setLocation(location);
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    public void setStatus(EventStatus status) {
        delegate.setStatus(status);
    }

    public void setStartDate(Date startDate) {
        delegate.setStartDate(startDate);
    }

    public void setStartTimezone(String startTimezone) {
        delegate.setStartTimezone(startTimezone);
    }

    public void setEndDate(Date endDate) {
        delegate.setEndDate(endDate);
    }

    public void setEndTimezone(String endTimezone) {
        delegate.setEndTimezone(endTimezone);
    }

    public void setAllDay(boolean allDay) {
        delegate.setAllDay(allDay);
    }

    public void setTransp(TimeTransparency transp) {
        delegate.setTransp(transp);
    }

    public void setRecurrenceId(int recurrenceId) {
        delegate.setRecurrenceId(recurrenceId);
    }

    public void setRecurrenceRule(String recurrenceRule) {
        delegate.setRecurrenceRule(recurrenceRule);
    }

    public void setDeleteExceptionDates(List<Date> deleteExceptionDates) {
        delegate.setDeleteExceptionDates(deleteExceptionDates);
    }

    public void setChangeExceptionDates(List<Date> changeExceptionDates) {
        delegate.setChangeExceptionDates(changeExceptionDates);
    }

    public void setOrganizer(Organizer organizer) {
        delegate.setOrganizer(organizer);
    }

    public void setAttendees(List<Attendee> attendees) {
        delegate.setAttendees(attendees);
    }

    public void setAttachments(List<Attachment> attachments) {
        delegate.setAttachments(attachments);
    }

    public void setClassification(Classification classification) {
        delegate.setClassification(classification);
    }

    public void setSequence(Integer sequence) {
        delegate.setSequence(sequence);
    }

    public void setCategories(List<String> categories) {
        delegate.setCategories(categories);
    }

}
