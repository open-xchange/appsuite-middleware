/**
 * This bundle contains the framework and core implementation of the Appsuite Report system. A report collects and cumulates information about the users and contexts in a system.
 * It does so by investigating each context and user in turn, distributed via Hazelcast in the entire cluster. Depending on what you're looking for, here some pointers for further reading:
 * <p>
 * <h4> How a report is created </h4>
 * <p>
 *  In order to create a report, every context and every user in the system is examined.
 *
 *  First, a {@link com.openexchange.report.appsuite.ContextReport} is created and implementations of {@link com.openexchange.report.appsuite.ReportContextHandler}s are asked to
 *  contribute information about the context. Next, for every User in the context a {@link com.openexchange.report.appsuite.UserReport} is created and implementations of {@link com.openexchange.report.appsuite.ReportUserHandler}
 *  are asked for contributions. After that, the UserReport is merged with the ContextReport by way of {@link com.openexchange.report.appsuite.UserReportCumulator}s. A ContextReport is added
 *  to the overall system {@link com.openexchange.report.appsuite.Report} with the help of the {@link com.openexchange.report.appsuite.ContextReportCumulator}. Lastly
 *  overall information about the system can be added by {@link com.openexchange.report.appsuite.ReportSystemHandler}s and cumulated in the {@link com.openexchange.report.appsuite.ReportFinishingTouches}.
 *  Consider as an example the {@link com.openexchange.report.appsuite.defaultHandlers.CapabilityHandler}, which plays a role in most of these steps:
 *
 *  <ol>
 *  <li> As the ReportContextHandler, it determines the filestore quota of a context and stores it in the context report
 *  <li> As the ReportUserHandler, it looks up all capabilities of a user and stores them in the user context, along with marking a user as being disabled or an admin.
 *  <li> As the UserReportCumulator, it takes the UserReport and manages a count of all unique capablity/quota combinations for regular users, disabled users and admins in one context
 *  <li> As the ContextReportCumulator, it sums up these individual counts for contexts for the entire system
 *  <li> As the ReportFinishingTouches, it reformats these sums suitable for the report system
 *  </ol>
 *
 *  These tasks of analyzing a context and its users are performed by the entire cluster in groups of 200 contexts per task. See the class {@link com.openexchange.report.appsuite.internal.HazelcastReportService}
 *  for how this work is coordinated. The Orchestration class is the implementation for the ReportService and the basis for the ReportMXBean.
 *
 * <p>
 * <h4> How to trigger a report</h4>
 * <p>
 *
 * Reports can be triggered via RMI on the com.openexchange.reporting.appsuite.AppsuiteReporting MBean ( see {@link com.openexchange.report.appsuite.management.ReportMXBean} or via the report client commandline tool. If you want to trigger reports
 * programmatically from within OSGi, have a look at the {@link com.openexchange.report.appsuite.ReportService}. A report always comes with its own UUID, uniquely identifying a
 * report run.
 *
 * <p>
 * <h4> How to build a custom report </h4>
 * <p>
 *
 * To build a custom report, implement one or more of the analysis interfaces ( {@link com.openexchange.report.appsuite.ReportContextHandler}, {@link com.openexchange.report.appsuite.ReportUserHandler}, {@link com.openexchange.report.appsuite.ReportSystemHandler})
 * or the cumulation interfaces ( {@link com.openexchange.report.appsuite.UserReportCumulator}, {@link com.openexchange.report.appsuite.ContextReportCumulator}, {@link com.openexchange.report.appsuite.ReportFinishingTouches} )
 * and export them via the OSGi system. Note that even if a single class implements more than one of these service interfaces, it must be registered in OSGi once for each of these interfaces.
 * The method #appliesTo is used to determine whether an analysis or cumulator applies to a given report run. If a report run is triggered with the reportType (a String) as an argument
 * that reportType is passed along to #appliesTo so an implementation can decide whether it wants to participate in a given report run. Have a look at {@link com.openexchange.report.appsuite.defaultHandlers.CapabilityHandler}, {@link com.openexchange.report.appsuite.defaultHandlers.ClientLoginCount} and {@link com.openexchange.report.appsuite.defaultHandlers.Total}
 * for implementations for the default report. These classes are exported, so can be used as superclasses for custom reports
 *
 */
package com.openexchange.report.appsuite;

