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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.test.ContactTestManager;
import com.openexchange.test.FolderTestManager;
import com.openexchange.tools.servlet.AjaxException;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractPubSubTest extends AbstractAJAXSession {

    private FolderTestManager folderMgr;

    private ContactTestManager contactMgr;

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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setFolderManager(new FolderTestManager(getClient()));
        setContactManager(new ContactTestManager(getClient()));
    }

    @Override
    protected void tearDown() throws Exception {
        getContactManager().cleanUp();
        getFolderManager().cleanUp();
        super.tearDown();
    }

    protected FolderObject generateFolder(String name, int moduleType) throws AjaxException, IOException, SAXException, JSONException {
        // create a folder
        FolderObject folderObject1 = new FolderObject();
        folderObject1.setFolderName(name);
        folderObject1.setType(FolderObject.PUBLIC);
        folderObject1.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        folderObject1.setModule(moduleType);
        // create permissions
        final OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(getClient().getValues().getUserId());
        perm1.setGroupPermission(false);
        perm1.setFolderAdmin(true);
        perm1.setAllPermission(
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
        folderObject1.setPermissionsAsArray(new OCLPermission[] { perm1 });
        return folderObject1;
    }

    protected Contact generateContact(String firstname, String lastname) {
        Contact contact = new Contact();
        contact.setGivenName(firstname);
        contact.setSurName(lastname);
        contact.setEmail1(firstname + "." + lastname + "@ox-test.invalid");
        contact.setDisplayName(firstname + " " + lastname);
        contact.setPosition("Testee");
        contact.setTitle("Tester");
        return contact;
    }

    protected PublicationTarget generateTarget(DynamicFormDescription form, String type) {
        PublicationTarget target = new PublicationTarget();
        target.setFormDescription(form);
        target.setId("com.openexchange.publish.microformats." + type + ".online");
        return target;
    }

    protected Publication generatePublication(String type, String folder) {
        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();
        return generatePublication(type, folder, discovery);
    }

    protected DynamicFormDescription generateOXMFFormDescription() {
        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("siteName", "Site Name")).add(FormElement.checkbox("protected", "Protected"));
        return form;
    }

    protected Publication generatePublication(String type, String folder, SimPublicationTargetDiscoveryService discovery) {
        DynamicFormDescription form = generateOXMFFormDescription();
        PublicationTarget target = generateTarget(form, type);

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("siteName", "publication");
        config.put("protected", Boolean.valueOf(true));

        discovery.addTarget(target);

        Publication pub = new Publication();
        pub.setModule(type);
        pub.setEntityId(folder);
        pub.setTarget(target);
        pub.setConfiguration(config);
        return pub;
    }

    protected Subscription generateOXMFSubscription(DynamicFormDescription formDescription, String folderID) throws AjaxException, IOException, SAXException, JSONException {
        Subscription sub = generateOXMFSubscription(formDescription);
        sub.setFolderId(folderID);
        return sub;
    }

    protected Subscription generateOXMFSubscription(DynamicFormDescription formDescription) throws AjaxException, IOException, SAXException, JSONException {
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

    protected FolderObject createDefaultContactFolder() throws AjaxException, IOException, SAXException, JSONException {
        FolderObject folder = generateFolder("publishedContacts", FolderObject.CONTACT);
        getFolderManager().insertFolderOnServer(folder);
        return folder;
    }

    protected Contact createDefaultContactFolderWithOneContact() throws AjaxException, IOException, SAXException, JSONException {
        FolderObject folder = createDefaultContactFolder();

        Contact contact = generateContact("Herbert", "Meier");
        contact.setParentFolderID(folder.getObjectID());
        getContactManager().insertContactOnServer(contact);
        return contact;
    }

}
