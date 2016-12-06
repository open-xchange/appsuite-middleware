
package com.openexchange.ajax.find.mail;

import static org.junit.Assert.assertFalse;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.UUIDs;

public class Bug36522Test extends AbstractMailFindTest {

    private Contact contact;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        contact = randomContact(UUIDs.getUnformattedStringFromRandom(), getClient().getValues().getPrivateContactFolder());
        /*
         * The empty string was formerly returned as query of the contacts filter.
         * This leads to a match-all behavior on certain mail backends
         */
        contact.setEmail2(" ");
        contact = contactManager.newAction(contact);
    }

    @Test
    public void testDontReturnEmptyFilterQueries() throws Exception {
        List<ActiveFacet> activeFacets = prepareFacets("default0/INBOX");
        List<Facet> facets = autocomplete(contact.getGivenName(), activeFacets);
        FacetValue contactValue = detectContact(contact, facets);
        List<String> queries = contactValue.getOptions().get(0).getFilter().getQueries();
        assertFalse("filter queries must not contain empty values", queries.contains(" "));
    }

}
