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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;

/**
 * {@link TryAddVersionTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.2
 */
public class TryAddVersionTest extends AbstractInfostoreTest {

    private List<String> ids;
    private final int[] COLUMNS = new int[] { 700, 702, 710, 711 };
    private final String filename = "bug.eml";

    public TryAddVersionTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ids = new ArrayList<>(2);
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("tryAddVersion");
        java.io.File f = new java.io.File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + filename);
        NewInfostoreRequest req = new NewInfostoreRequest(file, f);
        NewInfostoreResponse resp = getClient().execute(req);
        ids.add(resp.getID());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (ids != null) {
                for (String id : ids) {
                    DeleteInfostoreRequest req = new DeleteInfostoreRequest(id, String.valueOf(getClient().getValues().getPrivateInfostoreFolder()), new Date());
                    req.setHardDelete(true);
                    getClient().execute(req);
                }
            }
            ids = null;
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testAddVersion() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("tryAddVersion");
        java.io.File f = new java.io.File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + filename);
        NewInfostoreRequest req = new NewInfostoreRequest(file, f, true);
        NewInfostoreResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
        ids.add(resp.getID());
        GetInfostoreRequest getReq = new GetInfostoreRequest(resp.getID(), COLUMNS);
        GetInfostoreResponse getResp = getClient().execute(getReq);
        assertNotNull(getResp);
        File uploaded = getResp.getDocumentMetadata();
        assertEquals(2, uploaded.getNumberOfVersions());
    }

    @Test
    public void testFallback() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("tryAddVersion");
        java.io.File f = new java.io.File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + filename);
        NewInfostoreRequest req = new NewInfostoreRequest(file, f, false);
        NewInfostoreResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
        ids.add(resp.getID());
        GetInfostoreRequest getReq = new GetInfostoreRequest(resp.getID(), COLUMNS);
        GetInfostoreResponse getResp = getClient().execute(getReq);
        assertNotNull(getResp);
        File uploaded = getResp.getDocumentMetadata();
        assertFalse("tryAddVersion".equals(uploaded.getFileName()));
        assertTrue(uploaded.getFileName().endsWith("(1)"));
        ids.add(uploaded.getId());
    }

    @Test
    public void testBug55425() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("TryAddVersion");
        java.io.File f = new java.io.File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + filename);
        NewInfostoreRequest req = new NewInfostoreRequest(file, f, true);
        NewInfostoreResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
        ids.add(resp.getID());
        GetInfostoreRequest getReq = new GetInfostoreRequest(resp.getID(), COLUMNS);
        GetInfostoreResponse getResp = getClient().execute(getReq);
        assertNotNull(getResp);
        File uploaded = getResp.getDocumentMetadata();
        assertEquals(2, uploaded.getNumberOfVersions());
    }

}
