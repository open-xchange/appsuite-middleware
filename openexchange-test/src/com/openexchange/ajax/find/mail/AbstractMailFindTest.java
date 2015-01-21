package com.openexchange.ajax.find.mail;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.exception.OXException;
import com.openexchange.find.Module;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.container.Contact;


public abstract class AbstractMailFindTest extends AbstractFindTest {

    protected String defaultAddress;

    public AbstractMailFindTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        defaultAddress = client.getValues().getSendAddress();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    protected List<ActiveFacet> prepareFacets() throws OXException, IOException, JSONException {
        return prepareFacets(client.getValues().getInboxFolder());
    }
    
    protected List<ActiveFacet> prepareFacets(String folder) {
        List<ActiveFacet> facets = new LinkedList<ActiveFacet>();
        facets.add(createActiveFacet(CommonFacetType.FOLDER, folder, Filter.NO_FILTER));
        return facets;
    }

    protected List<Facet> autocomplete(String prefix) throws Exception {
        return autocomplete(Module.MAIL, prefix, prepareFacets());
    }

    protected List<Facet> autocomplete(String prefix, List<ActiveFacet> facets) throws Exception {
        return autocomplete(Module.MAIL, prefix, facets);
    }

    protected List<PropDocument> query(List<ActiveFacet> facets, int start, int size) throws Exception {
        return query(Module.MAIL, facets, start, size);
    }

    protected List<PropDocument> query(List<ActiveFacet> facets) throws Exception {
        return query(Module.MAIL, facets);
    }

    protected List<PropDocument> query(List<ActiveFacet> facets, Map<String, String> options) throws Exception {
        return query(Module.MAIL, facets, options);
    }


    protected List<PropDocument> query(List<ActiveFacet> facets, int[] columns) throws Exception {
        return query(Module.MAIL, facets, columns);
    }

    protected FacetValue detectContact(List<Facet> facets) throws OXException, IOException, JSONException {
        GetRequest getRequest = new GetRequest(client.getValues().getUserId(), client.getValues().getTimeZone());
        GetResponse getResponse = client.execute(getRequest);
        Contact contact = getResponse.getContact();
        FacetValue found = findByDisplayName(facets, DisplayItems.convert(contact).getDisplayName());
        return found;
    }

    protected Contact randomContact(String givenName, int folderId) {
        Contact contact = new Contact();
        contact.setParentFolderID(folderId);
        contact.setSurName(randomUID());
        contact.setGivenName(givenName);
        contact.setDisplayName(contact.getGivenName() + " " + contact.getSurName());
        contact.setEmail1(randomUID() + "@example.com");
        contact.setUid(randomUID());
        return contact;
    }

    protected Contact specificContact(String givenName, String surName, String mailAddress, int folderId) {
        Contact contact = new Contact();
        contact.setParentFolderID(folderId);
        if (surName != null) {
            contact.setSurName(surName);
        }

        if (givenName != null) {
            contact.setGivenName(givenName);
        }

        contact.setEmail1(mailAddress);
        contact.setUid(randomUID());
        return contact;
    }

}
