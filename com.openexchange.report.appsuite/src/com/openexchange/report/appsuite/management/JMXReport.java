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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.report.appsuite.management;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.report.appsuite.Report;
import com.openexchange.version.Version;


/**
 * The {@link JMXReport} is a wrapper around a {@link Report} that can be transmitted via JMXs MXBean conventions. It contains
 * nearly the same attributes as the report, but opts to serialize the report data in JSON format, adding into it the uuid, reportType, start and stop timestamps and the
 * OX versioning information.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class JMXReport {
    private String uuid;
    private int pendingTasks;
    private int tasks;
    private long startTime;
    private long stopTime;
    private String type;
    private String data;

    /**
     * Creates a JMX friendly version of the given report
     */
    public JMXReport(Report report) throws Exception {
        
        this.uuid = report.getUUID();
        this.pendingTasks = report.getNumberOfPendingTasks();
        this.tasks = report.getNumberOfTasks();
        this.startTime = report.getStartTime();
        this.stopTime = report.getStopTime();
    
        this.type = report.getType();
    
        try {
            JSONObject jsonData = (JSONObject) JSONCoercion.coerceToJSON(report.getData());
            
            jsonData.put("uuid", uuid);
            jsonData.put("reportType", type);
            
            jsonData.put("timestamps", new JSONObject().put("start", startTime).put("stop", stopTime));
            jsonData.put("version", new JSONObject().put("version", Version.getInstance().getVersionString()).put("buildDate", Version.getInstance().getBuildDate()));
            
            this.data = jsonData.toString();
        } catch (JSONException e) {
            throw new Exception(e.getMessage());
        }
    }

    
    public String getUuid() {
        return uuid;
    }

    
    public int getPendingTasks() {
        return pendingTasks;
    }

    
    public int getTasks() {
        return tasks;
    }

    
    public long getStartTime() {
        return startTime;
    }

    
    public long getStopTime() {
        return stopTime;
    }

    
    public String getType() {
        return type;
    }

    
    public String getData() {
        return data;
    }
    
    /**
     * A utility method for wrapping an array of reports
     */
    public static JMXReport[] wrap(Report[] reports) throws Exception {
        JMXReport[] wrapped = new JMXReport[reports.length];
        for (int i = 0; i < reports.length; i++) {
            wrapped[i] = new JMXReport(reports[i]);
        }
        
        return wrapped;
    }
}
