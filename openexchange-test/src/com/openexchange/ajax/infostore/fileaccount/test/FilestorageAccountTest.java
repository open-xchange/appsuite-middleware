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

package com.openexchange.ajax.infostore.fileaccount.test;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.infostore.fileaccount.actions.AllFileaccountRequest;
import com.openexchange.ajax.infostore.fileaccount.actions.AllFileaccountResponse;
import com.openexchange.ajax.infostore.fileaccount.actions.GetFileaccountRequest;
import com.openexchange.ajax.infostore.fileaccount.actions.GetFileaccountResponse;
import com.openexchange.file.storage.FileStorageCapability;

/**
 * {@link FilestorageAccountTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public final class FilestorageAccountTest extends AbstractAJAXSession {

    private static final String[] POSSIBLE_CAPABILITIES = new String[] { FileStorageCapability.FILE_VERSIONS.name(), FileStorageCapability.EXTENDED_METADATA.name(), FileStorageCapability.RANDOM_FILE_ACCESS.name(),
        FileStorageCapability.LOCKS.name(), FileStorageCapability.AUTO_NEW_VERSION.name(), FileStorageCapability.ZIPPABLE_FOLDER.name() };

    /**
     * Initializes a new {@link FilestorageAccountTest}.
     * 
     * @param name
     */
    public FilestorageAccountTest(String name) {
        super(name);
    }

    
    public void testGetFilestorageAccountCapabilities() throws Throwable {

        AJAXClient client = getClient();

        GetFileaccountResponse response = client.execute(new GetFileaccountRequest("infostore", "com.openexchange.infostore"));
        assertNotNull("Response is empty!", response);
        Object data = response.getData();
        assertNotNull("Response is empty!", data);
        assertTrue("Response contains unexpected data!", data instanceof JSONObject);
        JSONObject account = ((JSONObject) data);
        Object caps = account.asMap().get("capabilities");
        assertNotNull("Response contains no capabilities field!", caps);
        assertTrue("The capabilities field is not a array list!", caps instanceof ArrayList);
        @SuppressWarnings("unchecked") ArrayList<String> capStrings = (ArrayList<String>) caps;
        for (String str : capStrings) {
            boolean contains = false;
            for (String cap : POSSIBLE_CAPABILITIES) {
                if (cap.equals(str)) {
                    contains = true;
                }
            }
            assertTrue("Returns unknown capability " + str, contains);
        }
    }
    
    public void testGetAllFilestorageAccountCapabilities() throws Throwable {

        AJAXClient client = getClient();

        AllFileaccountResponse response = client.execute(new AllFileaccountRequest(null));
        assertNotNull("Response is empty!", response);
        Object data = response.getData();
        assertNotNull("Response is empty!", data);
        assertTrue("Response contains unexpected data!", data instanceof JSONArray);
        JSONObject account = ((JSONArray) data).getJSONObject(0);
        Object caps = account.asMap().get("capabilities");
        assertNotNull("Response contains no capabilities field!", caps);
        assertTrue("The capabilities field is not a array list!", caps instanceof ArrayList);
        @SuppressWarnings("unchecked") ArrayList<String> capStrings = (ArrayList<String>) caps;
        for (String str : capStrings) {
            boolean contains = false;
            for (String cap : POSSIBLE_CAPABILITIES) {
                if (cap.equals(str)) {
                    contains = true;
                }
            }
            assertTrue("Returns unknown capability " + str, contains);
        }
    }

}
