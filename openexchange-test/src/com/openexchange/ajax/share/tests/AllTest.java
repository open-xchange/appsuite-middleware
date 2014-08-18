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

package com.openexchange.ajax.share.tests;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.AllRequest;
import com.openexchange.ajax.share.actions.AllResponse;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link AllTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AllTest extends ShareTest {

    /**
     * Initializes a new {@link AllTest}.
     *
     * @param name The test name
     */
    public AllTest(String name) {
        super(name);
    }

    public void testListAllShares() throws Exception {
        /*
         * create multiple random shares
         */
        List<Entry<FolderObject, OCLGuestPermission>> sharedFolders = new ArrayList<Entry<FolderObject, OCLGuestPermission>>();
        for (int i = 0; i < 10; i++) {
            int module = randomModule();
            int parent = getDefaultFolder(module);
            OCLGuestPermission guestPermission = randomGuestPermission();
            FolderObject folder = insertSharedFolder(randomFolderAPI(), module, parent, guestPermission);
            sharedFolders.add(new AbstractMap.SimpleEntry<FolderObject, OCLGuestPermission>(folder, guestPermission));
        }
        /*
         * list all shares
         */
        AllResponse allResponse = client.execute(new AllRequest());
        List<ParsedShare> allShares = allResponse.getParsedShares();
        /*
         * verify each shared folder
         */
        for (Entry<FolderObject, OCLGuestPermission> entry : sharedFolders) {
            /*
             * check permissions
             */
            FolderObject folder = entry.getKey();
            OCLGuestPermission guestPermission = entry.getValue();
            OCLPermission matchingPermission = null;
            for (OCLPermission permission : folder.getPermissions()) {
                if (permission.getEntity() != client.getValues().getUserId()) {
                    matchingPermission = permission;
                    break;
                }
            }
            assertNotNull("No matching permission in created folder found", matchingPermission);
            checkPermissions(guestPermission, matchingPermission);
            /*
             * discover & check share
             */
            ParsedShare share = discoverShare(allShares, folder.getObjectID(), matchingPermission.getEntity());
            checkShare(guestPermission, share);
        }
    }

}
