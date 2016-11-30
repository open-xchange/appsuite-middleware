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

package com.openexchange.ajax.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.ajax.mail.actions.ArchiveRequest;
import com.openexchange.ajax.mail.actions.ConversationResponse;
import com.openexchange.ajax.mail.actions.CopyRequest;
import com.openexchange.ajax.mail.actions.CopyResponse;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.ForwardRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.ajax.mail.actions.MailSearchRequest;
import com.openexchange.ajax.mail.actions.MailSearchResponse;
import com.openexchange.ajax.mail.actions.MoveMailRequest;
import com.openexchange.ajax.mail.actions.MoveMailToCategoryRequest;
import com.openexchange.ajax.mail.actions.MoveMailToCategoryResponse;
import com.openexchange.ajax.mail.actions.ReplyAllRequest;
import com.openexchange.ajax.mail.actions.ReplyRequest;
import com.openexchange.ajax.mail.actions.ReplyResponse;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.ajax.mail.actions.ThreadedAllRequest;
import com.openexchange.ajax.mail.actions.TrainRequest;
import com.openexchange.ajax.mail.actions.TrainResponse;
import com.openexchange.ajax.mail.actions.UnreadRequest;
import com.openexchange.ajax.mail.actions.UnreadResponse;
import com.openexchange.ajax.mail.actions.UpdateMailRequest;
import com.openexchange.ajax.mail.actions.UpdateMailResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.ThreadSortMailMessage;

/**
 * {@link MailTestManager}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class MailTestManager {

    private final List<MailCleaner> cleaningSteps;

    private boolean failOnError;

    private AJAXClient client;

    private AbstractAJAXResponse lastResponse;

    public MailTestManager() {
        cleaningSteps = new LinkedList<>();
    }

    public MailTestManager(AJAXClient client) {
        this();
        this.client = client;
    }

    public MailTestManager(AJAXClient client, boolean failOnError) {
        this(client);
        this.failOnError = failOnError;
    }

    public void setFailOnError(boolean failOnError) {
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
    public void deleteSimilarMails(TestMail mail, AJAXClient client) throws JSONException, OXException, IOException, SAXException {
        LinkedList<String[]> similarMails = findSimilarMailsInSameFolder(mail, client);

        DeleteRequest deleteRequest = new DeleteRequest(similarMails.toArray(new String[][] {}));
        lastResponse = client.execute(deleteRequest);
    }

    public LinkedList<String[]> findSimilarMailsInSameFolder(TestMail mail, AJAXClient client) throws JSONException, OXException, IOException, SAXException {
        return findSimilarMails(mail, client, mail.getFolder());
    }

    public LinkedList<String[]> findSimilarMails(TestMail mail, AJAXClient client, String folder) throws JSONException, OXException, IOException, SAXException {
        JSONArray pattern = new JSONArray();
        JSONObject param = new JSONObject();
        param.put(Mail.PARAMETER_COL, MailListField.SUBJECT.getField());
        param.put(Mail.PARAMETER_SEARCHPATTERN, mail.getSubject());
        pattern.put(param);

        int[] columns = new int[] { MailListField.ID.getField() };
        MailSearchRequest searchRequest = new MailSearchRequest(pattern, folder, columns, -1, null, false);
        MailSearchResponse searchResponse = client.execute(searchRequest);

        JSONArray ids = searchResponse.getDataAsJSONArray();
        if (null == ids || 0 == ids.length()) {
            return new LinkedList<>();
        }

        LinkedList<String[]> FoldersAndIds = new LinkedList<>();
        for (int i = 0, length = ids.length(); i < length; i++) {
            JSONArray temp = ids.getJSONArray(i);
            FoldersAndIds.add(new String[] { folder, temp.getString(0) });
        }

        return FoldersAndIds;
    }

    public List<TestMail> findAndLoadSimilarMails(TestMail mail, AJAXClient client, String folder) throws JSONException, OXException, IOException, SAXException {
        LinkedList<String[]> mailIDs = findSimilarMails(mail, client, folder);
        LinkedList<TestMail> results = new LinkedList<>();
        for (String[] folderAndId : mailIDs) {
            results.add(get(folderAndId));
        }
        return results;
    }

    /**
     * Sends a mail. This methods also sets the lastResponse field.
     *
     * @return The mail as placed in the sent box.
     */
    public TestMail send(TestMail mail) throws JSONException, OXException, IOException, SAXException {
        SendRequest request = new SendRequest(mail.toJSON().toString(), failOnError);
        SendResponse response = client.execute(request);
        lastResponse = response;
        if (lastResponse.hasError()) {
            return null;
        }
        String[] folderAndID = response.getFolderAndID();
        mail = get(folderAndID[0], folderAndID[1]);

        cleaningSteps.add(new MailCleaner(mail, client));
        markCopyInInboxIfNecessary(mail);

        return mail;
    }

    /**
     * Sends a mail. This methods also sets the lastResponse field.
     *
     * @return The mail as placed in the sent box.
     */
    public TestMail send(TestMail mail, InputStream upload) throws JSONException, OXException, IOException, SAXException {
        SendRequest request = new SendRequest(mail.toJSON().toString(), upload, failOnError);
        SendResponse response = client.execute(request);
        lastResponse = response;
        if (lastResponse.hasError()) {
            return null;
        }
        String[] folderAndID = response.getFolderAndID();
        mail = get(folderAndID[0], folderAndID[1]);

        cleaningSteps.add(new MailCleaner(mail, client));
        markCopyInInboxIfNecessary(mail);

        return mail;
    }

    /**
     * Imports a mail either into the test mails folder or into the INBOX, if not specified.
     * After the succeful import folder and mail id are set on the TestMail object.
     * @param mail The mail
     */
    public void importMail(TestMail mail) throws OXException, IOException, JSONException {
        String folder = mail.getFolder();
        if (folder == null) {
            folder = "default0/INBOX";
        }

        ByteArrayInputStream mailStream = new ByteArrayInputStream(mail.toRFC822String().getBytes());
        ImportMailRequest request = new ImportMailRequest(folder, 0, true, true, new ByteArrayInputStream[] { mailStream });
        ImportMailResponse response = client.execute(request);
        mail.setFolderAndID(response.getIds()[0]);
    }

    /**
     * Moves a mail from its own folder to a given one. Returns a new TestMail containing the new, moved object or null if the move didn't
     * work. This method sets the lastResponse field.
     */
    public TestMail move(TestMail mail, String destination) throws OXException, IOException, SAXException, JSONException {
        MoveMailRequest request = new MoveMailRequest(mail.getFolder(), destination, mail.getId(), failOnError);
        UpdateMailResponse response = client.execute(request);
        lastResponse = response;
        if (lastResponse.hasError()) {
            return null;
        }
        TestMail modifiedMail = get(destination, response.getID());
        updateForCleanup(mail, modifiedMail);
        return modifiedMail;
    }

    /**
     * Moves a mail from the current category to another. This method sets the lastResponse field.
     */
    public void moveToCategory(TestMail mail, String categoryId) throws OXException, IOException, SAXException, JSONException {

        MoveMailToCategoryRequest request = new MoveMailToCategoryRequest(categoryId);
        request.addMail(mail.getId(), mail.getFolder());
        MoveMailToCategoryResponse response = client.execute(request);
        lastResponse = response;
    }

    /**
     * Retrieves the unread count for the given mail category
     * 
     * @param categoryId The category identifier
     * @return The unread count
     * @throws Exception
     * 
     */
    public int getUnreadCount(String categoryId) throws Exception {

        UnreadRequest request = new UnreadRequest();

        UnreadResponse response = client.execute(request);
        lastResponse = response;
        if (lastResponse.hasError()) {
            return -1;
        }

        return response.getUnreadCount(categoryId);
    }
    
    /**
     * Trains the given category
     * 
     * @param categoryId The category identifier
     * @param applyToExistingOnes Reorganizes the existing mails
     * @param applyToFutureOnes Defines whether a rule is created or not
     * @param mails One or more mail addresses
     * @throws Exception
     * 
     */
    public void trainCategory(String categoryId, boolean applyToExistingOnes, boolean applyToFutureOnes, String... mails) throws Exception {

        TrainRequest request = new TrainRequest(categoryId, applyToFutureOnes, applyToExistingOnes);
        for(String mail: mails){
            request.addAddress(mail);
        }

        TrainResponse response = client.execute(request);
        lastResponse = response;
    }

    /**
     * Retrieves an array of {@link MailMessage}
     * 
     * @param folderPath The folder path
     * @param columns The columns
     * @param sort The sort field
     * @param order The sort order
     * @param failOnError Whether the request should fail on error
     * @param categoryId The optional category identifier
     * @return An array of {@link MailMessage}
     * @throws Exception
     */
    public MailMessage[] listMails(String folderPath, int[] columns, int sort, Order order, boolean failOnError, String categoryId) throws Exception {
        AllRequest request = new AllRequest(folderPath, columns, sort, order, failOnError, categoryId);
        AllResponse response = client.execute(request);
        lastResponse = response;
        return response.getMailMessages(columns);
    }

    /**
     * Retrieves a list of {@link ThreadSortMailMessage}
     * 
     * @param folderPath The folder path
     * @param columns The columns
     * @param sort The sort field
     * @param order The sort order
     * @param failOnError Whether the request should fail on error
     * @param categoryId The optional category identifier
     * @return A list of {@link ThreadSortMailMessage}
     * @throws Exception
     */
    public List<ThreadSortMailMessage> listConversations(String folderPath, int[] columns, int sort, Order order, boolean failOnError, String categoryId, int leftLimit, int rightLimit) throws Exception {
        ThreadedAllRequest request = new ThreadedAllRequest(folderPath, columns, sort, order, failOnError, true, categoryId);
        request.setLeftHandLimit(leftLimit);
        request.setRightHandLimit(rightLimit);
        ConversationResponse response = client.execute(request);
        lastResponse = response;
        return response.getConversations();
    }

    /**
     * Gets a mail from the server or null if it could not be found. This sets the last response field.
     */
    public TestMail get(String folder, String id) throws OXException, IOException, SAXException, JSONException {
        GetRequest request = new GetRequest(folder, id, failOnError);
        lastResponse = client.execute(request);
        if (lastResponse.hasError()) {
            return null;
        }
        return new TestMail((JSONObject) lastResponse.getData());
    }

    /**
     * Gets a mail from the server or null if it could not be found. This sets the last response field.
     */
    public TestMail get(String[] folderAndId) throws OXException, IOException, SAXException, JSONException {
        return get(folderAndId[0], folderAndId[1]);
    }

    /**
     * Copies a mail from its own folder to another. This method sets the lastResponse field to the result of the CopyRequest
     */
    public TestMail copy(TestMail original, String destination) throws OXException, IOException, SAXException, JSONException {
        CopyRequest request = new CopyRequest(original.getId(), original.getFolder(), destination);
        CopyResponse response = client.execute(request);
        String id = response.getID();
        TestMail copy = get(destination, id);
        cleaningSteps.add(new MailCleaner(copy, client));
        lastResponse = response;
        return copy;
    }

    /**
     * Archives a mail.
     */
    public void archive(TestMail original) throws OXException, IOException, JSONException {
        final ArchiveRequest archiveRequest = new ArchiveRequest(new String[] { original.getId() }, original.getFolder());
        archiveRequest.setCreateIfAbsent(true);
        archiveRequest.setUseDefaultName(true);
        client.execute(archiveRequest);
    }

    /**
     * Does not actually send an e-mail, just formats it like a forwarded e-mail. This mirrors the exact behavior of the forward request.
     * This method sets the lastResponse field to the result of the ForwardRequest.
     */
    public TestMail forwardButDoNotSend(TestMail forwardMe) throws OXException, IOException, SAXException, JSONException {
        ForwardRequest request = new ForwardRequest(forwardMe.getFolderAndId());
        ReplyResponse response = client.execute(request);
        TestMail forwarded = new TestMail((JSONObject) response.getData());
        lastResponse = response;
        return forwarded;
    }

    /**
     * Convenience method: Takes a mail that is supposed to be forwarded. Sends it, gets its ID, prepares that for forwarding. Does not send
     * the forwarded mail, though. This method sets the lastResponse field to the result of the ForwardRequest.
     */
    public TestMail forwardAndSendBefore(TestMail forwardMe) throws OXException, JSONException, IOException, SAXException {
        TestMail forwarded = send(forwardMe);
        TestMail mailFormattedForForwarding = forwardButDoNotSend(forwarded);

        cleaningSteps.add(new MailCleaner(forwarded, client));
        return mailFormattedForForwarding;
    }

    /**
     * Formats a given mail to look like a reply.
     */
    public TestMail replyAndDoNotSend(TestMail replyToMe) throws OXException, IOException, SAXException, JSONException {
        ReplyRequest request = new ReplyRequest(replyToMe.getFolderAndId());
        ReplyResponse response = client.execute(request);
        lastResponse = response;
        return new TestMail((JSONObject) response.getData());
    }

    public TestMail replyToAllAndDoNotSend(TestMail replyToMe) throws OXException, IOException, SAXException, JSONException {
        ReplyAllRequest request = new ReplyAllRequest(replyToMe.getFolderAndId());
        ReplyResponse response = client.execute(request);
        lastResponse = response;
        return new TestMail((JSONObject) response.getData());
    }

    /**
     * Deletes all mails that where created during this process.
     */
    public void cleanUp() {
        for (MailCleaner cleanup : cleaningSteps) {
            try {
                cleanup.cleanUp();
            } catch (Exception e) {
                System.out.println("Could not delete a mail allegedly created. Was probably deleted before.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the cleanup structure and replaces one mail to cleanup with another (e.g. the given one with a moved one)
     *
     * @param originalMail
     * @param modifiedMail
     */
    protected void updateForCleanup(TestMail originalMail, TestMail modifiedMail) {
        for (int i = 0, length = cleaningSteps.size(); i < length; i++) {
            MailCleaner step = cleaningSteps.get(i);
            if (originalMail.equals(step.getMail())) {
                step.setMail(modifiedMail);
            }
        }
    }

    /**
     * Returns all recipients of a mail, be they from to, cc, or bcc
     */
    protected Set<String> extractAllRecipients(TestMail mail) {
        Set<String> recipients = new HashSet<>();
        if (mail.getFrom() != null) {
            recipients.addAll(mail.getTo());
        }
        if (mail.getCc() != null) {
            recipients.addAll(mail.getCc());
        }
        if (mail.getBcc() != null) {
            recipients.addAll(mail.getBcc());
        }
        return recipients;
    }

    private void markCopyInInboxIfNecessary(TestMail mail) throws OXException, JSONException, IOException, SAXException {
        Set<String> allRecipients = extractAllRecipients(mail);
        String sender = client.getValues().getSendAddress();
        if (containsSomewhat(sender, allRecipients)) {
            LinkedList<String[]> similarMails = findSimilarMails(mail, client, client.getValues().getInboxFolder());
            for (String[] folderAndId : similarMails) {
                TestMail deleteMe = get(folderAndId[0], folderAndId[1]);
                cleaningSteps.add(new MailCleaner(deleteMe, client));
            }
        }
    }

    private boolean containsSomewhat(String needle, Collection<String> haystack) {
        for (String hay : haystack) {
            if (hay.contains(needle) || needle.contains(hay)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates a mail identified by folder and id with data in a TestMail. Returns the complete updated mail. Sets lastResponse to the
     * result of the updateRequest.
     */
    public TestMail update(String folder, String id, TestMail updates, boolean add) throws OXException, IOException, SAXException, JSONException {
        UpdateMailRequest request = new UpdateMailRequest(folder, id);
        if (updates.getColor() != -1) {
            request.setColor(updates.getColor());
        }
        if (updates.getFlags() != -1) {
            request.setFlags(updates.getFlags());
        }
        if(add) {
            request.doesUpdateFlags();
        } else {
            request.removeFlags();
        }
        UpdateMailResponse response = client.execute(request);
        TestMail result = get(folder, id);
        lastResponse = response;
        return result;
    }
}
