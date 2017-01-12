
package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

public class Bug19543Test_DeletingContactsInDistributionList extends AbstractManagedContactTest {

    private static final int MAX_ATTEMPTS = 5;

    public Bug19543Test_DeletingContactsInDistributionList(String name) {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cotm.setSleep(0);
    }

    @After
    public void tearDown() throws Exception {
        try {
            cotm.setSleep(500);
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testWithExternalContacts() throws Exception {
        int type = DistributionListEntryObject.INDEPENDENT;
        DistributionListEntryObject[] members = new DistributionListEntryObject[] { new DistributionListEntryObject("Displayname 1", "user1@oxample.invalid", type), new DistributionListEntryObject("Displayname 2", "user2@oxample.invalid", type), new DistributionListEntryObject("Displayname 3", "user3@oxample.invalid", type), new DistributionListEntryObject("Displayname 4", "user4@oxample.invalid", type)
        };
        runTests(members);
    }

    @Test
    public void testWithInternalContacts() throws Exception {
        String email1 = "abel@oxample.invalid";
        Contact c1 = generateContact("Abel");
        c1.setEmail1(email1);
        cotm.newAction(c1);

        String email2 = "baker@oxample.invalid";
        Contact c2 = generateContact("Baker");
        c2.setEmail1(email2);
        cotm.newAction(c2);

        int type = DistributionListEntryObject.EMAILFIELD1;

        DistributionListEntryObject entry1 = new DistributionListEntryObject("Displayname 1", "abel@oxample.invalid", type);
        entry1.setEntryID(c1.getObjectID());
        entry1.setFolderID(folderID);

        DistributionListEntryObject entry2 = new DistributionListEntryObject("Displayname 2", "baker2@oxample.invalid", type);
        entry2.setEntryID(c2.getObjectID());
        entry2.setFolderID(folderID);

        DistributionListEntryObject[] members = new DistributionListEntryObject[] { entry1, entry2 };
        runTests(members);
    }

    public void runTests(DistributionListEntryObject[] members) throws Exception {
        int sleep = 0;
        int expectedSize = members.length;

        //create
        Contact distributionList = makeDistro(-1);
        InsertResponse insertResponse = getClient().execute(new InsertRequest(distributionList), sleep);
        int objId = insertResponse.getId();

        Date timeStamp = insertResponse.getTimestamp();
        int listErrors = 0, allErrors = 0, updatesErrors = 0, getFullErrors = 0, getEmptyErrors = 0;

        for (int attempts = 0; attempts < MAX_ATTEMPTS; attempts++) {
            //add members
            Contact addMemberUpdate = makeDistro(objId);
            addMemberUpdate.setDistributionList(members);
            addMemberUpdate.setLastModified(timeStamp);
            cotm.updateAction(addMemberUpdate);

            timeStamp = cotm.getLastResponse().getTimestamp();

            Date updatesTimeStamp = new Date(timeStamp.getTime() - 1);

            //list, all, updates are performed before the get
            int actualSize;

            actualSize = cotm.listAction(new int[] { folderID, objId })[0].getNumberOfDistributionLists();
            assertEquals("[list] Attempt #" + attempts + " failed", expectedSize, actualSize);
            //			if(actualSize != expectedSize) listErrors++;

            actualSize = cotm.allAction(folderID, Contact.ALL_COLUMNS)[0].getNumberOfDistributionLists();
            assertEquals("[all] Attempt #" + attempts + " failed", expectedSize, actualSize);
            //			if(actualSize != expectedSize) allErrors++;

            actualSize = cotm.updatesAction(folderID, updatesTimeStamp)[0].getNumberOfDistributionLists();
            assertEquals("[updates] Attempt #" + attempts + " failed", expectedSize, actualSize);
            //			if(actualSize != expectedSize) updatesErrors++;

            // get for editing
            Contact actual = cotm.getAction(folderID, objId);
            actualSize = actual.getNumberOfDistributionLists();
            assertEquals("[get] Attempt #" + attempts + " failed", expectedSize, actualSize);
            //			if(actual.getNumberOfDistributionLists() != expectedSize) getFullErrors++;

            //remove members
            Contact removeMemberUpdate = makeDistro(objId);
            removeMemberUpdate.setDistributionList(new DistributionListEntryObject[] {});
            removeMemberUpdate.setLastModified(timeStamp);
            cotm.updateAction(removeMemberUpdate);
            timeStamp = cotm.getLastResponse().getTimestamp();

            //check
            actual = cotm.getAction(folderID, objId);
            assertEquals("[get] Attempt #" + attempts + " failed", 0, actual.getNumberOfDistributionLists());
            //			if(actual.getNumberOfDistributionLists() != expectedSize) getEmptyErrors++;
        }
        if (allErrors + updatesErrors + listErrors + getEmptyErrors + getFullErrors > 0) {
            fail("Errors during the following requests :" + "\nall: " + allErrors + ", updates: " + updatesErrors + ", list: " + listErrors + ", get (before deletion): " + getFullErrors + ", get (after deletion): " + getEmptyErrors + ", during " + MAX_ATTEMPTS + " attempts");
        }
    }

    protected Contact makeDistro(int objectId) {
        Contact distro = new Contact();
        distro.setDisplayName("Distribution list for Bug 19543");
        distro.setSurName("Distribution list for Bug 19543");
        distro.setDistributionList(new DistributionListEntryObject[] {});
        distro.setLabel(0);
        distro.setParentFolderID(folderID);
        if (objectId != -1) {
            distro.setObjectID(objectId);
        }
        return distro;
    }

}
