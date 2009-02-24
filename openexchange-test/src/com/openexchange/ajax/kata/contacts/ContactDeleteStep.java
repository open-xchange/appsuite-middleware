package com.openexchange.ajax.kata.contacts;

import org.junit.Assert;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.kata.NeedExistingStep;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.test.ContactTestManager;


public class ContactDeleteStep extends NeedExistingStep<ContactObject> {
	
	private ContactObject entry;

    public ContactDeleteStep(ContactObject entry, String name, String expectedError) {
        super(name, expectedError);
        this.entry = entry;
    }

    public void cleanUp() throws Exception {
    }

    public void perform(AJAXClient client) throws Exception {
        assumeIdentity(entry);
        ContactTestManager manager = new ContactTestManager(client);
        Assert.assertNotNull("Should have found contact before deletion" , manager.getContactFromServer(this.entry , false) );        
        manager.deleteContactOnServer(this.entry, false);
        Assert.assertNull("Should not have found contact after deletion" , manager.getContactFromServer(this.entry , false) );
        forgetIdentity(entry);
    }


}
