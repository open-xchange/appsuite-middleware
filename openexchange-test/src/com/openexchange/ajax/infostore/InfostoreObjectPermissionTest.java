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

package com.openexchange.ajax.infostore;

import java.io.File;
import java.util.Collections;
import java.util.UUID;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.configuration.MailConfig;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.util.UUIDs;


/**
 * {@link InfostoreObjectPermissionTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreObjectPermissionTest extends AbstractAJAXSession {

    private AJAXClient client2;
    private InfostoreTestManager itm;
    private DocumentMetadata expectedDocument;
    private String testDataDir;

    /**
     * Initializes a new {@link InfostoreObjectPermissionTest}.
     * @param name
     */
    public InfostoreObjectPermissionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client2 = new AJAXClient(User.User2);
        itm = new InfostoreTestManager(client);
        expectedDocument = new DocumentMetadataImpl();
        expectedDocument.setTitle(UUIDs.getUnformattedString(UUID.randomUUID()));
        expectedDocument.setDescription("Infostore Item Description");
        expectedDocument.setFileMIMEType("image/png");
        expectedDocument.setFolderId(client.getValues().getPrivateInfostoreFolder());
        expectedDocument.setObjectPermissions(
            Collections.singletonList(new ObjectPermission(client2.getValues().getUserId(), false, ObjectPermission.READ)));
        testDataDir = MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR);
        File upload = new File(testDataDir, "contact_image.png");
        expectedDocument.setFileName(upload.getName());
        itm.newAction(expectedDocument, upload);
    }

    @Override
    protected void tearDown() throws Exception {
        if (client2 != null) {
            client2.logout();
        }
        if (itm != null) {
            itm.cleanUp();
        }
        super.tearDown();
    }

    public void testReadPermission() throws Exception {
        AbstractColumnsResponse allResp = client2.execute(new AllInfostoreRequest(
            10,
            convertColumns(Metadata.HTTPAPI_VALUES_ARRAY),
            Metadata.ID,
            Order.ASCENDING));

        int docId = -1;
        for (Object[] doc : allResp.getArray()) {
            docId = Integer.parseInt((String) doc[allResp.getColumnPos(Metadata.ID)]);
        }

        assertEquals(expectedDocument.getId(), docId);
    }

    private static int[] convertColumns(Metadata[] columns) {
        int[] iColumns = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            Metadata column = columns[i];
            iColumns[i] = column.getId();
        }

        return iColumns;
    }

}
