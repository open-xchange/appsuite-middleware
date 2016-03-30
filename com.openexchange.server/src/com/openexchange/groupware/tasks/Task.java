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

package com.openexchange.groupware.tasks;

import static com.openexchange.java.Autoboxing.I;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.tasks.mapping.Alarm;

/**
 * This class defines the data container for tasks.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:marcus@open-xchange.com">Marcus Klein</a>
 */
public class Task extends CalendarObject {

    private static final long serialVersionUID = 3292310353395679976L;

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
    public static final int START_TIME = 316;
    public static final int END_TIME = 317;

    public static final int[] ALL_COLUMNS = {
        // From Task itself
        STATUS, PERCENT_COMPLETED, ACTUAL_COSTS, ACTUAL_DURATION, BILLING_INFORMATION, TARGET_COSTS, TARGET_DURATION, PRIORITY, CURRENCY,
        TRIP_METER, COMPANIES, DATE_COMPLETED,
        // already covered by START_DATE: START_TIME,
        // already covered by END_DATE: END_TIME,

        // From CalendarObject
        TITLE, START_DATE, END_DATE, NOTE, ALARM, RECURRENCE_TYPE, DAYS, DAY_IN_MONTH, MONTH, INTERVAL, UNTIL, PARTICIPANTS, UID, FULL_TIME,
        // not yet implemented: NOTIFICATION, USERS,
        // not implemented anymore: RECURRING_OCCURRENCE, PROJECT_ID,

        // From CommonObject
        CATEGORIES, PRIVATE_FLAG, COLOR_LABEL, NUMBER_OF_ATTACHMENTS,
        // not yet implemented LABEL_NONE, LABEL_1, LABEL_2, LABEL_3, LABEL_4, LABEL_5, LABEL_6, LABEL_7, LABEL_8, LABEL_9, LABEL_10,
        // NUMBER_OF_LINKS,

        // From FolderChildObject
        FOLDER_ID,
        // From DataObject
        OBJECT_ID, CREATED_BY, MODIFIED_BY, CREATION_DATE, LAST_MODIFIED};//, LAST_MODIFIED_UTC };

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

    private BigDecimal targetCosts;

    private BigDecimal actualCosts;

    /**
     * The duration is measured by outlook in minutes. We do here the same.
     */
    private Long targetDuration;

    /**
     * The duration is measured by outlook in minutes. We do here the same.
     */
    private Long actualDuration;

    private Integer priority = null;

    private int percentComplete = 0;

    private String currency = null;

    private int status = 0;

    private String tripMeter = null;

    private String billing_information = null;

    private String companies = null;

    private Date afterComplete = null;

    private Date date_completed = null;

    private Date alarm = null;

    private boolean projectIdSet = false;

    private boolean targetCostsSet = false;

    private boolean actualCostsSet = false;

    private boolean targetDurationSet = false;

    private boolean actualDurationSet = false;

    private boolean prioritySet = false;

    private boolean percentCompleteSet = false;

    private boolean currencySet = false;

    private boolean statusSet = false;

    private boolean tripMeterSet = false;

    private boolean billingInformationSet = false;

    private boolean companiesSet = false;

    private boolean afterCompleteSet = false;

    private boolean dateCompletedSet = false;

    private boolean bAlarm = false;

    public Task() {
        super();
        topic = "ox/common/task";
    }

    // GET METHODS
    public int getProjectID() {
        return projectId;
    }

    public BigDecimal getTargetCosts() {
        return targetCosts;
    }

    public BigDecimal getActualCosts() {
        return actualCosts;
    }

    /**
     * @return the target duration
     */
    public Long getTargetDuration() {
        return targetDuration;
    }

    /**
     * @return the actual duration
     */
    public Long getActualDuration() {
        return actualDuration;
    }

    public Integer getPriority() {
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
        return tripMeter;
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
    public void setProjectID(final int project_id) {
        this.projectId = project_id;
        projectIdSet = true;
    }

    public void setTargetCosts(final BigDecimal targetCosts) {
        this.targetCosts = targetCosts;
        targetCostsSet = true;
    }

    public void setActualCosts(final BigDecimal actualCosts) {
        this.actualCosts = actualCosts;
        actualCostsSet = true;
    }

    /**
     * @param targetDuration the target duration to set
     */
    public void setTargetDuration(final Long targetDuration) {
        this.targetDuration = targetDuration;
        targetDurationSet = true;
    }

    /**
     * @param actualDuration the actual duration to set
     */
    public void setActualDuration(final Long actualDuration) {
        this.actualDuration = actualDuration;
        actualDurationSet = true;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
        prioritySet = true;
    }

    public void setPercentComplete(final int percent_complete) {
        this.percentComplete = percent_complete;
        percentCompleteSet = true;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
        currencySet = true;
    }

    public void setStatus(final int status) {
        this.status = status;
        statusSet = true;
    }

    public void setTripMeter(final String trip_meter) {
        this.tripMeter = trip_meter;
        tripMeterSet = true;
    }

    public void setBillingInformation(final String billing_information) {
        this.billing_information = billing_information;
        billingInformationSet = true;
    }

    public void setCompanies(final String companies) {
        this.companies = companies;
        companiesSet = true;
    }

    public void setAfterComplete(final Date afterComplete) {
        this.afterComplete = afterComplete;
        afterCompleteSet = true;
    }

    public void setDateCompleted(final Date date_completed) {
        this.date_completed = date_completed;
        dateCompletedSet = true;
    }

    public void setAlarm(final Date alarm) {
        this.alarm = alarm;
        bAlarm = true;
    }

    // REMOVE METHODS
    public void removeProjectID() {
        projectId = 0;
        projectIdSet = false;
    }

    public void removeTargetCosts() {
        targetCosts = null;
        targetCostsSet = false;
    }

    public void removeActualCosts() {
        actualCosts = null;
        actualCostsSet = false;
    }

    public void removeTargetDuration() {
        targetDuration = null;
        targetDurationSet = false;
    }

    public void removeActualDuration() {
        actualDuration = null;
        actualDurationSet = false;
    }

    public void removePriority() {
        priority = null;
        prioritySet = false;
    }

    public void removePercentComplete() {
        percentComplete = 0;
        percentCompleteSet = false;
    }

    public void removeCurrency() {
        currency = null;
        currencySet = false;
    }

    public void removeStatus() {
        status = 0;
        statusSet = false;
    }

    public void removeTripMeter() {
        tripMeter = null;
        tripMeterSet = false;
    }

    public void removeBillingInformation() {
        billing_information = null;
        billingInformationSet = false;
    }

    public void removeCompanies() {
        companies = null;
        companiesSet = false;
    }

    public void removeAfterComplete() {
        afterComplete = null;
        afterCompleteSet = false;
    }

    public void removeDateCompleted() {
        date_completed = null;
        dateCompletedSet = false;
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
        return actualDurationSet;
    }

    public boolean containsPriority() {
        return prioritySet;
    }

    public boolean containsPercentComplete() {
        return percentCompleteSet;
    }

    public boolean containsCurrency() {
        return currencySet;
    }

    public boolean containsStatus() {
        return statusSet;
    }

    public boolean containsTripMeter() {
        return tripMeterSet;
    }

    public boolean containsBillingInformation() {
        return billingInformationSet;
    }

    public boolean containsCompanies() {
        return companiesSet;
    }

    public boolean containsAfterComplete() {
        return afterCompleteSet;
    }

    public boolean containsDateCompleted() {
        return dateCompletedSet;
    }

    public boolean containsAlarm() {
        return bAlarm;
    }

    @Override
    public void reset() {
        super.reset();

        projectId = 0;
        targetCosts = null;
        actualCosts = null;

        targetDuration = null;

        actualDuration = null;

        priority = null;
        percentComplete = 0;

        currency = null;
        status = 0;
        tripMeter = null;
        billing_information = null;
        companies = null;
        afterComplete = null;
        date_completed = null;
        alarm = null;

        projectIdSet = false;
        targetCostsSet = false;
        actualCostsSet = false;
        targetDurationSet = false;
        actualDurationSet = false;
        prioritySet = false;
        percentCompleteSet = false;
        currencySet = false;
        statusSet = false;
        tripMeterSet = false;
        billingInformationSet = false;
        companiesSet = false;
        afterCompleteSet = false;
        dateCompletedSet = false;
        bAlarm = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Task ID: ");
        sb.append(objectId);
        return sb.toString();
    }

    private static Collection<Mapper<?>> ALL_MAPPERS = new ArrayList<Mapper<?>>(Mapping.getAllFieldMappers());
    static {
        ALL_MAPPERS.add(new Alarm());
    }

    private static Map<Integer, Mapper<?>> MAPPER_LOOKUP = new HashMap<Integer, Mapper<?>>();
    static {
        for (final Mapper<?> mapper : ALL_MAPPERS) {
            MAPPER_LOOKUP.put(I(mapper.getId()), mapper);
        }
    }

    /**
     * Gets a set containing the identifiers of those fields that are different in this instance compared to the supplied task.
     *
     * @param other The task to determine the differing fields for
     * @return A set of column identifiers of the differing fields
     */
    public Set<Integer> findDifferingFields(Task other) {
        Set<Integer> differingFields = new HashSet<Integer>();
        for (final Mapper<?> mapper : ALL_MAPPERS) {
            if (mapper.isSet(this) && (!mapper.isSet(other) || !mapper.equals(this, other))) {
                differingFields.add(Integer.valueOf(mapper.getId()));
            }
        }
        return differingFields;
    }

    @Override
    public void set(final int field, final Object value) {
        switch (field) {
        case STATUS:
            setStatus(((Integer) value).intValue());
            break;
        case TARGET_DURATION:
            setTargetDuration((Long) value);
            break;
        case DATE_COMPLETED:
            setDateCompleted((Date) value);
            break;
        case TARGET_COSTS:
            setTargetCosts((BigDecimal) value);
            break;
        case PRIORITY:
            setPriority((Integer) value);
            break;
        case BILLING_INFORMATION:
            setBillingInformation((String) value);
            break;
        case ALARM:
            setAlarm((Date) value);
            break;
        case PERCENT_COMPLETED:
            setPercentComplete(((Integer) value).intValue());
            break;
        case COMPANIES:
            setCompanies((String) value);
            break;
        case CURRENCY:
            setCurrency((String) value);
            break;
        case ACTUAL_COSTS:
            setActualCosts((BigDecimal) value);
            break;
        case PROJECT_ID:
            setProjectID(((Integer) value).intValue());
            break;
        case TRIP_METER:
            setTripMeter((String) value);
            break;
        case ACTUAL_DURATION:
            setActualDuration((Long) value);
            break;
        default:
            super.set(field, value);
        }
    }

    @Override
    public Object get(final int field) {
        switch (field) {
        case STATUS:
            return I(getStatus());
        case TARGET_DURATION:
            return getTargetDuration();
        case DATE_COMPLETED:
            return getDateCompleted();
        case TARGET_COSTS:
            return getTargetCosts();
        case PRIORITY:
            return getPriority();
        case BILLING_INFORMATION:
            return getBillingInformation();
        case ALARM:
            return getAlarm();
        case PERCENT_COMPLETED:
            return I(getPercentComplete());
        case COMPANIES:
            return getCompanies();
        case CURRENCY:
            return getCurrency();
        case ACTUAL_COSTS:
            return getActualCosts();
        case PROJECT_ID:
            return I(getProjectID());
        case TRIP_METER:
            return getTripMeter();
        case ACTUAL_DURATION:
            return getActualDuration();
        default:
            return super.get(field);
        }
    }

    @Override
    public boolean contains(final int field) {
        switch (field) {
        case STATUS:
            return containsStatus();
        case TARGET_DURATION:
            return containsTargetDuration();
        case DATE_COMPLETED:
            return containsDateCompleted();
        case TARGET_COSTS:
            return containsTargetCosts();
        case PRIORITY:
            return containsPriority();
        case BILLING_INFORMATION:
            return containsBillingInformation();
        case ALARM:
            return containsAlarm();
        case PERCENT_COMPLETED:
            return containsPercentComplete();
        case COMPANIES:
            return containsCompanies();
        case CURRENCY:
            return containsCurrency();
        case ACTUAL_COSTS:
            return containsActualCosts();
        case PROJECT_ID:
            return containsProjectID();
        case TRIP_METER:
            return containsTripMeter();
        case ACTUAL_DURATION:
            return containsActualDuration();
        default:
            return super.contains(field);
        }
    }

    @Override
    public void remove(final int field) {
        switch (field) {
        case STATUS:
            removeStatus();
            break;
        case TARGET_DURATION:
            removeTargetDuration();
            break;
        case DATE_COMPLETED:
            removeDateCompleted();
            break;
        case TARGET_COSTS:
            removeTargetCosts();
            break;
        case PRIORITY:
            removePriority();
            break;
        case BILLING_INFORMATION:
            removeBillingInformation();
            break;
        case ALARM:
            removeAlarm();
            break;
        case PERCENT_COMPLETED:
            removePercentComplete();
            break;
        case COMPANIES:
            removeCompanies();
            break;
        case CURRENCY:
            removeCurrency();
            break;
        case ACTUAL_COSTS:
            removeActualCosts();
            break;
        case PROJECT_ID:
            removeProjectID();
            break;
        case TRIP_METER:
            removeTripMeter();
            break;
        case ACTUAL_DURATION:
            removeActualDuration();
            break;
        default:
            super.remove(field);
        }

    }
}
