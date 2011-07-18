package com.openexchange.ajax.contact;

import java.util.TimeZone;

import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.UpdateRequest;
import com.openexchange.ajax.contact.action.UpdateResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

public class Bug19543Test_DeletingContactsInDistributionList extends
		AbstractManagedContactTest {


	private Contact c1,c2,c3,c4;

	public Bug19543Test_DeletingContactsInDistributionList(String name) {
		super(name);
	}

	public void testReproduceItTheBiggelebenWay() throws Exception {
		int sleep = 0;

		//create
		Contact distributionList = makeDistro(-1);
		InsertResponse insertResponse = getClient().execute(new InsertRequest(distributionList),sleep);
		int objId = insertResponse.getId();
		
		//add members
		int type = DistributionListEntryObject.INDEPENDENT;
		DistributionListEntryObject[] members = new DistributionListEntryObject[]{
				new DistributionListEntryObject("Displayname 1", "user1@oxample.invalid", type),
				new DistributionListEntryObject("Displayname 2", "user2@oxample.invalid", type),
				new DistributionListEntryObject("Displayname 3", "user3@oxample.invalid", type),
				new DistributionListEntryObject("Displayname 4", "user4@oxample.invalid", type)
		};
		Contact addMemberUpdate = makeDistro(objId);
		addMemberUpdate.setDistributionList(members);
		addMemberUpdate.setLastModified(insertResponse.getTimestamp());
		UpdateResponse addMemberResponse = getClient().execute(new UpdateRequest(addMemberUpdate),sleep);
		
		//remove members
		Contact removeMemberUpdate = makeDistro(objId); 
		removeMemberUpdate.setDistributionList(new DistributionListEntryObject[]{});
		removeMemberUpdate.setLastModified(addMemberResponse.getTimestamp());
		getClient().execute(new UpdateRequest(removeMemberUpdate),sleep);

		//check
		Contact actual = getClient().execute(new GetRequest(folderID, objId, TimeZone.getDefault()),sleep).getContact();
		assertEquals(0,actual.getNumberOfDistributionLists());
		assertNull(actual.getDistributionList());
	}
	

	protected Contact makeDistro(int objectId){
		Contact distro = new Contact();
		distro.setDisplayName("Distribution list for Bug 19543");
		distro.setSurName("Distribution list for Bug 19543");
		distro.setDistributionList(new DistributionListEntryObject[]{});
		distro.setLabel(0);
		distro.setParentFolderID(folderID);
		if(objectId != -1){
			distro.setObjectID(objectId);
		}
		return distro;
	}

}
