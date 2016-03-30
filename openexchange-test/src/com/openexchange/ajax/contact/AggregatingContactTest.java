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

package com.openexchange.ajax.contact;

import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.AggregatingContactTestManager;
import com.openexchange.test.FolderTestManager;


/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AggregatingContactTest extends AbstractAJAXSession{

    private FolderTestManager folderMgr;
    private AggregatingContactTestManager contactMgr;
    private Contact aggregator;
    private Contact contributor;
    private FolderObject folder;

    // FIXME: Reactivate me, when the update task is ready.

    public AggregatingContactTest(String name) throws Exception{
        super(name);
    }

    /*
    @Override
    public void setUp() throws Exception {
        super.setUp();

        folderMgr = new FolderTestManager(client);
        contactMgr = new AggregatingContactTestManager(client);
        contactMgr.setFailOnError(true);

        UserValues values = client.getValues();
        folder = folderMgr.generateFolder("aggregatedContactTest"+(new Date().getTime()), Module.CONTACTS.getFolderConstant(), values.getPrivateContactFolder(), values.getUserId());
        folderMgr.insertFolderOnServer(folder);

        aggregator = new Contact();
        contributor = new Contact();


        for(Contact c: new Contact[]{aggregator,contributor}){
            c.setParentFolderID(folder.getObjectID());
            c.setSurName("Mueller");
            c.setGivenName("Michael");
            c.setBirthday(new Date());
            c.setEmail1("m.mueller@host.invalid");
        }
        contributor.setEmail2("m@mueller.invalid");
        contactMgr.newAction(aggregator, contributor);
    }




    @Override
    protected void tearDown() throws Exception {
        contactMgr.cleanUp();
        folderMgr.cleanUp();
        super.tearDown();
    }


    public void testPerformTheUnknownGreenRedCycle() throws Exception{
        ContactUnificationState state;

        state = contactMgr.getAssociationBetween(contributor,aggregator);
        assertEquals(ContactUnificationState.UNDEFINED, state);

        contactMgr.associateTwoContacts(aggregator, contributor);
        state = contactMgr.getAssociationBetween(contributor,aggregator);
        assertEquals(ContactUnificationState.GREEN, state);

        contactMgr.separateTwoContacts(aggregator, contributor);
        state = contactMgr.getAssociationBetween(contributor,aggregator);
        assertEquals(ContactUnificationState.RED, state);
    }

    public void testFindAssociatedContacts() throws Exception, IOException, SAXException, JSONException{
        ContactUnificationState state = contactMgr.getAssociationBetween(contributor,aggregator);
        assertEquals(ContactUnificationState.UNDEFINED, state);
        List<UUID> associatedContacts = contactMgr.getAssociatedContacts(contributor);
        assertEquals("Should have no associated contacts", 0, associatedContacts.size());

        contactMgr.associateTwoContacts(aggregator, contributor);
        state = contactMgr.getAssociationBetween(contributor,aggregator);
        assertEquals(ContactUnificationState.GREEN, state);

        associatedContacts = contactMgr.getAssociatedContacts(contributor);
        assertEquals("Should have one associated contact", 1, associatedContacts.size());
        assertEquals("Should have the same UUID", aggregator.getUserField20(), associatedContacts.get(0).toString());

        contactMgr.separateTwoContacts(aggregator, contributor);
        state = contactMgr.getAssociationBetween(contributor,aggregator);
        assertEquals(ContactUnificationState.RED, state);

        associatedContacts = contactMgr.getAssociatedContacts(contributor);
        assertEquals("Should have no associated contacts", 0, associatedContacts.size());
    }

    public void testWorksWithUUIDsNotGivenByFinalContactModule() throws Exception {
    	//Karsten's case
    }

    public void testWorksWithMoreThanOneAssociatedPair() throws Exception {

    }

    public void testSecurity() throws Exception {
        contactMgr.associateTwoContacts(aggregator, contributor);
        UUID aggregatorUUID = UUID.fromString( contactMgr.getAction(aggregator).getUserField20());
        UUID contributorUUID = UUID.fromString( contactMgr.getAction(contributor).getUserField20());

        TimeZone tz = TimeZone.getDefault();

        AJAXClient client2 = new AJAXClient(User.User2);
        GetResponse getResponse = client2.execute(new GetContactByUIDRequest(aggregatorUUID, tz));
        OXException exception = getResponse.getException();
        assertNotNull("Should not be able to retrieve contact", exception);
        assertEquals("Should prohibit access", "CON-0104", exception.getErrorCode());

        getResponse = client2.execute(new GetContactByUIDRequest(contributorUUID, tz));
        exception = getResponse.getException();
        assertNotNull("Should not be able to retrieve other contact", getResponse.getException());
        assertEquals("Should prohibit access", "CON-0104", exception.getErrorCode());
    } */

    public void testDummy() {

    }
}
