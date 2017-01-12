
package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;

public class YomiContactSearchTests extends AbstractManagedContactTest {

    private static final String YOMI_LAST_NAME = "\u4f50\u85e4";
    protected static final String YOMI_FIRST_NAME = "\u660e\u65e5\u9999";
    private Contact contact;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        contact = new Contact();
        contact.setTitle("Herr");
        contact.setGivenName("Baab");
        contact.setSurName("Abba");
        contact.setDisplayName("Baab Abba");
        contact.setStreetBusiness("Franz-Meier Weg 17");
        contact.setCityBusiness("Test Stadt");
        contact.setStateBusiness("NRW");
        contact.setCountryBusiness("Deutschland");
        contact.setTelephoneBusiness1("+49112233445566");
        contact.setCompany("Internal Test AG");
        contact.setEmail1("baab.abba@open-foobar.com");
        contact.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        contact.setYomiFirstName(YOMI_FIRST_NAME);
        contact.setYomiLastName(YOMI_LAST_NAME);
        contact.setParentFolderID(folderID);
        cotm.newAction(contact);
    }

    /**
     * This is how the GUI does a search
     */
    @Test
    public void testFindWithContactSearchObject() {
        ContactSearchObject search = new ContactSearchObject();
        search.setFolder(folderID);
        String b = YOMI_LAST_NAME;
        search.setGivenName(b);
        search.setSurname(b);
        search.setDisplayName(b);
        search.setEmail1(b);
        search.setEmail2(b);
        search.setEmail3(b);
        search.setCatgories(b);
        search.setYomiFirstname(b);
        search.setYomiLastName(b);
        search.setOrSearch(true);
        Contact[] results = cotm.searchAction(search);

        assertEquals("Should find one contact", 1, results.length);
        assertEquals("Should find the right contact", YOMI_LAST_NAME, results[0].getYomiLastName());

    }

}
