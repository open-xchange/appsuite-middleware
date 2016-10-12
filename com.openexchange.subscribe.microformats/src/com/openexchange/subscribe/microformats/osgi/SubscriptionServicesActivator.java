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

package com.openexchange.subscribe.microformats.osgi;

import static com.openexchange.subscribe.microformats.FormStrings.FORM_LABEL_URL;
import static com.openexchange.subscribe.microformats.FormStrings.SOURCE_NAME_CONTACTS;
import static com.openexchange.subscribe.microformats.FormStrings.SOURCE_NAME_INFOSTORE;
import com.openexchange.config.ConfigurationService;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.microformats.MicroformatSubscribeService;
import com.openexchange.subscribe.microformats.OXMFParserFactoryService;
import com.openexchange.subscribe.microformats.OXMFServiceRegistry;
import com.openexchange.subscribe.microformats.datasources.HTTPOXMFDataSource;
import com.openexchange.subscribe.microformats.objectparser.OXHCardParser;
import com.openexchange.subscribe.microformats.parser.CybernekoOXMFFormParser;
import com.openexchange.subscribe.microformats.parser.HTMLMicroformatParserFactory;
import com.openexchange.subscribe.microformats.parser.OXMFFormParser;
import com.openexchange.subscribe.microformats.transformers.MapToContactObjectTransformer;
import com.openexchange.subscribe.microformats.transformers.MapToDocumentMetadataHolderTransformer;

/**
 * {@link SubscriptionServicesActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SubscriptionServicesActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link SubscriptionServicesActivator}.
     */
    public SubscriptionServicesActivator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        final HTTPOXMFDataSource dataSource = new HTTPOXMFDataSource();
        final HTMLMicroformatParserFactory parserFactory = new HTMLMicroformatParserFactory();
        final MapToContactObjectTransformer mapToContactObject = new MapToContactObjectTransformer();

        final SubscriptionSource contactSubscriptionSource = new SubscriptionSource();
        contactSubscriptionSource.setDisplayName(SOURCE_NAME_CONTACTS);
        contactSubscriptionSource.setLocalizableDisplayName();
        contactSubscriptionSource.setId("com.openexchange.subscribe.microformats.contacts.http");
        contactSubscriptionSource.setFolderModule(FolderObject.CONTACT);

        final DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("url", FORM_LABEL_URL, true, null));
        contactSubscriptionSource.setFormDescription(form);

        final MicroformatSubscribeService subscribeService = new MicroformatSubscribeService();
        subscribeService.setOXMFParserFactory(parserFactory);
        subscribeService.setOXMFSource(dataSource);
        subscribeService.setTransformer(mapToContactObject);
        subscribeService.setSource(contactSubscriptionSource);
        subscribeService.addContainerElement("ox_contact");
        subscribeService.addPrefix("ox_");
        subscribeService.addObjectParser(new OXHCardParser());

        contactSubscriptionSource.setSubscribeService(subscribeService);

        final MapToDocumentMetadataHolderTransformer mapToDocumentMetadataHolder = new MapToDocumentMetadataHolderTransformer();

        final SubscriptionSource infostoreSubscriptionSource = new SubscriptionSource();
        infostoreSubscriptionSource.setDisplayName(SOURCE_NAME_INFOSTORE);
        infostoreSubscriptionSource.setLocalizableDisplayName();
        infostoreSubscriptionSource.setId("com.openexchange.subscribe.microformats.infostore.http");
        infostoreSubscriptionSource.setFolderModule(FolderObject.INFOSTORE);

        infostoreSubscriptionSource.setFormDescription(form);

        final MicroformatSubscribeService infostoreService = new MicroformatSubscribeService();
        infostoreService.setOXMFParserFactory(parserFactory);
        infostoreService.setOXMFSource(dataSource);
        infostoreService.setTransformer(mapToDocumentMetadataHolder);
        infostoreService.setSource(infostoreSubscriptionSource);
        infostoreService.addContainerElement("ox_infoitem");
        infostoreService.addPrefix("ox_");

        infostoreSubscriptionSource.setSubscribeService(infostoreService);
        /*
         * Add and register services
         */
        registerService(SubscribeService.class, subscribeService, null);
        registerService(SubscribeService.class, infostoreService, null);

        registerService(OXMFParserFactoryService.class, parserFactory, null);
        registerService(OXMFFormParser.class, new CybernekoOXMFFormParser(), null);
        /*
         * Add and open service trackers
         */
        track(ConfigurationService.class, new RegistryServiceTrackerCustomizer<ConfigurationService>(context, OXMFServiceRegistry.getInstance(), ConfigurationService.class));
        track(SSLSocketFactoryProvider.class, new RegistryServiceTrackerCustomizer<SSLSocketFactoryProvider>(context, OXMFServiceRegistry.getInstance(), SSLSocketFactoryProvider.class));
        openTrackers();
    }

    @Override
    public void stopBundle() throws Exception {
        unregisterServices();
        closeTrackers();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        // Nothing to do
        return null;
    }

}
