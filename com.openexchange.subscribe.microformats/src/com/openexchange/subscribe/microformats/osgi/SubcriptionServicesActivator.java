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

package com.openexchange.subscribe.microformats.osgi;

import static com.openexchange.subscribe.microformats.FormStrings.FORM_LABEL_URL;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.microformats.MicroformatSubscribeService;
import com.openexchange.subscribe.microformats.OXMFParserFactoryService;
import com.openexchange.subscribe.microformats.OXMFSubscriptionErrorMessage;
import com.openexchange.subscribe.microformats.datasources.HTTPOXMFDataSource;
import com.openexchange.subscribe.microformats.parser.CybernekoOXMFFormParser;
import com.openexchange.subscribe.microformats.parser.HTMLMicroformatParserFactory;
import com.openexchange.subscribe.microformats.parser.OXMFFormParser;
import com.openexchange.subscribe.microformats.transformers.MapToContactObjectTransformer;
import com.openexchange.subscribe.microformats.transformers.MapToDocumentMetadataHolderTransformer;

public class SubcriptionServicesActivator implements BundleActivator {

    private ComponentRegistration componentRegistration;

    public void start(BundleContext context) throws Exception {
        componentRegistration = new ComponentRegistration(
            context,
            "MFS",
            "com.openexchange.subscribe.microformats",
            OXMFSubscriptionErrorMessage.EXCEPTIONS);

        HTTPOXMFDataSource dataSource = new HTTPOXMFDataSource();
        HTMLMicroformatParserFactory parserFactory = new HTMLMicroformatParserFactory();
        MapToContactObjectTransformer mapToContactObject = new MapToContactObjectTransformer();

        SubscriptionSource contactSubscriptionSource = new SubscriptionSource();
        contactSubscriptionSource.setDisplayName("OXMF Contacts");
        contactSubscriptionSource.setId("com.openexchange.subscribe.microformats.contacts.http");
        contactSubscriptionSource.setFolderModule(FolderObject.CONTACT);

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("url", FORM_LABEL_URL, true, null));
        contactSubscriptionSource.setFormDescription(form);

        MicroformatSubscribeService subscribeService = new MicroformatSubscribeService();
        subscribeService.setOXMFParserFactory(parserFactory);
        subscribeService.setOXMFSource(dataSource);
        subscribeService.setTransformer(mapToContactObject);
        subscribeService.setSource(contactSubscriptionSource);
        subscribeService.addContainerElement("ox_contact");
        subscribeService.addPrefix("ox_");

        contactSubscriptionSource.setSubscribeService(subscribeService);


        MapToDocumentMetadataHolderTransformer mapToDocumentMetadataHolder = new MapToDocumentMetadataHolderTransformer();

        SubscriptionSource infostoreSubscriptionSource = new SubscriptionSource();
        infostoreSubscriptionSource.setDisplayName("OXMF Infostore");
        infostoreSubscriptionSource.setId("com.openexchange.subscribe.microformats.infostore.http");
        infostoreSubscriptionSource.setFolderModule(FolderObject.INFOSTORE);

        infostoreSubscriptionSource.setFormDescription(form);

        MicroformatSubscribeService infostoreService = new MicroformatSubscribeService();
        infostoreService.setOXMFParserFactory(parserFactory);
        infostoreService.setOXMFSource(dataSource);
        infostoreService.setTransformer(mapToDocumentMetadataHolder);
        infostoreService.setSource(infostoreSubscriptionSource);
        infostoreService.addContainerElement("ox_infoitem");
        infostoreService.addPrefix("ox_");

        infostoreSubscriptionSource.setSubscribeService(infostoreService);

        context.registerService(SubscribeService.class.getName(), subscribeService, null);
        context.registerService(SubscribeService.class.getName(), infostoreService, null);


        context.registerService(OXMFParserFactoryService.class.getName(), parserFactory, null);
        context.registerService(OXMFFormParser.class.getName(), new CybernekoOXMFFormParser(), null);
    }

    public void stop(BundleContext context) throws Exception {
        componentRegistration.unregister();
    }
}
