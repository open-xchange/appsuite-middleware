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

package com.openexchange.ajax.publish.tests;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.test.ContactTestManager;
import com.openexchange.test.FolderTestManager;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractPubSubTest extends AbstractAJAXSession {

    private FolderTestManager folderMgr;

    private ContactTestManager contactMgr;

    private InfostoreTestManager infostoreMgr;

    public AbstractPubSubTest(String name) {
        super(name);
    }

    public void setFolderManager(FolderTestManager folderMgr) {
        this.folderMgr = folderMgr;
    }

    public FolderTestManager getFolderManager() {
        return folderMgr;
    }

    public void setContactManager(ContactTestManager contactMgr) {
        this.contactMgr = contactMgr;
    }

    public ContactTestManager getContactManager() {
        return contactMgr;
    }

    public void setInfostoreManager(InfostoreTestManager infostoreMgr) {
        this.infostoreMgr = infostoreMgr;
    }

    public InfostoreTestManager getInfostoreManager() {
        return infostoreMgr;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setFolderManager(new FolderTestManager(getClient()));
        setContactManager(new ContactTestManager(getClient()));
        setInfostoreManager(new InfostoreTestManager(getClient()));
    }

    @Override
    protected void tearDown() throws Exception {
        getContactManager().cleanUp();
        getInfostoreManager().cleanUp();
        getFolderManager().cleanUp();
        super.tearDown();
    }

    protected Contact generateContact(String firstname, String lastname) {
        Contact contact = new Contact();
        contact.setGivenName(firstname);
        contact.setSurName(lastname);
        contact.setEmail1(firstname + "." + lastname + "@ox-test.invalid");
        contact.setDisplayName(firstname + " " + lastname);
        contact.setPosition("Testee");
        contact.setTitle("Tester");
        contact.setCompany("Testing-Company");
        return contact;
    }

    protected PublicationTarget generateMicroformatTarget(DynamicFormDescription form, String type) {
        PublicationTarget target = new PublicationTarget();
        target.setFormDescription(form);
        target.setId("com.openexchange.publish.microformats." + type + ".online");
        return target;
    }

    protected DynamicFormDescription generateOXMFFormDescription() {
        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("siteName", "Site Name"));
        form.add(FormElement.checkbox("protected", "Protected"));
        form.add(FormElement.link("url", "URL"));
        return form;
    }

    protected Publication generatePublication(String type, String folder, SimPublicationTargetDiscoveryService discovery) {
        DynamicFormDescription form = generateOXMFFormDescription();
        PublicationTarget target = generateMicroformatTarget(form, type);

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("siteName", "publication-"+System.currentTimeMillis());
        config.put("protected", Boolean.valueOf(true));

        discovery.addTarget(target);

        Publication pub = new Publication();
        pub.setModule(type);
        pub.setEntityId(folder);
        pub.setTarget(target);
        pub.setConfiguration(config);
        return pub;
    }

    
//      This does not work anymore, since com.openexchange.publish.online.infostore.document will only allow one document at a time and no folders
//    
//    protected Publication generateInfostoreFolderPublication(String folder, SimPublicationTargetDiscoveryService discovery) {
//        DynamicFormDescription form = generateOXMFFormDescription();
//
//        PublicationTarget target = new PublicationTarget();
//        target.setFormDescription(form);
//        target.setId("com.openexchange.publish.online.infostore.document");
//
//        Map<String, Object> config = new HashMap<String, Object>();
//        config.put("siteName", "publication-"+System.currentTimeMillis());
//        config.put("protected", Boolean.valueOf(true));
//
//        discovery.addTarget(target);
//
//        Publication pub = new Publication();
//        pub.setModule("infostore/object");
//        pub.setEntityId(folder);
//        pub.setTarget(target);
//        pub.setConfiguration(config);
//        return pub;
//    }

    protected Publication generateInfostoreItemPublication(String objId, SimPublicationTargetDiscoveryService discovery) {
        DynamicFormDescription form = generateOXMFFormDescription();

        PublicationTarget target = new PublicationTarget();
        target.setFormDescription(form);
        target.setId("com.openexchange.publish.online.infostore.document");

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("siteName", "publication-"+System.currentTimeMillis());
        config.put("protected", Boolean.valueOf(true));

        discovery.addTarget(target);

        Publication pub = new Publication();
        pub.setModule("infostore/object");
        pub.setEntityId(objId);
        pub.setTarget(target);
        pub.setConfiguration(config);
        return pub;
    }

    protected Subscription generateOXMFSubscription(DynamicFormDescription formDescription, String folderID) throws OXException, IOException, SAXException, JSONException {
        Subscription sub = generateOXMFSubscription(formDescription);
        sub.setFolderId(folderID);
        return sub;
    }

    protected Subscription generateOXMFSubscription(DynamicFormDescription formDescription) throws OXException, IOException, SAXException, JSONException {
        Subscription subscription = new Subscription();

        subscription.setDisplayName("mySubscription");

        SubscriptionSource source = new SubscriptionSource();
        source.setId("com.openexchange.subscribe.microformats.contacts.http");
        source.setFormDescription(formDescription);
        subscription.setSource(source);

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("url", "http://ox.open-xchange.com/1");
        subscription.setConfiguration(config);

        return subscription;
    }

    protected FolderObject createDefaultContactFolder() throws OXException, IOException, SAXException, JSONException {
        FolderObject folder = getFolderManager().generatePublicFolder(
            "pubsub default contact folder "+System.currentTimeMillis(),
            FolderObject.CONTACT,
            getClient().getValues().getPrivateContactFolder(),
            getClient().getValues().getUserId());
        getFolderManager().insertFolderOnServer(folder);
        return folder;
    }

    protected FolderObject createDefaultInfostoreFolder() throws OXException, IOException, SAXException, JSONException {
        return createDefaultInfostoreFolder(null);
    }

    protected FolderObject createDefaultInfostoreFolder(String folderName) throws OXException, IOException, SAXException, JSONException {
    	if (folderName == null) {
    		folderName = "pubsub default infostore folder "+getName()+"-"+System.currentTimeMillis();
    	}
    	FolderObject folder = getFolderManager().generatePublicFolder(
                folderName,
                FolderObject.INFOSTORE,
                getClient().getValues().getPrivateInfostoreFolder(),
                getClient().getValues().getUserId());
            getFolderManager().insertFolderOnServer(folder);
            return folder;
    }

    protected Contact createDefaultContactFolderWithOneContact() throws OXException, IOException, SAXException, JSONException {
        FolderObject folder = createDefaultContactFolder();

        Contact contact = generateContact("Herbert", "Meier");
        contact.setParentFolderID(folder.getObjectID());
        getContactManager().newAction(contact);
        return contact;
    }

    private WebResponse getResponse(String url) throws IOException {
        WebConversation conv = new WebConversation();
        String correctedUrl = url;
        if (!correctedUrl.startsWith("http")) {
            correctedUrl = "http://" + url;
        }
        GetMethodWebRequest req = new GetMethodWebRequest(correctedUrl);
        return conv.getResource(req);
    }

    public String getWebsite(String url) throws IOException {

        WebResponse resp = getResponse(url);
        assertEquals("Should respond with status 200", 200 , resp.getResponseCode());
        return resp .getText();
    }

    public InputStream getDownload(String url) throws IOException {
        InputStream in = null;

        try {
            in = getResponse(url).getInputStream();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return in;
    }
}
