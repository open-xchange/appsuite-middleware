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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.smtptest;

import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FoldersVisibilityResponse;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailResponse;
import com.openexchange.testing.httpclient.models.MailsCleanUpResponse;
import com.openexchange.testing.httpclient.models.MailsResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.MailApi;

/**
 * {@link MailManager}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Schuerholz</a>
 * @since v7.10.5
 */
public class MailManager {

    private final ApiClient apiClient;

    private final MailApi mailApi;

    public MailManager(ApiClient client) {
        apiClient = client;
        mailApi = new MailApi(client);

    }

    /**
     * Clears all mails.
     * 
     * @throws ApiException If fails to make API calls.
     *
     */
    public void clearMails() throws ApiException {
        List<String> requestBody = getMailFolders(new FoldersApi(apiClient));
        MailsCleanUpResponse clearMailResponse = mailApi.clearMailFolders(requestBody, L(System.currentTimeMillis()));
        checkResponse(clearMailResponse.getError(), clearMailResponse.getErrorDesc());

    }

    /**
     * 
     * Gets the number of mails in the default mail folder.
     *
     * @return The number of mails.
     * @throws Exception
     */
    public int getMailCount() throws Exception {
        String folder = getDefaultMailFolder(new FoldersApi(apiClient));
        List<List<String>> idResponse = getMailIds(folder);
        assertNotNull(idResponse);
        return idResponse.size();
    }

    /**
     * 
     * Get all mails from the default folder.
     *
     * @return A list with the mails.
     * @throws Exception
     */
    public List<MailData> getMails() throws Exception {
        String folder = getDefaultMailFolder(new FoldersApi(apiClient));
        List<List<String>> idResponse = getMailIds(folder);
        List<MailData> messages = new ArrayList<>();
        for (List<String> mail : idResponse) {
            MailResponse mailResponse = mailApi.getMail(folder, mail.get(0), null, null, null, null, null, null, null, null, null, null, null, null);
            messages.add(checkResponse(mailResponse.getError(), mailResponse.getErrorDesc(), mailResponse.getData()));
        }
        return messages;
    }

    /**
     * Retrieves the default contact folder of the user with the specified session
     *
     * @param session The session of the user
     * @param foldersApi The {@link FoldersApi}
     * @return The default contact folder of the user
     * @throws ApiException if the folders api call fails
     * @throws Exception if the default contact folder cannot be found
     */
    @SuppressWarnings("unchecked")
    private String getDefaultMailFolder(FoldersApi foldersApi) throws OXException, ApiException {
        FoldersVisibilityResponse visibleFolders = foldersApi.getVisibleFolders("mail", "1,308", "0", null, Boolean.TRUE);
        if (visibleFolders.getError() != null) {
            throw new OXException(new Exception(visibleFolders.getErrorDesc()));
        }

        Object privateFolders = visibleFolders.getData().getPrivate();
        ArrayList<ArrayList<?>> privateList = (ArrayList<ArrayList<?>>) privateFolders;
        if (privateList.size() == 1) {
            return (String) privateList.get(0).get(0);
        }
        for (ArrayList<?> folder : privateList) {
            String name = (String) folder.get(0);
            if (name.contains("INBOX")) {
                return name;
            }
            //            if (((Boolean) folder.get(1)).booleanValue() == false) {
            //                return (String) folder.get(0);
            //            }
        }
        throw new OXException(new Exception("Unable to find default contact folder!"));
    }

    /**
     * Retrieves the mail folder of the user with the specified session
     *
     * @param foldersApi The {@link FoldersApi}
     * @return The default contact folder of the user
     * @throws ApiException if the default contact folder cannot be found
     */
    private List<String> getMailFolders(FoldersApi foldersApi) throws ApiException {
        FoldersVisibilityResponse visibleFolders = foldersApi.getVisibleFolders("mail", "1,308", "0", null, Boolean.TRUE);
        checkResponse(visibleFolders.getError(), visibleFolders.getErrorDesc());

        Object privateFolders = visibleFolders.getData().getPrivate();
        @SuppressWarnings("unchecked") ArrayList<ArrayList<?>> privateList = (ArrayList<ArrayList<?>>) privateFolders;
        ArrayList<String> result = new ArrayList<>();

        for (ArrayList<?> folder : privateList) {
            if (((Boolean) folder.get(1)).booleanValue()) {
                result.add((String) folder.get(0));
            }
        }
        return result;
    }

    private List<List<String>> getMailIds(String folder) throws ApiException {
        MailsResponse allMailIdsResponse = mailApi.getAllMails(folder, "600,609", null, null, null, "609", "desc", null, null, null, null);
        List<List<String>> idResponse = checkResponse(allMailIdsResponse.getError(), allMailIdsResponse.getErrorDesc(), allMailIdsResponse.getData());
        return idResponse;
    }

    /**
     * Checks if a response doesn't contain any errors
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     */
    private static void checkResponse(String error, String errorDesc) {
        assertNull(errorDesc, error);
    }

    /**
     * Checks if a response doesn't contain any errors
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     * @param data The data element of the response
     * @return The data
     */
    private static <T> T checkResponse(String error, String errorDesc, T data) {
        assertNull(errorDesc, error);
        assertNotNull(data);
        return data;
    }

}
