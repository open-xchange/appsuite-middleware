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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.oauth.linkedin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthException;
import com.openexchange.oauth.linkedin.osgi.Activator;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionException;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * {@link LinkedInSubscribeService}
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class LinkedInSubscribeService extends AbstractSubscribeService {

    private static final String PROTECTED_RESOURCE_URL = "http://api.linkedin.com/v1/people/~/connections:(id,first-name,last-name,phone-numbers,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions)";

    private Activator activator;

    private static final Log LOG = LogFactory.getLog(LinkedInSubscribeService.class);

    private final SubscriptionSource source = new SubscriptionSource();

    public LinkedInSubscribeService(Activator activator) {
        this.activator = activator;

        source.setDisplayName("LinkedIn via OAUTH");
        source.setFolderModule(FolderObject.CONTACT);
        source.setId("com.openexchange.subscribe.socialplugin.linkedin");
        source.setSubscribeService(this);

        DynamicFormDescription form = new DynamicFormDescription();

        FormElement oauthAccount = FormElement.custom("oauthAccount", "account", "The OAuthAccount to use");
        oauthAccount.setOption("type", new OAuthServiceMetaDataLinkedInImpl().getId());
        form.add(oauthAccount);

        source.setFormDescription(form);
    }
    
    @Override
    public void modifyIncoming(Subscription subscription) throws SubscriptionException {
        super.modifyIncoming(subscription);
        Integer accountId = (Integer) subscription.getConfiguration().get("account");
        if(accountId != null) {
            subscription.getConfiguration().put("account", accountId.toString());
        }
    }
    
    @Override
    public void modifyOutgoing(Subscription subscription) throws SubscriptionException {
        String accountId = (String) subscription.getConfiguration().get("account");
        if (null != accountId){
            Integer accountIdInt = Integer.parseInt(accountId);
            if (null != accountIdInt) subscription.getConfiguration().put("account",accountIdInt);
            subscription.setDisplayName("LinkedIn"); //FIXME use account displayName
        }        
        super.modifyOutgoing(subscription);
    }

    public List<Contact> getData(int user, int contextId) {
        List<Contact> contacts = new ArrayList<Contact>();
        OAuthService service = new ServiceBuilder().provider(LinkedInApi.class).apiKey(activator.getLinkedInMetadata().getAPIKey()).apiSecret(
            activator.getLinkedInMetadata().getAPISecret()).build();

        List<OAuthAccount> accounts = new ArrayList<OAuthAccount>();
        OAuthAccount account = null;
        try {
            com.openexchange.oauth.OAuthService oAuthService = activator.getOauthService();
            accounts = oAuthService.getAccounts(activator.getLinkedInMetadata().getId(), user, contextId);
        } catch (OAuthException e) {
            LOG.error(e);
        }

        // There are accounts for this service already. take the first one
        if (null != accounts && accounts.size() >= 1) {
            account = accounts.get(0);
        }
        // TODO: (Possibly) There are no accounts for this service yet. create one and save it
        else {

        }

        // get the connections (contacts) with the given access token
        Token accessToken = new Token(account.getToken(), account.getSecret());
        OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
        service.signRequest(accessToken, request);
        Response response = request.send();

        // parse the returned xml into neat little contacts
        contacts = parseIntoContacts(response.getBody());
        return contacts;

    }

    public Activator getActivator() {
        return activator;
    }

    public void setActivator(Activator activator) {
        this.activator = activator;
    }

    public void getAccount() {

    }

    public void createAccount() {

    }

    public Collection<?> getContent(Subscription subscription) throws SubscriptionException {
        return getData(subscription.getUserId(), subscription.getContext().getContextId());
    }

    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    public boolean handles(int folderModule) {
        return FolderObject.CONTACT == folderModule;
    }

    private List<Contact> parseIntoContacts(String body) {
        final List<Contact> contactObjects = new ArrayList<Contact>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(body.getBytes("UTF-8")));
            Element root = doc.getDocumentElement();
            NodeList connections = root.getElementsByTagName("person");
            if (connections != null && connections.getLength() > 0) {
                for (int i = 0; i < connections.getLength(); i++) {

                    // fill each contact
                    Element person = (Element) connections.item(i);
                    Contact contact = new Contact();
                    contact.setGivenName(getTextValue(person, "first-name"));
                    contact.setSurName(getTextValue(person, "last-name"));
                    try {
                        String imageUrl = getTextValue(person, "picture-url");
                        if (null != imageUrl) OXContainerConverter.loadImageFromURL(contact, imageUrl);
                        //System.out.println("Image-Url: " + getTextValue(person, "picture-url"));
                    } catch (ConverterException e) {
                        LOG.error(e);
                    }

                    // get the current job and company
                    NodeList positions = person.getElementsByTagName("positions");
                    if (positions != null && positions.getLength() > 0) {
                        Element position = (Element) positions.item(0);
                        contact.setTitle(getTextValue(position, "title"));
                        NodeList companies = position.getElementsByTagName("company");
                        if (companies != null && companies.getLength() > 0) {
                            Element company = (Element) companies.item(0);
                            contact.setCompany(getTextValue(company, "name"));
                        }
                    }
                }
            }
        } catch (ParserConfigurationException pce) {
            LOG.error(pce);
        } catch (SAXException se) {
            LOG.error(se);
        } catch (IOException ioe) {
            LOG.error(ioe);
        }
        return contactObjects;
    }

    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }
}
