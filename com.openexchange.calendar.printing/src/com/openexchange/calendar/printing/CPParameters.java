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

package com.openexchange.calendar.printing;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.AJAXServlet;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CPParameters {

    public static final String PARAMETER_WEEK_START_DAY = "week_start_day";

    public static final String PARAMETER_WORK_DAY_START_TIME = "work_day_start_time";

    public static final String PARAMETER_WORK_DAY_END_TIME = "work_day_end_time";

    public static final String PARAMETER_WORK_WEEK_START_DAY = "work_week_start_day";

    public static final String PARAMETER_WORK_WEEK_DURATION = "work_week_days_amount";

    public static final String PARAMETER_USERTEMPLATE = "usertemplate";

    private Date start, end, workDayStart, workDayEnd;

    private int weekStart, workWeekStart, workWeekDuration;

    private String template, usertemplate;

    private TimeZone timezone;

    private int folder;

    private final List<String> missingMandatoryFields = new LinkedList<String>();

    private final List<String> missingOptionalFields = new LinkedList<String>();

    private List<String> unparseableFields = new LinkedList<String>();

    public CPParameters() {
        super();
    }

    public CPParameters(HttpServletRequest req, TimeZone zone) {
        this();
        parseRequest(req, zone);
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getUserTemplate() {
        return usertemplate;
    }

    public void setUserTemplate(String template) {
        this.usertemplate = template;
    }

    public boolean hasUserTemplate(){
        return this.usertemplate != null;
    }

    public int getFolder() {
        return folder;
    }

    public void setFolder(int folder) {
        this.folder = folder;
    }

    public boolean hasFolder(){
        return folder != -1;
    }

    public Date getWorkDayStart() {
        return workDayStart;
    }

    public void setWorkDayStart(Date workDayStart) {
        this.workDayStart = workDayStart;
    }

    public boolean hasWorkDayStart(){
        return this.workDayStart != null;
    }

    public Date getWorkDayEnd() {
        return workDayEnd;
    }

    public void setWorkDayEnd(Date workDayEnd) {
        this.workDayEnd = workDayEnd;
    }

    public boolean hasWorkDayEnd(){
        return this.workDayEnd != null;
    }

    public int getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(int weekStart) {
        this.weekStart = weekStart;
    }

    public boolean hasWeekStart(){
        return this.weekStart != -1;
    }

    public int getWorkWeekStart() {
        return workWeekStart;
    }

    public void setWorkWeekStart(int workWeekStart) {
        this.workWeekStart = workWeekStart;
    }

    public boolean hasWorkWeekStart(){
        return this.workWeekStart != -1;
    }

    public int getWorkWeekDuration() {
        return workWeekDuration;
    }

    public void setWorkWeekDuration(int workWeekDuration) {
        this.workWeekDuration = workWeekDuration;
    }

    public boolean hasWorkWeekDuration(){
        return this.workWeekDuration != -1;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public boolean hasTimezone(){
        return this.timezone != null;
    }

    public List<String> getMissingMandatoryFields() {
        return missingMandatoryFields;
    }

    public boolean isMissingMandatoryFields() {
        return missingMandatoryFields.size() > 0;
    }

    public List<String> getMissingOptionalFields() {
        return missingOptionalFields;
    }

    public boolean isMissingOptionalFields() {
        return missingOptionalFields.size() > 0;
    }

    public void setUnparseableFields(List<String> fields) {
        this.unparseableFields = fields;
    }

    public List<String> getUnparseableFields() {
        return unparseableFields;
    }

    public boolean hasUnparseableFields() {
        return unparseableFields.size() > 0;
    }

    public void parseRequest(HttpServletRequest req, TimeZone zone) {
        unparseableFields = new LinkedList<String>();

        start = extractMandatoryDateParam(req, AJAXServlet.PARAMETER_START, zone);
        end = extractMandatoryDateParam(req, AJAXServlet.PARAMETER_END, zone);
        workDayStart = extractOptionalDateParam(req, PARAMETER_WORK_DAY_START_TIME, zone);
        workDayEnd = extractOptionalDateParam(req, PARAMETER_WORK_DAY_END_TIME, zone);
        weekStart = extractOptionalIntParam(req, PARAMETER_WEEK_START_DAY);
        workWeekStart = extractOptionalIntParam(req, PARAMETER_WORK_WEEK_START_DAY);
        workWeekDuration = extractOptionalIntParam(req, PARAMETER_WORK_WEEK_DURATION);
        folder = extractOptionalIntParam(req, AJAXServlet.PARAMETER_FOLDERID);
        template = extractMandatoryStringParam(req, AJAXServlet.PARAMETER_TEMPLATE);
        usertemplate = extractOptionalStringParam(req, PARAMETER_USERTEMPLATE);
        timezone = extractOptionalTimezoneParam(req, AJAXServlet.PARAMETER_TIMEZONE);
    }

    private Date extractOptionalDateParam(HttpServletRequest req, String parameter, TimeZone zone) {
        String val = req.getParameter(parameter);
        if (val == null) {
            missingOptionalFields.add(parameter);
        } else {
            try {
                long time = Long.parseLong(val);
                int offset = zone.getOffset(time);
                return new Date(time - offset);
            } catch (NumberFormatException e) {
                unparseableFields.add(parameter);
                missingOptionalFields.add(parameter);
            }
        }
        return null;
    }

    private Date extractMandatoryDateParam(HttpServletRequest req, String parameter, TimeZone zone) {
        String val = req.getParameter(parameter);
        if (val == null) {
            missingMandatoryFields.add(parameter);
        } else {
            try {
                long time = Long.parseLong(val);
                int offset = zone.getOffset(time);
                return new Date(time - offset);
            } catch (NumberFormatException e) {
                unparseableFields.add(parameter);
                missingMandatoryFields.add(parameter);
            }
        }
        return null;
    }

    private int extractOptionalIntParam(HttpServletRequest req, String parameter) {
        String val = req.getParameter(parameter);
        if (val == null) {
            missingOptionalFields.add(parameter);
        } else {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                unparseableFields.add(parameter);
                missingOptionalFields.add(parameter);
            }
        }
        return -1;
    }

    private int extractMandatoryIntParam(HttpServletRequest req, String parameter) {
        String val = req.getParameter(parameter);
        if (val == null) {
            missingMandatoryFields.add(parameter);
        } else {
            try {
                return Integer.valueOf(val).intValue();
            } catch (NumberFormatException e) {
                unparseableFields.add(parameter);
                missingMandatoryFields.add(parameter);
            }
        }
        return -1;
    }

    private TimeZone extractOptionalTimezoneParam(HttpServletRequest req, String parameter) {
        String val = req.getParameter(parameter);
        if (val == null) {
            missingOptionalFields.add(parameter);
        } else {
            try {
                return TimeZone.getTimeZone(val);
            } catch (NumberFormatException e) {
                unparseableFields.add(parameter);
                missingOptionalFields.add(parameter);
            }
        }
        return null;
    }

    private String extractOptionalStringParam(HttpServletRequest req, String parameter) {
        String val = req.getParameter(parameter);
        if (val == null) {
            missingOptionalFields.add(parameter);
        }
        return val;
    }

    private String extractMandatoryStringParam(HttpServletRequest req, String parameter) {
        String val = req.getParameter(parameter);
        if (val == null) {
            missingMandatoryFields.add(parameter);
        }
        return val;
    }

    @Override
    public String toString() {
        return CPParameters.class.getName() + ": Start = " + start + ", end = " + end + ", folder = " + folder + ", template = " + template + ", missing fields : " + isMissingMandatoryFields();
    }

}
