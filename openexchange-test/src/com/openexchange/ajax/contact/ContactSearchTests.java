package com.openexchange.ajax.contact;

import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;

public class ContactSearchTests extends AbstractManagedContactTest {

    private static final String ALICE = "Alice";
    private static final String ALICE_MAIL1 = "alice@wonderland.invalid";
	private static final String BOB_LASTNAME = "Bob";
	private static final String BOB_DISPLAYNAME = "Carol19";
    private static final String BOB_MAIL2 = "bob@thebuilder.invalid";

	public ContactSearchTests(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		Contact c1 = generateContact();
		c1.setSurName(ALICE);
		c1.setEmail1(ALICE_MAIL1);
		Contact c2 = generateContact();
		c2.setSurName(BOB_LASTNAME);
		c2.setDisplayName(BOB_DISPLAYNAME);
		c2.setEmail2(BOB_MAIL2);

		manager.newAction(c1,c2);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}


	public void testSearchByInitial(){
		Contact[] results = manager.searchFirstletterAction("B", folderID);
		assertEquals(1, results.length);
		assertEquals("Should find the right contact", BOB_LASTNAME, results[0].getSurName());
	}


	public void testAsteriskSearch(){
		Contact[] results = manager.searchAction("*", folderID);
		assertEquals("Should find two contacts", 2, results.length);
	}

	public void testSearchWorksOnlyOnDisplayNameByDefault(){
		Contact[] results = manager.searchAction("*" + BOB_LASTNAME + "*", folderID);
		assertEquals("Should find no contact when searching for last name", 0, results.length);

		results = manager.searchAction("*" + BOB_DISPLAYNAME+ "*", folderID);
		assertEquals("Should find one contact when searching for display_name", 1, results.length);
	}


	public void testGuiLikeSearch(){
		ContactSearchObject search = new ContactSearchObject();
		search.setFolder(folderID);
		String b = BOB_LASTNAME;
		search.setGivenName(b);
		search.setSurname(b);
		search.setDisplayName(b);
		search.setEmail1(b);
		search.setEmail2(b);
		search.setEmail3(b);
		search.setCatgories(b);
		search.setOrSearch(true);
		Contact[] results= manager.searchAction(search);
		assertEquals("Should find one contact", 1, results.length);
		assertEquals("Should find the right contact", BOB_LASTNAME, results[0].getSurName());
	}

	public void testExactMatch() {
        ContactSearchObject search = new ContactSearchObject();
        search.setFolder(folderID);
        search.setOrSearch(true);
        search.setEmail1(ALICE_MAIL1);
        search.setEmail2(ALICE_MAIL1);
        search.setEmail3(ALICE_MAIL1);
        search.setExactMatch(true);
        Contact[] results = manager.searchAction(search);
        assertEquals("Should find one contact", 1, results.length);
        assertEquals("Should find the right contact", ALICE_MAIL1, results[0].getEmail1());
        search.setExactMatch(false);
        results = manager.searchAction(search);
        assertEquals("Should find one contact", 1, results.length);
        assertEquals("Should find the right contact", ALICE_MAIL1, results[0].getEmail1());

        String partialAddress = BOB_MAIL2.substring(0, BOB_MAIL2.lastIndexOf('.'));
        search.setEmail1(partialAddress);
        search.setEmail2(partialAddress);
        search.setEmail3(partialAddress);
        search.setExactMatch(true);
        results = manager.searchAction(search);
        assertEquals("Should find no contact", 0, results.length);
        search.setExactMatch(false);
        results = manager.searchAction(search);
        assertEquals("Should find one contact", 1, results.length);
        assertEquals("Should find the right contact", BOB_MAIL2, results[0].getEmail2());

	}

}
