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

import java.util.HashMap;
import java.util.Map;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.ContextReportCumulator;
import com.openexchange.report.appsuite.Report;


/**
 * The {@link Total} cumulator sums up the number of contexts and users. It is based on the results of the {@link CapabilityHandler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Total implements ContextReportCumulator{

    @Override
    public boolean appliesTo(String reportType) {
        return "default".equals(reportType);
    }
    
    @Override
    public void merge(ContextReport contextReport, Report report) {
        // Count up one for each context
        long contexts = report.get("total", "contexts", 0l, Long.class);
        report.set("total", "contexts", contexts + 1);
        
        // Sum up the totals of the capabilities combinations from the CapabilityHandler
        long users = report.get("total", "users", 0l, Long.class);
        
        Map<String, Object> macdetail = contextReport.getNamespace("macdetail");
        
        for(Map.Entry<String, Object> entry: macdetail.entrySet()) {
            HashMap<String, Long> counts = (HashMap) entry.getValue();
            
            if (counts != null && counts.containsKey("total")) {
                users += (Long) counts.get("total");
            }
        }
        
        report.set("total", "users", users);
        
        report.set("total", "report-format", "appsuite-short");
    }
        

}
