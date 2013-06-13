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

package com.openexchange.report.appsuite.jobs;

import java.io.Serializable;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.log.LogFactory;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.ReportContextHandler;
import com.openexchange.report.appsuite.ReportUserHandler;
import com.openexchange.report.appsuite.Services;
import com.openexchange.report.appsuite.UserReport;
import com.openexchange.report.appsuite.UserReportCumulator;
import com.openexchange.user.UserService;


/**
 * The {@link AnalyzeContextBatch} class is the workhorse of the reporting system. It runs the reports on a batch of
 * context ids and is distributed cluster-wide via hazelcasts executor service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AnalyzeContextBatch implements Runnable, Serializable {
    
    private static final long serialVersionUID = -578253218760102061L;

    private static final Log LOG = LogFactory.getLog(AnalyzeContextBatch.class);
    
    private String uuid;
    private String reportType;
    private List<Integer> contextIds;

    /**
     *       
     * Initializes a new {@link AnalyzeContextBatch}.
     * @param uuid The uuid of the report we're running
     * @param reportType The type of report that is being run
     * @param chunk a list of context IDs to analyze
     */
    public AnalyzeContextBatch(String uuid, String reportType, List<Integer> chunk) {
        super();
        this.uuid = uuid;
        this.reportType = reportType;
        this.contextIds = chunk;
    }

    @Override
    public void run() {
        try {
            
            if (reportType == null) {
                reportType = "default";
            }
            
            for(Integer ctxId: contextIds) {
                try {
                    // First let's have a look at the context
                    Context ctx = loadContext(ctxId);

                    ContextReport contextReport = new ContextReport(uuid, reportType, ctx);
                    
                    // Run all Context Analyzers that apply to this reportType
                    for(ReportContextHandler contextHandler: Services.getContextHandlers()) {
                        if(contextHandler.appliesTo(reportType)) {
                            contextHandler.runContextReport(contextReport);
                        }
                    }
                    
                    // Next, let's look at all the users in this context
                    for (User user: loadUsers(ctx)) {
                        UserReport userReport = new UserReport(uuid, reportType, ctx, user, contextReport);
                        // Run User Analyzers
                        for(ReportUserHandler userHandler: Services.getUserHandlers()) {
                            if (userHandler.appliesTo(reportType)) {
                                userHandler.runUserReport(userReport);
                            }
                        }
                        
                        // Compact User Analysis and add to context report
                        for (UserReportCumulator cumulator: Services.getUserReportCumulators()) {
                            if (cumulator.appliesTo(reportType)) {
                                cumulator.merge(userReport, contextReport);                    
                            }
                        }
                    }
                    
                    // Add context to general report and mark context as done
                    Orchestration.getInstance().done(contextReport);
                } catch (Throwable t) {
                    Orchestration.getInstance().abort(uuid, reportType, ctxId);
                    LOG.error(t.getMessage(), t);
                }

            }
            

        } catch (Throwable t) {
            // Shouldn't happen
            for (Integer ctxId: contextIds) {
                Orchestration.getInstance().abort(uuid, reportType, ctxId);
            }
            LOG.error(t.getMessage(), t);
        }
        
    }

    private User[] loadUsers(Context ctx) throws OXException {
        return Services.getService(UserService.class).getUser(ctx);
    }

    private Context loadContext(int contextId) throws OXException {
        return Services.getService(ContextService.class).getContext(contextId);
    }

}
