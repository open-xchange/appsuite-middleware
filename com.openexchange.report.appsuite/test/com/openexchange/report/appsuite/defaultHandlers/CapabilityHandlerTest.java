
package com.openexchange.report.appsuite.defaultHandlers;

import static org.junit.Assert.assertEquals;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.report.InfostoreInformationService;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.UserReport;
import com.openexchange.report.appsuite.internal.Services;
import com.openexchange.report.appsuite.serialization.Report;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Services.class)
public class CapabilityHandlerTest {

    private ContextReport contextReport;
    private Report report;
    private CapabilityHandler capabilityHandlerTest = new CapabilityHandler();

    private final String CAPS1 = "active_sync, autologin, boxcom, caldav, calendar, carddav, client-onboarding, collect_email_addresses, conflict_handling, contacts, delegate_tasks";
    private final String CAPS2 = "active_sync, autologin, boxcom, caldav, calendar, carddav, client-onboarding, collect_email_addresses, conflict_handling, contacts";
    private final String CAPS3 = "active_sync, autologin, boxcom, caldav, calendar, carddav, client-onboarding, collect_email_addresses";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.contextReport = initContextReport(2);
        this.initReport("default");
    }
    //-------------------Tests-------------------

    @SuppressWarnings("unchecked")
    @Test
    public void testAddContextToEmptyReport() {
        capabilityHandlerTest.merge(contextReport, report);
        long quota = contextReport.get(Report.MACDETAIL_QUOTA, Report.QUOTA, 0l, Long.class);
        String quotaSpec = "fileQuota[" + quota + "]";
        HashMap<String, Object> capS1 = report.get(Report.MACDETAIL, CAPS1 + "," + quotaSpec, HashMap.class);
        HashMap<String, Object> capS2 = report.get(Report.MACDETAIL, CAPS2 + "," + quotaSpec, HashMap.class);
        HashMap<String, Object> capS3 = report.get(Report.MACDETAIL, CAPS3 + "," + quotaSpec, HashMap.class);
        validateCapSCount(capS1, 1l, 0l, 1l, 0l, 0l, 1l, 1l, 1l, 1l);
        validateCapSCount(capS2, 0l, 2l, 5l, 3l, 0l, 5l, 5l, 5l, 1l);
        validateCapSCount(capS3, 0l, 0l, 2l, 0l, 8l, 2l, 2l, 2l, 1l);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddContextToReportCapSValues() {
        capabilityHandlerTest.merge(initContextReport(4), report);
        capabilityHandlerTest.merge(initContextReport(5), report);
        String quotaSpec = "fileQuota[0]";
        HashMap<String, Object> capS1 = report.get(Report.MACDETAIL, CAPS1 + "," + quotaSpec, HashMap.class);
        HashMap<String, Object> capS2 = report.get(Report.MACDETAIL, CAPS2 + "," + quotaSpec, HashMap.class);
        HashMap<String, Object> capS3 = report.get(Report.MACDETAIL, CAPS3 + "," + quotaSpec, HashMap.class);
        validateCapSCount(capS1, 2l, 0l, 2l, 0l, 0l, 1l, 1l, 1l, 2l);
        validateCapSCount(capS2, 0l, 4l, 10l, 6l, 0l, 5l, 5l, 5l, 2l);
        validateCapSCount(capS3, 0l, 0l, 4l, 0l, 16l, 2l, 2l, 2l, 2l);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddContextToReportTenantMapValues() {
        capabilityHandlerTest.merge(initContextReport(4), report);
        capabilityHandlerTest.merge(initContextReport(5), report);
        // all thre capS have their entry inside the tenant map on deployment level (index 0)
        assertEquals(3, report.getTenantMap().get("deployment").size());
        // Second capS has a context with id 4 and inside is a user with id 5
        assertEquals(true, ((LinkedHashMap<Integer, ArrayList<Integer>>) report.getTenantMap().get("deployment").get(CAPS2 + ",fileQuota[0]")).get(4).contains(5));
        // Second capS has two contexts total
        assertEquals(2, ((LinkedHashMap<Integer, ArrayList<Integer>>) report.getTenantMap().get("deployment").get(CAPS2 + ",fileQuota[0]")).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddAdminToContextReport() {
        UserReport userReport = initUserReport(7, false, true, false, contextReport, CAPS1);
        capabilityHandlerTest.merge(userReport, contextReport);
        HashMap<String, Object> capS1 = contextReport.get(Report.MACDETAIL, CAPS1, HashMap.class);
        // the number of admin users has increased from 1 to 2 and total from 1 to 2
        validateCapSCount(capS1, 2l, null, 2l, null, null, null, null, null, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddDisabledUserToContextReport() {
        UserReport userReport = initUserReport(7, false, false, true, contextReport, CAPS2);
        capabilityHandlerTest.merge(userReport, contextReport);
        HashMap<String, Object> capS2 = contextReport.get(Report.MACDETAIL, CAPS2, HashMap.class);
        // the number of disabled users has increased from 2 to 3 and total from 5 to 6
        validateCapSCount(capS2, null, 3l, 6l, 3l, null, null, null, null, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddGuestUserToContextReport() {
        UserReport userReport = initUserReport(7, true, false, false, contextReport, CAPS3);
        capabilityHandlerTest.merge(userReport, contextReport);
        HashMap<String, Object> capS3 = contextReport.get(Report.MACDETAIL, CAPS3, HashMap.class);
        // The number of guests has increased from 0 to 1
        validateCapSCount(capS3, null, null, 2l, 1l, 8l, null, null, null, null);
    }

    @Test
    public void testAddUserWithLoginsToContextReport() {
        UserReport userReport = initUserReport(7, false, false, false, contextReport, CAPS2);
        capabilityHandlerTest.merge(userReport, contextReport);
        // A logins ArrayList exists and has a size of 2
        assertEquals(2, userReport.get(Report.MACDETAIL, Report.USER_LOGINS, HashMap.class).size());
    }

    @Test
    public void testCalculatedDriveMetricsSingleCapS() {
        // Mock all potential return values for Drive calculations
        InfostoreInformationService informationService = new InfostoreInformationService() {

            @Override
            public Map<String, Integer> getStorageUseMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                return createPotentialInfostoreReturn(10, 20, 15, 30, null, 0);
            }

            @Override
            public Map<String, Integer> getQuotaUsageMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                return createPotentialInfostoreReturn(1, 1, 1, 2, "sum", 2);
            }

            @Override
            public Map<String, Integer> getFileSizeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                return createPotentialInfostoreReturn(10, 20, 15, 30, null, 0);
            }

            @Override
            public Map<String, Integer> getFileCountNoVersions(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                HashMap<String, Integer> returnMap = new HashMap<>();
                returnMap.put("total", 2);
                return returnMap;
            }

            @Override
            public Map<String, Integer> getFileCountMimetypeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                return new HashMap<>();
            }

            @Override
            public Map<String, Integer> getFileCountMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                return createPotentialInfostoreReturn(1, 1, 1, 2, "users", 2);
            }

            @Override
            public Map<String, Integer> getFileCountInTimeframeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext, Date start, Date end) throws SQLException, OXException {
                return createPotentialInfostoreReturn(1, 1, 1, 2, null, 0);
            }

            @Override
            public Map<String, Integer> getExternalStorageMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                return createPotentialInfostoreReturn(1, 1, 1, 1, null, 0);
            }

            @Override
            public void closeAllDBConnections() {}

        };

        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(InfostoreInformationService.class)).thenReturn(informationService);
        this.initReport("extended");
        initTenantMapForReport();
        addFirstCapSToReport();
        capabilityHandlerTest.finish(report);
        validateDriveTotalAvgs(report.get(Report.TOTAL, Report.DRIVE_TOTAL, LinkedHashMap.class), 15l, 15l, 1l, 1l, 1l, 1l);
    }

    @Test
    public void testCalculatedDriveMetricsTwoCapS() {
        // Mock all potential return values for Drive calculations, for each context different values
        InfostoreInformationService informationService = new InfostoreInformationService() {

            @Override
            public Map<String, Integer> getStorageUseMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                if (usersInContext.containsKey(20)) {
                    return createPotentialInfostoreReturn(30, 60, 45, 135, null, 0);
                }
                return createPotentialInfostoreReturn(10, 20, 15, 30, null, 0);
            }

            @Override
            public Map<String, Integer> getQuotaUsageMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                if (usersInContext.containsKey(20)) {
                    return createPotentialInfostoreReturn(5, 20, 15, 3, "sum", 35);
                }
                return createPotentialInfostoreReturn(1, 1, 1, 1, "sum", 1);
            }

            @Override
            public Map<String, Integer> getFileSizeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                if (usersInContext.containsKey(20)) {
                    return createPotentialInfostoreReturn(30, 60, 45, 135, null, 0);
                }
                return createPotentialInfostoreReturn(10, 20, 15, 30, null, 0);
            }

            @Override
            public Map<String, Integer> getFileCountNoVersions(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                HashMap<String, Integer> returnMap = new HashMap<>();
                if (usersInContext.containsKey(20)) {
                    returnMap.put("total", 3);
                    return returnMap;
                }
                returnMap.put("total", 2);
                return returnMap;
            }

            @Override
            public Map<String, Integer> getFileCountMimetypeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                return new HashMap<>();
            }

            @Override
            public Map<String, Integer> getFileCountMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                if (usersInContext.containsKey(20)) {
                    return createPotentialInfostoreReturn(1, 1, 1, 3, "users", 3);
                }
                return createPotentialInfostoreReturn(1, 1, 1, 2, "users", 2);
            }

            @Override
            public Map<String, Integer> getFileCountInTimeframeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext, Date start, Date end) throws SQLException, OXException {
                if (usersInContext.containsKey(20)) {
                    return createPotentialInfostoreReturn(1, 1, 1, 3, null, 0);
                }
                return createPotentialInfostoreReturn(1, 1, 1, 2, null, 0);
            }

            @Override
            public Map<String, Integer> getExternalStorageMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
                if (usersInContext.containsKey(20)) {
                    return createPotentialInfostoreReturn(0, 0, 0, 0, null, 0);
                }
                return createPotentialInfostoreReturn(1, 1, 1, 1, null, 0);
            }

            @Override
            public void closeAllDBConnections() {}

        };

        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(InfostoreInformationService.class)).thenReturn(informationService);
        this.initReport("extended");
        initTenantMapForReport();
        addFirstCapSToReport();
        addSecondCapSToReport();
        capabilityHandlerTest.finish(report);
        validateDriveTotalAvgs(report.get(Report.TOTAL, Report.DRIVE_TOTAL, LinkedHashMap.class), 33l, 33l, 1l, 1l, 9l, 1l);
    }

    //-------------------Validation-------------------
    private void validateCapSCount(HashMap<String, Object> valueMap, Long admin, Long disabled, Long total, Long guests, Long links, Long contextMin, Long contextMax, Long contextAvg, Long contexts) {
        assertEquals(admin, valueMap.get(Report.ADMIN));
        assertEquals(disabled, valueMap.get(Report.DISABLED));
        assertEquals(total, valueMap.get(Report.TOTAL));
        assertEquals(guests, valueMap.get(Report.GUESTS));
        assertEquals(links, valueMap.get(Report.LINKS));
        assertEquals(contexts, valueMap.get(Report.CONTEXTS));
        assertEquals(contextMin, valueMap.get(Report.CONTEXT_USERS_MIN));
        assertEquals(contextMax, valueMap.get(Report.CONTEXT_USERS_MAX));
        assertEquals(contextAvg, valueMap.get(Report.CONTEXT_USERS_AVG));
    }

    private void validateDriveTotalAvgs(HashMap<String, Long> valueMap, Long fileSize, Long storageUse, Long fileCountOverall, Long fileCountTimerange, Long quotaUsage, Long exernalStorages) {
        assertEquals(fileSize, valueMap.get("file-size-avg"));
        assertEquals(storageUse, valueMap.get("storage-use-avg"));
        assertEquals(fileCountOverall, valueMap.get("file-count-overall-avg"));
        assertEquals(fileCountTimerange, valueMap.get("file-count-in-timerange-avg"));
        assertEquals(quotaUsage, valueMap.get("quota-usage-percent-avg"));
        assertEquals(exernalStorages, valueMap.get("external-storages-avg"));
    }

    //-------------------Helpers-------------------
    private ContextReport initContextReport(Integer contextId) {

        ContextImpl ctx = new ContextImpl(contextId);
        ctx.setEnabled(false);
        ContextReport contextReport = new ContextReport(UUID.randomUUID().toString(), "default", ctx);
        contextReport.set(ContextReport.MACDETAIL_LISTS, CAPS1, CAPS1);
        contextReport.set(ContextReport.MACDETAIL_LISTS, CAPS2, CAPS2);
        contextReport.set(ContextReport.MACDETAIL_LISTS, CAPS3, CAPS3);
        Map<String, Object> macdetail = contextReport.getNamespace(Report.MACDETAIL);
        macdetail.put(CAPS1, new HashMap<String, Long>());
        macdetail.put(CAPS2, new HashMap<String, Long>());
        macdetail.put(CAPS3, new HashMap<String, Long>());
        addValuesToMap(macdetail, CAPS1, Report.ADMIN, 1l);
        addValuesToMap(macdetail, CAPS1, Report.TOTAL, 1l);
        addValuesToMap(macdetail, CAPS2, Report.TOTAL, 5l);
        addValuesToMap(macdetail, CAPS2, Report.GUESTS, 3l);
        addValuesToMap(macdetail, CAPS2, Report.DISABLED, 2l);
        addValuesToMap(macdetail, CAPS3, Report.TOTAL, 2l);
        addValuesToMap(macdetail, CAPS3, Report.LINKS, 8l);

        addUserToCapS(contextReport, CAPS1, 1);
        addUserToCapS(contextReport, CAPS2, 1);
        addUserToCapS(contextReport, CAPS2, 2);
        addUserToCapS(contextReport, CAPS2, 3);
        addUserToCapS(contextReport, CAPS2, 4);
        addUserToCapS(contextReport, CAPS2, 5);
        addUserToCapS(contextReport, CAPS3, 1);
        addUserToCapS(contextReport, CAPS3, 2);
        return contextReport;
    }

    private void initReport(String type) {
        report = new Report(UUID.randomUUID().toString(), type, new Date().getTime());
        report.setConsideredTimeframeStart(new Date().getTime() - 100000);
        report.setConsideredTimeframeEnd(new Date().getTime());
    }

    private void addFirstCapSToReport() {
        Map<String, Object> macdetail = report.getNamespace(Report.MACDETAIL);
        macdetail.put(CAPS1, new HashMap<String, Object>());

    }

    private void addSecondCapSToReport() {
        Map<String, Object> macdetail = report.getNamespace(Report.MACDETAIL);
        macdetail.put(CAPS2, new HashMap<String, Object>());
        ArrayList<Integer> userList = new ArrayList<>();
        userList.add(6);
        userList.add(7);
        userList.add(8);
        addValuesToTenantMap(CAPS2, 20, userList);

    }

    private void initTenantMapForReport() {
        ArrayList<Integer> userList = new ArrayList<>();
        userList.add(2);
        userList.add(3);
        userList.add(4);
        addValuesToTenantMap(CAPS1, 15, userList);
    }

    private void addValuesToTenantMap(String capS, Integer context, ArrayList<Integer> users) {
        LinkedHashMap<String, Object> capSMap = new LinkedHashMap<>();
        LinkedHashMap<Integer, ArrayList<Integer>> contextMap = new LinkedHashMap<>();
        if (report.getTenantMap().get("deployment") == null) {
            contextMap.put(context, users);
            capSMap.put(capS, contextMap);
            report.getTenantMap().put("deployment", capSMap);
        } else {
            contextMap.put(context, users);
            report.getTenantMap().get("deployment").put(capS, contextMap);
        }
    }

    private Map<String, Integer> createPotentialInfostoreReturn(int min, int max, int avg, int total, String counterName, int counterValue) {
        HashMap<String, Integer> returnMap = new HashMap<>();
        returnMap.put("min", min);
        returnMap.put("max", max);
        returnMap.put("avg", avg);
        returnMap.put("total", total);
        if (counterName != null) {
            returnMap.put(counterName, counterValue);
        }
        return returnMap;
    }

    private UserReport initUserReport(Integer userID, boolean isGuest, boolean isAdmin, boolean isDisabled, ContextReport contextReport, String userCapS) {
        UserImpl user = new UserImpl();
        user.setId(userID);
        user.setMailEnabled(!isDisabled);
        UserReport userReport = new UserReport(UUID.randomUUID().toString(), "default", contextReport.getContext(), user, contextReport);
        if (isGuest) {
            user.setCreatedBy(1);
            user.setMail("abc@sonstwo.de");
        }
        if (isAdmin) {
            ((ContextImpl) contextReport.getContext()).setMailadmin(userID);
        }
        userReport.set(Report.MACDETAIL, Report.MAILADMIN, isAdmin);
        userReport.set(Report.MACDETAIL, Report.DISABLED, isDisabled);
        HashMap<String, Long> userLogins = new HashMap<>();
        userLogins.put("open-xchange-appsuite", 1453879860000L);
        userLogins.put("com.openexchange.mobileapp", 1453879860000L);
        userReport.set(Report.MACDETAIL, Report.USER_LOGINS, userLogins);
        ArrayList<String> capSList = new ArrayList<>();
        capSList.add("boxcom");
        userReport.set(Report.MACDETAIL, Report.CAPABILITY_LIST, capSList);
        userReport.set(Report.MACDETAIL, Report.CAPABILITIES, userCapS);
        return userReport;
    }

    private void addUserToCapS(ContextReport contextReport, String capS, Integer userID) {
        LinkedHashMap<Integer, ArrayList<Integer>> capSContextMap = contextReport.getCapSToContext().get(capS);
        if (capSContextMap == null) {
            capSContextMap = new LinkedHashMap<Integer, ArrayList<Integer>>();
            capSContextMap.put(contextReport.getContext().getContextId(), new ArrayList<Integer>());
            contextReport.getCapSToContext().put(capS, capSContextMap);
        }
        capSContextMap.get(contextReport.getContext().getContextId()).add(userID);
    }

    private void addValuesToMap(Map<String, Object> macdetail, String macDetailKey, String insertKey, Long insertValue) {
        HashMap<String, Long> counts = new HashMap<>();
        counts.put(insertKey, insertValue);
        ((HashMap<String, Long>) macdetail.get(macDetailKey)).put(insertKey, insertValue);
    }
}
