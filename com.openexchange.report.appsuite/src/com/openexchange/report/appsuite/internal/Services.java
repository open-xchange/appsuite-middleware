/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
