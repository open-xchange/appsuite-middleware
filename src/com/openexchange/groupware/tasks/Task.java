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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.tasks;

import java.util.Date;

import com.openexchange.groupware.container.CalendarObject;

/**
 * This class defines the data container for tasks.
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 * @author <a href="mailto:marcus@open-xchange.com">Marcus Klein</a>
 */
public class Task extends CalendarObject {

    public static final int STATUS = 300;

    public static final int PERCENT_COMPLETED = 301;

    public static final int ACTUAL_COSTS = 302;

    public static final int ACTUAL_DURATION = 303;

    public static final int BILLING_INFORMATION = 305;

    public static final int PROJECT_ID = 306;

    public static final int TARGET_COSTS = 307;

    public static final int TARGET_DURATION = 308;

    public static final int PRIORITY = 309;

    public static final int CURRENCY = 312;

    public static final int TRIP_METER = 313;

    public static final int COMPANIES = 314;

    public static final int DATE_COMPLETED = 315;

    public static final int LOW = 1;
    public static final int NORMAL = 2;
    public static final int HIGH = 3;

    public static final int NOT_STARTED = 1;
    public static final int IN_PROGRESS = 2;
    public static final int DONE = 3;
    public static final int WAITING = 4;
    public static final int DEFERRED = 5;

    /**
     * Maximum value of the percent complete attribute.
     */
    public static final int PERCENT_MAXVALUE = 100;

    public static final int DEFAULTFOLDER = -1;

    private int projectId = 0;
    private float targetCosts = 0;
    private float actualCosts = 0;

    /**
     * The duration is measured by outlook in minutes. We do here the same.
     */
    private long targetDuration = 0;

    /**
     * The duration is measured by outlook in minutes. We do here the same.
     */
    private long actualDuration = 0;

    private int priority = 0;
    private int percentComplete = 0;

    private String currency = null;
    private int status = 0;
    private String trip_meter = null;
    private String billing_information = null;
    private String companies = null;
    private Date afterComplete = null;
    private Date date_completed = null;
    private Date alarm = null;

    private boolean projectIdSet = false;
    private boolean targetCostsSet = false;
    private boolean actualCostsSet = false;
    private boolean targetDurationSet = false;
    private boolean b_actual_duration = false;
    private boolean b_priority = false;
    private boolean b_percent_complete = false;
    private boolean b_currency = false;
    private boolean b_status = false;
    private boolean b_trip_meter = false;
    private boolean b_billing_information = false;
    private boolean b_companies = false;
    private boolean b_after_complete = false;
    private boolean b_date_completed = false;
    private boolean bAlarm = false;

    public Task() {
        super();
    }

    // GET METHODS
    public int getProjectID() {
        return projectId;
    }

    public float getTargetCosts() {
        return targetCosts;
    }

    public float getActualCosts() {
        return actualCosts;
    }

    /**
     * @return the target duration
     */
    public long getTargetDuration() {
        return targetDuration;
    }

    /**
     * @return the actual duration
     */
    public long getActualDuration() {
        return actualDuration;
    }

    public int getPriority() {
        return priority;
    }

    public int getPercentComplete() {
        return percentComplete;
    }

    public Date getAlarm() {
        return alarm;
    }

    public String getCurrency() {
        return currency;
    }

    public int getStatus() {
        return status;
    }

    public String getTripMeter() {
        return trip_meter;
    }

    public String getBillingInformation() {
        return billing_information;
    }

    public String getCompanies() {
        return companies;
    }

    public Date getAfterComplete() {
        return afterComplete;
    }

    public Date getDateCompleted() {
        return date_completed;
    }

    // SET METHODS
    public void setProjectID(int project_id) {
        this.projectId = project_id;
        projectIdSet = true;
    }

    public void setTargetCosts(float target_costs) {
        this.targetCosts = target_costs;
        targetCostsSet = true;
    }

    public void setActualCosts(float actual_costs) {
        this.actualCosts = actual_costs;
        actualCostsSet = true;
    }

    /**
     * @param targetDuration the target duration to set
     */
    public void setTargetDuration(final long targetDuration) {
        this.targetDuration = targetDuration;
        targetDurationSet = true;
    }

    /**
     * @param actualDuration the actual duration to set
     */
    public void setActualDuration(final long actualDuration) {
        this.actualDuration = actualDuration;
        b_actual_duration = true;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        b_priority = true;
    }

    public void setPercentComplete(int percent_complete) {
        this.percentComplete = percent_complete;
        b_percent_complete = true;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
        b_currency = true;
    }

    public void setStatus(int status) {
        this.status = status;
        b_status = true;
    }

    public void setTripMeter(String trip_meter) {
        this.trip_meter = trip_meter;
        b_trip_meter = true;
    }

    public void setBillingInformation(String billing_information) {
        this.billing_information = billing_information;
        b_billing_information = true;
    }

    public void setCompanies(String companies) {
        this.companies = companies;
        b_companies = true;
    }

    public void setAfterComplete(Date afterComplete) {
        this.afterComplete = afterComplete;
        b_after_complete = true;
    }

    public void setDateCompleted(Date date_completed) {
        this.date_completed = date_completed;
        b_date_completed = true;
    }

    public void setAlarm(Date alarm) {
        this.alarm = alarm;
        bAlarm = true;
    }

    // REMOVE METHODS
    public void removeProjectID() {
        projectId = 0;
        projectIdSet = false;
    }

    public void removeTargetCosts() {
        targetCosts = 0;
        targetCostsSet = false;
    }

    public void removeActualCosts() {
        actualCosts = 0;
        actualCostsSet = false;
    }

    public void removeTargetDuration() {
        targetDuration = 0;
        targetDurationSet = false;
    }

    public void removeActualDuration() {
        actualDuration = 0;
        b_actual_duration = false;
    }

    public void removePriority() {
        priority = 0;
        b_priority = false;
    }

    public void removePercentComplete() {
        percentComplete = 0;
        b_percent_complete = false;
    }

    public void removeCurrency() {
        currency = null;
        b_currency = false;
    }

    public void removeStatus() {
        status = 0;
        b_status = false;
    }

    public void removeTripMeter() {
        trip_meter = null;
        b_trip_meter = false;
    }

    public void removeBillingInformation() {
        billing_information = null;
        b_billing_information = false;
    }

    public void removeCompanies() {
        companies = null;
        b_companies = false;
    }

    public void removeAfterComplete() {
        afterComplete = null;
        b_after_complete = false;
    }

    public void removeDateCompleted() {
        date_completed = null;
        b_date_completed = false;
    }

    public void removeAlarm() {
        alarm = null;
        bAlarm = false;
    }

    // CONTAINS METHODS
    public boolean containsProjectID() {
        return projectIdSet;
    }

    public boolean containsTargetCosts() {
        return targetCostsSet;
    }

    public boolean containsActualCosts() {
        return actualCostsSet;
    }

    public boolean containsTargetDuration() {
        return targetDurationSet;
    }

    public boolean containsActualDuration() {
        return b_actual_duration;
    }

    public boolean containsPriority() {
        return b_priority;
    }

    public boolean containsPercentComplete() {
        return b_percent_complete;
    }

    public boolean containsCurrency() {
        return b_currency;
    }

    public boolean containsStatus() {
        return b_status;
    }

    public boolean containsTripMeter() {
        return b_trip_meter;
    }

    public boolean containsBillingInformation() {
        return b_billing_information;
    }

    public boolean containsCompanies() {
        return b_companies;
    }

    public boolean containsAfterComplete() {
        return b_after_complete;
    }

    public boolean containsDateCompleted() {
        return b_date_completed;
    }

    public boolean containsAlarm() {
        return bAlarm;
    }

    public void reset() {
        super.reset();

        projectId = 0;
        targetCosts = 0;
        actualCosts = 0;

        targetDuration = 0;

        actualDuration = 0;

        priority = 0;
        percentComplete = 0;

        currency = null;
        status = 0;
        trip_meter = null;
        billing_information = null;
        companies = null;
        afterComplete = null;
        date_completed = null;
        alarm = null;

        projectIdSet = false;
        targetCostsSet = false;
        actualCostsSet = false;
        targetDurationSet = false;
        b_actual_duration = false;
        b_priority = false;
        b_percent_complete = false;
        b_currency = false;
        b_status = false;
        b_trip_meter = false;
        b_billing_information = false;
        b_companies = false;
        b_after_complete = false;
        b_date_completed = false;
        bAlarm = false;
    }

    public boolean isValid() {
        return true;
    }
}
