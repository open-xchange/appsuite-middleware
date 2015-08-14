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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.share.bugs;

import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.ajax.share.actions.ShareLink;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.ShareTarget;

/**
 * {@link Bug40369Test}
 *
 * NPE when calling "getLink" for shares
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug40369Test extends ShareTest {

    private static final int NUM_THREADS = 10;

    /**
     * Initializes a new {@link Bug40369Test}.
     *
     * @param name The test name
     */
    public Bug40369Test(String name) {
        super(name);
    }

    public void testCreateLinkConcurrently() throws Exception {
        /*
         * create folder and a file inside
         */
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE));
        File file = insertFile(folder.getObjectID());
        /*
         * get a link for the file concurrently
         */
        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, file.getFolderId(), file.getId());
        final GetLinkRequest request = new GetLinkRequest(target, getTimeZone());
        request.setFailOnError(false);
        Thread[] threads = new Thread[NUM_THREADS];
        final GetLinkResponse[] responses = new GetLinkResponse[threads.length];
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
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
        /*
         * check responses, assert the same link for the same target, thereof one marked as "new"
         */
        String shareURL = null;
        boolean oneNew = false;
        for (GetLinkResponse response : responses) {
            if (response.hasError()) {
                fail(response.getErrorMessage());
            }
            ShareLink shareLink = response.getShareLink();
            assertNotNull(shareLink);
            if (null == shareURL) {
                shareURL = shareLink.getShareURL();
            } else {
                assertEquals(shareURL, shareLink.getShareURL());
            }
            if (shareLink.isNew()) {
                assertFalse(oneNew);
                oneNew = true;
            }
        }
    }

}
