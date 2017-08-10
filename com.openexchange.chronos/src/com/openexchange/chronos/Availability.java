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
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.service.AvailabilityField;
import com.openexchange.chronos.service.CalendarAvailabilityField;

/**
 * {@link Availability} - Defines periods of availability for a calendar user.
 * Provides a grouping of available time information over a specific range of time.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @see <a href="https://tools.ietf.org/html/rfc7953#section-3.1">RFC 7953, section 3.1</a>
 */
public class Availability implements FieldAware, Comparable<Availability>, Cloneable {

    private String id;

    /** The 'dtstamp' */
    private Date creationTimestamp;
    private String uid;
    private int calendarUser;

    private BusyType busyType = BusyType.BUSY_UNAVAILABLE;
    private Classification classification;

    private int priority;
    private int sequence;

    private Date created;
    private int createdBy;
    private Date lastModified;

    private DateTime startTime;
    private DateTime endTime;

    private String description;
    private String summary;
    private String location;
    private Organizer organizer;
    private String url;
    private List<String> comments;

    private int duration;

    private ExtendedProperties extendedProperties;
    private List<String> categories;

    private List<Available> available;

    private EnumSet<AvailabilityField> fields;

    // TODO: map iana-properties?

    /**
     * Initialises a new {@link Availability}.
     */
    public Availability() {
        super();
        fields = EnumSet.noneOf(AvailabilityField.class);
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
        fields.add(AvailabilityField.id);
    }

    /**
     * Removes the id
     */
    public void removeId() {
        this.id = null;
        fields.remove(AvailabilityField.id);
    }

    /**
     * Sets the uid
     *
     * @param uid The uid to set
     */
    public void setUid(String uid) {
        this.uid = uid;
        fields.add(AvailabilityField.uid);
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
     * Removes the identifier
     */
    public void removeUid() {
        uid = null;
        fields.remove(AvailabilityField.uid);
    }

    /**
     * Sets the creationTimestamp
     *
     * @param creationTimestamp The creationTimestamp to set
     */
    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
        fields.add(AvailabilityField.dtstamp);
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
     * Removes the creation timestamp
     */
    public void removeCreationTimestamp() {
        creationTimestamp = null;
        fields.remove(AvailabilityField.dtstamp);
    }

    /**
     * Gets the busyType
     *
     * @return The busyType
     */
    public BusyType getBusyType() {
        return busyType;
    }

    /**
     * Sets the busyType
     *
     * @param busyType The busyType to set
     */
    public void setBusyType(BusyType busyType) {
        this.busyType = busyType;
        fields.add(AvailabilityField.busytype);
    }

    /**
     * Removes the busy type
     */
    public void removeBusyType() {
        busyType = null;
        fields.remove(AvailabilityField.busytype);
    }

    /**
     * Gets the classification
     *
     * @return The classification
     */
    public Classification getClassification() {
        return classification;
    }

    /**
     * Sets the classification
     *
     * @param classification The classification to set
     */
    public void setClassification(Classification classification) {
        this.classification = classification;
        fields.add(AvailabilityField.classification);
    }

    /**
     * Removes the classification
     */
    public void removeClassification() {
        classification = null;
        fields.remove(AvailabilityField.classification);
    }

    /**
     * Gets the priority
     *
     * @return The priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority
     *
     * @param priority The priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
        fields.add(AvailabilityField.priority);
    }

    /**
     * Removes the prioriry
     */
    public void removePriority() {
        priority = 0;
        fields.remove(AvailabilityField.priority);
    }

    /**
     * Gets the sequence
     *
     * @return The sequence
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * Sets the sequence
     *
     * @param sequence The sequence to set
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
        fields.add(AvailabilityField.seq);
    }

    /**
     * Removes the sequence
     */
    public void removeSequence() {
        sequence = -1;
        fields.remove(AvailabilityField.seq);
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
        fields.add(AvailabilityField.created);
    }

    /**
     * Removes the created timestamp
     */
    public void removeCreated() {
        created = null;
        fields.remove(AvailabilityField.created);
    }

    /**
     * Gets the createdBy
     *
     * @return The createdBy
     */
    public int getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the createdBy
     *
     * @param createdBy The createdBy to set
     */
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Removes the identifier of the user who has created the availability block.
     */
    public void removeCreatedBy() {
        createdBy = 0;
        fields.remove(AvailabilityField.createdBy);
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
        fields.add(AvailabilityField.lastModified);
    }

    /**
     * Removes the last modification date.
     */
    public void removeLastModified() {
        lastModified = null;
        fields.remove(AvailabilityField.lastModified);
    }

    /**
     * Gets the startTime
     *
     * @return The startTime
     */
    public DateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the startTime
     *
     * @param startTime The startTime to set
     */
    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
        fields.add(AvailabilityField.dtstart);
    }

    /**
     * Removes the start date
     */
    public void removeStartTime() {
        startTime = null;
        fields.remove(AvailabilityField.dtstart);
    }

    /**
     * Gets the endTime
     *
     * @return The endTime
     */
    public DateTime getEndTime() {
        return endTime;
    }

    /**
     * Sets the endTime
     *
     * @param endTime The endTime to set
     */
    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
        fields.add(AvailabilityField.dtend);
    }

    /**
     * Removes the end date
     */
    public void removeEndTime() {
        endTime = null;
        fields.remove(AvailabilityField.dtend);
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
        fields.add(AvailabilityField.description);
    }

    /**
     * Removes the description
     */
    public void removeDescription() {
        description = null;
        fields.remove(AvailabilityField.description);
    }

    /**
     * Gets the location
     *
     * @return The location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location
     *
     * @param location The location to set
     */
    public void setLocation(String location) {
        this.location = location;
        fields.add(AvailabilityField.location);
    }

    /**
     * Removes the location
     */
    public void removeLocation() {
        location = null;
        fields.remove(AvailabilityField.location);
    }

    /**
     * Gets the organizer
     *
     * @return The organizer
     */
    public Organizer getOrganizer() {
        return organizer;
    }

    /**
     * Sets the organizer
     *
     * @param organizer The organizer to set
     */
    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
        fields.add(AvailabilityField.organizer);
    }

    /**
     * Removes the organizer
     */
    public void removeOrganizer() {
        organizer = null;
        fields.remove(AvailabilityField.organizer);
    }

    /**
     * Gets the url
     *
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url
     *
     * @param url The url to set
     */
    public void setUrl(String url) {
        this.url = url;
        fields.add(AvailabilityField.url);
    }

    /**
     * Removes the url
     */
    public void removeUrl() {
        url = null;
        fields.remove(AvailabilityField.url);
    }

    /**
     * Gets the duration
     *
     * @return The duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the duration
     *
     * @param duration The duration to set
     */
    public void setDuration(int duration) {
        this.duration = duration;
        fields.add(AvailabilityField.duration);
    }

    /**
     * Removes the duration
     */
    public void removeDuration() {
        duration = -1;
        fields.remove(AvailabilityField.duration);
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
        fields.add(AvailabilityField.extendedProperties);
    }

    /**
     * Removes the extended properties
     */
    public void removeExtendedProperties() {
        extendedProperties = null;
        fields.remove(AvailabilityField.extendedProperties);
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
        fields.add(AvailabilityField.categories);
    }

    /**
     * Removes the categories
     */
    public void removeCategories() {
        categories = null;
        fields.remove(AvailabilityField.categories);
    }

    /**
     * Gets the {@link Available} components
     * s
     * 
     * @return The {@link Available} components
     */
    public List<Available> getAvailable() {
        return available;
    }

    /**
     * Sets the {@link Available} components
     *
     * @param available The {@link Available} components to set
     */
    public void setAvailable(List<Available> available) {
        this.available = available;
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
        fields.add(AvailabilityField.summary);
    }

    /**
     * Removes the summary
     */
    public void removeSummary() {
        summary = null;
        fields.remove(AvailabilityField.summary);
    }

    /**
     * Gets the comment
     *
     * @return The comment
     */
    public List<String> getComments() {
        return comments;
    }

    /**
     * Sets the comment
     *
     * @param comment The comment to set
     */
    public void setComments(List<String> comments) {
        this.comments = comments;
        fields.add(AvailabilityField.comment);
    }

    /**
     * Removes the comment
     */
    public void removeComments() {
        comments = null;
        fields.remove(AvailabilityField.comment);
    }

    /**
     * @return the calendarUser
     */
    public int getCalendarUser() {
        return calendarUser;
    }

    /**
     * @param calendarUser the calendarUser to set
     */
    public void setCalendarUser(int calendarUser) {
        this.calendarUser = calendarUser;
        fields.add(AvailabilityField.user);
    }

    public void removeCalendarUser() {
        calendarUser = 0;
        fields.remove(AvailabilityField.user);
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public Availability clone() {
        Availability clone;
        try {
            clone = (Availability) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
        if (contains(AvailabilityField.busytype)) {
            clone.setBusyType(busyType);
        }
        if (contains(AvailabilityField.categories)) {
            clone.setCategories(categories);
        }
        if (contains(AvailabilityField.classification)) {
            clone.setClassification(classification);
        }
        if (contains(AvailabilityField.comment)) {
            clone.setComments(comments);
        }
        if (contains(AvailabilityField.created)) {
            clone.setCreated(created);
        }
        if (contains(AvailabilityField.createdBy)) {
            clone.setCreatedBy(createdBy);
        }
        if (contains(AvailabilityField.description)) {
            clone.setDescription(description);
        }
        if (contains(AvailabilityField.dtend)) {
            clone.setEndTime(endTime);
        }
        if (contains(AvailabilityField.dtstamp)) {
            clone.setCreationTimestamp(creationTimestamp);
        }
        if (contains(AvailabilityField.dtstart)) {
            clone.setStartTime(startTime);
        }
        if (contains(AvailabilityField.duration)) {
            clone.setDuration(duration);
        }
        if (contains(AvailabilityField.extendedProperties)) {
            clone.setExtendedProperties(extendedProperties);
        }
        if (contains(AvailabilityField.id)) {
            clone.setId(id);
        }
        if (contains(AvailabilityField.lastModified)) {
            clone.setLastModified(lastModified);
        }
        if (contains(AvailabilityField.location)) {
            clone.setLocation(location);
        }
        if (contains(AvailabilityField.organizer)) {
            clone.setOrganizer(organizer);
        }
        if (contains(AvailabilityField.priority)) {
            clone.setPriority(priority);
        }
        if (contains(AvailabilityField.seq)) {
            clone.setSequence(sequence);
        }
        if (contains(AvailabilityField.summary)) {
            clone.setSummary(summary);
        }
        if (contains(AvailabilityField.uid)) {
            clone.setUid(uid);
        }
        if (contains(AvailabilityField.url)) {
            clone.setUrl(url);
        }
        clone.setCalendarUser(calendarUser);

        return clone;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CalendarAvailability [uid=").append(getUid()).append(", busyType=").append(busyType).append(", startTime=").append(startTime).append(", endTime=").append(endTime).append(", description=").append(description).append("]");
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Availability o) {
        //TODO: consider the '0' case
        if (getPriority() > o.getPriority()) {
            return -1;
        } else if (getPriority() < o.getPriority()) {
            return 1;
        } else {
            return 0;
        }
    }
}
