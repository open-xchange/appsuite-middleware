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

package com.openexchange.ajax.mail;

import java.io.IOException;
import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.MailSearchRequest;
import com.openexchange.ajax.mail.actions.MailSearchResponse;
import com.openexchange.ajax.mail.actions.MoveMailRequest;
import com.openexchange.ajax.mail.actions.UpdateMailResponse;
import com.openexchange.mail.MailListField;
import com.openexchange.tools.servlet.AjaxException;

/**
 * {@link MailTestManager}
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class MailTestManager {

    private boolean failOnError;

    private AJAXClient client;

    private AbstractAJAXResponse lastResponse;

    public MailTestManager() {

    }

    public MailTestManager(AJAXClient client) {
        this.client = client;
    }

    public MailTestManager(AJAXClient client, boolean failOnError) {
        this(client);
        this.failOnError = failOnError;
    }

    /**
     * returns the last response executed or null if none happened.
     */
    public AbstractAJAXResponse getLastResponse() {
        return lastResponse;
    }

    /**
     * Deletes mails that are similar to the given one in the same folder. Similarity is based on the subject. This sets the lastResponse
     * field.
     */
    public void deleteSimilarMails(TestMail mail, AJAXClient client) throws JSONException, AjaxException, IOException, SAXException {
        LinkedList<String[]> similarMails = findSimilarMails(mail, client);

        DeleteRequest deleteRequest = new DeleteRequest(similarMails.toArray(new String[][] {}));
        lastResponse = client.execute(deleteRequest);
    }

    public LinkedList<String[]> findSimilarMails(TestMail mail, AJAXClient client) throws JSONException, AjaxException, IOException, SAXException {
        JSONArray pattern = new JSONArray();
        JSONObject param = new JSONObject();
        param.put(Mail.PARAMETER_COL, MailListField.SUBJECT.getField());
        param.put(Mail.PARAMETER_SEARCHPATTERN, mail.getSubject());
        pattern.put(param);

        int[] columns = new int[] { MailListField.ID.getField() };
        String folder = mail.getFolder();
        MailSearchRequest searchRequest = new MailSearchRequest(pattern, folder, columns, -1, null, false);
        MailSearchResponse searchResponse = client.execute(searchRequest);

        JSONArray ids = searchResponse.getDataAsJSONArray();
        LinkedList<String[]> FoldersAndIds = new LinkedList<String[]>();
        for (int i = 0, length = ids.length(); i < length; i++) {
            JSONArray temp = ids.getJSONArray(i);
            FoldersAndIds.add(new String[] { folder, temp.getString(0) });
        }

        return FoldersAndIds;
    }

    /**
     * Moves a mail from its own folder to a given one.
     * Returns a new TestMail containing the new, moved object or null if the move didn't work.  
     * This method sets the lastResponse field.
     */
    public TestMail move(TestMail mail, String destination) throws AjaxException, IOException, SAXException, JSONException {
        MoveMailRequest request = new MoveMailRequest(mail.getFolder(), destination, mail.getId(), failOnError);
        UpdateMailResponse response = client.execute(request);
        lastResponse = response;
        if(lastResponse.hasError())
            return null;
        return get(destination, response.getID());
    }

    /**
     * Gets a mail from the server or null if it could not be found. 
     * This sets the last response field.
     */
    public TestMail get(String folder, String id) throws AjaxException, IOException, SAXException, JSONException {
        GetRequest request = new GetRequest(folder, id, failOnError);
        lastResponse = client.execute(request);
        if(lastResponse.hasError())
            return null;
        return new TestMail((JSONObject) lastResponse.getData());
    }
}
