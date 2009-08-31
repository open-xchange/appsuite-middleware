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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
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
        System.out.println(getName()+" : "+new Date());
        InfostoreTestManager infoMgr = getInfostoreManager();
        FolderObject folder = createDefaultInfostoreFolder();

        DocumentMetadata data = new DocumentMetadataImpl();
        data.setTitle("roundtripper");
        data.setDescription("Round-trippin' infostore file");
        data.setFileMIMEType("text/plain");
        data.setFolderId(folder.getObjectID());
        File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        infoMgr.newAction(data, upload);

        SimPublicationTargetDiscoveryService pubDiscovery = new SimPublicationTargetDiscoveryService();

        Publication publication = generatePublication("infostore", String.valueOf(folder.getObjectID()), pubDiscovery);
        PublicationTestManager pubMgr = getPublishManager();
        pubMgr.setPublicationTargetDiscoveryService(pubDiscovery);
        
        pubMgr.newAction(publication);
        String base = AJAXConfig.getProperty(Property.PROTOCOL) + "://" + AJAXConfig.getProperty(Property.HOSTNAME);
        String pubUrl = (String) publication.getConfiguration().get("url");
        String website = getWebsite(base + pubUrl);
        
        System.out.println("=========");
        System.out.println(website);
        System.out.println("=========");
        
        assertTrue("Should contain reference to a published infostore item", website.contains("<div class=\"ox_infoitem\" id=\"infoitem_0\">"));
        assertFalse("Should not contain reference to a second published infostore item", website.contains("<div class=\"ox_infoitem\" id=\"infoitem_1\">"));
        assertTrue("Should contain a link to the published infostore item", website.contains("ox_file"));

        Pattern urlPattern = Pattern.compile("href=\"(.+?/publications/files/.+?/"+data.getId()+"/.+?)\"");
        Matcher matcher = urlPattern.matcher(website);
        matcher.find();
        String downloadUrl = matcher.group(1);

        assertSameStream(new FileInputStream(upload), getDownload(downloadUrl));
    }
    
    public void testLifeCycleOfInfostoreItemPublication() throws Exception{
        System.out.println(getName()+" : "+new Date());
        InfostoreTestManager infoMgr = getInfostoreManager();
        FolderObject folder = createDefaultInfostoreFolder();

        DocumentMetadata data = new DocumentMetadataImpl();
        data.setTitle("roundtripper2");
        data.setDescription("Round-trippin' infostore file");
        data.setFileMIMEType("text/plain");
        data.setFolderId(folder.getObjectID());
        File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        infoMgr.newAction(data, upload);

        SimPublicationTargetDiscoveryService pubDiscovery = new SimPublicationTargetDiscoveryService();

        Publication publication = generateInfostoreItemPublication(String.valueOf(data.getId()), pubDiscovery);
        PublicationTestManager pubMgr = getPublishManager();

        pubMgr.setPublicationTargetDiscoveryService(pubDiscovery);
        
        pubMgr.newAction(publication);
        String base = AJAXConfig.getProperty(Property.PROTOCOL) + "://" + AJAXConfig.getProperty(Property.HOSTNAME);
        String pubUrl = (String) publication.getConfiguration().get("url");

        assertSameStream("Comparing uploaded files", new FileInputStream(upload), getDownload((base + pubUrl)));
    }

}
