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

package com.openexchange.ajax.share.tests;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.ajax.share.actions.ShareLink;
import com.openexchange.ajax.share.actions.UpdateLinkRequest;
import com.openexchange.ajax.share.actions.UpdateLinkResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.ShareTarget;

/**
 * {@link LinkUpdateTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LinkUpdateTest extends ShareTest {

    /**
     * Initializes a new {@link LinkUpdateTest}.
     *
     * @param name The test name
     */
    public LinkUpdateTest(String name) {
        super(name);
    }

    public void testLinkExpiryDateRandomly() throws Exception {
        testLinkExpiryDate(randomFolderAPI(), randomModule());
    }

    public void noTestLinkExpiryDateExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testLinkExpiryDate(api, module);
            }
        }
    }

    private void testLinkExpiryDate(EnumAPI api, int module) throws Exception {
        testLinkExpiryDate(api, module, getDefaultFolder(module));
    }

    private void testLinkExpiryDate(EnumAPI api, int module, int parent) throws Exception {
        /*
         * create link for a new folder
         */
        FolderObject folder = insertPrivateFolder(api, module, parent);
        ShareTarget target = new ShareTarget(module, String.valueOf(folder.getObjectID()));
        GetLinkResponse getResponse = client.execute(new GetLinkRequest(target, getTimeZone()));
        assertFalse(getResponse.getErrorMessage(), getResponse.hasError());
        ShareLink link = getResponse.getShareLink();
        assertNotNull("got no link", link);
        assertTrue(link.isNew());
        /*
         * update link & apply expiry date
         */
        Date expiryDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        UpdateLinkRequest updateRequest = new UpdateLinkRequest(target, getTimeZone(), getResponse.getTimestamp().getTime());
        updateRequest.setExpiryDate(expiryDate);
        UpdateLinkResponse updateResponse = client.execute(updateRequest);
        assertFalse(updateResponse.getErrorMessage(), updateResponse.hasError());
        /*
         * verify updated link
         */
        getResponse = client.execute(new GetLinkRequest(target, getTimeZone()));
        assertFalse(getResponse.getErrorMessage(), getResponse.hasError());
        ShareLink updatedLink = getResponse.getShareLink();
        assertNotNull("got no updated link", updatedLink);
        assertEquals(link.getShareURL(), updatedLink.getShareURL());
        assertFalse(updatedLink.isNew());
        assertEquals("expiry date wrong", expiryDate, updatedLink.getExpiry());
        /*
         * update link & change expiry date
         */
        expiryDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3));
        updateRequest = new UpdateLinkRequest(target, getTimeZone(), updateResponse.getTimestamp().getTime());
        updateRequest.setExpiryDate(expiryDate);
        updateResponse = client.execute(updateRequest);
        assertFalse(updateResponse.getErrorMessage(), updateResponse.hasError());
        /*
         * verify updated link
         */
        getResponse = client.execute(new GetLinkRequest(target, getTimeZone()));
        assertFalse(getResponse.getErrorMessage(), getResponse.hasError());
        updatedLink = getResponse.getShareLink();
        assertNotNull("got no updated link", updatedLink);
        assertEquals(link.getShareURL(), updatedLink.getShareURL());
        assertFalse(updatedLink.isNew());
        assertEquals("expiry date wrong", expiryDate, updatedLink.getExpiry());
        /*
         * update link & remove expiry date
         */
        updateRequest = new UpdateLinkRequest(target, getTimeZone(), updateResponse.getTimestamp().getTime());
        updateRequest.setExpiryDate(null);
        updateResponse = client.execute(updateRequest);
        assertFalse(updateResponse.getErrorMessage(), updateResponse.hasError());
        /*
         * verify updated link
         */
        getResponse = client.execute(new GetLinkRequest(target, getTimeZone()));
        assertFalse(getResponse.getErrorMessage(), getResponse.hasError());
        updatedLink = getResponse.getShareLink();
        assertNotNull("got no updated link", updatedLink);
        assertEquals(link.getShareURL(), updatedLink.getShareURL());
        assertFalse(updatedLink.isNew());
        assertEquals("expiry date wrong", null, updatedLink.getExpiry());
    }

    public void testLinkPasswordRandomly() throws Exception {
        testLinkPassword(randomFolderAPI(), randomModule());
    }

    public void noTestPasswordExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testLinkPassword(api, module);
            }
        }
    }

    private void testLinkPassword(EnumAPI api, int module) throws Exception {
        testLinkPassword(api, module, getDefaultFolder(module));
    }

    private void testLinkPassword(EnumAPI api, int module, int parent) throws Exception {
        /*
         * create link for a new folder
         */
        FolderObject folder = insertPrivateFolder(api, module, parent);
        ShareTarget target = new ShareTarget(module, String.valueOf(folder.getObjectID()));
        GetLinkResponse getResponse = client.execute(new GetLinkRequest(target, getTimeZone()));
        assertFalse(getResponse.getErrorMessage(), getResponse.hasError());
        ShareLink link = getResponse.getShareLink();
        assertNotNull("got no link", link);
        assertTrue(link.isNew());
        /*
         * update link & apply password
         */
        String password = randomUID();
        UpdateLinkRequest updateRequest = new UpdateLinkRequest(target, getTimeZone(), getResponse.getTimestamp().getTime());
        updateRequest.setPassword(password);
        UpdateLinkResponse updateResponse = client.execute(updateRequest);
        assertFalse(updateResponse.getErrorMessage(), updateResponse.hasError());
        /*
         * verify updated link
         */
        getResponse = client.execute(new GetLinkRequest(target, getTimeZone()));
        assertFalse(getResponse.getErrorMessage(), getResponse.hasError());
        ShareLink updatedLink = getResponse.getShareLink();
        assertNotNull("got no updated link", updatedLink);
        assertEquals(link.getShareURL(), updatedLink.getShareURL());
        assertFalse(updatedLink.isNew());
        assertEquals("password wrong", password, updatedLink.getPassword());
        /*
         * update link & change password
         */
        password = randomUID();
        updateRequest = new UpdateLinkRequest(target, getTimeZone(), updateResponse.getTimestamp().getTime());
        updateRequest.setPassword(password);
        updateResponse = client.execute(updateRequest);
        assertFalse(updateResponse.getErrorMessage(), updateResponse.hasError());
        /*
         * verify updated link
         */
        getResponse = client.execute(new GetLinkRequest(target, getTimeZone()));
        assertFalse(getResponse.getErrorMessage(), getResponse.hasError());
        updatedLink = getResponse.getShareLink();
        assertNotNull("got no updated link", updatedLink);
        assertEquals(link.getShareURL(), updatedLink.getShareURL());
        assertFalse(updatedLink.isNew());
        assertEquals("password wrong", password, updatedLink.getPassword());
        /*
         * update link & remove password
         */
        updateRequest = new UpdateLinkRequest(target, getTimeZone(), updateResponse.getTimestamp().getTime());
        updateRequest.setPassword(null);
        updateResponse = client.execute(updateRequest);
        assertFalse(updateResponse.getErrorMessage(), updateResponse.hasError());
        /*
         * verify updated link
         */
        getResponse = client.execute(new GetLinkRequest(target, getTimeZone()));
        assertFalse(getResponse.getErrorMessage(), getResponse.hasError());
        updatedLink = getResponse.getShareLink();
        assertNotNull("got no updated link", updatedLink);
        assertEquals(link.getShareURL(), updatedLink.getShareURL());
        assertFalse(updatedLink.isNew());
        assertEquals("password wrong", null, updatedLink.getPassword());
    }

}
