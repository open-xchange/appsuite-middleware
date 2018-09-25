/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All Integerellectual property rights in the Software are protected by
 *    Integerernational copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the Integererpretation of the term
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
 *     You should have received a copy of the GNU General Public License aLong
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos;

import java.util.TimeZone;

/**
 * {@link AlarmTrigger}s contain the information about when an alarm will be triggered.
 * It also tracks whether this trigger was already triggered or not.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AlarmTrigger implements Comparable<AlarmTrigger> {

    private String action;
    private Integer alarm;
    private String eventId;
    private String folder;
    private RecurrenceId recurrenceId;
    private Long time;
    private Long relatedTime;
    private Long processed;
    private Integer userId;
    private Boolean pushed;
    private TimeZone timezone;

    private boolean isActionSet=false;
    private boolean isAlarmSet=false;
    private boolean isEventIdSet=false;
    private boolean isRecurrenceIdSet = false;
    private boolean isTimeSet=false;
    private boolean isUserIdSet=false;
    private boolean isPushedSet = false;
    private boolean isFolderSet = false;
    private boolean isTimeZoneSet = false;
    private boolean isRelatedTimeSet = false;
    private boolean isProcessedSet = false;


    /**
     * Initializes a new {@link AlarmTrigger}.
     */
    public AlarmTrigger() {
        super();
    }

    /**
     * Gets the isActionSet
     *
     * @return The isActionSet
     */
    public boolean containsAction() {
        return isActionSet;
    }

    /**
     * Gets the isalarmSet
     *
     * @return The isalarmSet
     */
    public boolean containsAlarm() {
        return isAlarmSet;
    }

    /**
     * Gets the isEventIdSet
     *
     * @return The isEventIdSet
     */
    public boolean containsEventId() {
        return isEventIdSet;
    }

    public boolean containsPushed() {
        return isPushedSet;
    }

    /**
     * Gets the isRecurrenceSet
     *
     * @return The isRecurrenceSet
     */
    public boolean containsRecurrenceId() {
        return isRecurrenceIdSet;
    }

    /**
     * Gets the isTimeSet
     *
     * @return The isTimeSet
     */
    public boolean containsTime() {
        return isTimeSet;
    }

    /**
     * Gets the isUserIdSet
     *
     * @return The isUserIdSet
     */
    public boolean containsUserId() {
        return isUserIdSet;
    }

    /**
     * Gets the action
     *
     * @return The action
     */
    public String getAction() {
        return action;
    }

    /**
     * Gets the alarm
     *
     * @return The alarm
     */
    public Integer getAlarm() {
        return alarm;
    }

    /**
     * Gets the eventId
     *
     * @return The eventId
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Gets the folder
     *
     * @return The folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Sets the folder
     *
     * @param folder The folder to set
     */
    public void setFolder(String folder) {
        this.folder = folder;
        this.isFolderSet = true;
    }

    /**
     * Gets the isFolderSet
     *
     * @return The isFolderSet
     */
    public boolean containsFolder() {
        return isFolderSet;
    }

    /**
     * Removes the folder
     */
    public void removeFolder() {
        this.folder = null;
        this.isFolderSet = false;
    }

    /**
     * Whether the alarm is already pushed or not
     *
     * @return The pushed
     */
    public Boolean isPushed() {
        return pushed;
    }

    /**
     * Gets the recurrence identifier
     *
     * @return The recurrence identifier
     */
    public RecurrenceId getRecurrenceId() {
        return recurrenceId;
    }

    /**
     * Gets the time
     *
     * @return The time
     */
    public Long getTime() {
        return time;
    }

    /**
     * Gets the userId
     *
     * @return The userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Removes the action
     */
    public void removeAction() {
        this.action=null;
        this.isActionSet=false;
    }

    /**
     * Removes the alarm
     */
    public void removeAlarm() {
        this.alarm=null;
        this.isAlarmSet=false;
    }

    /**
     * Removes the eventId
     */
    public void removeEventId() {
        this.eventId=null;
        this.isEventIdSet=false;
    }

    /**
     * Removes the pushed
     */
    public void removePushed() {
        this.pushed=null;
        this.isPushedSet = false;
    }

    /**
     * Removes the recurrence identifier
     */
    public void removeRecurrenceId() {
        this.recurrenceId=null;
        this.isRecurrenceIdSet = false;
    }

    /**
     * Removes the user id
     */
    public void removeTime() {
        this.time=null;
        this.isTimeSet=false;
    }

    /**
     * Removes the user id
     */
    public void removeUserId() {
        this.userId=null;
        this.isUserIdSet=false;
    }

    /**
     * Sets the action
     *
     * @param action The action to set
     */
    public void setAction(String action) {
        this.action = action;
        this.isActionSet=true;
    }

    /**
     * Sets the alarm
     *
     * @param alarm The alarm to set
     */
    public void setAlarm(Integer alarm) {
        this.alarm = alarm;
        this.isAlarmSet=true;
    }

    /**
     * Sets the pushed
     *
     * @param pushed A boolean indicating whether the alarm is pushed or not
     */
    public void setPushed(Boolean pushed) {
        this.pushed = pushed;
        this.isPushedSet = true;
    }

    /**
     * Sets the recurrence
     *
     * @param recurrenceId The recurrence identifier to set
     */
    public void setRecurrenceId(RecurrenceId recurrenceId) {
        this.recurrenceId = recurrenceId;
        this.isRecurrenceIdSet = true;
    }

    /**
     * Sets the time
     *
     * @param time The time to set
     */
    public void setTime(Long time) {
        this.time = time;
        this.isTimeSet=true;
    }

    /**
     * Sets the userId
     *
     * @param userId The userId to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
        this.isUserIdSet=true;
    }

    /**
     * Sets the eventId
     *
     * @param eventId The eventId to set
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
        this.isEventIdSet=true;
    }

    /**
     * Gets the timezone
     *
     * @return The timezone
     */
    public TimeZone getTimezone() {
        return timezone;
    }

    /**
     * Sets the timezone
     *
     * @param timezone The timezone to set
     */
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
        this.isTimeZoneSet = true;
    }

    /**
     * Removes the timezone
     */
    public void removeTimezone(){
        this.timezone = null;
        this.isTimeZoneSet = false;
    }

    /**
     * Checks whether the {@link AlarmTrigger} contains a timezone
     *
     * @return <code>true</code> if a timezone is set, <code>false</code> otherwise
     */
    public boolean containsTimezone(){
        return isTimeZoneSet;
    }

    /**
     * Gets the relatedTime
     *
     * @return The relatedTime
     */
    public Long getRelatedTime() {
        return relatedTime;
    }

    /**
     * Sets the relatedTime
     *
     * @param relatedTime The relatedTime to set
     */
    public void setRelatedTime(Long relatedTime) {
        this.relatedTime = relatedTime;
        this.isRelatedTimeSet = true;
    }

    public boolean containsRelatedTime(){
        return isRelatedTimeSet;
    }

    public void removeRelatedTime(){
        this.relatedTime = null;
        this.isRelatedTimeSet = false;
    }

    /**
     * Gets the processed value
     *
     * @return The processed value
     */
    public Long getProcessed() {
        return processed;
    }

    /**
     * Sets the relatedTime
     *
     * @param processed The processed value
     */
    public void setProcessed(Long processed) {
        this.processed = processed;
        this.isProcessedSet = true;
    }

    public boolean containsProcessed(){
        return isProcessedSet;
    }

    public void removeProcessed(){
        this.processed = null;
        this.isProcessedSet = false;
    }

    @Override
    public int compareTo(AlarmTrigger o) {
        long thisTime = this.getTime().longValue();
        long otherTime = o.getTime().longValue();
        return thisTime == otherTime ? 0 : thisTime > otherTime ? 1 : 0;
    }

}
