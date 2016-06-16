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
 * {@link Event}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.6.1">RFC 5545, section 3.6.1</a>
 */
public class Event {

    int id;
    int publicFolderId;
    String uid;
    String filename;
    String iCalId;

    Date created;
    Date lastModified;
    int createdBy;
    int modifiedBy;
    private Integer sequence;

    String summary;
    String location;
    String description;
    EventStatus status;
    private List<Attachment> attachments;
    private Classification classification;
    private List<String> categories;
    private String color;

    Date startDate;
    String startTimezone;
    Date endDate;
    String endTimezone;
    boolean allDay;
    TimeTransparency transp;

    int recurrenceId;
    String recurrenceRule;
    List<Date> deleteExceptionDates;
    List<Date> changeExceptionDates;

    Organizer organizer;
    List<Attendee> attendees;

    /**
     * Gets the id
     *
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id
     *
     * @param id The id to set
     */
    public void setId(int id) {
        this.id = id;
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
     * Sets the uid
     *
     * @param uid The uid to set
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets the filename
     *
     * @return The filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the filename
     *
     * @param filename The filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Gets the iCalId
     *
     * @return The iCalId
     */
    public String getiCalId() {
        return iCalId;
    }

    /**
     * Sets the iCalId
     *
     * @param iCalId The iCalId to set
     */
    public void setiCalId(String iCalId) {
        this.iCalId = iCalId;
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
    }

    /**
     * Gets the modified
     *
     * @return The modified
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the modified
     *
     * @param lastModified The modified to set
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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
     * Gets the modifiedBy
     *
     * @return The modifiedBy
     */
    public int getModifiedBy() {
        return modifiedBy;
    }

    /**
     * Sets the modifiedBy
     *
     * @param modifiedBy The modifiedBy to set
     */
    public void setModifiedBy(int modifiedBy) {
        this.modifiedBy = modifiedBy;
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
    }

    /**
     * Gets the status
     *
     * @return The status
     */
    public EventStatus getStatus() {
        return status;
    }

    /**
     * Sets the status
     *
     * @param status The status to set
     */
    public void setStatus(EventStatus status) {
        this.status = status;
    }

    /**
     * Gets the startDate
     *
     * @return The startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the startDate
     *
     * @param startDate The startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the startTimezone
     *
     * @return The startTimezone
     */
    public String getStartTimezone() {
        return startTimezone;
    }

    /**
     * Sets the startTimezone
     *
     * @param startTimezone The startTimezone to set
     */
    public void setStartTimezone(String startTimezone) {
        this.startTimezone = startTimezone;
    }

    /**
     * Gets the endDate
     *
     * @return The endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the endDate
     *
     * @param endTime The endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Gets the endTimezone
     *
     * @return The endTimezone
     */
    public String getEndTimezone() {
        return endTimezone;
    }

    /**
     * Sets the endTimezone
     *
     * @param endTimezone The endTimezone to set
     */
    public void setEndTimezone(String endTimezone) {
        this.endTimezone = endTimezone;
    }

    /**
     * Gets the allDay
     *
     * @return The allDay
     */
    public boolean isAllDay() {
        return allDay;
    }

    /**
     * Sets the allDay
     *
     * @param allDay The allDay to set
     */
    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    /**
     * Gets the transp
     *
     * @return The transp
     */
    public TimeTransparency getTransp() {
        return transp;
    }

    /**
     * Sets the transp
     *
     * @param transp The transp to set
     */
    public void setTransp(TimeTransparency transp) {
        this.transp = transp;
    }

    /**
     * Gets the recurrenceId
     *
     * @return The recurrenceId
     */
    public int getRecurrenceId() {
        return recurrenceId;
    }

    /**
     * Sets the recurrenceId
     *
     * @param recurrenceId The recurrenceId to set
     */
    public void setRecurrenceId(int recurrenceId) {
        this.recurrenceId = recurrenceId;
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
     * Gets the deleteExceptionDates
     *
     * @return The deleteExceptionDates
     */
    public List<Date> getDeleteExceptionDates() {
        return deleteExceptionDates;
    }

    /**
     * Sets the deleteExceptionDates
     *
     * @param deleteExceptionDates The deleteExceptionDates to set
     */
    public void setDeleteExceptionDates(List<Date> deleteExceptionDates) {
        this.deleteExceptionDates = deleteExceptionDates;
    }

    /**
     * Gets the changeExceptionDates
     *
     * @return The changeExceptionDates
     */
    public List<Date> getChangeExceptionDates() {
        return changeExceptionDates;
    }

    /**
     * Sets the changeExceptionDates
     *
     * @param changeExceptionDates The changeExceptionDates to set
     */
    public void setChangeExceptionDates(List<Date> changeExceptionDates) {
        this.changeExceptionDates = changeExceptionDates;
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
    }

    /**
     * Gets the attendees
     *
     * @return The attendees
     */
    public List<Attendee> getAttendees() {
        return attendees;
    }

    /**
     * Sets the attendees
     *
     * @param attendees The attendees to set
     */
    public void setAttendees(List<Attendee> attendees) {
        this.attendees = attendees;
    }

    /**
     * Gets the attachments
     *
     * @return The attachments
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets the attachments
     *
     * @param attachments The attachments to set
     */
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
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
    }

    /**
     * Gets the sequence
     *
     * @return The sequence
     */
    public Integer getSequence() {
        return sequence;
    }

    /**
     * Sets the sequence
     *
     * @param sequence The sequence to set
     */
    public void setSequence(Integer sequence) {
        this.sequence = sequence;
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
    }

    /**
     * Gets the color
     * 
     * @return The color
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the color
     * 
     * @param color The color to set
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return the publicFolderID
     */
    public int getPublicFolderId() {
        return publicFolderId;
    }

    /**
     * @param publicFolderId the publicFolderID to set
     */
    public void setPublicFolderId(int publicFolderId) {
        this.publicFolderId = publicFolderId;
    }

}
