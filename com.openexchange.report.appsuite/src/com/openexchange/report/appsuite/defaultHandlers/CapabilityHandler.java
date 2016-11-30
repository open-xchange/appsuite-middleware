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

package com.openexchange.report.appsuite.defaultHandlers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.report.InfostoreInformationService;
import com.openexchange.report.LoginCounterService;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.ContextReportCumulator;
import com.openexchange.report.appsuite.ReportContextHandler;
import com.openexchange.report.appsuite.ReportFinishingTouches;
import com.openexchange.report.appsuite.ReportService;
import com.openexchange.report.appsuite.ReportUserHandler;
import com.openexchange.report.appsuite.UserReport;
import com.openexchange.report.appsuite.UserReportCumulator;
import com.openexchange.report.appsuite.internal.ReportProperties;
import com.openexchange.report.appsuite.internal.Services;
import com.openexchange.report.appsuite.serialization.Report;
import com.openexchange.report.appsuite.serialization.Report.JsonObjectType;
import com.openexchange.report.appsuite.storage.ChunkingUtilities;
import com.openexchange.server.ServiceExceptionCode;

/**
 * The {@link CapabilityHandler} analyzes a users capabilities and filestore quota. It sums up unique combinations of capabilities and quota and gives counts for
 * the total number of users that have these settings, admins, and deactivated users.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public class CapabilityHandler implements ReportUserHandler, ReportContextHandler, UserReportCumulator, ContextReportCumulator, ReportFinishingTouches {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CapabilityHandler.class);

    @Override
    public boolean appliesTo(String reportType) {
        // This is the cornerstone of the default report
        return reportType.equals("default") || reportType.equals("extended");
    }

    @Override
    public void runContextReport(ContextReport contextReport) {
        // Grab the file store quota from the context and save them in the report
        Context ctx = contextReport.getContext();
        try {
            QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
            if (null == storageService) {
                throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
            }
            QuotaFileStorage userStorage = storageService.getQuotaFileStorage(ctx.getContextId());
            long quota = userStorage.getQuota();
            contextReport.set(Report.MACDETAIL_QUOTA, Report.QUOTA, quota);
        } catch (OXException e) {
            LOG.error("", e);
            Services.getService(ReportService.class).abortContextReport(contextReport.getUUID(), contextReport.getType());
        }
    }

    @Override
    public void runUserReport(UserReport userReport) throws OXException {
        this.createCapabilityInformations(userReport);
        this.addUserInformationToReport(userReport);
    }

    private void createCapabilityInformations(UserReport userReport) throws OXException {
        CapabilitySet userCapabilitySet = getUserCapabilities(userReport.getUser(), userReport.getContext());
        ArrayList<String> userCapabilityIds = createSortedListOfCapabilityIds(userCapabilitySet);
        userReport.set(Report.MACDETAIL, Report.CAPABILITIES, createCommaSeparatedStringOfIds(userCapabilityIds));
        userReport.set(Report.MACDETAIL, Report.CAPABILITY_LIST, userCapabilityIds);
    }

    private CapabilitySet getUserCapabilities(User user, Context context) throws OXException {
        CapabilitySet userCapabilitySet = new CapabilitySet(0);
        if (user.isGuest()) {
            userCapabilitySet = Services.getService(CapabilityService.class).getCapabilities(user.getCreatedBy(), context.getContextId());
        } else {
            userCapabilitySet = Services.getService(CapabilityService.class).getCapabilities(user.getId(), context.getContextId());
        }
        return userCapabilitySet;
    }

    private ArrayList<String> createSortedListOfCapabilityIds(CapabilitySet userCapabilitySet) {
        ArrayList<String> userCapabilityIds = new ArrayList<String>(userCapabilitySet.size());

        for (Capability capability : userCapabilitySet) {
            userCapabilityIds.add(capability.getId().toLowerCase());
        }
        Collections.sort(userCapabilityIds);
        return userCapabilityIds;
    }

    private String createCommaSeparatedStringOfIds(ArrayList<String> userCapabilityIds) {
        StringBuilder capabilityIdsAsString = new StringBuilder();
        for (String capabilityId : userCapabilityIds) {
            capabilityIdsAsString.append(capabilityId).append(",");
        }
        capabilityIdsAsString.setLength(capabilityIdsAsString.length() - 1);
        return capabilityIdsAsString.toString();
    }

    private void addUserInformationToReport(UserReport userReport) throws OXException {
        if (!userReport.getUser().isGuest()) {
            // Determine if the user is disabled
            if (!userReport.getUser().isMailEnabled()) {
                userReport.set(Report.MACDETAIL, Report.DISABLED, Boolean.TRUE);
            } else {
                userReport.set(Report.MACDETAIL, Report.DISABLED, Boolean.FALSE);
            }

            // Determine if the user is the admin user
            if (userReport.getContext().getMailadmin() == userReport.getUser().getId()) {
                userReport.set(Report.MACDETAIL, Report.MAILADMIN, Boolean.TRUE);
            } else {
                userReport.set(Report.MACDETAIL, Report.MAILADMIN, Boolean.FALSE);
            }
            userReport.set(Report.MACDETAIL, Report.USER_LOGINS, getUserLoginsForPastYear(userReport.getContext().getContextId(), userReport.getUser().getId()));
        }
    }

    public HashMap<String, Long> getUserLoginsForPastYear(int contextId, int userId) throws OXException {
        Calendar calender = Calendar.getInstance();
        Date endDate = calender.getTime();
        calender.add(Calendar.YEAR, -1);
        Date startDate = calender.getTime();
        LoginCounterService loginCounterService = Services.getService(LoginCounterService.class);
        return loginCounterService.getLastClientLogIns(userId, contextId, startDate, endDate);
    }

    // In the context report we keep a count of users/disabled users/admins that share the same capabilities
    // So we have to count every unique combination of capabilities
    @Override
    public void merge(UserReport userReport, ContextReport contextReport) {
        if (userReport.getUser().isGuest()) {
            incrementGuestOrLinkCount(userReport, contextReport);
        } else {
            handleInternalUser(userReport, contextReport);
        }
    }

    // The system report contains an overall count of unique capability and quota combinations
    // So the numbers from the context report have to be added to the numbers already in the report
    @Override
    public void merge(ContextReport contextReport, Report report) {
        // Retrieve the quota
        boolean storeCapS = false;
        long quota = contextReport.get(Report.MACDETAIL_QUOTA, Report.QUOTA, 0l, Long.class);
        Collection<Object> reportValues = ((Map<String, Object>) report.getNamespace(Report.MACDETAIL)).values();
        if (reportValues.size() >= ReportProperties.getMaxChunkSize()) {
            storeCapS = true;
        }

        // Retrieve all capabilities combinations
        Map<String, Object> macdetail = contextReport.getNamespace(Report.MACDETAIL);

        String quotaSpec = "fileQuota[" + quota + "]";

        for (Map.Entry<String, Object> entry : macdetail.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(Report.GUESTS) || entry.getKey().equalsIgnoreCase(Report.LINKS)) {// at this moment, do ignore guest entries
                continue;
            }
            // The report contains a count of unique capablities + quotas, so our identifier is the
            // alphabetically sorted and comma separated String of capabilities combined with a quota specification
            String capSpec = entry.getKey() + "," + quotaSpec;
            HashMap<String, Object> counts = (HashMap) entry.getValue();
            counts.put(Report.QUOTA, quota);

            // Retrieve or create (if this is the first merge) the total counts for the system thusfar
            HashMap<String, Object> storedCapSData = report.get(Report.MACDETAIL, capSpec, HashMap.class);
            if (storedCapSData == null) {
                storedCapSData = prepareCapSData(contextReport, entry.getKey(), quota);
            }
            // And add our counts to it
            addNewValuesToExistingValues(storedCapSData, counts);
            // Save it back to the report
            report.set(Report.MACDETAIL, capSpec, storedCapSData);
        }
        //Only for single tenant deployment
        if (report.isSingleDeployment()) {
            addContextAndUserToDeployment(report, contextReport, quotaSpec);
        }

        if (storeCapS) {
            prepareReportForStorageOfContent(report);
            storeAndMergeReportParts(report);
        }
        //What to do with multi-tenant deployment
    }

    private HashMap<String, Object> prepareCapSData(ContextReport contextReport, String capS, long quota) {
        HashMap<String, Object> capSData = new HashMap<>();
        capSData = new HashMap<String, Object>();
        capSData.put(Report.ADMIN, 0l);
        capSData.put(Report.DISABLED, 0l);
        capSData.put(Report.TOTAL, 0l);
        capSData.put(Report.CAPABILITIES, contextReport.get(Report.MACDETAIL_LISTS, capS, ArrayList.class));
        capSData.put(Report.QUOTA, quota);
        capSData.put(Report.GUESTS, 0l);
        capSData.put(Report.LINKS, 0l);
        capSData.put(Report.CONTEXTS, 0l);
        capSData.put(Report.CONTEXT_USERS_MAX, 0l);
        capSData.put(Report.CONTEXT_USERS_MIN, 0l);
        capSData.put(Report.CONTEXT_USERS_AVG, 0l);

        return capSData;
    }

    private void addContextAndUserToDeployment(Report report, ContextReport contextReport, String quotaSpec) {
        // Get all capS of the currentContext
        for (Entry<String, LinkedHashMap<Integer, ArrayList<Integer>>> capS : contextReport.getCapSToContext().entrySet()) {
            // Add all Context/UserIds to this reports capS
            String capSpec = capS.getKey() + "," + quotaSpec;
            LinkedHashMap<Integer, ArrayList<Integer>> capSContextMap = (LinkedHashMap<Integer, ArrayList<Integer>>) report.getTenantMap().get("deployment").get(capSpec);
            // This capS are not available yet
            if (capSContextMap == null) {
                capSContextMap = new LinkedHashMap<Integer, ArrayList<Integer>>();
                report.getTenantMap().get("deployment").put(capSpec, capSContextMap);
            }
            // For each context in this capSMap, add the context/User map
            for (Entry<Integer, ArrayList<Integer>> singleContext : capS.getValue().entrySet()) {
                if (capSContextMap.get(singleContext.getKey()) == null) {
                    capSContextMap.put(singleContext.getKey(), new ArrayList<Integer>());
                }
                capSContextMap.get(singleContext.getKey()).addAll(singleContext.getValue());
            }
        }
    }

    public void prepareReportForStorageOfContent(Report report) {
        report.setNeedsComposition(true);
        Map<String, Object> reportMacdetail = report.getNamespace(Report.MACDETAIL);
        ArrayList values = new ArrayList(reportMacdetail.values());
        report.clearNamespace(Report.MACDETAIL);
        this.sumClientsInSingleMap(values);
        report.set(Report.MACDETAIL, Report.CAPABILITY_SETS, values);
    }

    // A little cleanup. We don't need the unwieldly mapping of capability String + quota to counts anymore.
    @Override
    public void finish(Report report) {
        Map<String, Object> macdetail = report.getNamespace(Report.MACDETAIL);

        ArrayList<Object> values = new ArrayList(macdetail.values());

        if (report.getType().equals("extended")) {
            addDriveMetricsToReport(report);
        }
        // Merge all stored data into report files, if neccessary
        this.sumClientsInSingleMap(values);

        if (report.isNeedsComposition()) {
            storeAndMergeReportParts(report);
            report.composeReportFromStoredParts(Report.CAPABILITY_SETS, JsonObjectType.ARRAY, Report.MACDETAIL, 1);
        }
        report.clearNamespace(Report.MACDETAIL);

        report.set(Report.MACDETAIL, Report.CAPABILITY_SETS, values);
    }

    private void addDriveMetricsToReport(Report report) {
        Map<String, Object> macdetail = report.getNamespace(Report.MACDETAIL);
        for (Entry<String, LinkedHashMap<String, Object>> currentTenant : report.getTenantMap().entrySet()) {
            for (Entry<String, Object> currentCapS : currentTenant.getValue().entrySet()) {
                String compositionCapS = currentCapS.getKey().substring(0, currentCapS.getKey().lastIndexOf(","));
                ArrayList<String> compositionCapSList = null;
                if (report.isNeedsComposition()) {
                    macdetail.put(currentCapS.getKey(), new HashMap<>());
                    compositionCapSList = new ArrayList<>(Arrays.asList(compositionCapS.split(",")));
                }
                addDriveMetricsToCapS((Map<String, Object>) macdetail.get(currentCapS.getKey()), (Map<Integer, List<Integer>>) currentCapS.getValue(), new Date(report.getConsideredTimeframeStart()), new Date(report.getConsideredTimeframeEnd()), report, compositionCapSList);
            }
        }
        // calculate correct drive average values
        this.calculateCorrectDriveAvg(report.get(Report.TOTAL, Report.DRIVE_TOTAL, LinkedHashMap.class));
        report.set(Report.MACDETAIL, Report.CAPABILITY_SETS, new ArrayList(macdetail.values()));
    }

    @Override
    public void storeAndMergeReportParts(Report report) {
        // create storage folder, if not already exists
        File storageFolder = new File(ReportProperties.getStoragePath());
        if (!storageFolder.exists()) {
            if (!storageFolder.mkdir()) {
                LOG.error("Failed to create storage folder");
                return;
            }
        }

        // Get all capability sets
        ArrayList<HashMap<String, Object>> capSets = report.get(Report.MACDETAIL, Report.CAPABILITY_SETS, new ArrayList<>(), ArrayList.class);
        report.clearNamespace(Report.MACDETAIL);
        // serialize each capability set into a single HashMap and merge the data if a file for the
        // given capability set already exists
        for (HashMap<String, Object> singleCapS : capSets) {
            try {
                ChunkingUtilities.storeCapSContentToFiles(report.getUUID(), ReportProperties.getStoragePath(), singleCapS);
            } catch (JSONException e) {
                LOG.error("Error while trying create JSONObject from stored capability-set data. ", e);
            } catch (IOException e) {
                LOG.error("Error while trying to read stored capability-set data. ", e);
            }
        }
    }

    /**
     * Set the user specific data for the given {@link ContextReport}. The capability-set specific values
     * like total, admin... are incremented depending on the given {@link UserReport}.
     *
     * @param userReport
     * @param contextReport
     */
    private void handleInternalUser(UserReport userReport, ContextReport contextReport) {
        // Retrieve the capabilities String and List from the userReport
        String capString = userReport.get(Report.MACDETAIL, Report.CAPABILITIES, String.class);
        ArrayList capSet = userReport.get(Report.MACDETAIL, Report.CAPABILITY_LIST, ArrayList.class);

        // The context report maintains a mapping of unique capabilities set -> a map of counts for admins / disabled users  and regular users
        HashMap<String, Long> counts = contextReport.get(Report.MACDETAIL, capString, HashMap.class);
        if (counts == null) {
            counts = new HashMap<String, Long>();
        }
        // Depending on the users type, we have to increase the accompanying count
        if (userReport.get(Report.MACDETAIL, Report.MAILADMIN, Boolean.class)) {
            incrementCounter(counts, Report.ADMIN);
        } else if (userReport.get(Report.MACDETAIL, Report.DISABLED, Boolean.class)) {
            incrementCounter(counts, Report.DISABLED);
        }

        // Get the users client logins and save them also to this context/capability-set
        HashMap<String, Long> userLogins = userReport.get(Report.MACDETAIL, Report.USER_LOGINS, HashMap.class);
        for (Entry<String, Long> clientName : userLogins.entrySet()) {
            incrementCounter(counts, clientName.getKey());
        }

        incrementCounter(counts, Report.TOTAL);

        contextReport.set(Report.MACDETAIL, capString, userLogins);

        // For the given set of capabilities, remember the counts and a plain old array list of capabilities
        contextReport.set(Report.MACDETAIL, capString, counts);
        contextReport.set(Report.MACDETAIL_LISTS, capString, capSet);
        LinkedHashMap<Integer, ArrayList<Integer>> capSContextMap = contextReport.getCapSToContext().get(capString);
        if (capSContextMap == null) {
            capSContextMap = new LinkedHashMap<Integer, ArrayList<Integer>>();
            capSContextMap.put(contextReport.getContext().getContextId(), new ArrayList<Integer>());
            contextReport.getCapSToContext().put(capString, capSContextMap);
        }
        capSContextMap.get(contextReport.getContext().getContextId()).add(userReport.getUser().getId());
    }

    private void incrementGuestOrLinkCount(UserReport userReport, ContextReport contextReport) {
        String userCapabilities = userReport.get(Report.MACDETAIL, Report.CAPABILITIES, String.class);
        HashMap<String, Long> contextTotals = contextReport.get(Report.MACDETAIL, userCapabilities, new HashMap<String, Long>(), HashMap.class);
        if (userReport.getUser().getMail().isEmpty()) {
            incrementCounter(contextTotals, Report.LINKS);
        } else {
            incrementCounter(contextTotals, Report.GUESTS);
        }
        contextReport.set(Report.MACDETAIL, userCapabilities, contextTotals);
    }

    private void incrementCounter(HashMap<String, Long> counterMap, String keyOfValueToIncrement) {
        Long value = counterMap.get(keyOfValueToIncrement);
        if (value == null) {
            value = Long.valueOf(0);
        }
        counterMap.put(keyOfValueToIncrement, value + 1);
    }

    /**
     * Calculate drive specific average-metrics and clean up the given map form unneeded parameters.
     *
     * @param driveTotalMap, the map with all relevant drive metrics.
     */
    private void calculateCorrectDriveAvg(LinkedHashMap<String, Long> driveTotalMap) {
        Long totalDriveUsers = driveTotalMap.get("users");
        // No Drive users, nothing to do here
        if (totalDriveUsers != null && totalDriveUsers != 0) {
            if (driveTotalMap.get("file-size-total") != null && driveTotalMap.get("file-count-overall-total") != null && driveTotalMap.get("file-count-overall-total") != 0) {
                driveTotalMap.put("file-size-avg", driveTotalMap.get("file-size-total") / driveTotalMap.get("file-count-overall-total"));
            }
            if (driveTotalMap.get("storage-use-total") != null) {
                driveTotalMap.put("storage-use-avg", driveTotalMap.get("storage-use-total") / totalDriveUsers);
            }
            if (driveTotalMap.get("file-count-overall-total") != null) {
                driveTotalMap.put("file-count-overall-avg", driveTotalMap.get("file-count-overall-total") / totalDriveUsers);
            }
            if (driveTotalMap.get("file-count-in-timerange-total") != null) {
                driveTotalMap.put("file-count-in-timerange-avg", driveTotalMap.get("file-count-in-timerange-total") / totalDriveUsers);
            }
            if (driveTotalMap.get("quota-usage-percent-sum") != null && driveTotalMap.get("quota-usage-percent-total") != null && driveTotalMap.get("quota-usage-percent-total") != 0) {
                driveTotalMap.put("quota-usage-percent-avg", driveTotalMap.get("quota-usage-percent-sum") / driveTotalMap.get("quota-usage-percent-total"));
            }
            driveTotalMap.remove("quota-usage-percent-total");
            driveTotalMap.remove("quota-usage-percent-sum");
        }

        if (driveTotalMap.get("external-storages-users") != null && driveTotalMap.get("external-storages-users") != 0) {
            driveTotalMap.put("external-storages-avg", driveTotalMap.get("external-storages-total") / driveTotalMap.get("external-storages-users"));
        }
    }

    /**
     * Get all drive metrics from db for the given usersInContext map. The result is saved into the given
     * capSMap. All new total values on report level are then recalculated and saved into the given
     * report.
     *
     * @param capSMap, the capability-set key/value pairs
     * @param usersInContext, all relevant contexts and users for this capability-set
     * @param consideredTimeframeStart, beginning of potential timeframe for calculating file count
     * @param consideredTimeframeEnd, end of potential timeframe for calculating file count
     * @param report, the report with all values
     */
    private void addDriveMetricsToCapS(Map<String, Object> capSMap, Map<Integer, List<Integer>> usersInContext, Date consideredTimeframeStart, Date consideredTimeframeEnd, Report report, List<String> compositionCapS) {
        InfostoreInformationService informationService = Services.getService(InfostoreInformationService.class);
        LinkedHashMap<String, Integer> driveUserMetrics = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> driveMetrics = new LinkedHashMap<>();

        try {
            for (Entry<String, Integer> fileSizes : informationService.getFileSizeMetrics(usersInContext).entrySet()) {
                driveUserMetrics.put("file-size-" + fileSizes.getKey(), fileSizes.getValue());
            }
            for (Entry<String, Integer> mimeTypes : informationService.getFileCountMimetypeMetrics(usersInContext).entrySet()) {
                driveMetrics.put("mime-type-" + mimeTypes.getKey(), mimeTypes.getValue());
            }
            for (Entry<String, Integer> storageUse : informationService.getStorageUseMetrics(usersInContext).entrySet()) {
                driveUserMetrics.put("storage-use-" + storageUse.getKey(), storageUse.getValue());
            }
            for (Entry<String, Integer> fileCount : informationService.getFileCountMetrics(usersInContext).entrySet()) {
                driveUserMetrics.put("file-count-overall-" + fileCount.getKey(), fileCount.getValue());
            }
            for (Entry<String, Integer> fileCountTimeRange : informationService.getFileCountInTimeframeMetrics(usersInContext, consideredTimeframeStart, consideredTimeframeEnd).entrySet()) {
                driveUserMetrics.put("file-count-in-timerange-" + fileCountTimeRange.getKey(), fileCountTimeRange.getValue());
            }
            for (Entry<String, Integer> fileExternalSorages : informationService.getExternalStorageMetrics(usersInContext).entrySet()) {
                driveUserMetrics.put("external-storages-" + fileExternalSorages.getKey(), fileExternalSorages.getValue());
            }
            for (Entry<String, Integer> fileCount : informationService.getFileCountNoVersions(usersInContext).entrySet()) {
                driveUserMetrics.put("distinct-files-" + fileCount.getKey(), fileCount.getValue());
            }
            for (Entry<String, Integer> quotaUsage : informationService.getQuotaUsageMetrics(usersInContext).entrySet()) {
                driveUserMetrics.put("quota-usage-percent-" + quotaUsage.getKey(), quotaUsage.getValue());
            }
        } catch (final SQLException e) {
            LOG.error("Unable to execute SQL for drive metric gathering.", e);
        } catch (OXException e) {
            LOG.error("Unable to gather drive metrics.", e);
        } finally {
            informationService.closeAllDBConnections();
        }
        driveUserMetrics.put("users", driveUserMetrics.get("file-count-overall-users") == null ? 0 : driveUserMetrics.get("file-count-overall-users"));
        driveUserMetrics.remove("file-count-overall-users");
        capSMap.put(Report.DRIVE_USER, driveUserMetrics);
        capSMap.put(Report.DRIVE_OVERALL, driveMetrics);

        LinkedHashMap<String, Long> totalDrive = report.get(Report.TOTAL, Report.DRIVE_TOTAL, LinkedHashMap.class);
        if (totalDrive == null) {
            totalDrive = new LinkedHashMap<>();
        }
        for (Entry<String, Integer> entry : driveUserMetrics.entrySet()) {
            Long value = totalDrive.get(entry.getKey());
            Long newValue = entry.getValue() == null ? 0l : entry.getValue().longValue();
            if (value == null) {
                totalDrive.put(entry.getKey(), newValue);
            } else {
                if (entry.getKey().contains("min") && newValue < value) {
                    totalDrive.put(entry.getKey(), newValue);
                } else if (entry.getKey().contains("max") && newValue > value) {
                    totalDrive.put(entry.getKey(), newValue);
                } else if (entry.getKey().contains("total") || entry.getKey().contains("sum") || entry.getKey().contains("users")) {
                    totalDrive.put(entry.getKey(), totalDrive.get(entry.getKey()) + newValue);
                }
            }

        }
        // clean up, this metrics are only needed for the total part of the report
        driveUserMetrics.remove("quota-usage-percent-total");
        driveUserMetrics.remove("quota-usage-percent-sum");
        // place all calculated metrics into the total report
        for (Entry<String, Integer> entry : driveMetrics.entrySet()) {
            Long value = totalDrive.get(entry.getKey());
            if (value == null) {
                totalDrive.put(entry.getKey(), entry.getValue().longValue());
            } else {
                totalDrive.put(entry.getKey(), totalDrive.get(entry.getKey()) + entry.getValue());
            }
        }
        report.set(Report.TOTAL, Report.DRIVE_TOTAL, totalDrive);
        // Merge drive metrics into files and remove the data from the report, if neccessary
        if (report.isNeedsComposition()) {
            capSMap.put(Report.CAPABILITIES, compositionCapS);
            report.get(Report.MACDETAIL, Report.CAPABILITY_SETS, new ArrayList<>(), ArrayList.class).add(capSMap);
            storeAndMergeReportParts(report);
        }
    }

    private void addNewValuesToExistingValues(Map<String, Object> existingValues, Map<String, Object> newValues) {
        existingValues.put(Report.CONTEXTS, Long.parseLong(String.valueOf(existingValues.get(Report.CONTEXTS))) + 1L);
        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            if (entry.getValue() instanceof Long) {
                Long value = (Long) existingValues.get(entry.getKey());
                if (value == null) {
                    value = Long.valueOf(0);
                }
                existingValues.put(entry.getKey(), value + (Long) entry.getValue());
                if (entry.getKey().equals(Report.TOTAL)) {
                    Long newValue = (Long) entry.getValue();
                    if (newValue > (Long) existingValues.get(Report.CONTEXT_USERS_MAX)) {
                        existingValues.put(Report.CONTEXT_USERS_MAX, newValue);
                    }
                    if (newValue < (Long) existingValues.get(Report.CONTEXT_USERS_MIN) || (Long) existingValues.get(Report.CONTEXT_USERS_MIN) == 0l) {
                        existingValues.put(Report.CONTEXT_USERS_MIN, newValue);
                    }
                    existingValues.put(Report.CONTEXT_USERS_AVG, (Long) existingValues.get(Report.TOTAL) / (Long) existingValues.get(Report.CONTEXTS));
                }
            }
        }
    }

    /**
     * Sum all clients of the given attribute list in one single Map and remove them from from the given {@link ArrayList} afterwards.
     * A client is identified by the preceding string "client:". In the new Map, this preceding string is removed and the rest
     * represents the key. The value is the amount. The result is added to the given ArrayList.
     *
     * @param capSValueMap - A list of {@link HashMap}<String, Object>s with the counted values of a capability-set
     */
    private void sumClientsInSingleMap(List<Object> capSValueMap) {
        for (Object valueMap : capSValueMap) {
            Map<String, Object> capSMap = (HashMap<String, Object>) valueMap;
            capSMap.put(Report.CLIENT_LOGINS, extractClientMapFromCapSContent(capSMap));
        }
    }

    private Map<String, Object> extractClientMapFromCapSContent(Map<String, Object> capSMap) {
        Map<String, Object> clients = new HashMap<>();
        for (Iterator<Map.Entry<String, Object>> it = capSMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            if (entry.getKey().contains("client:")) {
                clients.put(entry.getKey().replace("client:", ""), entry.getValue());
                it.remove();
            }
        }
        return clients;
    }
}
