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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;

/**
 * {@link TryAddVersionTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.2
 */
public class TryAddVersionTest extends AbstractInfostoreTest {

    private List<String> ids = new ArrayList<>(2);

    public TryAddVersionTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(client.getValues().getPrivateInfostoreFolder()));
        file.setFileName("tryAddVersion");
        NewInfostoreRequest req = new NewInfostoreRequest(file);
        NewInfostoreResponse resp = client.execute(req);
        ids.add(resp.getID());
    }

    @Override
    public void tearDown() throws Exception {
        DeleteInfostoreRequest req = new DeleteInfostoreRequest(ids, Collections.singletonList(String.valueOf(client.getValues().getPrivateInfostoreFolder())), new Date());
        client.execute(req);
        super.tearDown();
    }

    public void testAddVersion() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(client.getValues().getPrivateInfostoreFolder()));
        file.setFileName("tryAddVersion");
        NewInfostoreRequest req = new NewInfostoreRequest(file, true);
        NewInfostoreResponse resp = client.execute(req);
        assertFalse(resp.hasError());
        ids.add(resp.getID());
        GetInfostoreRequest getReq = new GetInfostoreRequest(resp.getID());
        GetInfostoreResponse getResp = client.execute(getReq);
        assertNotNull(getResp);
        File uploaded = getResp.getDocumentMetadata();
        assertEquals(2, uploaded.getNumberOfVersions());
    }

    public void testFallback() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(client.getValues().getPrivateInfostoreFolder()));
        file.setFileName("tryAddVersion");
        NewInfostoreRequest req = new NewInfostoreRequest(file, false);
        NewInfostoreResponse resp = client.execute(req);
        assertFalse(resp.hasError());
        ids.add(resp.getID());
        GetInfostoreRequest getReq = new GetInfostoreRequest(resp.getID());
        GetInfostoreResponse getResp = client.execute(getReq);
        assertNotNull(getResp);
        File uploaded = getResp.getDocumentMetadata();
        assertFalse("tryAddVersion".equals(uploaded.getFileName()));
        assertTrue(uploaded.getFileName().endsWith("(1)"));
        ids.add(uploaded.getId());
    }

}
