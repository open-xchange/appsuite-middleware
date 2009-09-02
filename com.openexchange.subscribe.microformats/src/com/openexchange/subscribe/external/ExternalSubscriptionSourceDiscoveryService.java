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

package com.openexchange.subscribe.external;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.modules.Module;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionException;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.external.parser.ListingParser;
import com.openexchange.subscribe.microformats.MicroformatSubscribeService;
import com.openexchange.subscribe.microformats.OXMFParserFactoryService;
import com.openexchange.subscribe.microformats.OXMFSubscriptionErrorMessage;
import com.openexchange.subscribe.microformats.OXMFSubscriptionException;
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

    private static final Log LOG = LogFactory.getLog(ExternalSubscriptionSourceDiscoveryService.class);
    
    private String sourceURL;
    private Map<String, SubscriptionSource> sources = new HashMap<String, SubscriptionSource>();
    private OXMFParserFactoryService parserFactory;
    private OXMFFormParser formParser;
    
    public ExternalSubscriptionSourceDiscoveryService(String sourceURL, OXMFParserFactoryService parserFactory, OXMFFormParser formParser) {
        this.sourceURL = sourceURL;
        this.parserFactory = parserFactory;
        this.formParser = formParser;
    }
    
    public SubscriptionSource getSource(String identifier) {
        return sources.get(identifier);
    }

    public SubscriptionSource getSource(Context context, int subscriptionId) throws AbstractOXException {
        for(SubscriptionSource source : sources.values()) {
            if(source.getSubscribeService().knows(context, subscriptionId)) {
                return source;
            }
        }
        return null;
    }

    public List<SubscriptionSource> getSources() {
        return new ArrayList<SubscriptionSource>(sources.values());
    }

    public List<SubscriptionSource> getSources(int folderModule) {
        if(-1 == folderModule) {
            return getSources();
        }
        List<SubscriptionSource> matching = new ArrayList<SubscriptionSource>(sources.size());
        for(SubscriptionSource source : sources.values()) {
            if(source.getFolderModule() == folderModule) {
                matching.add(source);
            }
        }
        return matching;
    }

    public boolean knowsSource(String identifier) {
        return sources.containsKey(identifier);
    }
    
    public void refresh() throws SubscriptionException {
        try {
            List<ExternalSubscriptionSource> listing = grabListing();
            Map<String, SubscriptionSource> sources = new HashMap<String, SubscriptionSource>(listing.size());
            for (ExternalSubscriptionSource external : listing) {
                MicroformatSubscribeService service = grabService(external);
                if(service != null) {
                    sources.put(service.getSubscriptionSource().getId(), service.getSubscriptionSource());
                }
            }
            this.sources = sources;
        } catch (SubscriptionException x) {
            throw x;
        } catch (AbstractOXException x) {
            throw new SubscriptionException(x);
        }
    }

    private MicroformatSubscribeService grabService(final ExternalSubscriptionSource external) throws OXMFSubscriptionException {
        String externalAddress = resolveRelative(sourceURL, external.getExternalAddress());
        external.setExternalAddress(externalAddress);
        
        String icon = resolveRelative(sourceURL, external.getIcon());
        external.setIcon(icon);
        
        try {
            Reader r = HTTPToolkit.grab(externalAddress);
            final OXMFForm form = formParser.parse(r);
            
            ListingParser.apply(external, form.getMetaInfo());
            
            icon = resolveRelative(externalAddress, external.getIcon());
            external.setIcon(icon);

            String action = resolveRelative(externalAddress, form.getAction());
            form.setAction(action);
            
            MapToObjectTransformer transformer = getTransformer(external.getFolderModule());
            if(transformer == null) {
                LOG.error("We don't support subscription sources of type "+Module.getModuleString(external.getFolderModule(), -1)+" yet");
                return null;
            }
            MicroformatSubscribeService subscribeService = new MicroformatSubscribeService() {
                @Override
                protected String getDisplayName(Subscription subscription) {
                    if(form.getDisplayNameField() == null) {
                        return external.getDisplayName();
                    } else {
                        return subscription.getConfiguration().get(form.getDisplayNameField().getName()).toString();
                    }
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
        } catch (HttpException e) {
            LOG.error(e.getMessage(), e);
            throw OXMFSubscriptionErrorMessage.HttpException.create(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw OXMFSubscriptionErrorMessage.IOException.create(e.getMessage(), e);
        }
    }

   

    private String resolveRelative(String sibling, String relative) {
        if(relative == null) {
            return null;
        }
        if(relative.startsWith("http")) {
            return relative;
        }

        if(relative.startsWith("/")) {
            return resolveServerRelative(sibling, relative);
        }
        
        String directory = sibling.substring(0,sibling.lastIndexOf('/'));
        return directory + '/'+ relative;
    }

    private String resolveServerRelative(String sibling, String relative) {
        try {
            java.net.URL url = new java.net.URL(sibling);
            return url.getProtocol()+"://"+url.getHost()+relative;
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage(), e);
            return relative;
        }
    }

    private String getContainerElement(int folderModule) {
        if(folderModule == Module.CONTACTS.getFolderConstant()) {
            return "ox_contact";
        } else if (folderModule == Module.INFOSTORE.getFolderConstant()) {
            return "ox_infoitem";
        }
        return null;
    }

    private MapToObjectTransformer getTransformer(int folderModule) {
        if(folderModule == Module.CONTACTS.getFolderConstant()) {
            return new MapToContactObjectTransformer();
        } else if (folderModule == Module.INFOSTORE.getFolderConstant()) {
            return new MapToDocumentMetadataHolderTransformer();
        }
        
        return null;
    }

    private List<ExternalSubscriptionSource> grabListing() throws AbstractOXException {
        ListingParser parser = new ListingParser(parserFactory);
        try {
            Reader r = HTTPToolkit.grab(sourceURL);
            return parser.parse(r);
        } catch (HttpException e) {
            LOG.error(e.getMessage(), e);
            throw OXMFSubscriptionErrorMessage.HttpException.create(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw OXMFSubscriptionErrorMessage.IOException.create(e.getMessage(), e);
        }
    }

}
