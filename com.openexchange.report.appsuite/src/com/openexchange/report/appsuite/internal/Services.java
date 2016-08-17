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

package com.openexchange.report.appsuite.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.report.appsuite.ContextReportCumulator;
import com.openexchange.report.appsuite.ReportContextHandler;
import com.openexchange.report.appsuite.ReportFinishingTouches;
import com.openexchange.report.appsuite.ReportSystemHandler;
import com.openexchange.report.appsuite.ReportUserHandler;
import com.openexchange.report.appsuite.UserReportCumulator;
import com.openexchange.server.ServiceLookup;


/**
 * The {@link Services} class manages services as discovered in the OSGi system. Only used internally by the Report Framework.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Services {
    
    public static AtomicReference<ServiceLookup> services = new AtomicReference<ServiceLookup>();

    
    private static final CopyOnWriteArrayList<ContextReportCumulator> contextReportCumulators = new CopyOnWriteArrayList<ContextReportCumulator>();
    private static final CopyOnWriteArrayList<ReportContextHandler> contextHandlers = new CopyOnWriteArrayList<ReportContextHandler>();
    private static final CopyOnWriteArrayList<ReportUserHandler> userHandlers = new CopyOnWriteArrayList<ReportUserHandler>();
    private static final CopyOnWriteArrayList<UserReportCumulator> userReportCumulators = new CopyOnWriteArrayList<UserReportCumulator>();
    private static final CopyOnWriteArrayList<ReportFinishingTouches> finishingTouches = new CopyOnWriteArrayList<ReportFinishingTouches>();
    private static final CopyOnWriteArrayList<ReportSystemHandler> systemHandlers = new CopyOnWriteArrayList<ReportSystemHandler>();
    
    private Services() {
        
    }
    
    public static void setServices(ServiceLookup lookup) {
        services.set(lookup);
    }
    
    
    public static <T> T getService(Class<T> klass) {
        return services.get().getService(klass);
    }

    public static void add(ContextReportCumulator service) {
        contextReportCumulators.add(service);
    }

    public static void remove(ContextReportCumulator service) {
        contextReportCumulators.remove(service);
    }
    
    public static List<ContextReportCumulator> getContextReportCumulators() {
        return contextReportCumulators;
    }

    public static void add(ReportContextHandler service) {
        contextHandlers.add(service);
    }

    public static void remove(ReportContextHandler service) {
        contextHandlers.remove(service);
    }
    
    public static List<ReportContextHandler> getContextHandlers() {
        return contextHandlers;
    }

    public static void add(ReportUserHandler service) {
        userHandlers.add(service);
    }

    public static void remove(ReportUserHandler service) {
        userHandlers.remove(service);
    }
    
    public static List<ReportUserHandler> getUserHandlers() {
        return userHandlers;
    }

    public static void add(UserReportCumulator service) {
        userReportCumulators.add(service);
    }

    public static void remove(UserReportCumulator service) {
        userReportCumulators.remove(service);
    }
    
    public static List<UserReportCumulator> getUserReportCumulators() {
        return userReportCumulators;
    }
    
    public static void add(ReportSystemHandler handler) {
        systemHandlers.add(handler);
    }
    
    public static void remove(ReportSystemHandler handler) {
        systemHandlers.remove(handler);
    }

    public static List<ReportSystemHandler> getSystemHandlers() {
        return systemHandlers;
    }
    
    public static void add(ReportFinishingTouches handler) {
        finishingTouches.add(handler);
    }
    
    public static void remove(ReportFinishingTouches handler) {
        finishingTouches.remove(handler);
    }
    
    public static List<ReportFinishingTouches> getFinishingTouches() {
        return finishingTouches;
    }
}
