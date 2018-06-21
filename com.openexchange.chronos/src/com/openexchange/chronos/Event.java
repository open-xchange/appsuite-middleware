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
import java.util.Objects;
import java.util.SortedSet;
import org.dmfs.rfc5545.DateTime;

/**
 * {@link Event}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.6.1">RFC 5545, section 3.6.1</a>
 */
public class Event {

    private String id;
    private String folderId;
    private CalendarUser calendarUser;
    private String uid;
    private RelatedTo relatedTo;
    private String filename;
    private long timestamp;

    private Date created;
    private CalendarUser createdBy;
    private Date lastModified;
    private CalendarUser modifiedBy;
    private int sequence;

    private String summary;
    private String location;
    private String description;
    private List<String> categories;
    private Classification classification;
    private String color;
    private String url;
    private double[] geo;

    private DateTime startDate;
    private DateTime endDate;
    private Transp transp;

    private String seriesId;
    private String recurrenceRule;
    private RecurrenceId recurrenceId;
    private SortedSet<RecurrenceId> recurrenceDates;
    private SortedSet<RecurrenceId> changeExceptionDates;
    private SortedSet<RecurrenceId> deleteExceptionDates;

    private EventStatus status;
    private Organizer organizer;
    private List<Attendee> attendees;

    private List<Attachment> attachments;
    private List<Alarm> alarms;

    private ExtendedProperties extendedProperties;
    private EnumSet<EventFlag> flags;

    private final EnumSet<EventField> setFields;

    /**
     * Initializes a new {@link Event}.
     */
    public Event() {
        super();
        this.setFields = EnumSet.noneOf(EventField.class);
    }

    /**
     * Gets a value indicating whether a specific property is set in the event or not.
     *
     * @param field The field to check
     * @return <code>true</code> if the field is set, <code>false</code>, otherwise
     */
    public boolean isSet(EventField field) {
        return setFields.contains(field);
    }

    /**
     * Gets a value indicating whether multiple properties are set in the event or not.
     *
     * @param fields The fields to check
     * @return <code>true</code> if all fields are <i>set</i>, <code>false</code>, otherwise
     */
    public boolean areSet(EventField... fields) {
        if (null == fields || 0 == fields.length) {
            return true;
        }
        for (EventField field : fields) {
            if (false == isSet(field)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the object identifier of the event.
     *
     * @return The object identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the object identifier of the event.
     *
     * @param value The object identifier to set
     */
    public void setId(String value) {
        id = value;
        setFields.add(EventField.ID);
    }

    /**
     * Removes the object identifier of the event.
     */
    public void removeId() {
        id = null;
        setFields.remove(EventField.ID);
    }

    /**
     * Gets a value indicating whether the object identifier of the event has been set or not.
     *
     * @return <code>true</code> if the object identifier is set, <code>false</code>, otherwise
     */
    public boolean containsId() {
        return isSet(EventField.ID);
    }

    /**
     * Gets the folder identifier of the event.
     *
     * @return The folder identifier
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Sets the folder identifier of the event.
     *
     * @param value The folder identifier to set
     */
    public void setFolderId(String value) {
        folderId = value;
        setFields.add(EventField.FOLDER_ID);
    }

    /**
     * Removes the folder identifier of the event.
     */
    public void removeFolderId() {
        folderId = null;
        setFields.remove(EventField.FOLDER_ID);
    }

    /**
     * Gets a value indicating whether the folder identifier of the event has been set or not.
     *
     * @return <code>true</code> if the folder identifier is set, <code>false</code>, otherwise
     */
    public boolean containsFolderId() {
        return isSet(EventField.FOLDER_ID);
    }

    /**
     * Gets the universal identifier of the event.
     *
     * @return The universal identifier
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the universal identifier of the event.
     *
     * @param value The universal identifier to set
     */
    public void setUid(String value) {
        uid = value;
        setFields.add(EventField.UID);
    }

    /**
     * Removes the universal identifier of the event.
     */
    public void removeUid() {
        uid = null;
        setFields.remove(EventField.UID);
    }

    /**
     * Gets a value indicating whether the universal identifier of the event has been set or not.
     *
     * @return <code>true</code> if the universal identifier is set, <code>false</code>, otherwise
     */
    public boolean containsUid() {
        return setFields.contains(EventField.UID);
    }

    /**
     * Gets the related-to of the event.
     *
     * @return The related-to
     */
    public RelatedTo getRelatedTo() {
        return relatedTo;
    }

    /**
     * Sets the related-to of the event.
     *
     * @param value The related-to to set
     */
    public void setRelatedTo(RelatedTo value) {
        relatedTo = value;
        setFields.add(EventField.RELATED_TO);
    }

    /**
     * Removes the related-to of the event.
     */
    public void removeRelatedTo() {
        relatedTo = null;
        setFields.remove(EventField.RELATED_TO);
    }

    /**
     * Gets a value indicating whether the related-to of the event has been set or not.
     *
     * @return <code>true</code> if the related-to is set, <code>false</code>, otherwise
     */
    public boolean containsRelatedTo() {
        return setFields.contains(EventField.RELATED_TO);
    }

    /**
     * Gets the filename of the event.
     *
     * @return The filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the filename of the event.
     *
     * @param value The filename to set
     */
    public void setFilename(String value) {
        filename = value;
        setFields.add(EventField.FILENAME);
    }

    /**
     * Removes the filename of the event.
     */
    public void removeFilename() {
        filename = null;
        setFields.remove(EventField.FILENAME);
    }

    /**
     * Gets a value indicating whether the filename of the event has been set or not.
     *
     * @return <code>true</code> if the filename is set, <code>false</code>, otherwise
     */
    public boolean containsFilename() {
        return setFields.contains(EventField.FILENAME);
    }

    /**
     * Gets the sequence number of the event.
     *
     * @return The sequence number
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * Sets the sequence number of the event.
     *
     * @param value The sequence number to set
     */
    public void setSequence(int value) {
        sequence = value;
        setFields.add(EventField.SEQUENCE);
    }

    /**
     * Removes the sequence number of the event.
     */
    public void removeSequence() {
        sequence = 0;
        setFields.remove(EventField.SEQUENCE);
    }

    /**
     * Gets a value indicating whether the sequence number of the event has been set or not.
     *
     * @return <code>true</code> if the sequence number is set, <code>false</code>, otherwise
     */
    public boolean containsSequence() {
        return isSet(EventField.SEQUENCE);
    }

    /**
     * Gets the timestamp of the event.
     *
     * @return The timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the event.
     *
     * @param value The timestamp to set
     */
    public void setTimestamp(long value) {
        timestamp = value;
        setFields.add(EventField.TIMESTAMP);
    }

    /**
     * Removes the timestamp of the event.
     */
    public void removeTimestamp() {
        timestamp = 0L;
        setFields.remove(EventField.TIMESTAMP);
    }

    /**
     * Gets a value indicating whether the timestamp of the event has been set or not.
     *
     * @return <code>true</code> if the timestamp is set, <code>false</code>, otherwise
     */
    public boolean containsTimestamp() {
        return isSet(EventField.TIMESTAMP);
    }

    /**
     * Gets the creation date of the event.
     *
     * @return The creation date
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets the creation date of the event.
     *
     * @param value The creation date to set
     */
    public void setCreated(Date value) {
        created = value;
        setFields.add(EventField.CREATED);
    }

    /**
     * Removes the creation date of the event.
     */
    public void removeCreated() {
        created = null;
        setFields.remove(EventField.CREATED);
    }

    /**
     * Gets a value indicating whether the creation date of the event has been set or not.
     *
     * @return <code>true</code> if the creation date is set, <code>false</code>, otherwise
     */
    public boolean containsCreated() {
        return isSet(EventField.CREATED);
    }

    /**
     * Gets the calendar the user who has created the event.
     *
     * @return The creator
     */
    public CalendarUser getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the calendar user who has created the event.
     *
     * @param value The creator to set
     */
    public void setCreatedBy(CalendarUser value) {
        createdBy = value;
        setFields.add(EventField.CREATED_BY);
    }

    /**
     * Removes the calendar user who has created the event.
     */
    public void removeCreatedBy() {
        createdBy = null;
        setFields.remove(EventField.CREATED_BY);
    }

    /**
     * Gets a value indicating whether the identifier of the user who has created the event has been set or not.
     *
     * @return <code>true</code> if the identifier of the creator is set, <code>false</code>, otherwise
     */
    public boolean containsCreatedBy() {
        return isSet(EventField.CREATED_BY);
    }

    /**
     * Gets the last modification date of the event.
     *
     * @return The last modification date
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last modification date of the event.
     *
     * @param value The last modification date to set
     */
    public void setLastModified(Date value) {
        lastModified = value;
        setFields.add(EventField.LAST_MODIFIED);
    }

    /**
     * Removes the last modification date of the event.
     */
    public void removeLastModified() {
        lastModified = null;
        setFields.remove(EventField.LAST_MODIFIED);
    }

    /**
     * Gets a value indicating whether the last modification date of the event has been set or not.
     *
     * @return <code>true</code> if the last modification date is set, <code>false</code>, otherwise
     */
    public boolean containsLastModified() {
        return isSet(EventField.LAST_MODIFIED);
    }

    /**
     * Gets the calendar user who has last modified the event.
     *
     * @return The last modifying user
     */
    public CalendarUser getModifiedBy() {
        return modifiedBy;
    }

    /**
     * Sets the calendar user who has last modified the event.
     *
     * @param value The last modifying user to set
     */
    public void setModifiedBy(CalendarUser value) {
        modifiedBy = value;
        setFields.add(EventField.MODIFIED_BY);
    }

    /**
     * Removes the calendar user who has last modified the event.
     */
    public void removeModifiedBy() {
        modifiedBy = null;
        setFields.remove(EventField.MODIFIED_BY);
    }

    /**
     * Gets a value indicating whether the identifier of the user who has last modified the event has been set or not.
     *
     * @return <code>true</code> if the identifier of the last modifying user is set, <code>false</code>, otherwise
     */
    public boolean containsModifiedBy() {
        return isSet(EventField.MODIFIED_BY);
    }

    /**
     * Gets the calendar user of the event.
     *
     * @return The calendar user
     */
    public CalendarUser getCalendarUser() {
        return calendarUser;
    }

    /**
     * Sets the calendar user of the event.
     *
     * @param value The calendar user to set
     */
    public void setCalendarUser(CalendarUser value) {
        calendarUser = value;
        setFields.add(EventField.CALENDAR_USER);
    }

    /**
     * Removes the calendar user of the event.
     */
    public void removeCalendarUser() {
        calendarUser = null;
        setFields.remove(EventField.CALENDAR_USER);
    }

    /**
     * Gets a value indicating whether the identifier of the calendar user of the event has been set or not.
     *
     * @return <code>true</code> if the identifier of the calendar user is set, <code>false</code>, otherwise
     */
    public boolean containsCalendarUser() {
        return isSet(EventField.CALENDAR_USER);
    }

    /**
     * Gets the summary of the event.
     *
     * @return The summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the summary of the event.
     *
     * @param value The summary to set
     */
    public void setSummary(String value) {
        summary = value;
        setFields.add(EventField.SUMMARY);
    }

    /**
     * Removes the summary of the event.
     */
    public void removeSummary() {
        summary = null;
        setFields.remove(EventField.SUMMARY);
    }

    /**
     * Gets a value indicating whether the summary of the event has been set or not.
     *
     * @return <code>true</code> if the summary is set, <code>false</code>, otherwise
     */
    public boolean containsSummary() {
        return setFields.contains(EventField.SUMMARY);
    }

    /**
     * Gets the location of the event.
     *
     * @return The location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the event.
     *
     * @param value The location to set
     */
    public void setLocation(String value) {
        location = value;
        setFields.add(EventField.LOCATION);
    }

    /**
     * Removes the location of the event.
     */
    public void removeLocation() {
        location = null;
        setFields.remove(EventField.LOCATION);
    }

    /**
     * Gets a value indicating whether the location of the event has been set or not.
     *
     * @return <code>true</code> if the location is set, <code>false</code>, otherwise
     */
    public boolean containsLocation() {
        return setFields.contains(EventField.LOCATION);
    }

    /**
     * Gets the description of the event.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the event.
     *
     * @param value The description to set
     */
    public void setDescription(String value) {
        description = value;
        setFields.add(EventField.DESCRIPTION);
    }

    /**
     * Removes the description of the event.
     */
    public void removeDescription() {
        description = null;
        setFields.remove(EventField.DESCRIPTION);
    }

    /**
     * Gets a value indicating whether the description of the event has been set or not.
     *
     * @return <code>true</code> if the description is set, <code>false</code>, otherwise
     */
    public boolean containsDescription() {
        return setFields.contains(EventField.DESCRIPTION);
    }

    /**
     * Gets the categories of the event.
     *
     * @return The categories
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * Sets the categories of the event.
     *
     * @param value The categories to set
     */
    public void setCategories(List<String> value) {
        categories = value;
        setFields.add(EventField.CATEGORIES);
    }

    /**
     * Removes the categories of the event.
     */
    public void removeCategories() {
        categories = null;
        setFields.remove(EventField.CATEGORIES);
    }

    /**
     * Gets a value indicating whether the categories of the event has been set or not.
     *
     * @return <code>true</code> if the categories is set, <code>false</code>, otherwise
     */
    public boolean containsCategories() {
        return setFields.contains(EventField.CATEGORIES);
    }

    /**
     * Gets the classification of the event.
     *
     * @return The classification
     */
    public Classification getClassification() {
        return classification;
    }

    /**
     * Sets the classification of the event.
     *
     * @param value The classification to set
     */
    public void setClassification(Classification value) {
        classification = value;
        setFields.add(EventField.CLASSIFICATION);
    }

    /**
     * Removes the classification of the event.
     */
    public void removeClassification() {
        classification = null;
        setFields.remove(EventField.CLASSIFICATION);
    }

    /**
     * Gets a value indicating whether the classification of the event has been set or not.
     *
     * @return <code>true</code> if the classification is set, <code>false</code>, otherwise
     */
    public boolean containsClassification() {
        return setFields.contains(EventField.CLASSIFICATION);
    }

    /**
     * Gets the color of the event.
     *
     * @return The color
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the color of the event.
     *
     * @param value The color to set
     */
    public void setColor(String value) {
        color = value;
        setFields.add(EventField.COLOR);
    }

    /**
     * Removes the color of the event.
     */
    public void removeColor() {
        color = null;
        setFields.remove(EventField.COLOR);
    }

    /**
     * Gets a value indicating whether the color of the event has been set or not.
     *
     * @return <code>true</code> if the color is set, <code>false</code>, otherwise
     */
    public boolean containsColor() {
        return setFields.contains(EventField.COLOR);
    }

    /**
     * Gets the url of the event.
     *
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url of the event.
     *
     * @param value The url to set
     */
    public void setUrl(String value) {
        url = value;
        setFields.add(EventField.URL);
    }

    /**
     * Removes the url of the event.
     */
    public void removeUrl() {
        url = null;
        setFields.remove(EventField.URL);
    }

    /**
     * Gets a value indicating whether the url of the event has been set or not.
     *
     * @return <code>true</code> if the url is set, <code>false</code>, otherwise
     */
    public boolean containsUrl() {
        return setFields.contains(EventField.URL);
    }

    /**
     * Gets the global position of the event.
     *
     * @return The global position
     */
    public double[] getGeo() {
        return geo;
    }

    /**
     * Sets the global position of the event.
     *
     * @param value The global position to set
     */
    public void setGeo(double[] value) {
        geo = value;
        setFields.add(EventField.GEO);
    }

    /**
     * Removes the global position of the event.
     */
    public void removeGeo() {
        geo = null;
        setFields.remove(EventField.GEO);
    }

    /**
     * Gets a value indicating whether the global position of the event has been set or not.
     *
     * @return <code>true</code> if the global position is set, <code>false</code>, otherwise
     */
    public boolean containsGeo() {
        return setFields.contains(EventField.GEO);
    }

    /**
     * Gets the start date of the event.
     *
     * @return The start date
     */
    public DateTime getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of the event.
     *
     * @param value The start date to set
     */
    public void setStartDate(DateTime value) {
        startDate = value;
        setFields.add(EventField.START_DATE);
    }

    /**
     * Removes the start date of the event.
     */
    public void removeStartDate() {
        startDate = null;
        setFields.remove(EventField.START_DATE);
    }

    /**
     * Gets a value indicating whether the start date of the event has been set or not.
     *
     * @return <code>true</code> if the start date is set, <code>false</code>, otherwise
     */
    public boolean containsStartDate() {
        return setFields.contains(EventField.START_DATE);
    }

    /**
     * Gets the end date of the event.
     *
     * @return The end date
     */
    public DateTime getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of the event.
     *
     * @param value The end date to set
     */
    public void setEndDate(DateTime value) {
        endDate = value;
        setFields.add(EventField.END_DATE);
    }

    /**
     * Removes the end date of the event.
     */
    public void removeEndDate() {
        endDate = null;
        setFields.remove(EventField.END_DATE);
    }

    /**
     * Gets a value indicating whether the end date of the event has been set or not.
     *
     * @return <code>true</code> if the end date is set, <code>false</code>, otherwise
     */
    public boolean containsEndDate() {
        return setFields.contains(EventField.END_DATE);
    }

    /**
     * Gets the time transparency of the event.
     *
     * @return The time transparency
     */
    public Transp getTransp() {
        return transp;
    }

    /**
     * Sets the time transparency of the event.
     *
     * @param value The time transparency to set
     */
    public void setTransp(Transp value) {
        transp = value;
        setFields.add(EventField.TRANSP);
    }

    /**
     * Removes the time transparency of the event.
     */
    public void removeTransp() {
        transp = null;
        setFields.remove(EventField.TRANSP);
    }

    /**
     * Gets a value indicating whether the time transparency of the event has been set or not.
     *
     * @return <code>true</code> if the time transparency is set, <code>false</code>, otherwise
     */
    public boolean containsTransp() {
        return setFields.contains(EventField.TRANSP);
    }

    /**
     * Gets the series identifier of the event.
     *
     * @return The series identifier
     */
    public String getSeriesId() {
        return seriesId;
    }

    /**
     * Sets the series identifier of the event.
     *
     * @param value The series identifier to set
     */
    public void setSeriesId(String value) {
        seriesId = value;
        setFields.add(EventField.SERIES_ID);
    }

    /**
     * Removes the series identifier of the event.
     */
    public void removeSeriesId() {
        seriesId = null;
        setFields.remove(EventField.SERIES_ID);
    }

    /**
     * Gets a value indicating whether the series identifier of the event has been set or not.
     *
     * @return <code>true</code> if the series identifier is set, <code>false</code>, otherwise
     */
    public boolean containsSeriesId() {
        return setFields.contains(EventField.SERIES_ID);
    }

    /**
     * Gets the recurrence rule of the event.
     *
     * @return The recurrence rule
     */
    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    /**
     * Sets the recurrence rule of the event.
     *
     * @param value The recurrence rule to set
     */
    public void setRecurrenceRule(String value) {
        recurrenceRule = value;
        setFields.add(EventField.RECURRENCE_RULE);
    }

    /**
     * Removes the recurrence rule of the event.
     */
    public void removeRecurrenceRule() {
        recurrenceRule = null;
        setFields.remove(EventField.RECURRENCE_RULE);
    }

    /**
     * Gets a value indicating whether the recurrence rule of the event has been set or not.
     *
     * @return <code>true</code> if the recurrence rule is set, <code>false</code>, otherwise
     */
    public boolean containsRecurrenceRule() {
        return setFields.contains(EventField.RECURRENCE_RULE);
    }

    /**
     * Gets the recurrence identifier of the event.
     *
     * @return The recurrence identifier
     */
    public RecurrenceId getRecurrenceId() {
        return recurrenceId;
    }

    /**
     * Sets the recurrence identifier of the event.
     *
     * @param value The recurrence identifier to set
     */
    public void setRecurrenceId(RecurrenceId value) {
        recurrenceId = value;
        setFields.add(EventField.RECURRENCE_ID);
    }

    /**
     * Removes the recurrence identifier of the event.
     */
    public void removeRecurrenceId() {
        recurrenceId = null;
        setFields.remove(EventField.RECURRENCE_ID);
    }

    /**
     * Gets a value indicating whether the recurrence identifier of the event has been set or not.
     *
     * @return <code>true</code> if the recurrence identifier is set, <code>false</code>, otherwise
     */
    public boolean containsRecurrenceId() {
        return setFields.contains(EventField.RECURRENCE_ID);
    }

    /**
     * Gets the recurrence dates of the event.
     *
     * @return The recurrence dates
     */
    public SortedSet<RecurrenceId> getRecurrenceDates() {
        return recurrenceDates;
    }

    /**
     * Sets the recurrence dates of the event.
     *
     * @param value The recurrence dates to set
     */
    public void setRecurrenceDates(SortedSet<RecurrenceId> value) {
        recurrenceDates = value;
        setFields.add(EventField.RECURRENCE_DATES);
    }

    /**
     * Removes the recurrence dates of the event.
     */
    public void removeRecurrenceDates() {
        recurrenceDates = null;
        setFields.remove(EventField.RECURRENCE_DATES);
    }

    /**
     * Gets a value indicating whether the recurrence dates of the event has been set or not.
     *
     * @return <code>true</code> if the recurrence dates is set, <code>false</code>, otherwise
     */
    public boolean containsRecurrenceDates() {
        return setFields.contains(EventField.RECURRENCE_DATES);
    }

    /**
     * Gets the change exception dates of the event.
     *
     * @return The change exception dates
     */
    public SortedSet<RecurrenceId> getChangeExceptionDates() {
        return changeExceptionDates;
    }

    /**
     * Sets the change exception dates of the event.
     *
     * @param value The change exception dates to set
     */
    public void setChangeExceptionDates(SortedSet<RecurrenceId> value) {
        changeExceptionDates = value;
        setFields.add(EventField.CHANGE_EXCEPTION_DATES);
    }

    /**
     * Removes the change exception dates of the event.
     */
    public void removeChangeExceptionDates() {
        changeExceptionDates = null;
        setFields.remove(EventField.CHANGE_EXCEPTION_DATES);
    }

    /**
     * Gets a value indicating whether the change exception dates of the event has been set or not.
     *
     * @return <code>true</code> if the change exception dates is set, <code>false</code>, otherwise
     */
    public boolean containsChangeExceptionDates() {
        return setFields.contains(EventField.CHANGE_EXCEPTION_DATES);
    }

    /**
     * Gets the delete exception dates of the event.
     *
     * @return The delete exception dates
     */
    public SortedSet<RecurrenceId> getDeleteExceptionDates() {
        return deleteExceptionDates;
    }

    /**
     * Sets the delete exception dates of the event.
     *
     * @param value The delete exception dates to set
     */
    public void setDeleteExceptionDates(SortedSet<RecurrenceId> value) {
        deleteExceptionDates = value;
        setFields.add(EventField.DELETE_EXCEPTION_DATES);
    }

    /**
     * Removes the delete exception dates of the event.
     */
    public void removeDeleteExceptionDates() {
        deleteExceptionDates = null;
        setFields.remove(EventField.DELETE_EXCEPTION_DATES);
    }

    /**
     * Gets a value indicating whether the delete exception dates of the event has been set or not.
     *
     * @return <code>true</code> if the delete exception dates is set, <code>false</code>, otherwise
     */
    public boolean containsDeleteExceptionDates() {
        return setFields.contains(EventField.DELETE_EXCEPTION_DATES);
    }

    /**
     * Gets the status of the event.
     *
     * @return The status
     */
    public EventStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the event.
     *
     * @param value The status to set
     */
    public void setStatus(EventStatus value) {
        status = value;
        setFields.add(EventField.STATUS);
    }

    /**
     * Removes the status of the event.
     */
    public void removeStatus() {
        status = null;
        setFields.remove(EventField.STATUS);
    }

    /**
     * Gets a value indicating whether the status of the event has been set or not.
     *
     * @return <code>true</code> if the status is set, <code>false</code>, otherwise
     */
    public boolean containsStatus() {
        return setFields.contains(EventField.STATUS);
    }

    /**
     * Gets the organizer of the event.
     *
     * @return The organizer
     */
    public Organizer getOrganizer() {
        return organizer;
    }

    /**
     * Sets the organizer of the event.
     *
     * @param value The organizer to set
     */
    public void setOrganizer(Organizer value) {
        organizer = value;
        setFields.add(EventField.ORGANIZER);
    }

    /**
     * Removes the organizer of the event.
     */
    public void removeOrganizer() {
        organizer = null;
        setFields.remove(EventField.ORGANIZER);
    }

    /**
     * Gets a value indicating whether the organizer of the event has been set or not.
     *
     * @return <code>true</code> if the organizer is set, <code>false</code>, otherwise
     */
    public boolean containsOrganizer() {
        return setFields.contains(EventField.ORGANIZER);
    }

    /**
     * Gets the attendees of the event.
     *
     * @return The attendees
     */
    public List<Attendee> getAttendees() {
        return attendees;
    }

    /**
     * Sets the attendees of the event.
     *
     * @param value The attendees to set
     */
    public void setAttendees(List<Attendee> value) {
        attendees = value;
        setFields.add(EventField.ATTENDEES);
    }

    /**
     * Removes the attendees of the event.
     */
    public void removeAttendees() {
        attendees = null;
        setFields.remove(EventField.ATTENDEES);
    }

    /**
     * Gets a value indicating whether the attendees of the event have been set or not.
     *
     * @return <code>true</code> if the attendees are set, <code>false</code>, otherwise
     */
    public boolean containsAttendees() {
        return setFields.contains(EventField.ATTENDEES);
    }

    /**
     * Gets the attachments of the event.
     *
     * @return The attachments
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets the attachments of the event.
     *
     * @param value The attachments to set
     */
    public void setAttachments(List<Attachment> value) {
        attachments = value;
        setFields.add(EventField.ATTACHMENTS);
    }

    /**
     * Removes the attachments of the event.
     */
    public void removeAttachments() {
        attachments = null;
        setFields.remove(EventField.ATTACHMENTS);
    }

    /**
     * Gets a value indicating whether the attachments of the event have been set or not.
     *
     * @return <code>true</code> if the attachments are set, <code>false</code>, otherwise
     */
    public boolean containsAttachments() {
        return setFields.contains(EventField.ATTACHMENTS);
    }

    /**
     * Gets the alarms of the event.
     *
     * @return The alarms
     */
    public List<Alarm> getAlarms() {
        return alarms;
    }

    /**
     * Sets the alarms of the event.
     *
     * @param value The alarms to set
     */
    public void setAlarms(List<Alarm> value) {
        alarms = value;
        setFields.add(EventField.ALARMS);
    }

    /**
     * Removes the alarms of the event.
     */
    public void removeAlarms() {
        alarms = null;
        setFields.remove(EventField.ALARMS);
    }

    /**
     * Gets a value indicating whether the alarms of the event have been set or not.
     *
     * @return <code>true</code> if the alarms are set, <code>false</code>, otherwise
     */
    public boolean containsAlarms() {
        return setFields.contains(EventField.ALARMS);
    }

    /**
     * Gets the extended properties of the event.
     *
     * @return The extended properties
     */
    public ExtendedProperties getExtendedProperties() {
        return extendedProperties;
    }

    /**
     * Sets the extended properties of the event.
     *
     * @param value The extended properties to set
     */
    public void setExtendedProperties(ExtendedProperties value) {
        extendedProperties = value;
        setFields.add(EventField.EXTENDED_PROPERTIES);
    }

    /**
     * Removes the extended properties of the event.
     */
    public void removeExtendedProperties() {
        extendedProperties = null;
        setFields.remove(EventField.EXTENDED_PROPERTIES);
    }

    /**
     * Gets a value indicating whether extended properties of the event have been set or not.
     *
     * @return <code>true</code> if extended properties are set, <code>false</code>, otherwise
     */
    public boolean containsExtendedProperties() {
        return setFields.contains(EventField.EXTENDED_PROPERTIES);
    }

    /**
     * Gets the flags of the event.
     *
     * @return The flags
     */
    public EnumSet<EventFlag> getFlags() {
        return flags;
    }

    /**
     * Sets the flags of the event.
     *
     * @param value The flags to set
     */
    public void setFlags(EnumSet<EventFlag> value) {
        flags = value;
        setFields.add(EventField.FLAGS);
    }

    /**
     * Removes the flags of the event.
     */
    public void removeFlags() {
        flags = null;
        setFields.remove(EventField.FLAGS);
    }

    /**
     * Gets a value indicating whether the flags of the event have been set or not.
     *
     * @return <code>true</code> if the flags are set, <code>false</code>, otherwise
     */
    public boolean containsFlags() {
        return setFields.contains(EventField.FLAGS);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Event [id=").append(getId());
        stringBuilder.append(", seriesId=").append(getSeriesId());
        stringBuilder.append(", recurrenceId=").append(getRecurrenceId());
        stringBuilder.append(", folderId=").append(getFolderId());
        CalendarUser calendarUser = getCalendarUser();
        if (null != calendarUser) {
            stringBuilder.append(", calendarUser=").append(calendarUser.getEntity());
        }
        DateTime startDate = getStartDate();
        stringBuilder.append(", startDate=").append(startDate);
        if (null != startDate && null != startDate.getTimeZone()) {
            stringBuilder.append(" (").append(startDate.getTimeZone().getID()).append(')');
        }
        DateTime endDate = getEndDate();
        stringBuilder.append(", endDate=").append(endDate);
        if (null != endDate && null != endDate.getTimeZone()) {
            stringBuilder.append(" (").append(endDate.getTimeZone().getID()).append(')');
        }
        stringBuilder.append(", summary=").append(getSummary());
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, seriesId, recurrenceId, folderId, calendarUser, startDate, endDate, summary);
    }

    @Override
    public boolean equals(Object other) {
        if (null != other && Event.class.isInstance(other)) {
            Event otherEvent = (Event) other;
            return Objects.equals(id, otherEvent.getId()) && Objects.equals(seriesId, otherEvent.getSeriesId()) && Objects.equals(recurrenceId, otherEvent.getRecurrenceId()) && 
                Objects.equals(folderId, otherEvent.getFolderId()) && Objects.equals(calendarUser, otherEvent.getCalendarUser()) && Objects.equals(startDate, otherEvent.getStartDate()) &&
                Objects.equals(endDate, otherEvent.getEndDate()) && Objects.equals(summary, otherEvent.getSummary());
        }
        return false;
    }

}
