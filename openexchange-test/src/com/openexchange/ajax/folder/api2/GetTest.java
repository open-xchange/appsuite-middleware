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

package com.openexchange.ajax.folder.api2;

import org.json.JSONObject;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link GetTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetTest extends AbstractAJAXSession {

    private AJAXClient client;

    /**
     * Initializes a new {@link GetTest}.
     *
     * @param name name of the test.
     */
    public GetTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    public void testGetRoot() throws Throwable {
        // Get root folder
        final GetRequest request = new GetRequest(EnumAPI.OUTLOOK, String.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID), true);
        final GetResponse response = client.execute(request);

        final JSONObject jsonObject = (JSONObject) response.getResponse().getData();

        assertEquals("Unexpected root folder ID.", "0", jsonObject.get("id"));
    }

    public void testGetPrivate() throws Throwable {
        // Get private folder
        final GetRequest request = new GetRequest(EnumAPI.OUTLOOK, String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID), true);
        final GetResponse response = client.execute(request);

        final JSONObject jsonObject = (JSONObject) response.getResponse().getData();

        assertEquals("Unexpected private folder ID.", "1", jsonObject.get("id"));
        assertTrue("Subfolder expected below private folder", jsonObject.getBoolean("subfolders"));
    }

    public void testGetPublic() throws Throwable {
        // Get public folder
        final GetRequest request = new GetRequest(EnumAPI.OUTLOOK, String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID), true);
        final GetResponse response = client.execute(request);

        final JSONObject jsonObject = (JSONObject) response.getResponse().getData();

        assertEquals("Unexpected public folder ID.", "2", jsonObject.get("id"));
        assertTrue("Subfolder expected below public folder", jsonObject.getBoolean("subfolders"));
    }

    public void testGetShared() throws Throwable {
        // Get shared folder
        final GetRequest request = new GetRequest(EnumAPI.OUTLOOK, String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID), true);
        final GetResponse response = client.execute(request);

        final JSONObject jsonObject = (JSONObject) response.getResponse().getData();

        assertEquals("Unexpected public folder ID.", "3", jsonObject.get("id"));
    }

}
