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

package com.openexchange.ajax.share.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
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

    @Test
    public void testLinkPasswordRandomly() throws Exception {
        testLinkPassword(randomFolderAPI(), randomModule());
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
        GetLinkResponse getResponse = getClient().execute(new GetLinkRequest(target, getTimeZone()));
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
        UpdateLinkResponse updateResponse = getClient().execute(updateRequest);
        assertFalse(updateResponse.getErrorMessage(), updateResponse.hasError());
        /*
         * verify updated link
         */
        getResponse = getClient().execute(new GetLinkRequest(target, getTimeZone()));
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
        updateResponse = getClient().execute(updateRequest);
        assertFalse(updateResponse.getErrorMessage(), updateResponse.hasError());
        /*
         * verify updated link
         */
        getResponse = getClient().execute(new GetLinkRequest(target, getTimeZone()));
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
        updateResponse = getClient().execute(updateRequest);
        assertFalse(updateResponse.getErrorMessage(), updateResponse.hasError());
        /*
         * verify updated link
         */
        getResponse = getClient().execute(new GetLinkRequest(target, getTimeZone()));
        assertFalse(getResponse.getErrorMessage(), getResponse.hasError());
        updatedLink = getResponse.getShareLink();
        assertNotNull("got no updated link", updatedLink);
        assertEquals(link.getShareURL(), updatedLink.getShareURL());
        assertFalse(updatedLink.isNew());
        assertEquals("password wrong", null, updatedLink.getPassword());
    }

}
