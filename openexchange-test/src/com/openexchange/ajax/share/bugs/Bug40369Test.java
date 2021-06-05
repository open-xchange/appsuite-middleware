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

package com.openexchange.ajax.share.bugs;

import static com.openexchange.java.Autoboxing.D;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.ajax.share.actions.ShareLink;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link Bug40369Test}
 *
 * NPE when calling "getLink" for shares
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug40369Test extends ShareTest {

    private static final int NUM_THREADS = 20;

    @Test
    @TryAgain
    public void testCreateFolderLinkConcurrentlyRandomly() throws Exception {
        testCreateFolderLinkConcurrently(randomFolderAPI(), randomModule());
    }

    public void noTestCreateFolderLinkConcurrentlyExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testCreateFolderLinkConcurrently(api, module);
            }
        }
    }

    private void testCreateFolderLinkConcurrently(EnumAPI api, int module) throws Exception {
        testCreateFolderLinkConcurrently(api, module, getDefaultFolder(module));
    }

    private void testCreateFolderLinkConcurrently(EnumAPI api, int module, int parent) throws Exception {
        /*
         * create folder
         */
        FolderObject folder = insertPrivateFolder(api, module, parent);
        /*
         * get a link for the file concurrently
         */
        ShareTarget target = new ShareTarget(module, String.valueOf(folder.getObjectID()));
        GetLinkResponse[] responses = getLinkConcurrently(target, NUM_THREADS);
        Arrays.asList(responses).stream().forEach((r) -> Assert.assertFalse(r.getErrorMessage(), r.hasError()));
        /*
         * check that there's exactly one anonymous guest entity in folder afterwards
         */
        folder = getFolder(api, folder.getObjectID());
        assertNotNull(folder.getPermissions());
        assertEquals(2, folder.getPermissions().size());
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        ExtendedPermissionEntity guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        assertNotNull(guest);
        /*
         * check responses, assert the same link for the same target is returned "most" of the time.
         * Due to the combination of db transaction and db synchronization the response can contain a different sharelink
         * But this should only happen in a very few occasions
         */
        HashSet<String> links = new HashSet<>();
        for (GetLinkResponse response : responses) {
            if (response.hasError()) {
                fail(response.getErrorMessage());
                continue;
            }
            ShareLink shareLink = response.getShareLink();
            assertNotNull(shareLink);
            links.add(shareLink.getShareURL());
        }

        assertThat(Double.valueOf(links.size()), lessThanOrEqualTo(D(NUM_THREADS * 0.1d))); // allow 10% to fail (2 req)
        // Check that the share url stays the same from now on
        final GetLinkRequest request = new GetLinkRequest(target, getTimeZone());
        GetLinkResponse resp = getClient().execute(request);
        ShareLink shareLink = resp.getShareLink();
        assertTrue(links.contains(shareLink.getShareURL()));
        resp = getClient().execute(request);
        assertEquals(shareLink.getShareURL(), resp.getShareLink().getShareURL());

    }

    private GetLinkResponse[] getLinkConcurrently(ShareTarget target, int numThreads) throws Exception {
        final GetLinkRequest request = new GetLinkRequest(target, getTimeZone());
        request.setFailOnError(false);
        Thread[] threads = new Thread[numThreads];
        final GetLinkResponse[] responses = new GetLinkResponse[threads.length];
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            AJAXClient client = getClient();
            threads[i] = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        responses[index] = client.execute(request);
                    } catch (Exception e) {
                        fail(e.getMessage());
                    }
                }
            });
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        return responses;
    }
}
