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

package com.openexchange.ajax.publish.tests;

import static com.openexchange.test.OXTestToolkit.assertSameStream;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.publish.Publication;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;
import com.openexchange.test.TestInit;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class OXMFInfostoreTest extends AbstractPublicationTest {

    public OXMFInfostoreTest(String name) {
        super(name);
    }

    public void testLifeCycleOfInfostoreFolderPublication() throws Exception {
        InfostoreTestManager infoMgr = getInfostoreManager();
        FolderObject folder = createDefaultInfostoreFolder();

        File data = new DefaultFile();
        data.setTitle("roundtripper");
        data.setDescription("Round-trippin' infostore folder");
        data.setFileMIMEType("text/plain");
        data.setFolderId(String.valueOf(folder.getObjectID()));
        java.io.File upload = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
        data.setFileName(upload.getName());

        infoMgr.newAction(data, upload);

        SimPublicationTargetDiscoveryService pubDiscovery = new SimPublicationTargetDiscoveryService();

        Publication publication = generatePublication("infostore", String.valueOf(folder.getObjectID()), pubDiscovery);
        PublicationTestManager pubMgr = getPublishManager();
        pubMgr.setPublicationTargetDiscoveryService(pubDiscovery);

        pubMgr.newAction(publication);
        String pubUrl = (String) publication.getConfiguration().get("url");
        String website = getWebsite(pubUrl);

        Pattern urlPattern = Pattern.compile("href=\"(.+?/publications/files/.+?/" + getObjectId(data) + "/.+?)\"");
        Matcher matcher = urlPattern.matcher(website);
        boolean found = matcher.find();
        assertTrue("Should contain reference to a published infostore item", found);
        String downloadUrl = matcher.group(1);
        assertSameStream(new FileInputStream(upload), getDownload(downloadUrl));
    }

    public void testLifeCycleOfInfostoreItemPublication() throws Exception {
        InfostoreTestManager infoMgr = getInfostoreManager();
        FolderObject folder = createDefaultInfostoreFolder();

        File data = new DefaultFile();
        data.setTitle("roundtripper2");
        data.setDescription("Round-trippin' infostore file");
        data.setFileMIMEType("text/plain");
        data.setFolderId(String.valueOf(folder.getObjectID()));
        java.io.File upload = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
        data.setFileName(upload.getName());

        infoMgr.newAction(data, upload);

        SimPublicationTargetDiscoveryService pubDiscovery = new SimPublicationTargetDiscoveryService();

        Publication publication = generateInfostoreItemPublication(String.valueOf(getObjectId(data)), pubDiscovery);
        PublicationTestManager pubMgr = getPublishManager();

        pubMgr.setPublicationTargetDiscoveryService(pubDiscovery);

        pubMgr.newAction(publication);
        String pubUrl = (String) publication.getConfiguration().get("url");

        assertSameStream("Comparing uploaded files", new FileInputStream(upload), getDownload((pubUrl)));
    }

}
