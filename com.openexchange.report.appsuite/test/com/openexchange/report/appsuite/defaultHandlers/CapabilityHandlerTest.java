
package com.openexchange.report.appsuite.defaultHandlers;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.report.InfostoreInformationService;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.UserReport;
import com.openexchange.report.appsuite.internal.Services;
import com.openexchange.report.appsuite.serialization.Report;
import com.openexchange.report.appsuite.serialization.ReportConfigs;
import com.openexchange.server.services.ServerServiceRegistry;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Services.class, ServerServiceRegistry.class})
public class CapabilityHandlerTest {

    @Mock
    private ConfigurationService configService;

    private ContextReport contextReport;
    private Report report;
    private final CapabilityHandler capabilityHandlerTest = new CapabilityHandler();

    private final String CAPS1 = "active_sync, autologin, boxcom, caldav, calendar, carddav, client-onboarding, collect_email_addresses, conflict_handling, contacts, delegate_tasks";
    private final String CAPS2 = "active_sync, autologin, boxcom, caldav, calendar, carddav, client-onboarding, collect_email_addresses, conflict_handling, contacts";
    private final String CAPS3 = "active_sync, autologin, boxcom, caldav, calendar, carddav, client-onboarding, collect_email_addresses";

    //--------------------Report for storage tests--------------------
    private Report reportStoring;
    private final String REPORT_UUID = "storageUID";
    private final String REPORT_UUID_LOCKS = "locksUID";
    private final String REPORT_PATH = "./test/testfiles/storage";
    private final String REPORT_TYPE = "default";
    private final Long REPORT_TIME = L(new Date().getTime());
    private Report reportStoringLocks;

    ExecutorService eService = null;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(ConfigurationService.class)).thenReturn(configService);
        Mockito.when(configService.getProperty("com.openexchange.report.appsuite.fileStorage", "/tmp")).thenReturn(REPORT_PATH);
        Mockito.when(I(configService.getIntProperty("com.openexchange.report.appsuite.maxChunkSize", 200))).thenReturn(I(200));
        this.contextReport = initContextReport(I(2));
        this.initReport("default");
        this.initReportForStorage();
        this.initReportForStorageWithLocks();
        this.eService = Executors.newFixedThreadPool(2);
    }

    @After
    public void reset() {
        restoreDefaultForComposureTests();
        cleanUpDataForLocks();
        this.eService.shutdown();
    }
    //-------------------Tests-------------------

    @SuppressWarnings("unchecked")
     @Test
     public void testAddContextToEmptyReport() {
        capabilityHandlerTest.merge(contextReport, report);
        long quota = contextReport.get(Report.MACDETAIL_QUOTA, Report.QUOTA, L(0l), Long.class).longValue();
        String quotaSpec = "fileQuota[" + quota + "]";
        HashMap<String, Object> capS1 = report.get(Report.MACDETAIL, CAPS1 + "," + quotaSpec, HashMap.class);
        HashMap<String, Object> capS2 = report.get(Report.MACDETAIL, CAPS2 + "," + quotaSpec, HashMap.class);
        HashMap<String, Object> capS3 = report.get(Report.MACDETAIL, CAPS3 + "," + quotaSpec, HashMap.class);
        validateCapSCount(capS1, L(1l), L(0l), L(1l), L(0l), L(0l), L(1l), L(1l), L(1l), L(1l));
        validateCapSCount(capS2, L(0l), L(2l), L(5l), L(3l), L(0l), L(5l), L(5l), L(5l), L(1l));
        validateCapSCount(capS3, L(0l), L(0l), L(2l), L(0l), L(8l), L(2l), L(2l), L(2l), L(1l));
    }

    @SuppressWarnings("unchecked")
     @Test
     public void testAddContextToReportCapSValues() {
        capabilityHandlerTest.merge(initContextReport(I(4)), report);
        capabilityHandlerTest.merge(initContextReport(I(5)), report);
        String quotaSpec = "fileQuota[0]";
        HashMap<String, Object> capS1 = report.get(Report.MACDETAIL, CAPS1 + "," + quotaSpec, HashMap.class);
        HashMap<String, Object> capS2 = report.get(Report.MACDETAIL, CAPS2 + "," + quotaSpec, HashMap.class);
        HashMap<String, Object> capS3 = report.get(Report.MACDETAIL, CAPS3 + "," + quotaSpec, HashMap.class);
        validateCapSCount(capS1, L(2l), L(0l), L(2l), L(0l), L(0l), L(1l), L(1l), L(1l), L(2l));
        validateCapSCount(capS2, L(0l), L(4l), L(10l), L(6l), L(0l), L(5l), L(5l), L(5l), L(2l));
        validateCapSCount(capS3, L(0l), L(0l), L(4l), L(0l), L(16l), L(2l), L(2l), L(2l), L(2l));
    }

    @SuppressWarnings("unchecked")
     @Test
     public void testAddContextToReportTenantMapValues() {
        capabilityHandlerTest.merge(initContextReport(I(4)), report);
        capabilityHandlerTest.merge(initContextReport(I(5)), report);
        // all thre capS have their entry inside the tenant map on deployment level (index 0)
        assertEquals(3, report.getTenantMap().get("deployment").size());
        // Second capS has a context with id 4 and inside is a user with id 5
        assertEquals(Boolean.TRUE, B(((LinkedHashMap<Integer, ArrayList<Integer>>) report.getTenantMap().get("deployment").get(CAPS2 + ",fileQuota[0]")).get(I(4)).contains(I(5))));
        // Second capS has two contexts total
        assertEquals(2, ((LinkedHashMap<Integer, ArrayList<Integer>>) report.getTenantMap().get("deployment").get(CAPS2 + ",fileQuota[0]")).size());
    }

    @SuppressWarnings("unchecked")
     @Test
     public void testAddAdminToContextReport() {
        UserReport userReport = initUserReport(I(7), false, true, false, contextReport, CAPS1);
        capabilityHandlerTest.merge(userReport, contextReport);
        HashMap<String, Object> capS1 = contextReport.get(Report.MACDETAIL, CAPS1, HashMap.class);
        // the number of admin users has increased from 1 to 2 and total from 1 to 2
        validateCapSCount(capS1, L(2l), null, L(2l), null, null, null, null, null, null);
    }

    @SuppressWarnings("unchecked")
     @Test
     public void testAddDisabledUserToContextReport() {
        UserReport userReport = initUserReport(I(7), false, false, true, contextReport, CAPS2);
        capabilityHandlerTest.merge(userReport, contextReport);
        HashMap<String, Object> capS2 = contextReport.get(Report.MACDETAIL, CAPS2, HashMap.class);
        // the number of disabled users has increased from 2 to 3 and total from 5 to 6
        validateCapSCount(capS2, null, L(3l), L(6l), L(3l), null, null, null, null, null);
    }

    @SuppressWarnings("unchecked")
     @Test
     public void testAddGuestUserToContextReport() {
        UserReport userReport = initUserReport(I(7), true, false, false, contextReport, CAPS3);
        capabilityHandlerTest.merge(userReport, contextReport);
        HashMap<String, Object> capS3 = contextReport.get(Report.MACDETAIL, CAPS3, HashMap.class);
        // The number of guests has increased from 0 to 1
        validateCapSCount(capS3, null, null, L(2l), L(1l), L(8l), null, null, null, null);
    }

     @Test
     public void testAddUserWithLoginsToContextReport() {
        UserReport userReport = initUserReport(I(7), false, false, false, contextReport, CAPS2);
        capabilityHandlerTest.merge(userReport, contextReport);
        // A logins ArrayList exists and has a size of 2
        assertEquals(2, userReport.get(Report.MACDETAIL, Report.USER_LOGINS, HashMap.class).size());
    }

     @Test
     public void testCalculatedDriveMetricsSingleCapS() throws OXException {
        // Mock all potential return values for Drive calculations
        InfostoreInformationService informationService = new InfostoreInformationService() {

            @Override
            public Map<String, Integer> getStorageUseMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                return createPotentialInfostoreReturn(10, 20, 15, 30, null, 0);
            }

            @Override
            public Map<String, Integer> getQuotaUsageMetrics(Map<Integer, List<Integer>> usersInContext) throws SQLException, OXException {
                return createPotentialInfostoreReturn(1, 1, 1, 2, "sum", 2);
            }

            @Override
            public Map<String, Integer> getFileSizeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                return createPotentialInfostoreReturn(10, 20, 15, 30, null, 0);
            }

            @Override
            public Map<String, Integer> getFileCountNoVersions(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                HashMap<String, Integer> returnMap = new HashMap<>();
                returnMap.put("total", I(2));
                return returnMap;
            }

            @Override
            public Map<String, Integer> getFileCountMimetypeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                return new HashMap<>();
            }

            @Override
            public Map<String, Integer> getFileCountMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                return createPotentialInfostoreReturn(1, 1, 1, 2, "users", 2);
            }

            @Override
            public Map<String, Integer> getFileCountInTimeframeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash, Date start, Date end) throws SQLException, OXException {
                return createPotentialInfostoreReturn(1, 1, 1, 2, null, 0);
            }

            @Override
            public Map<String, Integer> getExternalStorageMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                return createPotentialInfostoreReturn(1, 1, 1, 1, null, 0);
            }


        };

        PowerMockito.mockStatic(Services.class);
        PowerMockito.mockStatic(ServerServiceRegistry.class);
        PowerMockito.when(Services.getService(InfostoreInformationService.class)).thenReturn(informationService);
        ServerServiceRegistry serverServiceRegistry = PowerMockito.mock(ServerServiceRegistry.class);
        PowerMockito.when(ServerServiceRegistry.getInstance()).thenReturn(serverServiceRegistry);

        ContextService contextService = PowerMockito.mock(ContextService.class);
        PowerMockito.when(serverServiceRegistry.getService(ContextService.class)).thenReturn(contextService);

        Map<PoolAndSchema, List<Integer>> poolContextMap = new HashMap<>();
        PoolAndSchema poolAndSchema = new PoolAndSchema(4, "testSchema");
        poolContextMap.put(poolAndSchema, Arrays.asList(I(15)));
        PowerMockito.when(contextService.getSchemaAssociationsFor(ArgumentMatchers.anyList())).thenReturn(poolContextMap);

        this.initReport("extended");
        initTenantMapForReport();
        addCapSToReport(report, CAPS1);
        capabilityHandlerTest.finish(report);
        validateDriveTotalAvgs(report.get(Report.TOTAL, Report.DRIVE_TOTAL, LinkedHashMap.class), L(15l), L(15l), L(1l), L(1l), L(1l), L(1l));
    }

     @Test
     public void testCalculatedDriveMetricsTwoCapS() throws OXException {
        // Mock all potential return values for Drive calculations, for each context different values
        InfostoreInformationService informationService = new InfostoreInformationService() {

            @Override
            public Map<String, Integer> getStorageUseMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                for (PoolAndSchema pool : dbContextToUserBash.keySet()) {
                    if (dbContextToUserBash.get(pool).containsKey(I(20)) && dbContextToUserBash.get(pool).get(I(20)) != null) {
                        return createPotentialInfostoreReturn(30, 60, 45, 135, null, 0);
                    }
                }
                return createPotentialInfostoreReturn(10, 20, 15, 30, null, 0);
            }

            @Override
            public Map<String, Integer> getQuotaUsageMetrics(Map<Integer, List<Integer>> usersInContext) throws SQLException, OXException {
                if (usersInContext.containsKey(I(20)) && usersInContext.get(I(20)) != null) {
                    return createPotentialInfostoreReturn(5, 20, 15, 3, "sum", 35);
                }
                return createPotentialInfostoreReturn(1, 1, 1, 1, "sum", 1);
            }

            @Override
            public Map<String, Integer> getFileSizeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                for (PoolAndSchema pool : dbContextToUserBash.keySet()) {
                        if (dbContextToUserBash.get(pool).containsKey(I(20)) && dbContextToUserBash.get(pool).get(I(20)) != null) {
                        return createPotentialInfostoreReturn(30, 60, 45, 135, null, 0);
                    }
                }
                return createPotentialInfostoreReturn(10, 20, 15, 30, null, 0);
            }

            @Override
            public Map<String, Integer> getFileCountNoVersions(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                HashMap<String, Integer> returnMap = new HashMap<>();
                for (PoolAndSchema pool : dbContextToUserBash.keySet()) {
                        if (dbContextToUserBash.get(pool).containsKey(I(20)) && dbContextToUserBash.get(pool).get(I(20)) != null) {
                        returnMap.put("total", I(3));
                        return returnMap;
                    }
                }
                returnMap.put("total", I(2));
                return returnMap;
            }

            @Override
            public Map<String, Integer> getFileCountMimetypeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                return new HashMap<>();
            }

            @Override
            public Map<String, Integer> getFileCountMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                for (PoolAndSchema pool : dbContextToUserBash.keySet()) {
                        if (dbContextToUserBash.get(pool).containsKey(I(20)) && dbContextToUserBash.get(pool).get(I(20)) != null) {
                        return createPotentialInfostoreReturn(1, 1, 1, 3, "users", 3);
                    }
                }
                return createPotentialInfostoreReturn(1, 1, 1, 2, "users", 2);
            }

            @Override
            public Map<String, Integer> getFileCountInTimeframeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash, Date start, Date end) throws SQLException, OXException {
                for (PoolAndSchema pool : dbContextToUserBash.keySet()) {
                        if (dbContextToUserBash.get(pool).containsKey(I(20)) && dbContextToUserBash.get(pool).get(I(20)) != null) {
                        return createPotentialInfostoreReturn(1, 1, 1, 3, null, 0);
                    }
                }
                return createPotentialInfostoreReturn(1, 1, 1, 2, null, 0);
            }

            @Override
            public Map<String, Integer> getExternalStorageMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
                for (PoolAndSchema pool : dbContextToUserBash.keySet()) {
                        if (dbContextToUserBash.get(pool).containsKey(I(20)) && dbContextToUserBash.get(pool).get(I(20)) != null) {
                        return createPotentialInfostoreReturn(0, 0, 0, 0, null, 0);
                    }
                }
                return createPotentialInfostoreReturn(1, 1, 1, 1, null, 0);
            }
        };

        PowerMockito.mockStatic(Services.class);
        PowerMockito.mockStatic(ServerServiceRegistry.class);
        PowerMockito.when(Services.getService(InfostoreInformationService.class)).thenReturn(informationService);
        ServerServiceRegistry serverServiceRegistry = PowerMockito.mock(ServerServiceRegistry.class);
        PowerMockito.when(ServerServiceRegistry.getInstance()).thenReturn(serverServiceRegistry);

        ContextService contextService = PowerMockito.mock(ContextService.class);
        PowerMockito.when(serverServiceRegistry.getService(ContextService.class)).thenReturn(contextService);

        Map<PoolAndSchema, List<Integer>> poolContextMap = new HashMap<>();
        PoolAndSchema poolAndSchema = new PoolAndSchema(4, "testSchema");
        poolContextMap.put(poolAndSchema, Arrays.asList(I(15), I(20)));
        PowerMockito.when(contextService.getSchemaAssociationsFor(ArgumentMatchers.anyList())).thenReturn(poolContextMap);

        this.initReport("extended");
        initTenantMapForReport();
        addCapSToReport(report, CAPS1);
        addSecondCapSToReport();
        capabilityHandlerTest.finish(report);
        validateDriveTotalAvgs(report.get(Report.TOTAL, Report.DRIVE_TOTAL, LinkedHashMap.class), L(33l), L(33l), L(1l), L(1l), L(9l), L(1l));
    }

     @Test
     public void testStoreAndMergeReportPartsStore() {
        capabilityHandlerTest.storeAndMergeReportParts(reportStoring);
        LinkedList<File> parts = getFilesFromReportFolder(reportStoring, ".part");
        // Have all 4 .part files been created?
        assertEquals("Not all capability-set entrys were stored into a file", 4, parts.size());
        File result = new File(reportStoring.getStorageFolderPath() + "/" + reportStoring.getUUID() + "_-1364233380.part");
        assertTrue("Report content was not composed. ", result.exists());
        File expected = new File(reportStoring.getStorageFolderPath() + "/" + "newCapS-1364233380.test");
        try {
            byte[] resultBytes = Files.readAllBytes(result.toPath());
            byte[] expectedBytes = Files.readAllBytes(expected.toPath());
            // Has a newly created .part-file the expected content?
            assertTrue("Composed report-content is not like expected.", Arrays.equals(resultBytes, expectedBytes));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to compare the newly created file with expectation");
        }
        File resultMerge = new File(reportStoring.getStorageFolderPath() + "/" + reportStoring.getUUID() + "_-1051342765.part");
        Scanner sc;
        try {
            sc = new Scanner(resultMerge);
            String content = sc.useDelimiter("\\Z").next();
            sc.close();
            HashMap<String, Object> mergedData = (HashMap<String, Object>) JSONCoercion.parseAndCoerceToNative(content);
            // Have the merged .part file the correct content?
            assertEquals("Merged total users are wrong", I(70), mergedData.get(Report.TOTAL));
            assertEquals("Merged context-users-max are wrong", I(30), mergedData.get(Report.CONTEXT_USERS_MAX));
            assertEquals("Merged context-users-min are wrong", I(5), mergedData.get(Report.CONTEXT_USERS_MIN));
            assertEquals("Merged context-users-avg are wrong", I(5), mergedData.get(Report.CONTEXT_USERS_AVG));
            // Are loaded Long-values also calculated correctly
            assertEquals(L(100000000001l), mergedData.get(Report.ADMIN));
            // Are client login informations also calculated correctly?
            int appsuiteClients = ((HashMap<String, Integer>) mergedData.get("client-list")).get("open-xchange-appsuite").intValue();
            assertEquals(2, appsuiteClients);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

     @Test
     public void testStoreAndMergeReportPartsCleanUp() {
        capabilityHandlerTest.storeAndMergeReportParts(reportStoring);
        assertEquals("Not all capability-set entrys were removed after storing and merge", null, reportStoring.get(Report.MACDETAIL, Report.CAPABILITY_SETS, ArrayList.class));
    }

     @Test
     public void testStoreAndMergeReportsPartsWithLocks() {
        // Prepare wrong data
        initWrongDataForLocks();
        // Start thread that locks the existing file and replaces the data inside with correct data
        eService.execute(new Runnable() {

            @Override
            public void run() {
                FileLock fileLock = null;
                RandomAccessFile raf = null;
                try {
                    raf = new RandomAccessFile(new File(reportStoringLocks.getStorageFolderPath() + "/" + reportStoringLocks.getUUID() + "_-853702361.part"), "rw");
                    // Lock file
                    fileLock = raf.getChannel().tryLock();
                    // Sleep
                    Thread.sleep(1000);
                    // replace
                    replaceWrongWithCorrectDataForLocks();
                    // release lock
                    if (fileLock != null) {
                        fileLock.release();
                    }
                    if (raf != null) {
                        raf.close();
                    }
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        eService.execute(new Runnable() {

            @Override
            public void run() {
                capabilityHandlerTest.storeAndMergeReportParts(reportStoringLocks);
            }
        });
        try {
            eService.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Analyze the stored and manipulated file
        File result = new File(reportStoringLocks.getStorageFolderPath() + "/" + reportStoringLocks.getUUID() + "_-853702361.part");
        Scanner sc;
        try {
            sc = new Scanner(result);
            String content = sc.useDelimiter("\\Z").next();
            sc.close();
            HashMap<String, Object> mergedData = (HashMap<String, Object>) JSONCoercion.parseAndCoerceToNative(content);
            // Have the merged .part file the correct content?
            assertEquals("Merged total users are wrong", I(20), mergedData.get(Report.TOTAL));
            assertEquals("Merged disabled users are wrong", I(1), mergedData.get(Report.DISABLED));
            // Are loaded Long-values also calculated correctly
            // Are client login informations also calculated correctly?
            int appsuiteClients = ((HashMap<String, Integer>) mergedData.get("client-list")).get("open-xchange-appsuite").intValue();
            assertEquals(2, appsuiteClients);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

     @Test
     public void test() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.shutdown();
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

    private void initWrongDataForLocks() {
        try {
            copyFileUsingStream(new File(reportStoringLocks.getStorageFolderPath() + "/filelock_test_wrong.init"), new File(reportStoringLocks.getStorageFolderPath() + "/" + reportStoringLocks.getUUID() + "_-853702361.part"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replaceWrongWithCorrectDataForLocks() {
        try {
            copyFileUsingStream(new File(reportStoringLocks.getStorageFolderPath() + "/filelock_test_right.init"), new File(reportStoringLocks.getStorageFolderPath() + "/" + reportStoringLocks.getUUID() + "_-853702361.part"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanUpDataForLocks() {
        File toDelete = new File(reportStoringLocks.getStorageFolderPath() + "/" + reportStoringLocks.getUUID() + "_-853702361.part");
        if (toDelete.exists()) {
            toDelete.delete();
        }
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    private void restoreDefaultForComposureTests() {
        LinkedList<File> parts = getFilesFromReportFolder(reportStoring, ".part");
        String stayingPartName = reportStoring.getStorageFolderPath() + "/" + reportStoring.getUUID() + "_-1051342765.part";
        String stayingPartContentName = reportStoring.getStorageFolderPath() + "/" + "storedCapS-1051342765.test";
        for (File file : parts) {
            if (!file.getName().equals(stayingPartName)) {
                file.delete();
            }
        }
        File stayingFile = new File(stayingPartName);
        File stayingFileContent = new File(stayingPartContentName);
        try {
            // Load and parse the existing data first into an Own JSONObject
            Scanner sc = new Scanner(stayingFileContent);
            String content = sc.useDelimiter("\\Z").next();
            sc.close();
            // overwrite the so far stored data
            JSONObject jsonData = (JSONObject) JSONCoercion.coerceToJSON(JSONCoercion.parseAndCoerceToNative(content));
            FileWriter fw = null;

            fw = new FileWriter(stayingFile);
            fw.write(jsonData.toString(2));
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LinkedList<File> getFilesFromReportFolder(Report report, final String endsWith) {
        File partsFolder = new File(report.getStorageFolderPath());
        LinkedList<File> files = new LinkedList<>((Arrays.asList(partsFolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return (endsWith != null ? name.endsWith(endsWith) : true);
            }
        }))));
        return files;
    }

    private ContextReport initContextReport(Integer contextId) {

        ContextImpl ctx = new ContextImpl(contextId.intValue());
        ctx.setEnabled(false);
        ContextReport contextReport = new ContextReport(UUID.randomUUID().toString(), "default", ctx);
        contextReport.set(ContextReport.MACDETAIL_LISTS, CAPS1, CAPS1);
        contextReport.set(ContextReport.MACDETAIL_LISTS, CAPS2, CAPS2);
        contextReport.set(ContextReport.MACDETAIL_LISTS, CAPS3, CAPS3);
        Map<String, Object> macdetail = contextReport.getNamespace(Report.MACDETAIL);
        macdetail.put(CAPS1, new HashMap<String, Long>());
        macdetail.put(CAPS2, new HashMap<String, Long>());
        macdetail.put(CAPS3, new HashMap<String, Long>());
        addValuesToMap(macdetail, CAPS1, Report.ADMIN, L(1l));
        addValuesToMap(macdetail, CAPS1, Report.TOTAL, L(1l));
        addValuesToMap(macdetail, CAPS2, Report.TOTAL, L(5l));
        addValuesToMap(macdetail, CAPS2, Report.GUESTS, L(3l));
        addValuesToMap(macdetail, CAPS2, Report.DISABLED, L(2l));
        addValuesToMap(macdetail, CAPS3, Report.TOTAL, L(2l));
        addValuesToMap(macdetail, CAPS3, Report.LINKS, L(8l));

        addUserToCapS(contextReport, CAPS1, I(1));
        addUserToCapS(contextReport, CAPS2, I(1));
        addUserToCapS(contextReport, CAPS2, I(2));
        addUserToCapS(contextReport, CAPS2, I(3));
        addUserToCapS(contextReport, CAPS2, I(4));
        addUserToCapS(contextReport, CAPS2, I(5));
        addUserToCapS(contextReport, CAPS3, I(1));
        addUserToCapS(contextReport, CAPS3, I(2));
        return contextReport;
    }

    private void initReport(String type) {
        report = new Report(UUID.randomUUID().toString(), type, new Date().getTime());
        ReportConfigs rc = new ReportConfigs.ReportConfigsBuilder("default").consideredTimeframeStart(new Date().getTime() - 100000).consideredTimeframeEnd(new Date().getTime()).isConfigTimerange(true).build();
        report.setReportConfig(rc);
    }

    private void initReportForStorage() {
        reportStoring = new Report(REPORT_UUID, REPORT_TYPE, REPORT_TIME.longValue());
        reportStoring.setStorageFolderPath(REPORT_PATH);
        HashMap<String, Object> singleCaps = initDefaultValuesForCapS();
        singleCaps.put(Report.CONTEXT_USERS_MAX, L(10l));
        singleCaps.put(Report.CONTEXTS, L(1l));
        HashMap<String, Object> singleCaps2 = initDefaultValuesForCapS();
        HashMap<String, Object> singleCaps3 = initDefaultValuesForCapS();
        HashMap<String, Object> singleCaps4 = initDefaultValuesForCapS();
        ArrayList<HashMap<String, Object>> capabilitySets = new ArrayList<>();

        HashMap<String, Long> clientList = new HashMap<>();
        clientList.put("open-xchange-appsuite", L(1l));

        singleCaps.put("client-list", clientList);
        singleCaps2.put("client-list", clientList);
        singleCaps3.put("client-list", clientList);
        singleCaps4.put("client-list", clientList);

        List<String> capabilities = new ArrayList<>();
        capabilities.add("active_sync");
        capabilities.add("autologin");
        capabilities.add("boxcom");
        capabilities.add("caldav");
        singleCaps.put("capabilities", capabilities);
        singleCaps.put("total", L(10l));
        capabilitySets.add(singleCaps);

        List<String> capabilities2 = new ArrayList<>();
        capabilities2.add("active_sync");
        capabilities2.add("autologin");
        capabilities2.add("boxcom");
        singleCaps2.put("capabilities", capabilities2);
        singleCaps2.put("total", L(10l));
        capabilitySets.add(singleCaps2);

        List<String> capabilities3 = new ArrayList<>();
        capabilities3.add("active_sync");
        capabilities3.add("autologin");
        singleCaps3.put("capabilities", capabilities3);
        singleCaps3.put("total", L(10l));
        capabilitySets.add(singleCaps3);

        List<String> capabilities4 = new ArrayList<>();
        capabilities4.add("active_sync");
        singleCaps4.put("capabilities", capabilities4);
        singleCaps4.put("total", L(60l));
        singleCaps4.put("guests", L(1l));
        singleCaps4.put("admin", L(1l));
        singleCaps4.put("disabled", L(1l));
        singleCaps4.put("Context-users-max", L(30l));
        singleCaps4.put("Context-users-min", L(5l));
        singleCaps4.put("Context-users-avg", L(20l));
        singleCaps4.put("contexts", L(3l));

        capabilitySets.add(singleCaps4);
        reportStoring.set(Report.MACDETAIL, Report.CAPABILITY_SETS, capabilitySets);
    }

    private void initReportForStorageWithLocks() {
        reportStoringLocks = new Report(REPORT_UUID_LOCKS, REPORT_TYPE, REPORT_TIME.longValue());
        reportStoringLocks.setStorageFolderPath(REPORT_PATH);
        HashMap<String, Object> singleCaps3 = initDefaultValuesForCapS();
        ArrayList<HashMap<String, Object>> capabilitySets = new ArrayList<>();

        HashMap<String, Long> clientList = new HashMap<>();
        clientList.put("open-xchange-appsuite", L(1l));

        singleCaps3.put("client-list", clientList);

        List<String> capabilities3 = new ArrayList<>();
        capabilities3.add("active_sync");
        capabilities3.add("autologin");
        singleCaps3.put("capabilities", capabilities3);
        singleCaps3.put("total", L(10l));
        singleCaps3.put("admin", L(1l));
        singleCaps3.put("disabled", L(1l));
        singleCaps3.put("Context-users-max", L(10l));
        singleCaps3.put("Context-users-min", L(10l));
        singleCaps3.put("Context-users-avg", L(10l));
        singleCaps3.put("contexts", L(1l));
        capabilitySets.add(singleCaps3);

        reportStoringLocks.set(Report.MACDETAIL, Report.CAPABILITY_SETS, capabilitySets);
    }

    private HashMap<String, Object> initDefaultValuesForCapS() {
        HashMap<String, Object> capSMap = new HashMap<>();
        capSMap.put(Report.ADMIN, L(0l));
        capSMap.put(Report.DISABLED, L(0l));
        capSMap.put(Report.TOTAL, L(0l));
        capSMap.put(Report.GUESTS, L(0l));
        capSMap.put(Report.LINKS, L(0l));
        capSMap.put(Report.CONTEXTS, L(0l));
        capSMap.put(Report.CONTEXT_USERS_MAX, L(0l));
        capSMap.put(Report.CONTEXT_USERS_MIN, L(0l));
        capSMap.put(Report.CONTEXT_USERS_AVG, L(0l));
        return capSMap;
    }

    private void addCapSToReport(Report currentReport, String capS) {
        Map<String, Object> macdetail = currentReport.getNamespace(Report.MACDETAIL);
        macdetail.put(capS, new HashMap<String, Object>());
    }

    private void addSecondCapSToReport() {
        Map<String, Object> macdetail = report.getNamespace(Report.MACDETAIL);
        macdetail.put(CAPS2, new HashMap<String, Object>());
        ArrayList<Integer> userList = new ArrayList<>();
        userList.add(I(6));
        userList.add(I(7));
        userList.add(I(8));
        addValuesToTenantMap(CAPS2, I(20), userList);

    }

    private void initTenantMapForReport() {
        ArrayList<Integer> userList = new ArrayList<>();
        userList.add(I(2));
        userList.add(I(3));
        userList.add(I(4));
        addValuesToTenantMap(CAPS1, I(15), userList);
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

    Map<String, Integer> createPotentialInfostoreReturn(int min, int max, int avg, int total, String counterName, int counterValue) {
        HashMap<String, Integer> returnMap = new HashMap<>();
        returnMap.put("min", I(min));
        returnMap.put("max", I(max));
        returnMap.put("avg", I(avg));
        returnMap.put("total", I(total));
        if (counterName != null) {
            returnMap.put(counterName, I(counterValue));
        }
        return returnMap;
    }

    private UserReport initUserReport(Integer userID, boolean isGuest, boolean isAdmin, boolean isDisabled, ContextReport contextReport, String userCapS) {
        UserImpl user = new UserImpl();
        user.setId(userID.intValue());
        user.setMailEnabled(!isDisabled);
        UserReport userReport = new UserReport(UUID.randomUUID().toString(), "default", contextReport.getContext(), user, contextReport);
        if (isGuest) {
            user.setCreatedBy(1);
            user.setMail("abc@sonstwo.de");
        }
        if (isAdmin) {
            ((ContextImpl) contextReport.getContext()).setMailadmin(userID.intValue());
        }
        userReport.set(Report.MACDETAIL, Report.MAILADMIN, B(isAdmin));
        userReport.set(Report.MACDETAIL, Report.DISABLED, B(isDisabled));
        HashMap<String, Long> userLogins = new HashMap<>();
        userLogins.put("open-xchange-appsuite", L(1453879860000L));
        userLogins.put("com.openexchange.mobileapp", L(1453879860000L));
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
            capSContextMap.put(I(contextReport.getContext().getContextId()), new ArrayList<Integer>());
            contextReport.getCapSToContext().put(capS, capSContextMap);
        }
        capSContextMap.get(I(contextReport.getContext().getContextId())).add(userID);
    }

    private void addValuesToMap(Map<String, Object> macdetail, String macDetailKey, String insertKey, Long insertValue) {
        HashMap<String, Long> counts = new HashMap<>();
        counts.put(insertKey, insertValue);
        ((HashMap<String, Long>) macdetail.get(macDetailKey)).put(insertKey, insertValue);
    }
}
