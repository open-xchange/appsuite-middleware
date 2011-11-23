
package com.openexchange.ajax.mail;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
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

        final InsertRequest subFolderReq = new InsertRequest(EnumAPI.OX_NEW, subFolder, false);
        client.execute(subFolderReq);
        subFolder.setLastModified(new Date(0));

        // Move the mail
        final MoveMailRequest moveMailReq = new MoveMailRequest(folder, subFolder.getFullName(), ids[0][1]);
        final UpdateMailResponse moveMailResp = client.execute(moveMailReq);

        // Get moved Mail
        final GetRequest getMovedMailReq = new GetRequest(subFolder.getFullName(), moveMailResp.getID());
        final GetResponse getMovedMailResp = client.execute(getMovedMailReq);

        // Assert flags
        assertTrue("Flag 'answered' is missing", MailFlag.transform(getMovedMailResp.getMail(TimeZone.getDefault()).getFlags()).contains(
            MailFlag.ANSWERED));
    }

    @Override
    protected void tearDown() throws Exception {
        // Delete Mail
        final DeleteRequest del = new DeleteRequest(ids);
        client.execute(del);

        // Delete MailFolder
        final com.openexchange.ajax.folder.actions.DeleteRequest fDel = new com.openexchange.ajax.folder.actions.DeleteRequest(
            EnumAPI.OX_NEW,
            subFolder);
        client.execute(fDel);

        super.tearDown();
    }
}
