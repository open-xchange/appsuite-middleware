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

package com.openexchange.ajax.mail.oauth;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.junit.Test;
import com.openexchange.ajax.apiclient.oauth.AbstractOAuthAPIClient;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FoldersVisibilityResponse;
import com.openexchange.testing.httpclient.models.MailResponse;
import com.openexchange.testing.httpclient.models.MailsResponse;
import com.openexchange.testing.httpclient.models.SnippetData;
import com.openexchange.testing.httpclient.models.SnippetResponse;
import com.openexchange.testing.httpclient.models.SnippetUpdateResponse;
import com.openexchange.testing.httpclient.models.SnippetsResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.MailApi;
import com.openexchange.testing.httpclient.modules.SnippetApi;

/**
 * {@link OAuthShowcase} - is not a test but rather a showcase for the oauth mail and snippet functionality.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.5
 */
public class OAuthShowcase extends AbstractOAuthAPIClient {

    private static final String TREE = "0";

    private FoldersApi fApi;
    private MailApi mApi;
    private SnippetApi sApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        fApi = new FoldersApi(oauthclient);
        mApi = new MailApi(oauthclient);
        sApi = new SnippetApi(oauthclient);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().withUserPerContext(2).build();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void showcaseMailViaOAuth() throws ApiException, JSONException {

        // 1. Get all visible folders with mail content
        FoldersVisibilityResponse visibleFolderResp = fApi.getVisibleFolders("mail", "1,300,314,308,316", TREE, null, Boolean.TRUE);
        assertTrue(visibleFolderResp.getErrorDesc(), visibleFolderResp.getError() == null);
        ArrayList<ArrayList<Object>> priv = (ArrayList<ArrayList<Object>>) visibleFolderResp.getData().getPrivate();
        assertNotNull(priv);
        printList("Visible folders: ", priv);
        // Find the inbox (standard folder type == 7)
        Optional<ArrayList<Object>> inbox = priv.stream().filter(folder -> folder.get(4).equals(I(7))).findFirst();
        assertTrue(inbox.isPresent());
        String inboxFolderId = (String) inbox.get().get(0);
        print("Found inbox folder: " + inboxFolderId);
        // 2. Get inbox folder with all infos
        FolderResponse resp = fApi.getFolder(inboxFolderId, TREE, null, null);
        assertTrue(resp.getError() == null);
        print("Details of inbox: " + new JSONObject(resp.getData().toJson()).toString(4));

        // 3. Get mail ids for the inbox
        MailsResponse allMailsResp = mApi.getAllMails(inboxFolderId, "600,607", null, Boolean.FALSE, Boolean.TRUE, null, null, null, null, null, null);
        assertNull(allMailsResp.getError());
        print("Mails in the inbox: " + allMailsResp.getData().toString());

        // 4. Get first mail in the inbox
        MailResponse mailResp = mApi.getMail(inboxFolderId, allMailsResp.getData().get(0).get(0), null, null, null, null, Boolean.FALSE, null, null, null, null, null, null, null);
        assertNull(mailResp.getError());
        print("Content of the first mail: " + new JSONObject(mailResp.getData().toJson()).toString(4));

        // 5. Sent mail to other user in the same context
        String sendMail = mApi.sendMail(createMail(), null);

        print("Mail send:" + Jsoup.parse(sendMail).html());
    }

    @Test
    public void showcaseSnippetViaOAuth() throws ApiException, JSONException {
        SnippetsResponse allSnippetsResp = sApi.getAllSnippets(null);
        assertNull(allSnippetsResp.getErrorDesc(), allSnippetsResp.getError());
        printList("All snippets", allSnippetsResp.getData());

        if (allSnippetsResp.getData().size() > 0) {
            SnippetResponse snippetResponse = sApi.getSnippet(allSnippetsResp.getData().get(0).getId());
            assertNull(snippetResponse.getError());
            print("Snippet 1: " + new JSONObject(snippetResponse.getData().toJson()).toString(4));
        }
        SnippetData snippetData = new SnippetData();
        // @formatter:off
        snippetData.type("signature")
                   .module("io.ox./mail")
                   .displayname("showcase")
                   .content("<p>test</p>");
        // @formatter:on
        SnippetUpdateResponse createSnippetResp = sApi.createSnippet(snippetData);
        assertNull(createSnippetResp.getError());
        print("New snippet: " + createSnippetResp.getData());

        SnippetResponse snippetResponse2 = sApi.getSnippet(createSnippetResp.getData());
        assertNull(snippetResponse2.getError());

        print("Snippet new: " + new JSONObject(snippetResponse2.getData().toJson()).toString(4));
    }


    /**
     * Prints the given text surrounded by two lines dotted lines
     *
     * @param str The string to print
     */
    private void print(String str) {
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
        System.out.println(str);
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
    }

    /**
     * Prints the given text surrounded by two lines dotted lines
     *
     * @param str The string to print
     * @param list The list to print
     */
    private void printList(String str, List<?> list) {
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
        System.out.println(str);
        list.forEach(e -> System.out.println(e.toString()));
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
    }


    /**
     * Creates a mail with subject test from testuser to testuser2
     *
     * @return The mails as a json string
     * @throws JSONException
     */
    private String createMail() throws JSONException {
        JSONObject mail = new JSONObject();
        JSONArray from = new JSONArray();
        JSONArray innerFrom = new JSONArray();
        innerFrom.put(testUser.getUser());
        innerFrom.put(testUser.getLogin());
        from.put(innerFrom);
        mail.put("from", from);

        JSONArray to = new JSONArray();
        JSONArray innerTo = new JSONArray();
        innerTo.put(testUser2.getUser());
        innerTo.put(testUser2.getLogin());
        to.put(innerTo);
        mail.put("to", to);
        mail.put("subject", "test");
        return mail.toString();
    }

    @Override
    public String getScopes() {
        return "read_mail write_mail write_userconfig";
    }

}
