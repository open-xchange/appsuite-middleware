/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.find.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
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

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String inboxFolder = getClient().getValues().getInboxFolder();
        String folderName = "Bug39105Test_" + System.currentTimeMillis();
        testFolder = new FolderObject();
        testFolder.setModule(FolderObject.MAIL);
        testFolder.setFullName(inboxFolder + "/" + folderName);
        testFolder.setFolderName(folderName);
        testFolder = ftm.insertFolderOnServer(testFolder);
    }

    @Test
    public void testReturnCustomHeader() throws Exception {
        String mail = "From: #FROM#\n" + "To: #TO#\n" + "CC: #TO#\n" + "BCC: #TO#\n" + "Received: from ox.open-xchange.com;#DATE#\n" + "Date: #DATE#\n" + "Subject: #SUBJECT#\n" + "Disposition-Notification-To: #FROM#\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"UTF-8\"\n" + "Content-Transfer-Encoding: 8bit\n" + "X-OX-Test-Header: #HEADER_VALUE#\n" + "\n" + "Content\n" + "#BODY#\n";

        String header = randomUID();
        String subject = randomUID();
        mail = mail.replaceAll("#FROM#", defaultAddress).replaceAll("#TO#", defaultAddress).replaceAll("#DATE#", DateUtils.toStringRFC822(new Date(), TimeZones.UTC)).replaceAll("#SUBJECT#", subject).replaceAll("#BODY#", randomUID()).replaceAll("#HEADER_VALUE#", header);
        ByteArrayInputStream mailStream = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        ImportMailRequest request = new ImportMailRequest(testFolder.getFullName(), 0, true, true, new ByteArrayInputStream[] { mailStream });
        ImportMailResponse response = getClient().execute(request);
        String[][] mailIds = response.getIds();

        String[] columns = new String[] { "601", "600", "X-OX-Test-Header" };
        List<ActiveFacet> facets = prepareFacets(testFolder.getFullName());
        facets.add(createQuery(subject));
        QueryRequest queryRequest = new QueryRequest(0, Integer.MAX_VALUE, facets, Module.MAIL.getIdentifier(), columns);
        QueryResponse queryResponse = getClient().execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }

        PropDocument foundMail = findByProperty(propDocuments, "id", mailIds[0][1]);
        assertNotNull("Mail not found", foundMail);
        assertEquals("Header not set", header, foundMail.getProps().get("X-OX-Test-Header"));
    }

}
