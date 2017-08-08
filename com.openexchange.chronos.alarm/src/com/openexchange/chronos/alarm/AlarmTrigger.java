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

package com.openexchange.chronos.alarm;


/**
 * {@link AlarmTrigger}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AlarmTrigger {

    private Integer account;
    private String action;
    private Integer alarm;
    private Integer contextId;
    private String recurrence;
    private Long time;
    private Integer userId;
    private Boolean processed;

    private boolean isAccountSet=false;
    private boolean isActionSet=false;
    private boolean isAlarmSet=false;
    private boolean isContextIdSet=false;
    private boolean isRecurrenceSet=false;
    private boolean isTimeSet=false;
    private boolean isUserIdSet=false;
    private boolean isProcessedSet=false;


    /**
     * Initializes a new {@link AlarmTrigger}.
     */
    public AlarmTrigger() {
        super();
    }


    /**
     * Gets the isAccountSet
     *
     * @return The isAccountSet
     */
    public boolean containsAccount() {
        return isAccountSet;
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
     * Gets the isContextIdSet
     *
     * @return The isContextIdSet
     */
    public boolean containsContextId() {
        return isContextIdSet;
    }


    public boolean containsProcessed() {
        return isProcessedSet;
    }


    /**
     * Gets the isRecurrenceSet
     *
     * @return The isRecurrenceSet
     */
    public boolean containsRecurrence() {
        return isRecurrenceSet;
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
     * Gets the account
     *
     * @return The account
     */
    public Integer getAccount() {
        return account;
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
     * Gets the contextId
     *
     * @return The contextId
     */
    public Integer getContextId() {
        return contextId;
    }



    /**
     * Whether the alarm is processed or not
     *
     * @return The processed
     */
    public Boolean isProcessed() {
        return processed;
    }


    /**
     * Gets the recurrence
     *
     * @return The recurrence
     */
    public String getRecurrence() {
        return recurrence;
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
     * Removes the account
     */
    public void removeAccount() {
        this.account=null;
        this.isAccountSet=false;
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
     * Removes the recurrence
     */
    public void removeProcessed() {
        this.processed=null;
        this.isProcessedSet=false;
    }

    /**
     * Removes the recurrence
     */
    public void removeRecurrence() {
        this.recurrence=null;
        this.isRecurrenceSet=false;
    }



    /**
     * Removes the context id
     */
    public void removesContextId() {
        this.contextId=null;
        this.isContextIdSet=false;
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
     * Sets the account
     *
     * @param account The account to set
     */
    public void setAccount(Integer account) {
        this.account = account;
        this.isAccountSet=true;
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
     * Sets the contextId
     *
     * @param contextId The contextId to set
     */
    public void setContextId(Integer contextId) {
        this.contextId = contextId;
        this.isContextIdSet=true;
    }

    /**
     * Sets the processed
     *
     * @param processed A boolean indicating whether the alarm is processed or not
     */
    public void setProcessed(Boolean processed) {
        this.processed = processed;
        this.isProcessedSet=true;
    }


    /**
     * Sets the recurrence
     *
     * @param recurrence The recurrence to set
     */
    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
        this.isRecurrenceSet=true;
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



}
