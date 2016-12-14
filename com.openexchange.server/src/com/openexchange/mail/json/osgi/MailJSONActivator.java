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

package com.openexchange.mail.json.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXResultDecorator;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.datasource.ContactImageDataSource;
import com.openexchange.groupware.contact.datasource.UserImageDataSource;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.image.ImageLocation;
import com.openexchange.mail.api.AuthenticationFailedHandler;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.attachment.storage.DefaultMailAttachmentStorage;
import com.openexchange.mail.attachment.storage.DefaultMailAttachmentStorageRegistry;
import com.openexchange.mail.attachment.storage.MailAttachmentStorage;
import com.openexchange.mail.attachment.storage.MailAttachmentStorageRegistry;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.internal.MailCategoriesPreferenceItem;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailActionFactory;
import com.openexchange.mail.json.MailOAuthConstants;
import com.openexchange.mail.json.compose.ComposeHandler;
import com.openexchange.mail.json.compose.ComposeHandlerRegistry;
import com.openexchange.mail.json.compose.internal.ComposeHandlerRegistryImpl;
import com.openexchange.mail.json.converters.MailConverter;
import com.openexchange.mail.json.converters.MailJSONConverter;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.transport.config.TransportReloadable;
import com.openexchange.oauth.provider.resourceserver.scope.AbstractScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailJSONActivator} - The activator for mail module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailJSONActivator extends AJAXModuleActivator {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailJSONActivator.class);

    /**
     * The {@link ServiceLookup} reference.
     */
    public static final AtomicReference<ServiceLookup> SERVICES = new AtomicReference<ServiceLookup>();

    /**
     * Initializes a new {@link MailJSONActivator}.
     */
    public MailJSONActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContactService.class, ContactStorage.class, ConfigurationService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { MailCategoriesConfigService.class, CapabilityService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceLookup serviceLookup = new ExceptionOnAbsenceServiceLookup(this);
        SERVICES.set(serviceLookup);

        trackService(Dispatcher.class);
        trackService(AuthenticationFailedHandler.class);

        final BundleContext context = this.context;

        // Tracker for CapabilityService that declares "publish_mail_attachments" capability
        track(CapabilityService.class, new ServiceTrackerCustomizer<CapabilityService, CapabilityService>() {

            private volatile ServiceRegistration<CapabilityChecker> serviceRegistration;

            @Override
            public CapabilityService addingService(final ServiceReference<CapabilityService> reference) {
                final CapabilityService service = context.getService(reference);
                final String sCapability = "publish_mail_attachments";

                // Register CapabilityChecker for "publish_mail_attachments"
                final Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
                properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
                final CapabilityChecker capabilityChecker = new CapabilityChecker() {

                    @Override
                    public boolean isEnabled(final String capability, final Session ses) throws OXException {
                        if (sCapability.equals(capability)) {
                            return false;
                        }

                        return true;
                    }
                };
                serviceRegistration = context.registerService(CapabilityChecker.class, capabilityChecker, properties);

                // Declare "publish_mail_attachments" capability
                service.declareCapability(sCapability);

                // Return tracked service
                return service;
            }

            @Override
            public void modifiedService(final ServiceReference<CapabilityService> reference, final CapabilityService service) {
                // Ignore
            }

            @Override
            public void removedService(final ServiceReference<CapabilityService> reference, final CapabilityService service) {
                final ServiceRegistration<CapabilityChecker> serviceRegistration = this.serviceRegistration;
                if (null != serviceRegistration) {
                    serviceRegistration.unregister();
                    this.serviceRegistration = null;
                }
                context.ungetService(reference);
            }
        });

        ComposeHandlerRegistry composeHandlerRegisty;
        {
            RankingAwareNearRegistryServiceTracker<ComposeHandler> tracker = new RankingAwareNearRegistryServiceTracker<>(context, ComposeHandler.class);
            rememberTracker(tracker);
            composeHandlerRegisty = new ComposeHandlerRegistryImpl(tracker);
        }

        MimeMailExceptionHandlerTracker exceptionHandlerTracker = new MimeMailExceptionHandlerTracker(context);
        rememberTracker(exceptionHandlerTracker);
        MimeMailException.setExceptionHandlers(exceptionHandlerTracker);

        openTrackers();

        registerService(ComposeHandlerRegistry.class, composeHandlerRegisty);

        DefaultMailAttachmentStorageRegistry.initInstance(context);
        registerService(MailAttachmentStorageRegistry.class, DefaultMailAttachmentStorageRegistry.getInstance());

        {
            Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put(Constants.SERVICE_RANKING, Integer.valueOf(0));
            registerService(MailAttachmentStorage.class, new DefaultMailAttachmentStorage(), properties);
        }

        {
            final String topicRemoveSession = SessiondEventConstants.TOPIC_REMOVE_SESSION;
            final String topicRemoveContainer = SessiondEventConstants.TOPIC_REMOVE_CONTAINER;
            final String topicRemoveData = SessiondEventConstants.TOPIC_REMOVE_DATA;

            EventHandler eventHandler = new EventHandler() {

                @Override
                public void handleEvent(Event event) {
                    String topic = event.getTopic();
                    if (topicRemoveSession.equals(topic)) {
                        handleSession((Session) event.getProperty(SessiondEventConstants.PROP_SESSION));
                    } else if (topicRemoveContainer.equals(topic) || topicRemoveData.equals(topic)) {
                        @SuppressWarnings("unchecked") Map<String, Session> sessions = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                        for (Session session : sessions.values()) {
                            handleSession(session);
                        }
                    }
                }

                private void handleSession(Session session) {
                    CompositionSpace.dropCompositionSpaces(session);
                }
            };

            Dictionary<String, Object> props = new Hashtable<String, Object>(2);
            props.put(EventConstants.EVENT_TOPIC, new String[] { topicRemoveSession, topicRemoveContainer, topicRemoveData });
            registerService(EventHandler.class, eventHandler, props);
        }

        registerModule(MailActionFactory.initializeActionFactory(serviceLookup), "mail");
        final MailConverter converter = MailConverter.getInstance();
        registerService(ResultConverter.class, converter);
        registerService(ResultConverter.class, new MailJSONConverter(converter));

        registerService(Reloadable.class, MailReloadable.getInstance());
        registerService(Reloadable.class, TransportReloadable.getInstance());
        registerService(ForcedReloadable.class, new ForcedReloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                MailConfig.invalidateAuthTypeCache();
            }

            @Override
            public Interests getInterests() {
                return null;
            }
        });

        registerService(PreferencesItemService.class, new MailCategoriesPreferenceItem(this));

        final ContactField[] fields = new ContactField[] { ContactField.OBJECT_ID, ContactField.INTERNAL_USERID, ContactField.FOLDER_ID, ContactField.NUMBER_OF_IMAGES };
        registerService(AJAXResultDecorator.class, new DecoratorImpl(converter, fields, this));

        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(MailOAuthConstants.OAUTH_SEND_DATA, OAuthScopeDescription.SEND_DATA) {

            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.WEBMAIL.getCapabilityName());
            }
        });
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        MimeMailException.unsetExceptionHandlers();
        ServerServiceRegistry.getInstance().removeService(ComposeHandlerRegistry.class);
        DefaultMailAttachmentStorageRegistry.dropInstance();
        MailActionFactory.releaseActionFactory();
        SERVICES.set(null);
    }

    private static final class DecoratorImpl implements AJAXResultDecorator {

        private final MailConverter converter;
        private final ContactField[] fields;
        private final ServiceLookup services;

        /**
         * Initializes a new {@link DecoratorImpl}.
         */
        protected DecoratorImpl(MailConverter converter, ContactField[] fields, ServiceLookup services) {
            super();
            this.converter = converter;
            this.fields = fields;
            this.services = services;
        }

        @Override
        public String getIdentifier() {
            return "mail.senderImageUrl";
        }

        @Override
        public String getFormat() {
            return "mail";
        }

        @Override
        public void decorate(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException {
            Object resultObject = result.getResultObject();
            if (null == resultObject) {
                LOG.warn("Result object is null.");
                result.setResultObject(JSONObject.NULL, "json");
                return;
            }

            if ("get".equals(requestData.getAction()) && (resultObject instanceof MailMessage)) {
                try {
                    MailMessage mailMessage = (MailMessage) resultObject;
                    InternetAddress[] from = mailMessage.getFrom();
                    if (null == from || 0 == from.length) {
                        return;
                    }

                    // Discover image URL for 'from' address
                    ContactService contactService = services.getService(ContactService.class);
                    if (null == contactService) {
                        return;
                    }
                    SearchIterator<Contact> searchIterator = null;
                    String imageURL = null;
                    try {
                        searchIterator = contactService.searchContacts(session, createContactSearchObject(from[0]), fields, new SortOptions(ContactField.FOLDER_ID, Order.ASCENDING));
                        if (null != searchIterator) {
                            while (null == imageURL && searchIterator.hasNext()) {
                                final Contact contact = searchIterator.next();
                                imageURL = getImageURL(session, contact);
                            }
                        }
                    } finally {
                        SearchIterators.close(searchIterator);
                    }

                    // Convert to JSON, decorate with image URL
                    converter.convert2JSON(requestData, result, session);
                    JSONArray fromImageURLs = new JSONArray(2);
                    if (null != imageURL) {
                        fromImageURLs.put(imageURL);
                    }
                    ((JSONObject) result.getResultObject()).put("from_image_urls", fromImageURLs);
                } catch (final JSONException e) {
                    throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
                }
            }
        }

        /**
         * Tries to generate an URL for the image of the supplied contact if available.
         *
         * @param session The server session
         * @param contact The contact to generate the image URL for
         * @return The image URL, or <code>null</code> if not available or something went wrong
         */
        private String getImageURL(final ServerSession session, final Contact contact) {
            if (0 < contact.getNumberOfImages() || contact.containsImage1() && null != contact.getImage1()) {
                final String timestamp = null != contact.getLastModified() ? String.valueOf(contact.getLastModified().getTime()) : null;
                try {
                    if (FolderObject.SYSTEM_LDAP_FOLDER_ID == contact.getParentFolderID() && contact.containsInternalUserId()) {
                        /*
                         * prefer user contact image url
                         */
                        final ImageLocation imageLocation = new ImageLocation.Builder().id(
                            String.valueOf(contact.getInternalUserId())).timestamp(timestamp).build();
                        return UserImageDataSource.getInstance().generateUrl(imageLocation, session);
                    } else {
                        /*
                         * use default contact image data source
                         */
                        final ImageLocation imageLocation = new ImageLocation.Builder().folder(String.valueOf(contact.getParentFolderID()))
                            .id(String.valueOf(contact.getObjectID())).timestamp(timestamp).build();
                        return ContactImageDataSource.getInstance().generateUrl(imageLocation, session);
                    }
                } catch (final OXException e) {
                    LOG.warn("Error generating contact image URL", e);
                }
            }
            return null;
        }

        private ContactSearchObject createContactSearchObject(final InternetAddress from) {
            final ContactSearchObject searchObject = new ContactSearchObject();
            // searchObject.addFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID); // Global address book
            searchObject.setOrSearch(true);
            final String address = from.getAddress();
            searchObject.setEmail1(address);
            searchObject.setEmail2(address);
            searchObject.setEmail3(address);
            return searchObject;
        }

    } // End of class DecoratorImpl

}
