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
package com.openexchange.ajax.find.mail;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchResult;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.TimeZones;
import com.openexchange.mail.utils.DateUtils;

/**
 * {@link Bug39105Test}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug39105Test extends AbstractMailFindTest {

    private FolderObject testFolder;

    public Bug39105Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String inboxFolder = client.getValues().getInboxFolder();
        String folderName = "Bug39105Test_" + System.currentTimeMillis();
        testFolder = new FolderObject();
        testFolder.setModule(FolderObject.MAIL);
        testFolder.setFullName(inboxFolder + "/" + folderName);
        testFolder.setFolderName(folderName);
        testFolder = folderManager.insertFolderOnServer(testFolder);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testReturnCustomHeader() throws Exception {
        String mail = "From: #FROM#\n" +
            "To: #TO#\n" +
            "CC: #TO#\n" +
            "BCC: #TO#\n" +
            "Received: from ox.open-xchange.com;#DATE#\n" +
            "Date: #DATE#\n" +
            "Subject: #SUBJECT#\n" +
            "Disposition-Notification-To: #FROM#\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/plain; charset=\"UTF-8\"\n" +
            "Content-Transfer-Encoding: 8bit\n" +
            "X-OX-Test-Header: #HEADER_VALUE#\n" +
            "\n" +
            "Content\n" +
            "#BODY#\n";

        String header = randomUID();
        String subject = randomUID();
        mail = mail
            .replaceAll("#FROM#", defaultAddress)
            .replaceAll("#TO#", defaultAddress)
            .replaceAll("#DATE#", DateUtils.toStringRFC822(new Date(), TimeZones.UTC))
            .replaceAll("#SUBJECT#", subject)
            .replaceAll("#BODY#", randomUID())
            .replaceAll("#HEADER_VALUE#", header);
        ByteArrayInputStream mailStream = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        ImportMailRequest request = new ImportMailRequest(testFolder.getFullName(), 0, true, true, new ByteArrayInputStream[] { mailStream });
        ImportMailResponse response = client.execute(request);
        String[][] mailIds = response.getIds();

        String[] columns = new String[] { "601", "600", "X-OX-Test-Header" };
        List<ActiveFacet> facets = prepareFacets(testFolder.getFullName());
        facets.add(createQuery(subject));
        QueryRequest queryRequest = new QueryRequest(0, Integer.MAX_VALUE, facets, Module.MAIL.getIdentifier(), columns);
        QueryResponse queryResponse = client.execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }

        PropDocument foundMail = findByProperty(propDocuments, "id", mailIds[0][1]);
        assertNotNull("Mail not found", foundMail);
        assertEquals("Header not set", header,  foundMail.getProps().get("X-OX-Test-Header"));
    }

}
