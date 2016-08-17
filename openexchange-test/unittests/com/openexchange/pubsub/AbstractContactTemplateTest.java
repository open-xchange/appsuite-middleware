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

package com.openexchange.pubsub;

import static com.openexchange.subscribe.microformats.FormStrings.FORM_LABEL_URL;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.config.ConfigurationService;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.publish.microformats.tools.ContactTemplateUtils;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.microformats.MicroformatSubscribeService;
import com.openexchange.subscribe.microformats.datasources.HTTPOXMFDataSource;
import com.openexchange.subscribe.microformats.objectparser.OXHCardParser;
import com.openexchange.subscribe.microformats.parser.HTMLMicroformatParserFactory;
import com.openexchange.subscribe.microformats.transformers.MapToContactObjectTransformer;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.impl.OXTemplateImpl;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

/**
 * Tests a template for proper rendering of contacts by checking if they can be converted back after being rendered in a template. The tests
 * derived from this class usually depend on properly setting the path to the templates to be tested. This is set in
 * <code>pubsub.properties</code>. The tests are in the general test repository, because they depend both on the publish bundle (for the
 * templates), the templating bundle (for templating) and the subscribe bundle (for parsing).
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractContactTemplateTest extends TestCase {

    /**
     * Initializes a new {@link AbstractContactTemplateTest}.
     */
    public AbstractContactTemplateTest() {
        super();
    }

    /**
     * Initializes a new {@link AbstractContactTemplateTest}.
     *
     * @param name
     */
    public AbstractContactTemplateTest(String name) {
        super(name);
    }

    public Contact generateContact(String identifier) {
        Contact contact = new Contact();
        contact.setGivenName("givenname" + identifier);
        contact.setSurName("surname" + identifier);
        contact.setMiddleName("middlename" + identifier);
        contact.setSuffix("suffix" + identifier);
        contact.setEmail1("email1" + identifier + "@ox.invalid");
        contact.setEmail2("email2" + identifier + "@ox.invalid");
        contact.setEmail3("email3" + identifier + "@ox.invalid");
        contact.setDisplayName("displayname" + identifier);
        contact.setPosition("position" + identifier);
        contact.setTitle("title" + identifier);
        contact.setCompany("company" + identifier);
        contact.setStateBusiness("state_business" + identifier);
        contact.setCountryBusiness("country_business" + identifier);
        contact.setStreetBusiness("street_business" + identifier);
        contact.setPostalCodeBusiness("postal_code_business" + identifier);
        contact.setCityBusiness("city_business" + identifier);
        contact.setTelephoneBusiness1("telephone_business1" + identifier);
        contact.setTelephoneHome1("telephone_home1" + identifier);
        contact.setTelephoneBusiness2("telephone_business2" + identifier);
        contact.setTelephoneHome2("telephone_home2" + identifier);
        return contact;
    }

    protected SubscriptionSource getSubscriptionSource() {
        final SubscriptionSource contactSubscriptionSource = new SubscriptionSource();
        contactSubscriptionSource.setDisplayName("OXMF Contacts");
        contactSubscriptionSource.setId("com.openexchange.subscribe.microformats.contacts.http");
        contactSubscriptionSource.setFolderModule(FolderObject.CONTACT);

        final DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("url", FORM_LABEL_URL, true, null));
        contactSubscriptionSource.setFormDescription(form);
        return contactSubscriptionSource;
    }

    protected MicroformatSubscribeService getSubscribeService() {
        final HTTPOXMFDataSource dataSource = new HTTPOXMFDataSource();
        final HTMLMicroformatParserFactory parserFactory = new HTMLMicroformatParserFactory();
        final MapToContactObjectTransformer mapToContactObject = new MapToContactObjectTransformer();

        final MicroformatSubscribeService subscribeService = new MicroformatSubscribeService();
        subscribeService.setOXMFParserFactory(parserFactory);
        subscribeService.setOXMFSource(dataSource);
        subscribeService.setTransformer(mapToContactObject);
        subscribeService.addContainerElement("ox_contact");
        subscribeService.addPrefix("ox_");
        subscribeService.addObjectParser(new OXHCardParser());
        return subscribeService;
    }

    protected void introduceToEachOther(MicroformatSubscribeService service, SubscriptionSource source) {
        source.setSubscribeService(service);
        service.setSource(source);
    }

    protected OXTemplate getTemplate(String templateName) throws Exception{
        Init.startServer();
        ConfigurationService conf = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        File path = new File(conf.getProperty("com.openexchange.templating.path"));

        TemplateLoader templateLoader = new FileTemplateLoader(path);
        Configuration config = new Configuration();
        config.setTemplateLoader(templateLoader);
        OXTemplateImpl oxTempl = new OXTemplateImpl();
        oxTempl.setTemplate(config.getTemplate(templateName));
        return oxTempl;
    }

    protected Map<String, Object> getVariables() {
        Map<String, Object> variables = new HashMap<String, Object>();

        variables.put("dateFormat", new SimpleDateFormat("yyyy-MM-dd"));
        variables.put("timeFormat", new SimpleDateFormat("yyyy-MM-dd H:m:s.S z"));

        variables.put("privacy", "");
        variables.put("userContact", "");
        variables.put("utils", new ContactTemplateUtils());

        return variables;
    }

    protected List<Contact> getContacts() {
        List<Contact> contacts = new LinkedList<Contact>();
        for (int i = 0; i < 20; i++) {
            contacts.add(generateContact("_" + i));
        }
        return contacts;
    }

}
