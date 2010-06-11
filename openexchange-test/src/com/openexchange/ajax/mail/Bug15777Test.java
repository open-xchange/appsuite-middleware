
package com.openexchange.ajax.mail;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.ajax.mail.actions.MoveMailRequest;
import com.openexchange.ajax.mail.actions.UpdateMailResponse;
import com.openexchange.groupware.container.FolderObject;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug15777Test extends AbstractAJAXSession {
    
    private AJAXClient client;

    private String folder;

    private String address;

    private String[][] ids;
    
    private UserValues values;
    
    FolderObject subFolder;

    public Bug15777Test(final String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        values = client.getValues();
        folder = values.getInboxFolder();
        address = values.getSendAddress();
        final String testmail = TestMails.replaceAddresses(TestMails.DDDTDL_MAIL, address);
        final byte[] buf = testmail.getBytes();
        final ByteArrayInputStream mail = new ByteArrayInputStream(buf);
        final ImportMailRequest request = new ImportMailRequest(folder, 33, mail);
        final ImportMailResponse response = client.execute(request);
        ids = response.getIds();
    }
    
    public void testFlagsAfterMove() throws Exception {
        // Create new mail folder        
        subFolder = Create.createPrivateFolder("bug15777movefolder", FolderObject.MAIL, values.getUserId());
        subFolder.setFullName(folder + "/bug15777movefolder");

        final InsertRequest subFolderReq = new InsertRequest(API.OX_NEW, subFolder, false);
        client.execute(subFolderReq);
        subFolder.setLastModified(new Date(0));
        
        // Move the mail
        final MoveMailRequest moveMailReq = new MoveMailRequest(folder, subFolder.getFullName(), ids[0][1]);
        UpdateMailResponse moveMailResp = client.execute(moveMailReq);
        
        // Get moved Mail
        final GetRequest getMovedMailReq = new GetRequest(subFolder.getFullName(), moveMailResp.getID());
        final GetResponse getMovedMailResp = client.execute(getMovedMailReq);
        
        // Assert flags
        assertTrue("Flag 'answered' is missing", MailFlag.transform(getMovedMailResp.getMail(TimeZone.getDefault()).getFlags()).contains(MailFlag.ANSWERED));
    }
    
    @Override
    protected void tearDown() throws Exception {
        // Delete Mail
        final DeleteRequest del = new DeleteRequest(ids);
        client.execute(del);
        
        // Delete MailFolder
        final com.openexchange.ajax.folder.actions.DeleteRequest fDel = new com.openexchange.ajax.folder.actions.DeleteRequest(API.OX_NEW, subFolder);
        client.execute(fDel);
        
        super.tearDown();
    }

//    public void testReplyFlagAfterFolderMove() throws Exception {
//        final AJAXClient client1 = new AJAXClient(User.User1);
//        final AJAXClient client2 = new AJAXClient(User.User2);
//        final String mail1 = client1.getValues().getSendAddress();
//        final String mail2 = client2.getValues().getSendAddress();
//        
//        // Send Mail to reply
//        client = client2;
//        final JSONObject origMail = createEMail(mail1, "Reply test", MailContentType.ALTERNATIVE.toString(), MAIL_TEXT_BODY);
//        final String origId = sendMail(origMail.toString())[0];
//        
//        // Create reply mail
//        client = client1;
//        final TestMail replyMail = new TestMail(getFirstMailInFolder(getInboxFolder()));
//        replyMail.setTo(Arrays.asList(new String[] {mail2}));
//        replyMail.setFrom(Arrays.asList(new String[] {mail1}));
//        replyMail.setSubject("Re: Reply test");
//        final JSONObject replyObj = replyMail.toJSON();
//        replyObj.put(MailJSONField.MSGREF.getKey(), origId);
//        TestMail replyMail = new TestMail(replyObj);
//        sendMail(replyObj.toString());
//
//         AJAXClient client1 = new AJAXClient(User.User1);
//         AJAXClient client2 = new AJAXClient(User.User2);
//         String mail1 = client1.getValues().getSendAddress(); // note: doesn't work the other way around on the dev system, because only
//         the
//         String mail2 = client2.getValues().getSendAddress(); // first account is set up correctly.
//        
//         this.client = client2;
//         JSONObject mySentMail = createEMail(mail1, "Reply test", MailContentType.ALTERNATIVE.toString(), MAIL_TEXT_BODY);
//         sendMail(mySentMail.toString());
//        
//         this.client = client1;
//         MailTestManager manager = new MailTestManager(client, false);
//         TestMail receivedMail = new TestMail(getFirstMailInFolder(getInboxFolder()));
//         TestMail updateMail = new TestMail();
//         updateMail.setBody(receivedMail.getBody());
//         updateMail.setFlags(MailFlag.ANSWERED.getValue());
//         updateMail.setContentType(receivedMail.getContentType());
//         updateMail.setFolderAndID(receivedMail.getFolderAndId());
//         updateMail.setFrom(receivedMail.getFrom());
//         updateMail.setTo(receivedMail.getTo());
//         updateMail.setSubject(receivedMail.getSubject());
//         JSONObject j = updateMail.toJSON();
//         j.put(MailJSONField.);
//         manager.update(receivedMail.getFolder(), receivedMail.getId(), updateMail, false);
//        
//         TestMail changedMail = new TestMail(getFirstMailInFolder(getInboxFolder()));
//         System.out.println(changedMail.getFlags());
//         TestMail answerMail = manager.replyAndDoNotSend(receivedMail);
//         TestMail answeredMail = new TestMail(getFirstMailInFolder(getInboxFolder()));
//                
//         Set<MailFlag> mf = answeredMail.getFlagsAsSet();
//         JSONObject myReceivedMail = getFirstMailInFolder(getInboxFolder());
//         getReplyEMail(new TestMail(myReceivedMail));
//                
//                
//                
//         JSONObject m = getFirstMailInFolder(client.getValues().getInboxFolder());
//         TestMail tm = new TestMail(m);
//                
//         ReplyRequest repReq = new ReplyRequest(tm.getFolderAndId());
//         ReplyResponse repResp = client.execute(repReq);
//                
//         GetRequest getReq = new GetRequest(tm.getFolder(), tm.getId());
//         GetResponse getResp = client.execute(getReq);
//         TestMail t = new TestMail((JSONObject) getResp.getData());
//                
//         Set<MailFlag> s = t.getFlagsAsSet();
//         System.out.println(s.toString());
//    }
}
