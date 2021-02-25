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

package com.openexchange.ajax.folder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.manager.FolderApi;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AbstractAPIClientSession;

/**
 * {@link Bug16899Test}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com>Steffen Templin</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class Bug16899Test extends AbstractAPIClientSession {

    private static final String MODULE = "mail";
    private static final String COLUMNS = "1";
    private FolderManager folderManager;

    /**
     * Initializes a new {@link Bug16899Test}.
     */
    public Bug16899Test() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderManager = new FolderManager(new FolderApi(getApiClient(), testUser), String.valueOf(EnumAPI.OX_NEW.getTreeId()));
    }

    @Test
    public void testBug16899() throws Exception {
        String folderName = "Bug_16899_Test" + new UID().toString();
        String root = folderManager.getDefaultFolder(MODULE);
        String folderId = folderManager.createFolder(root, folderName, MODULE);

        ArrayList<ArrayList<Object>> listFolders = folderManager.listFolders(root, COLUMNS, Boolean.TRUE);
        assertNotNull(listFolders);
        assertTrue("Testfolder not found", listFolders.stream().filter(folder -> folder.get(0).equals(folderId)).findAny().isPresent());

        List<String> deleted = folderManager.deleteFolder(Collections.singletonList(folderId));
        assertTrue(deleted.isEmpty());
        folderManager.forgetFolder(folderId);

        listFolders = folderManager.listFolders(root, COLUMNS, Boolean.TRUE);
        assertNotNull(listFolders);
        assertFalse("Testfolder still found", listFolders.stream().filter(folder -> folder.get(0).equals(folderId)).findAny().isPresent());
    }

}
