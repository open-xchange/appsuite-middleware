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

package com.openexchange.test.resourcecache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.test.resourcecache.actions.AbstractResourceCacheRequest;
import com.openexchange.test.resourcecache.actions.ConfigurationRequest;
import com.openexchange.test.resourcecache.actions.ConfigurationResponse;
import com.openexchange.test.resourcecache.actions.DeleteRequest;
import com.openexchange.test.resourcecache.actions.DownloadRequest;
import com.openexchange.test.resourcecache.actions.DownloadResponse;
import com.openexchange.test.resourcecache.actions.UploadRequest;
import com.openexchange.test.resourcecache.actions.UploadResponse;
import com.openexchange.test.resourcecache.actions.UsedRequest;


/**
 * {@link ResourceCacheTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ResourceCacheTest extends AbstractAJAXSession {

    private static final String FS = "FS";

    private static final String DB = "DB";

    private String current = FS;

    public ResourceCacheTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        clearCache();
        super.tearDown();
    }

    private void clearCache() throws OXException, IOException, JSONException {
        DeleteRequest deleteRequest = new DeleteRequest();
        executeTyped(deleteRequest, current);
    }

    public void testLifecycleFS() throws Exception {
        current = FS;
        lifecycle();
    }

    public void testLifecycleDB() throws Exception {
        current = DB;
        lifecycle();
    }

    private void lifecycle() throws Exception {

        //Preperations if the cache holds old elements
        clearCache();
        long used = executeTyped(new UsedRequest(), current).getUsed();
        assertTrue("Cache is not empty", used == 0);

        byte file[] = prepareFile(1024);
        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.addFile("someimage.jpg", "image/jpeg", new ByteArrayInputStream(file));
        UploadResponse uploadResponse = executeTyped(uploadRequest, current);
        List<String> ids = uploadResponse.getIds();
        assertEquals("wrong number of ids", 1, ids.size());

        DownloadRequest downloadRequest = new DownloadRequest(ids.get(0));
        DownloadResponse downloadResponse = executeTyped(downloadRequest, current);
        byte[] reloaded = downloadResponse.getBytes();
        assertTrue("download was not equals upload", Arrays.equals(file, reloaded));

        DeleteRequest deleteRequest = new DeleteRequest(ids.get(0));
        executeTyped(deleteRequest, current);
        downloadResponse = executeTyped(downloadRequest, current);
        assertNull("resource was not deleted", downloadResponse.getBytes());
    }

    /*
     * Requires
     * com.openexchange.preview.cache.quotaPerDocument > 0
     * com.openexchange.preview.cache.quota = n * com.openexchange.preview.cache.quotaPerDocument
     */
    public void testQuotaAndInvalidationFS() throws Exception {
        current = FS;
        quotaAndInvalidation();
    }

    public void testQuotaAndInvalidationDB() throws Exception {
        current = DB;
        quotaAndInvalidation();
    }

    private void quotaAndInvalidation() throws Exception {
        clearCache();
        int[] qts = loadQuotas();
        int quota = qts[0];
        int perDocument = qts[1];
        int n = quota / perDocument;
        if (quota <= 0 || perDocument <= 0 || n < 1) {
            fail("test system is misconfigured. Set correct quotas in preview.properties!");
        }

        long used = executeTyped(new UsedRequest(), current).getUsed();
        assertTrue("Cache is not empty", used == 0);

        // Fill up the whole cache
        byte[] file = prepareFile(perDocument);
        UploadRequest uploadRequest = new UploadRequest();
        for (int i = 0; i < n; i++) {
            uploadRequest.addFile("someimage_" + i + ".jpg", "image/jpeg", new ByteArrayInputStream(file));
        }
        UploadResponse uploadResponse = executeTyped(uploadRequest, current);
        List<String> ids = uploadResponse.getIds();
        assertEquals("wrong number of ids", n, ids.size());

        used = executeTyped(new UsedRequest(), current).getUsed();
        assertTrue("Quota exceeded: used: " + used + " quota: " + quota , used <= quota);

        for (String id : ids) {
            DownloadRequest downloadRequest = new DownloadRequest(id);
            DownloadResponse downloadResponse = executeTyped(downloadRequest, current);
            byte[] reloaded = downloadResponse.getBytes();
            assertNotNull("resource not found", reloaded);
            assertTrue("download was not equals upload", Arrays.equals(file, reloaded));
        }

        uploadRequest = new UploadRequest(true);
        uploadRequest.addFile("someimage_" + n + ".jpg", "image/jpeg", new ByteArrayInputStream(file));
        uploadResponse = executeTyped(uploadRequest, current);
        assertEquals("newest resource was not added", 1, uploadResponse.getIds().size());
        DownloadRequest downloadRequest = new DownloadRequest(uploadResponse.getIds().get(0));
        DownloadResponse downloadResponse = executeTyped(downloadRequest, current);
        byte[] reloaded = downloadResponse.getBytes();
        assertNotNull("resource not found", reloaded);
        assertTrue("download was not equals upload", Arrays.equals(file, reloaded));

        int missing = 0;
        for (String id : ids) {
            downloadRequest = new DownloadRequest(id);
            downloadResponse = executeTyped(downloadRequest, current);
            reloaded = downloadResponse.getBytes();
            if (reloaded == null) {
                missing++;
            }
        }

        assertEquals("Exactly one old resource should have been deleted", 1, missing);
    }

    public void testPerformance() throws Exception {
        current = FS;

        //Preperations if the cache holds old elements
        clearCache();
        long used = executeTyped(new UsedRequest(), current).getUsed();
        assertTrue("Cache is not empty", used == 0);

        final int[] qts = loadQuotas();
        final int quota = qts[0];
        final int perDocument = qts[1];
        final int n = quota / perDocument;
        final int tasks = 16;
        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Future<?>> futures = new ArrayList<Future<?>>(tasks);
        for (int j = 0; j < tasks; j++) {
            futures.add(pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] file = prepareFile(perDocument);
                        UploadRequest uploadRequest = new UploadRequest();
                        for (int i = 0; i < n; i++) {
                            uploadRequest.addFile("someimage_" + i + ".jpg", "image/jpeg", new ByteArrayInputStream(file));
                        }
                        UploadResponse uploadResponse = executeTyped(uploadRequest, current);
                        List<String> ids = uploadResponse.getIds();
                        assertEquals("wrong number of ids", n, ids.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Thread.sleep(2000L);
        used = executeTyped(new UsedRequest(), current).getUsed();
        assertTrue("Quota exceeded", used <= quota);
    }

    public void testResourceExceedsQuota() throws Exception {
        current = FS;
        resourceExceedsQuota();
    }

    private void resourceExceedsQuota() throws Exception {

        //Preperations if the cache holds old elements
        clearCache();
        long used = executeTyped(new UsedRequest(), current).getUsed();
        assertTrue("Cache is not empty", used == 0);

        int[] qts = loadQuotas();
        int perDocument = qts[1];
        byte[] file = prepareFile(perDocument + 1);
        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.addFile("someimage.jpg", "image/jpeg", new ByteArrayInputStream(file));
        UploadResponse uploadResponse = executeTyped(uploadRequest, current);
        assertEquals("resource should not have been cached", 0, uploadResponse.getIds().size());
    }

    public void testUpdateFS() throws Exception {
        current = FS;

        //Preperations if the cache holds old elements
        clearCache();
        long used = executeTyped(new UsedRequest(), current).getUsed();
        assertTrue("Cache is not empty", used == 0);

        String id = UUIDs.getUnformattedString(UUID.randomUUID());
        byte file[] = prepareFile(1024);
        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.setResourceId(id);
        uploadRequest.addFile("someimage.jpg", "image/jpeg", new ByteArrayInputStream(file));
        UploadResponse uploadResponse = executeTyped(uploadRequest, current);
        List<String> ids = uploadResponse.getIds();
        assertEquals("wrong number of ids", 1, ids.size());

        DownloadRequest downloadRequest = new DownloadRequest(ids.get(0));
        DownloadResponse downloadResponse = executeTyped(downloadRequest, current);
        byte[] reloaded = downloadResponse.getBytes();
        assertTrue("download was not equals upload", Arrays.equals(file, reloaded));

        // Now update the file and check it again
        file = prepareFile(1024);
        uploadRequest = new UploadRequest();
        uploadRequest.addFile("someimage.jpg", "image/jpeg", new ByteArrayInputStream(file));
        uploadRequest.setResourceId(id);
        uploadResponse = executeTyped(uploadRequest, current);
        List<String> newIds = uploadResponse.getIds();
        assertEquals("wrong number of ids", 1, newIds.size());
        assertEquals("id has changed", ids.get(0), newIds.get(0));

        downloadRequest = new DownloadRequest(ids.get(0));
        downloadResponse = executeTyped(downloadRequest, current);
        reloaded = downloadResponse.getBytes();
        assertTrue("download was not equals upload", Arrays.equals(file, reloaded));

        DeleteRequest deleteRequest = new DeleteRequest(ids.get(0));
        executeTyped(deleteRequest, current);
        downloadResponse = executeTyped(downloadRequest, current);
        assertNull("resource was not deleted", downloadResponse.getBytes());
    }

    private int[] loadQuotas() throws Exception {
        ConfigurationRequest configurationRequest = new ConfigurationRequest();
        ConfigurationResponse configurationResponse = executeTyped(configurationRequest, current);
        JSONObject config = configurationResponse.getConfigObject();
        int quota = config.getInt("com.openexchange.preview.cache.quota");
        int perDocument = config.getInt("com.openexchange.preview.cache.quotaPerDocument");
        return new int[] { quota, perDocument };
    }

    private <T extends AbstractAJAXResponse> T executeTyped(AbstractResourceCacheRequest<T> request, String cacheType) throws OXException, IOException, JSONException {
        request.setCacheType(cacheType);
        return client.execute(request);
    }

    private byte[] prepareFile(int length) {
        Random r = new Random();
        byte file[] = new byte[length];
        r.nextBytes(file);
        return file;
    }

}
