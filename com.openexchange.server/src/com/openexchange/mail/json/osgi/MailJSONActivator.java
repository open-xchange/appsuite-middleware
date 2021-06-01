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

package com.openexchange.mail.json.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXResultDecorator;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Reloadable;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.tree.modules.mail.AllMessagesFolder;
import com.openexchange.groupware.settings.tree.modules.mail.DeleteDraftOnTransport;
import com.openexchange.groupware.settings.tree.modules.mail.ForwardUnquoted;
import com.openexchange.groupware.settings.tree.modules.mail.IgnoreSubscription;
import com.openexchange.groupware.settings.tree.modules.mail.MailColorModePreferenceItem;
import com.openexchange.groupware.settings.tree.modules.mail.MailFlaggedModePreferenceItem;
import com.openexchange.groupware.settings.tree.modules.mail.MaliciousCheck;
import com.openexchange.groupware.settings.tree.modules.mail.MaliciousListing;
import com.openexchange.groupware.settings.tree.modules.mail.MaxMailSize;
import com.openexchange.groupware.settings.tree.modules.mail.Whitelist;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.mail.MailFetchListener;
import com.openexchange.mail.MailFetchListenerRegistry;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.crypto.CryptographicAwareMailAccessFactory;
import com.openexchange.mail.attachment.storage.DefaultMailAttachmentStorage;
import com.openexchange.mail.attachment.storage.DefaultMailAttachmentStorageRegistry;
import com.openexchange.mail.attachment.storage.MailAttachmentStorage;
import com.openexchange.mail.attachment.storage.MailAttachmentStorageRegistry;
import com.openexchange.mail.authenticity.CustomPropertyJsonHandler;
import com.openexchange.mail.authenticity.GenericCustomPropertyJsonHandler;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.internal.MailCategoriesPreferenceItem;
import com.openexchange.mail.compose.old.OldCompositionSpace;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mail.config.MaliciousFolders;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailActionFactory;
import com.openexchange.mail.json.OAuthScopeDescription;
import com.openexchange.mail.json.compose.ComposeHandler;
import com.openexchange.mail.json.compose.ComposeHandlerRegistry;
import com.openexchange.mail.json.compose.internal.ComposeHandlerRegistryImpl;
import com.openexchange.mail.json.converters.MailConverter;
import com.openexchange.mail.json.converters.MailJSONConverter;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.crypto.PGPMailRecognizer;
import com.openexchange.mail.service.EncryptedMailService;
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
        return new Class<?>[] { MailCategoriesConfigService.class, CapabilityService.class, CryptographicServiceAuthenticationFactory.class, CryptographicAwareMailAccessFactory.class, EncryptedMailService.class, PGPMailRecognizer.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceLookup serviceLookup = new ExceptionOnAbsenceServiceLookup(this);
        SERVICES.set(serviceLookup);

        trackService(Dispatcher.class);

        final BundleContext context = this.context;

        // Tracker for CapabilityService that declares "publish_mail_attachments" capability
        track(CapabilityService.class, new CapabilitiesTracker(context));

        ComposeHandlerRegistry composeHandlerRegisty;
        {
            RankingAwareNearRegistryServiceTracker<ComposeHandler> tracker = new RankingAwareNearRegistryServiceTracker<>(context, ComposeHandler.class);
            rememberTracker(tracker);
            composeHandlerRegisty = new ComposeHandlerRegistryImpl(tracker);
        }

        MimeMailExceptionHandlerTracker exceptionHandlerTracker = new MimeMailExceptionHandlerTracker(context);
        rememberTracker(exceptionHandlerTracker);
        MimeMailException.setExceptionHandlers(exceptionHandlerTracker);

        RankingAwareNearRegistryServiceTracker<MailFetchListener> listing = new RankingAwareNearRegistryServiceTracker<MailFetchListener>(context, MailFetchListener.class);
        MailFetchListenerRegistry.initInstance(listing);
        rememberTracker(listing);
        track(CustomPropertyJsonHandler.class);
        trackService(AntiVirusService.class);
        openTrackers();

        registerService(CustomPropertyJsonHandler.class, new GenericCustomPropertyJsonHandler());
        registerService(ComposeHandlerRegistry.class, composeHandlerRegisty);

        DefaultMailAttachmentStorageRegistry.initInstance(context);
        registerService(MailAttachmentStorageRegistry.class, DefaultMailAttachmentStorageRegistry.getInstance());

        registerService(MailAttachmentStorage.class, new DefaultMailAttachmentStorage(this), withRanking(0));

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
                    OldCompositionSpace.dropCompositionSpaces(session);
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
                MaliciousFolders.invalidateCache();
                MailProperties.invalidateCache();
            }

        });

        MailCategoriesPreferenceItem item = new MailCategoriesPreferenceItem(this);
        registerService(PreferencesItemService.class, item);
        registerService(ConfigTreeEquivalent.class, item);

        final ContactField[] fields = new ContactField[] { ContactField.OBJECT_ID, ContactField.INTERNAL_USERID, ContactField.FOLDER_ID, ContactField.NUMBER_OF_IMAGES };
        registerService(AJAXResultDecorator.class, new DecoratorImpl(converter, fields, this));

        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(MailActionFactory.OAUTH_READ_SCOPE, OAuthScopeDescription.OAUTH_READ_SCOPE) {

            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.WEBMAIL.getCapabilityName());
            }
        });

        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(MailActionFactory.OAUTH_WRITE_SCOPE, OAuthScopeDescription.OAUTH_WRITE_SCOPE) {

            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.WEBMAIL.getCapabilityName());
            }
        });

        MailColorModePreferenceItem colorItem = new MailColorModePreferenceItem(); // --> Statically registered via ConfigTree class
        registerService(ConfigTreeEquivalent.class, colorItem);

        MailFlaggedModePreferenceItem flaggedItem = new MailFlaggedModePreferenceItem(); // --> Statically registered via ConfigTree class
        registerService(ConfigTreeEquivalent.class, flaggedItem);

        MaliciousCheck maliciousCheck = new MaliciousCheck(); // --> Statically registered via ConfigTree class
        registerService(ConfigTreeEquivalent.class, maliciousCheck);

        MaliciousListing maliciousListing = new MaliciousListing(); // --> Statically registered via ConfigTree class
        registerService(ConfigTreeEquivalent.class, maliciousListing);

        AllMessagesFolder allMessagesFolder = new AllMessagesFolder(); // --> Statically registered via ConfigTree class
        registerService(ConfigTreeEquivalent.class, allMessagesFolder);

        Whitelist whitelist = new Whitelist(); // --> Statically registered via ConfigTree class
        registerService(ConfigTreeEquivalent.class, whitelist);

        ForwardUnquoted forwardUnquoted = new ForwardUnquoted(); // --> Statically registered via ConfigTree class
        registerService(ConfigTreeEquivalent.class, forwardUnquoted);

        IgnoreSubscription ignoreSubscription = new IgnoreSubscription(); // --> Statically registered via ConfigTree class
        registerService(ConfigTreeEquivalent.class, ignoreSubscription);

        DeleteDraftOnTransport deleteDraftOnTransport = new DeleteDraftOnTransport(); // --> Statically registered via ConfigTree class
        registerService(ConfigTreeEquivalent.class, deleteDraftOnTransport);

        MaxMailSize maxMailSize = new MaxMailSize(); // --> Statically registered via ConfigTree class
        registerService(ConfigTreeEquivalent.class, maxMailSize);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        MimeMailException.unsetExceptionHandlers();
        ServerServiceRegistry.getInstance().removeService(ComposeHandlerRegistry.class);
        DefaultMailAttachmentStorageRegistry.dropInstance();
        MailActionFactory.releaseActionFactory();
        MailFetchListenerRegistry.releaseInstance();
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
        public void decorate(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session) throws OXException {
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
                } catch (JSONException e) {
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
        private String getImageURL(ServerSession session, Contact contact) {
            if (0 < contact.getNumberOfImages() || contact.containsImage1() && null != contact.getImage1()) {
                try {
                    return ContactUtil.generateImageUrl(session, contact);
                } catch (OXException e) {
                    LOG.warn("Error generating contact image URL", e);
                }
            }
            return null;
        }

        private ContactSearchObject createContactSearchObject(InternetAddress from) {
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
