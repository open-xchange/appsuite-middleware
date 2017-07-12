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

package com.openexchange.chronos;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import com.openexchange.chronos.service.CalendarAvailabilityField;
import com.openexchange.chronos.service.CalendarFreeSlotField;

/**
 * {@link CalendarFreeSlot} - Defines an available time range within a {@link CalendarAvailability} component
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @see <a href="https://tools.ietf.org/html/rfc7953#section-3.1">RFC 7953, section 3.1</a>
 */
public class CalendarFreeSlot implements FieldAware {

    private String id;

    private int calendarUser;
    private String calendarAvailabilityId;
    private String uid;
    private Date creationTimestamp;
    private Date startTime;

    private Date endTime;
    private long duration; //FIXME: use another type?

    private Date created;
    private Date lastModified;

    private String description;
    private String summary;
    private RecurrenceId recurrenceId;
    private String recurrenceRule;

    private List<String> categories;
    private ExtendedProperties extendedProperties;
    private List<String> comments;

    //TODO: iana-props, exdate, rdate, contact

    private final EnumSet<CalendarFreeSlotField> fields;

    /**
     * Initialises a new {@link CalendarFreeSlot}.
     */
    public CalendarFreeSlot() {
        super();
        fields = EnumSet.noneOf(CalendarFreeSlotField.class);
    }

    /**
     * Gets the id
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id
     *
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
        fields.add(CalendarFreeSlotField.id);
    }

    /**
     * Removes the id
     */
    public void removeId() {
        this.id = null;
        fields.remove(CalendarFreeSlotField.id);
    }

    /**
     * Sets the uid
     *
     * @param uid The uid to set
     */
    public void setUid(String uid) {
        this.uid = uid;
        fields.add(CalendarFreeSlotField.uid);
    }

    /**
     * Removes the uid
     */
    public void removeUid() {
        this.uid = null;
        fields.remove(CalendarFreeSlotField.uid);
    }

    /**
     * Sets the creationTimestamp
     *
     * @param creationTimestamp The creationTimestamp to set
     */
    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
        fields.add(CalendarFreeSlotField.dtstamp);
    }

    /**
     * Sets the startTime
     *
     * @param startTime The startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        fields.add(CalendarFreeSlotField.dtstart);
    }

    /**
     * Gets the endTime
     *
     * @return The endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Removes the start time
     */
    public void removeStartTime() {
        this.startTime = null;
        fields.remove(CalendarFreeSlotField.dtstart);
    }

    /**
     * Sets the endTime
     *
     * @param endTime The endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        fields.add(CalendarFreeSlotField.dtend);
    }

    /**
     * Removes the end time
     */
    public void removeEndTime() {
        this.endTime = null;
        fields.remove(CalendarFreeSlotField.dtend);
    }

    /**
     * Gets the duration
     *
     * @return The duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the duration
     *
     * @param duration The duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration;
        fields.add(CalendarFreeSlotField.duration);
    }

    /**
     * Gets the created
     *
     * @return The created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets the created
     *
     * @param created The created to set
     */
    public void setCreated(Date created) {
        this.created = created;
        fields.add(CalendarFreeSlotField.created);
    }

    /**
     * Removes the created timestamp
     */
    public void removeCreated() {
        this.created = null;
        fields.remove(CalendarFreeSlotField.created);
    }

    /**
     * Gets the lastModified
     *
     * @return The lastModified
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the lastModified
     *
     * @param lastModified The lastModified to set
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        fields.add(CalendarFreeSlotField.lastModified);
    }

    /**
     * Removes the last modified
     */
    public void removeLastModified() {
        this.lastModified = null;
        fields.remove(CalendarFreeSlotField.lastModified);
    }

    /**
     * Gets the description
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description
     *
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
        fields.add(CalendarFreeSlotField.description);
    }

    /**
     * Removes the description
     */
    public void removeDescription() {
        this.description = null;
        fields.remove(CalendarFreeSlotField.description);
    }

    /**
     * Gets the summary
     *
     * @return The summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the summary
     *
     * @param summary The summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
        fields.add(CalendarFreeSlotField.summary);
    }

    /**
     * Removes the summary
     */
    public void removeSummary() {
        this.summary = null;
        fields.remove(CalendarFreeSlotField.summary);
    }

    /**
     * Gets the recurrenceId
     *
     * @return The recurrenceId
     */
    public RecurrenceId getRecurrenceId() {
        return recurrenceId;
    }

    /**
     * Sets the recurrenceId
     *
     * @param recurrenceId The recurrenceId to set
     */
    public void setRecurrenceId(RecurrenceId recurrenceId) {
        this.recurrenceId = recurrenceId;
        fields.add(CalendarFreeSlotField.recurid);
    }

    /**
     * Removes the recurrence id
     */
    public void removeRecurrenceId() {
        this.recurrenceId = null;
        fields.remove(CalendarFreeSlotField.recurid);
    }

    /**
     * Gets the categories
     *
     * @return The categories
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * Sets the categories
     *
     * @param categories The categories to set
     */
    public void setCategories(List<String> categories) {
        this.categories = categories;
        fields.add(CalendarFreeSlotField.categories);
    }

    /**
     * Removes the categories
     */
    public void removeCategories() {
        this.categories = null;
        fields.remove(CalendarFreeSlotField.categories);
    }

    /**
     * Gets the extendedProperties
     *
     * @return The extendedProperties
     */
    public ExtendedProperties getExtendedProperties() {
        return extendedProperties;
    }

    /**
     * Sets the extendedProperties
     *
     * @param extendedProperties The extendedProperties to set
     */
    public void setExtendedProperties(ExtendedProperties extendedProperties) {
        this.extendedProperties = extendedProperties;
        fields.add(CalendarFreeSlotField.extendedProperties);
    }

    /**
     * Removes the extended properties
     */
    public void removeExtendedProperties() {
        this.extendedProperties = null;
        fields.remove(CalendarFreeSlotField.extendedProperties);
    }

    /**
     * Gets the comments
     *
     * @return The comments
     */
    public List<String> getComments() {
        return comments;
    }

    /**
     * Sets the comments
     *
     * @param comments The comments to set
     */
    public void setComments(List<String> comments) {
        this.comments = comments;
        fields.add(CalendarFreeSlotField.comment);
    }

    /**
     * Removes the comments
     */
    public void removeComments() {
        this.comments = null;
        fields.remove(CalendarFreeSlotField.comment);
    }

    /**
     * Gets the uid
     *
     * @return The uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * Gets the creationTimestamp
     *
     * @return The creationTimestamp
     */
    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * Gets the startTime
     *
     * @return The startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Gets the recurrenceRule
     *
     * @return The recurrenceRule
     */
    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    /**
     * Sets the recurrenceRule
     *
     * @param recurrenceRule The recurrenceRule to set
     */
    public void setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    /**
     * Removes the recurrence rule
     */
    public void removeRecurrenceRule() {
        this.recurrenceRule = null;
        fields.remove(CalendarFreeSlotField.rrule);
    }

    /**
     * Gets the calendarAvailabilityId
     *
     * @return The calendarAvailabilityId
     */
    public String getCalendarAvailabilityId() {
        return calendarAvailabilityId;
    }

    /**
     * Sets the calendarAvailabilityId
     *
     * @param calendarAvailabilityId The calendarAvailabilityId to set
     */
    public void setCalendarAvailabilityId(String calendarAvailabilityId) {
        this.calendarAvailabilityId = calendarAvailabilityId;
        fields.add(CalendarFreeSlotField.calendarAvailabilityId);
    }

    /**
     * Removes the calendar availability parent id
     */
    public void removeCalendarAvailabilityId() {
        this.calendarAvailabilityId = null;
        fields.remove(CalendarFreeSlotField.calendarAvailabilityId);
    }

    /**
     * Gets the calendarUser
     *
     * @return The calendarUser
     */
    public int getCalendarUser() {
        return calendarUser;
    }

    /**
     * Sets the calendarUser
     *
     * @param calendarUser The calendarUser to set
     */
    public void setCalendarUser(int calendarUser) {
        this.calendarUser = calendarUser;
        fields.add(CalendarFreeSlotField.user);
    }

    /**
     * Removes the calendar user
     */
    public void removeCalendarUser() {
        this.calendarUser = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.FieldAware#contains(com.openexchange.chronos.service.CalendarAvailabilityField)
     */
    @Override
    public boolean contains(CalendarAvailabilityField field) {
        return fields.contains(field);
    }

}
