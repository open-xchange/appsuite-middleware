/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.subscribe.test;


import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;

/**
* @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
*/
public abstract class AbstractPubSubTest extends AbstractAJAXSession {

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

    protected Subscription generateOXMFSubscription(DynamicFormDescription formDescription, String folderID) {
       Subscription sub = generateOXMFSubscription(formDescription);
       sub.setFolderId(folderID);
       return sub;
   }

    protected Subscription generateOXMFSubscription(DynamicFormDescription formDescription) {
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

    protected FolderObject createDefaultContactFolder() throws OXException, IOException, JSONException {
       FolderObject folder = ftm.generatePublicFolder("pubsub default contact folder " + UUID.randomUUID().toString(), FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
       ftm.insertFolderOnServer(folder);
       return folder;
   }

    protected FolderObject createDefaultInfostoreFolder() throws OXException, IOException, JSONException {
       return createDefaultInfostoreFolder(null);
   }

    protected FolderObject createDefaultInfostoreFolder(String folderName) throws OXException, IOException, JSONException {
       if (folderName == null) {
           folderName = "pubsub default infostore folder " + this.getClass().getCanonicalName() + "-" + System.currentTimeMillis();
       }
       FolderObject folder = ftm.generatePublicFolder(folderName, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), getClient().getValues().getUserId());
       ftm.insertFolderOnServer(folder);
       return folder;
   }

    protected Contact createDefaultContactFolderWithOneContact() throws OXException, IOException, JSONException {
       FolderObject folder = createDefaultContactFolder();

       Contact contact = generateContact("Herbert", "Meier");
       contact.setParentFolderID(folder.getObjectID());
       cotm.newAction(contact);
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
       assertEquals("Should respond with status 200", 200, resp.getResponseCode());
       return resp.getText();
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
