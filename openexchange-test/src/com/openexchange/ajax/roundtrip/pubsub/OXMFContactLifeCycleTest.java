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

package com.openexchange.ajax.roundtrip.pubsub;

import com.openexchange.ajax.publish.tests.PublicationTestManager;
import com.openexchange.ajax.subscribe.test.SubscriptionTestManager;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.publish.Publication;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.test.ContactTestManager;
import com.openexchange.test.FolderTestManager;


/**
 * This is a roundtrip test, doing Create-(verify)-update-(verify)-delete-(verify)
 * for a publication and subscription of OXMF.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class OXMFContactLifeCycleTest extends AbstractPubSubRoundtripTest {

    private ContactTestManager cMgr;
    private FolderTestManager fMgr;
    private FolderObject pubFolder;
    private FolderObject subFolder;

    public OXMFContactLifeCycleTest(String name) {
        super(name);
    }
    
    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        this.cMgr = getContactManager();
        this.fMgr = getFolderManager();

        //setup folders
        this.pubFolder = fMgr.generateFolder("publishRoundtripTest", FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        this.subFolder = fMgr.generateFolder("subscribeRoundtripTest", FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        fMgr.insertFolderOnServer(pubFolder);
        fMgr.insertFolderOnServer(subFolder);
    }

    @Override
    protected void tearDown() throws Exception {
        cMgr.cleanUp();
        fMgr.cleanUp();
        super.tearDown();
        
    }



    //disabled, because it takes more than 1 minute to run. Should be in smoke test suite
    public void doNot_testShouldNotLoseContactsWhileRoundtripping() throws Exception{        
        //setup contact
        Contact contact1 = generateContact("Herbert", "Meier");
        contact1.setParentFolderID(pubFolder.getObjectID());
        cMgr.newAction(contact1);
        
        //prepare pubsub
        PublicationTestManager pubMgr = getPublishManager();
        SubscriptionTestManager subMgr = getSubscribeManager();
        SimPublicationTargetDiscoveryService pubDiscovery = new SimPublicationTargetDiscoveryService();
        pubMgr.setPublicationTargetDiscoveryService(pubDiscovery);
        Publication publication = generatePublication("contacts", String.valueOf(pubFolder.getObjectID()), pubDiscovery);
                
        Contact[] contacts;
        
        //create publication        
        pubMgr.newAction(publication);

        //create subscription for that url
        DynamicFormDescription formDescription = publication.getTarget().getFormDescription();
        formDescription.add(FormElement.input("url", "URL"));
        Subscription subscription = generateOXMFSubscription(formDescription);
        subscription.setFolderId(subFolder.getObjectID());
        subMgr.setFormDescription(formDescription);
        String pubUrl = (String) publication.getConfiguration().get("url");
        subscription.getConfiguration().put("url", pubUrl);
        
        subMgr.newAction(subscription);
        
        //refresh and check subscription
        subMgr.refreshAction(subscription.getId());
        contacts = cMgr.allAction(subFolder.getObjectID());
        assertEquals("Should only contain one contact after first publication", 1, contacts.length);
        
        //publish another contact
        Contact contact2 = generateContact("Hubert", "Meier");
        contact2.setParentFolderID(pubFolder.getObjectID());
        cMgr.newAction(contact2);
        
        //bypass the 30 sec caching logic
        Thread.sleep(31*1000); 
        
        //refresh and check subscription again
        subMgr.refreshAction(subscription.getId());
        contacts = cMgr.allAction(subFolder.getObjectID());
        assertEquals("Should have two contacts after update", 2, contacts.length);
        
    }

    /**
     * Does a publish, then a subscribe and then checks whether basic data (mostly name 
     * and title)  survived the whole process. 
     */
    public void testContactTrippingWithCensoredDataSet() throws Exception{        
        //setup contact
        Contact contact1 = generateContact("Herbert", "Meier");
        contact1.setParentFolderID(pubFolder.getObjectID());
        cMgr.newAction(contact1);
        
        //prepare pubsub
        PublicationTestManager pubMgr = getPublishManager();
        SubscriptionTestManager subMgr = getSubscribeManager();
        SimPublicationTargetDiscoveryService pubDiscovery = new SimPublicationTargetDiscoveryService();
        pubMgr.setPublicationTargetDiscoveryService(pubDiscovery);
        Publication publication = generatePublication("contacts", String.valueOf(pubFolder.getObjectID()), pubDiscovery);
                
        Contact[] contacts;
        
        //create publication        
        pubMgr.newAction(publication);

        //create subscription for that url
        DynamicFormDescription formDescription = publication.getTarget().getFormDescription();
        formDescription.add(FormElement.input("url", "URL"));
        Subscription subscription = generateOXMFSubscription(formDescription);
        subscription.setFolderId(subFolder.getObjectID());
        subMgr.setFormDescription(formDescription);
        String pubUrl = (String) publication.getConfiguration().get("url");
        subscription.getConfiguration().put("url", pubUrl);
        
        subMgr.newAction(subscription);
        
        //refresh and check subscription
        subMgr.refreshAction(subscription.getId());
        contacts = cMgr.allAction(subFolder.getObjectID());
        assertEquals("Should only contain one contact after first publication", 1, contacts.length);
        assertNoDataMessedUpMinimumRequirements(contact1,contacts[0]);        
    }

    /**
     *  Does a publish, then a subscribe. Then checks whether all data of a contact
     *  survived those steps.
     *   
     *  This is usually disabled because we publish a censored template that does not
     *  export all data at all.
     */
    //disabled, because the main template is a censored one, so this would lack loads of data.
    public void doNot_testContactTrippingWithFullDataSet() throws Exception{        
        //setup contact
        Contact con = generateContact("Herbert", "Meier");
        con.setEmail2("invalid@open-xchange.com");
        con.setEmail3("invalid2@open-xchange.com");
        con.setTitle("Herr");
        con.setSuffix("Jr.");
        con.setCompany("Meier's Apostrophen Manufactur");
        con.setPosition("CAO");
        con.setUserField01("PEEEEENIS!");
        
        con.setStreetBusiness("Business Street");
        con.setStreetHome("Home Street");
        con.setStreetOther("Other Street");
        con.setPostalCodeBusiness("555");
        con.setPostalCodeHome("666");
        con.setPostalCodeOther("777");
        con.setCityBusiness("Business City");
        con.setCityHome("Home City");
        con.setCityOther("Other City");
        con.setCountryBusiness("Business Country");
        con.setCountryHome("Home Country");
        con.setCountryOther("Other Country");        
        con.setStateBusiness("Business State");
        con.setStateHome("Home State");
        con.setStateOther("Other State");        
                
        con.setParentFolderID(pubFolder.getObjectID());
        cMgr.newAction(con);
        
        //prepare pubsub
        PublicationTestManager pubMgr = getPublishManager();
        SubscriptionTestManager subMgr = getSubscribeManager();
        SimPublicationTargetDiscoveryService pubDiscovery = new SimPublicationTargetDiscoveryService();
        pubMgr.setPublicationTargetDiscoveryService(pubDiscovery);
        Publication publication = generatePublication("contacts", String.valueOf(pubFolder.getObjectID()), pubDiscovery);
                
        Contact[] contacts;
        
        //create publication        
        pubMgr.newAction(publication);

        //create subscription for that url
        DynamicFormDescription formDescription = publication.getTarget().getFormDescription();
        formDescription.add(FormElement.input("url", "URL"));
        Subscription subscription = generateOXMFSubscription(formDescription);
        subscription.setFolderId(subFolder.getObjectID());
        subMgr.setFormDescription(formDescription);
        String pubUrl = (String) publication.getConfiguration().get("url");
        subscription.getConfiguration().put("url", pubUrl);
        
        subMgr.newAction(subscription);
        
        //refresh and check subscription
        subMgr.refreshAction(subscription.getId());
        contacts = cMgr.allAction(subFolder.getObjectID());
        assertEquals("Should only contain one contact after first publication", 1, contacts.length);
        assertNoDataMessedUpMaximumRequirements(con,contacts[0]);    
    }
}