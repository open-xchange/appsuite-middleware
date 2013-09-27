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

package com.openexchange.report.appsuite.defaultHandlers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.log.LogFactory;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.ContextReportCumulator;
import com.openexchange.report.appsuite.Report;
import com.openexchange.report.appsuite.ReportContextHandler;
import com.openexchange.report.appsuite.ReportFinishingTouches;
import com.openexchange.report.appsuite.ReportUserHandler;
import com.openexchange.report.appsuite.Services;
import com.openexchange.report.appsuite.UserReport;
import com.openexchange.report.appsuite.UserReportCumulator;
import com.openexchange.tools.file.QuotaFileStorage;


/**
 * The {@link CapabilityHandler} analyzes a users capabilities and filestore quota. It sums up unique combinations of capabilities and quota and gives counts for
 * the total number of users that have these settings, admins, and deactivated users.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CapabilityHandler implements ReportUserHandler, ReportContextHandler, UserReportCumulator, ContextReportCumulator, ReportFinishingTouches {

    private static final Log LOG = LogFactory.getLog(CapabilityHandler.class);
    
    
    @Override
    public boolean appliesTo(String reportType) {
        // This is the cornerstone of the default report
        return "default".equals(reportType);
    }
    
    @Override
    public void runContextReport(ContextReport contextReport) {
        // Grab the file store quota from the context and save them in the report
        Context ctx = contextReport.getContext();
        try {
            long quota = QuotaFileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx).getQuota();
            contextReport.set("macdetail-quota", "quota", quota);
            
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
        }
    }


    @Override
    public void runUserReport(UserReport userReport) {
        try {
            // First look up the capabilities for this user
            Set<Capability> capabilities = Services.getService(CapabilityService.class).getCapabilities(userReport.getUser().getId(), userReport.getContext().getContextId());

            // Next, turn them into a list of strings
            ArrayList<String> c = new ArrayList<String>(capabilities.size());

            for (Capability capability : capabilities) {
                c.add(capability.getId().toLowerCase());
            }
            
            // Sort them alphabetically so we can more easily find the same list of capabilities again
            Collections.sort(c);
            
            StringBuilder cString = new StringBuilder();
            for(String cap: c) {
                cString.append(cap).append(",");
            }
            
            cString.setLength(cString.length() - 1);
            
            // Remember both the list and the identifying comma-separated String in the userReport
            userReport.set("macdetail", "capabilities", cString.toString());
            userReport.set("macdetail", "capabilityList", c);
            
            // Determine if the user is disabled
            if (!userReport.getUser().isMailEnabled()) {
                userReport.set("macdetail", "disabled", Boolean.TRUE);
            } else {
                userReport.set("macdetail", "disabled", Boolean.FALSE);
            }
            
            // Determine if the user is the admin user
            if (userReport.getContext().getMailadmin() == userReport.getUser().getId()) {
                userReport.set("macdetail", "mailadmin", Boolean.TRUE);
            } else {
                userReport.set("macdetail", "mailadmin", Boolean.FALSE);
            }
            
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    // In the context report we keep a count of users/disabled users/admins that share the same capabilities
    // So we have to count every unique combination of capabilities
    @Override
    public void merge(UserReport userReport, ContextReport contextReport) {
        
        // Retrieve the capabilities String and List from the userReport
        String capString = userReport.get("macdetail", "capabilities", String.class);
        ArrayList capSet = userReport.get("macdetail", "capabilityList", ArrayList.class);
        
        // The context report maintains a mapping of unique capabilities set -> a map of counts for admins / disabled users  and regular users
        HashMap<String, Long> counts = contextReport.get("macdetail", capString, HashMap.class);
        if (counts == null) {
            counts = new HashMap<String, Long>();
        }
        // Depending on the users type, we have to increase the accompanying count
        if (userReport.get("macdetail", "mailadmin", Boolean.class)) {
            incCount(counts, "admin");
        } else if (userReport.get("macdetail", "disabled", Boolean.class)) {
            incCount(counts, "disabled");
        }
        // Always increase the total count
        incCount(counts, "total");
        
        // For the given set of capabilities, remember the counts and a plain old array list of capabilities
        contextReport.set("macdetail", capString, counts);
        contextReport.set("macdetail-lists", capString, capSet);
    }

    private void incCount(HashMap<String, Long> counts, String count) {
        Long value = counts.get(count);
        if (value == null) {
            value = Long.valueOf(0);
        }
        counts.put(count, value + 1);
    }
    
    // The system report contains an overall count of unique capability and quota combinations 
    // So the numbers from the context report have to be added to the numbers already in the report
    @Override
    public void merge(ContextReport contextReport, Report report) {
        // Retrieve the quota
        long quota = contextReport.get("macdetail-quota", "quota", 0l, Long.class);
        
        // Retrieve all capabilities combinations
        Map<String, Object> macdetail = contextReport.getNamespace("macdetail");
        
        String quotaSpec = "fileQuota[" + quota + "]";
                
        for(Map.Entry<String, Object> entry: macdetail.entrySet()) {
            // The report contains a count of unique capablities + quotas, so our identifier is the
            // alphabetically sorted and comma separated String of capabilities combined with a quota specification
            String capSpec = entry.getKey() + "," + quotaSpec;
            HashMap<String, Object> counts = (HashMap) entry.getValue();
            
            // Retrieve or create (if this is the first merge) the total counts for the system thusfar
            HashMap<String, Object> savedCounts = report.get("macdetail", capSpec, HashMap.class);
            if (savedCounts == null) {
                savedCounts = new HashMap<String, Object>();
                savedCounts.put("admin", 0l);
                savedCounts.put("disabled", 0l);
                savedCounts.put("total", 0l);
                savedCounts.put("capabilities", (ArrayList) contextReport.get("macdetail-lists", entry.getKey(), ArrayList.class)); 
                savedCounts.put("quota", quota);
            }
            // And add our counts to it
            add(savedCounts, counts);
            
            // Save it back to the report
            report.set("macdetail", capSpec, savedCounts);
        }
    }
    
    // A little cleanup. We don't need the unwieldly mapping of capability String + quota to counts anymore.
    @Override
    public void finish(Report report) {
        Map<String, Object> macdetail = report.getNamespace("macdetail");
        
        ArrayList values = new ArrayList(macdetail.values());
        
        report.clearNamespace("macdetail");
        
        report.set("macdetail", "capabilitySets", values);
    }


    private void add(HashMap<String, Object> savedCounts, HashMap<String, Object> counts) {
        for(Map.Entry<String, Object> entry: counts.entrySet()) {
            if (entry.getValue() instanceof Long) {
                Long value = (Long) savedCounts.get(entry.getKey());
                if (value == null) {
                    value = Long.valueOf(0);
                }
                savedCounts.put(entry.getKey(), value + (Long) entry.getValue());                
            }
        }
    }    




}
