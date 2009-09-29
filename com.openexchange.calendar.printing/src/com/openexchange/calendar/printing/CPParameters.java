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

    public static final String WEEK_START_DAY = "week_start_day";

    public static final String WORK_DAY_START_TIME = "work_day_start_time";

    public static final String WORK_DAY_END_TIME = "work_day_end_time";

    public static final String WORK_WEEK_START_DAY = "work_week_start_day";

    public static final String WORK_WEEK_DURATION = "work_week_days_amount";


    private Date start, end, workDayStart, workDayEnd;
    
    private int weekStart, workWeekStart, workWeekDuration;

    private String template;
    
    private TimeZone timezone;

    private int folder;

    private List<String> missingFields;
    
    private boolean isValid = true;
    
    public CPParameters() {

    }

    public CPParameters(HttpServletRequest req) {
        this();
        parseRequest(req);
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

    public int getFolder() {
        return folder;
    }

    public void setFolder(int folder) {
        this.folder = folder;
    }

    
    public Date getWorkDayStart() {
        return workDayStart;
    }

    
    public void setWorkDayStart(Date workDayStart) {
        this.workDayStart = workDayStart;
    }

    
    public Date getWorkDayEnd() {
        return workDayEnd;
    }

    
    public void setWorkDayEnd(Date workDayEnd) {
        this.workDayEnd = workDayEnd;
    }

    
    public int getWeekStart() {
        return weekStart;
    }

    
    public void setWeekStart(int weekStart) {
        this.weekStart = weekStart;
    }

    
    public int getWorkWeekStart() {
        return workWeekStart;
    }

    
    public void setWorkWeekStart(int workWeekStart) {
        this.workWeekStart = workWeekStart;
    }

    
    public int getWorkWeekDuration() {
        return workWeekDuration;
    }

    
    public void setWorkWeekDuration(int workWeekDuration) {
        this.workWeekDuration = workWeekDuration;
    }

    
    public TimeZone getTimezone() {
        return timezone;
    }

    
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public List<String> getMissingFields() {
        return this.missingFields;
    }

    public boolean isMissingFields() {
        return missingFields != null;
    }

    public void parseRequest(HttpServletRequest req) {
        LinkedList<String> missingFields = new LinkedList<String>();

        String start = req.getParameter(AJAXServlet.PARAMETER_START);
        if (start == null)
            missingFields.add(AJAXServlet.PARAMETER_START);
        else
            this.start = new Date(Long.valueOf(start).longValue());

        String end = req.getParameter(AJAXServlet.PARAMETER_END);
        if (end == null)
            missingFields.add(AJAXServlet.PARAMETER_END);
        else
            this.end = new Date(Long.valueOf(end).longValue());

        String workDayStartTime = req.getParameter(WORK_DAY_START_TIME);
        if (workDayStartTime == null)
            missingFields.add(WORK_DAY_START_TIME);
        else
            this.workDayStart = new Date(Long.valueOf(workDayStartTime).longValue());
        
        String workDayEndTime = req.getParameter(WORK_DAY_END_TIME);
        if (workDayEndTime == null)
            missingFields.add(WORK_DAY_END_TIME);
        else
            this.workDayEnd = new Date(Long.valueOf(workDayEndTime).longValue());
        
        String weekStartDay = req.getParameter(WEEK_START_DAY);
        if (weekStartDay == null)
            missingFields.add(WEEK_START_DAY);
        else
            this.weekStart = Integer.valueOf(weekStartDay).intValue();
        
        String workWeekStartDay = req.getParameter(WORK_WEEK_START_DAY);
        if (workWeekStartDay == null)
            missingFields.add(WORK_WEEK_START_DAY);
        else
            this.workWeekStart = Integer.valueOf(workWeekStartDay).intValue();
        
        String workWeekDuration = req.getParameter(WORK_WEEK_DURATION);
        if (workWeekDuration == null)
            missingFields.add(WORK_WEEK_DURATION);
        else
            this.workWeekDuration = Integer.valueOf(workWeekDuration).intValue();
        
        String folder = req.getParameter(AJAXServlet.PARAMETER_FOLDERID);
        this.folder = Integer.valueOf(folder).intValue();

        String templateName = req.getParameter(AJAXServlet.PARAMETER_TEMPLATE);
        if (templateName == null)
            missingFields.add(AJAXServlet.PARAMETER_TEMPLATE);
        else
            this.template = templateName;
        
        String timezone = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
        if (timezone == null)
            missingFields.add(AJAXServlet.PARAMETER_TIMEZONE);
        else
            this.timezone = TimeZone.getTimeZone(timezone);    
    }

    @Override
    public String toString() {
        return CPParameters.class.getName() + ": Start = " + start + ", end = " + end + ", folder = " + folder + ", template = " + template + ", missing fields : " + isMissingFields();
    }

}
