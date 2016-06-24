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

package com.openexchange.subscribe.external;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.List;
import org.apache.commons.httpclient.HttpException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.modules.Module;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceCollector;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.external.parser.ListingParser;
import com.openexchange.subscribe.microformats.MicroformatSubscribeService;
import com.openexchange.subscribe.microformats.OXMFParserFactoryService;
import com.openexchange.subscribe.microformats.OXMFSubscriptionErrorMessage;
import com.openexchange.subscribe.microformats.datasources.HTTPFormSubmittingOXMFDataSource;
import com.openexchange.subscribe.microformats.datasources.HTTPToolkit;
import com.openexchange.subscribe.microformats.parser.OXMFForm;
import com.openexchange.subscribe.microformats.parser.OXMFFormParser;
import com.openexchange.subscribe.microformats.transformers.MapToContactObjectTransformer;
import com.openexchange.subscribe.microformats.transformers.MapToDocumentMetadataHolderTransformer;
import com.openexchange.subscribe.microformats.transformers.MapToObjectTransformer;


/**
 * {@link ExternalSubscriptionSourceDiscoveryService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ExternalSubscriptionSourceDiscoveryService implements SubscriptionSourceDiscoveryService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExternalSubscriptionSourceDiscoveryService.class);

    private final String sourceURL;
    private SubscriptionSourceCollector sources = new SubscriptionSourceCollector();
    private final OXMFParserFactoryService parserFactory;
    private final OXMFFormParser formParser;

    public ExternalSubscriptionSourceDiscoveryService(final String sourceURL, final OXMFParserFactoryService parserFactory, final OXMFFormParser formParser) {
        this.sourceURL = sourceURL;
        this.parserFactory = parserFactory;
        this.formParser = formParser;
    }

    @Override
    public SubscriptionSource getSource(final String identifier) {
        return sources.getSource(identifier);
    }

    @Override
    public SubscriptionSource getSource(final Context context, final int subscriptionId) throws OXException {
        return sources.getSource(context, subscriptionId);
    }

    @Override
    public List<SubscriptionSource> getSources() {
        return sources.getSources();
    }

    @Override
    public List<SubscriptionSource> getSources(final int folderModule) {
        return sources.getSources(folderModule);
    }

    @Override
    public boolean knowsSource(final String identifier) {
        return sources.knowsSource(identifier);
    }

    @Override
    public SubscriptionSourceDiscoveryService filter(final int user, final int context) throws OXException {
        return null;
    }

    public void refresh() throws OXException {
        try {
            final List<ExternalSubscriptionSource> listing = grabListing();
            final SubscriptionSourceCollector sources = new SubscriptionSourceCollector();
            for (final ExternalSubscriptionSource external : listing) {
                final MicroformatSubscribeService service = grabService(external);
                if(service != null) {
                    sources.addSubscribeService(service);
                }
            }
            this.sources = sources;
        } catch (final OXException x) {
            throw x;
        }
    }

    private MicroformatSubscribeService grabService(final ExternalSubscriptionSource external) throws OXException {
        final String externalAddress = resolveRelative(sourceURL, external.getExternalAddress());
        external.setExternalAddress(externalAddress);

        String icon = resolveRelative(sourceURL, external.getIcon());
        external.setIcon(icon);

        try {
            final Reader r = HTTPToolkit.grab(externalAddress);
            final OXMFForm form = formParser.parse(r);

            ListingParser.apply(external, form.getMetaInfo());

            icon = resolveRelative(externalAddress, external.getIcon());
            external.setIcon(icon);

            final String action = resolveRelative(externalAddress, form.getAction());
            form.setAction(action);

            final MapToObjectTransformer transformer = getTransformer(external.getFolderModule());
            if(transformer == null) {
                LOG.error("We don''t support subscription sources of type {} yet", Module.getModuleString(external.getFolderModule(), -1));
                return null;
            }
            final MicroformatSubscribeService subscribeService = new MicroformatSubscribeService() {
                @Override
                protected String getDisplayName(final Subscription subscription) {
                    if(form.getDisplayNameField() == null) {
                        return external.getDisplayName();
                    }
                    return subscription.getConfiguration().get(form.getDisplayNameField().getName()).toString();
                }
            };
            subscribeService.setOXMFParserFactory(parserFactory);
            subscribeService.setOXMFSource(new HTTPFormSubmittingOXMFDataSource());
            subscribeService.setTransformer(transformer);
            subscribeService.setSource(external);
            subscribeService.addContainerElement(getContainerElement(external.getFolderModule()));
            subscribeService.addPrefix("ox_");

            external.setFormDescription(form);
            external.setSubscribeService(subscribeService);

            return subscribeService;
        } catch (final HttpException e) {
            LOG.error("Could not grab external service: {} Got Error", externalAddress, e);
            throw OXMFSubscriptionErrorMessage.HttpException.create(e.getMessage(), externalAddress, e);
        } catch (final IOException e) {
            LOG.error("Could not grab external service: {} Got Error", externalAddress, e);
            throw OXMFSubscriptionErrorMessage.IOException.create(e.getMessage(), externalAddress, e);
        } catch (IllegalArgumentException e) {
            LOG.error("Could not grab external service: {} Got error: ", externalAddress, e);
            throw OXMFSubscriptionErrorMessage.ERROR_LOADING_SUBSCRIPTION.create(e, externalAddress);
        }
    }



    private String resolveRelative(final String sibling, final String relative) {
        if(relative == null) {
            return null;
        }
        if(relative.startsWith("http")) {
            return relative;
        }

        if(relative.length() > 0 && relative.charAt(0) == '/') {
            return resolveServerRelative(sibling, relative);
        }

        final String directory = sibling.substring(0,sibling.lastIndexOf('/'));
        return directory + '/'+ relative;
    }

    private String resolveServerRelative(final String sibling, final String relative) {
        try {
            final java.net.URL url = new java.net.URL(sibling);
            return url.getProtocol()+"://"+url.getHost()+relative;
        } catch (final MalformedURLException e) {
            LOG.error("", e);
            return relative;
        }
    }

    private String getContainerElement(final int folderModule) {
        if(folderModule == Module.CONTACTS.getFolderConstant()) {
            return "ox_contact";
        } else if (folderModule == Module.INFOSTORE.getFolderConstant()) {
            return "ox_infoitem";
        }
        return null;
    }

    private MapToObjectTransformer getTransformer(final int folderModule) {
        if(folderModule == Module.CONTACTS.getFolderConstant()) {
            return new MapToContactObjectTransformer();
        } else if (folderModule == Module.INFOSTORE.getFolderConstant()) {
            return new MapToDocumentMetadataHolderTransformer();
        }

        return null;
    }

    private List<ExternalSubscriptionSource> grabListing() throws OXException {
        final ListingParser parser = new ListingParser(parserFactory);
        try {
            final Reader r = HTTPToolkit.grab(sourceURL);
            return parser.parse(r);
        } catch (final HttpException e) {
            LOG.error("", e);
            throw OXMFSubscriptionErrorMessage.HttpException.create(e.getMessage(), e);
        } catch (final IOException e) {
            LOG.error("", e);
            throw OXMFSubscriptionErrorMessage.IOException.create(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOG.error("", e);
            throw OXMFSubscriptionErrorMessage.ERROR_LOADING_SUBSCRIPTION.create(e, sourceURL);
        }
    }

}
