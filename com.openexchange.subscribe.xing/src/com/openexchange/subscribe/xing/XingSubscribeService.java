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

package com.openexchange.subscribe.xing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.xing.Address;
import com.openexchange.xing.Contacts;
import com.openexchange.xing.PhotoUrls;
import com.openexchange.xing.User;
import com.openexchange.xing.UserField;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.access.XingOAuthAccessProvider;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingUnlinkedException;
import com.openexchange.xing.session.WebAuthSession;

/**
 * {@link XingSubscribeService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class XingSubscribeService extends AbstractSubscribeService {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(XingSubscribeService.class);

    // -------------------------------------------------------------------------------------------------------------------------- //

    private interface PhotoHandler {

        void handlePhoto(User xingUser, Contact contact, ServerSession session) throws OXException;
    }

    private final class CollectingPhotoHandler implements PhotoHandler {

        private final Map<String, String> photoUrlsMap;

        /**
         * Initializes a new {@link CollectingPhotoHandler}.
         */
        CollectingPhotoHandler(Map<String, String> photoUrlsMap) {
            super();
            this.photoUrlsMap = photoUrlsMap;
        }

        @Override
        public void handlePhoto(User xingUser, Contact contact, ServerSession session) throws OXException {
            final PhotoUrls photoUrls = xingUser.getPhotoUrls();
            String url = photoUrls.getMaxiThumbUrl();
            if (url == null) {
                url = photoUrls.getLargestAvailableUrl();
            }

            final String id = xingUser.getId();
            if (url != null && isNotNull(id)) {
                photoUrlsMap.put(id, url);
            }
        }
    }

    private final PhotoHandler loadingPhotoHandler = new PhotoHandler() {

        @Override
        public void handlePhoto(final User xingUser, final Contact contact, ServerSession session) throws OXException {
            if (null == xingUser || null == contact) {
                return;
            }

            final PhotoUrls photoUrls = xingUser.getPhotoUrls();
            String url = photoUrls.getMaxiThumbUrl();
            if (url == null) {
                url = photoUrls.getLargestAvailableUrl();
            }

            if (url != null) {
                XingOAuthAccess xingAccess = getXingOAuthAccess(session);
                XingAPI<WebAuthSession> api = xingAccess.getXingAPI();
                try {
                    IFileHolder photo = api.getPhoto(url);
                    if (photo != null) {
                        byte[] bytes = Streams.stream2bytes(photo.getStream());
                        contact.setImage1(bytes);
                        contact.setImageContentType(photo.getContentType());
                    }
                } catch (XingException e) {
                    throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                } catch (IOException e) {
                    throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        }
    };

    // -------------------------------------------------------------------------------------------------------------------------- //

    private final ServiceLookup services;
    private final SubscriptionSource source;

    /**
     * Initializes a new {@link XingSubscribeService}.
     *
     * @param services The service look-up
     */
    public XingSubscribeService(final ServiceLookup services) {
        super();
        this.services = services;

        final SubscriptionSource source = new SubscriptionSource();
        source.setDisplayName("XING");
        source.setFolderModule(FolderObject.CONTACT);
        source.setId("com.openexchange.subscribe.xing");
        source.setSubscribeService(this);

        final DynamicFormDescription form = new DynamicFormDescription();

        final FormElement oauthAccount = FormElement.custom("oauthAccount", "account", FormStrings.ACCOUNT_LABEL);
        oauthAccount.setOption("type", services.getService(OAuthServiceMetaData.class).getId());
        form.add(oauthAccount);

        source.setFormDescription(form);
        this.source = source;
    }

    /**
     * Gets the XING OAuth access.
     *
     * @param session The associated session
     * @return The XING OAuth access
     * @throws OXException If XING OAuth access cannot be returned
     */
    protected XingOAuthAccess getXingOAuthAccess(final ServerSession session) throws OXException {
        final XingOAuthAccessProvider provider = services.getService(XingOAuthAccessProvider.class);
        if (null == provider) {
            throw ServiceExceptionCode.absentService(XingOAuthAccessProvider.class);
        }

        final int xingOAuthAccount = provider.getXingOAuthAccount(session);
        return provider.accessFor(xingOAuthAccount, session);
    }

    @Override
    public Collection<?> getContent(final Subscription subscription) throws OXException {
        try {
            final ServerSession session = subscription.getSession();
            final XingOAuthAccess xingOAuthAccess = getXingOAuthAccess(session);
            final XingAPI<WebAuthSession> xingAPI = xingOAuthAccess.getXingAPI();
            final String userId = xingOAuthAccess.getXingUserId();
            final List<UserField> userFields = Arrays.asList(UserField.values());

            // Request first chunk to determine total number of contacts
            final int firstChunkLimit = 25;
            final Contacts contacts = xingAPI.getContactsFrom(userId, firstChunkLimit, 0, null, userFields);
            List<User> chunk = contacts.getUsers();
            if (chunk.size() < firstChunkLimit) {
                // Obtained less than requested; no more contacts available then
                return convert(chunk, loadingPhotoHandler, session);
            }
            final int maxLimit = 25;
            final int total = contacts.getTotal();
            // Check availability of tracked services needed for manual storing for contacts
            final FolderUpdaterRegistry folderUpdaterRegistry = Services.getOptionalService(FolderUpdaterRegistry.class);
            final ThreadPoolService threadPool = Services.getOptionalService(ThreadPoolService.class);
            final FolderUpdaterService<Contact> folderUpdater = null == folderUpdaterRegistry ? null : folderUpdaterRegistry.<Contact> getFolderUpdater(subscription);
            if (null == threadPool || null == folderUpdater) {
                // Retrieve all
                final List<User> users = new ArrayList<User>(total);
                users.addAll(chunk);
                int offset = chunk.size();
                // Request remaining chunks
                while (offset < total) {
                    final int remain = total - offset;
                    chunk = xingAPI.getContactsFrom(userId, remain > maxLimit ? maxLimit : remain, offset, null, userFields).getUsers();
                    users.addAll(chunk);
                    offset += chunk.size();
                }
                // All retrieved
                LOG.info("Going to converted {} XING contacts for user {} in context {}", total, session.getUserId(), session.getContextId());
                final Map<String, String> photoUrlsMap = new HashMap<String, String>(total);
                final PhotoHandler photoHandler = new CollectingPhotoHandler(photoUrlsMap);
                final List<Contact> retval = convert(chunk, photoHandler, session);
                LOG.info("Converted {} XING contacts for user {} in context {}", total, session.getUserId(), session.getContextId());

                // TODO: Schedule a separate task to fill photos

                return retval;
            }
            // Schedule task for remainder...
            final int startOffset = chunk.size();
            threadPool.submit(new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    int off = startOffset;
                    while (off < total) {
                        final int remain = total - off;
                        final List<User> chunk = xingAPI.getContactsFrom(userId, remain > maxLimit ? maxLimit : remain, off, null, userFields).getUsers();
                        // Store them
                        final List<Contact> convertees = convert(chunk, loadingPhotoHandler, session);
                        LOG.info("Converted {} XING contacts for user {} in context {}", chunk.size(), session.getUserId(), session.getContextId());
                        folderUpdater.save(new SearchIteratorDelegator<Contact>(convertees), subscription);
                        // Next chunk...
                        off += chunk.size();
                    }
                    return null;
                }
            });
            // Return first chunk with this thread
            return convert(chunk, loadingPhotoHandler, session);
        } catch (final XingUnlinkedException e) {
            throw XingExceptionCodes.UNLINKED_ERROR.create();
        } catch (final XingException e) {
            throw XingExceptionCodes.XING_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw XingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Converts specified XING users to contacts
     *
     * @param xingContacts The XING users
     * @param optPhotoHandler The photo handler
     * @return The resulting contacts
     */
    protected List<Contact> convert(final List<User> xingContacts, final PhotoHandler optPhotoHandler, final ServerSession session) {
        final List<Contact> ret = new ArrayList<Contact>(xingContacts.size());
        for (final User xingContact : xingContacts) {
            ret.add(convert(xingContact, optPhotoHandler, session));
        }
        return ret;
    }

    @Override
    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    @Override
    public boolean handles(final int folderModule) {
        return FolderObject.CONTACT == folderModule;
    }

    @Override
    public void modifyIncoming(final Subscription subscription) throws OXException {
        if (subscription != null) {
            super.modifyIncoming(subscription);
            if (subscription.getConfiguration() != null) {
                final Object accountId = subscription.getConfiguration().get("account");
                if (accountId != null) {
                    subscription.getConfiguration().put("account", accountId.toString());
                } else {
                    LOG.error("subscription.getConfiguration().get(\"account\") is null. Complete configuration is : {}", subscription.getConfiguration());
                }
            } else {
                LOG.error("subscription.getConfiguration() is null");
            }
        } else {
            LOG.error("subscription is null");
        }
    }

    /**
     * Gets the XING OAuth account.
     *
     * @param session The associated session
     * @return The XING OAuth account
     * @throws OXException If XING OAuth account cannot be returned
     */
    protected OAuthAccount getXingOAuthAccount(final ServerSession session) throws OXException {
        final OAuthService oAuthService = services.getService(OAuthService.class);
        if (null == oAuthService) {
            throw ServiceExceptionCode.absentService(OAuthService.class);
        }
        return oAuthService.getDefaultAccount(API.XING, session);
    }

    @Override
    public void modifyOutgoing(final Subscription subscription) throws OXException {
        final String accountId = (String) subscription.getConfiguration().get("account");
        if (null != accountId) {
            final Integer accountIdInt = Integer.valueOf(accountId);
            if (null != accountIdInt) {
                subscription.getConfiguration().put("account", accountIdInt);
            }
            String displayName = null;
            if (subscription.getSecret() != null) {
                displayName = getXingOAuthAccount(subscription.getSession()).getDisplayName();
            }
            if (com.openexchange.java.Strings.isEmpty(displayName)) {
                subscription.setDisplayName("XING");
            } else {
                subscription.setDisplayName(displayName);
            }

        }
        super.modifyOutgoing(subscription);
    }

    public void deleteAllUsingOAuthAccount(final Context context, final int id) throws OXException {
        final Map<String, Object> query = new HashMap<String, Object>();
        query.put("account", Integer.toString(id));
        removeWhereConfigMatches(context, query);
    }

    private Contact convert(final User xingUser, final PhotoHandler optPhotoHandler, final ServerSession session) {
        if (null == xingUser) {
            return null;
        }
        final Contact oxContact = new Contact();
        boolean email1Set = false;
        {
            final String s = xingUser.getActiveMail();
            if (isNotNull(s)) {
                oxContact.setEmail1(s);
                email1Set = true;
            }
        }
        {
            final String s = xingUser.getDisplayName();
            if (isNotNull(s)) {
                oxContact.setDisplayName(Strings.abbreviate(s, 320));
            }
        }
        {
            final String s = xingUser.getFirstName();
            if (isNotNull(s)) {
                oxContact.setGivenName(Strings.abbreviate(s, 128));
            }
        }
        {
            final String s = xingUser.getLastName();
            if (isNotNull(s)) {
                oxContact.setSurName(Strings.abbreviate(s, 128));
            }
        }
        {
            final String s = xingUser.getGender();
            if (isNotNull(s)) {
                oxContact.setTitle("m".equals(s) ? "Mr." : "Mrs.");
            }
        }
        {
            final String s = xingUser.getHaves();
            if (isNotNull(s)) {
                oxContact.setUserField02(Strings.abbreviate(s, 64));
            }
        }
        {
            final String s = xingUser.getInterests();
            if (isNotNull(s)) {
                oxContact.setUserField01(Strings.abbreviate(s, 64));
            }
        }
        {
            final String s = xingUser.getWants();
            if (isNotNull(s)) {
                oxContact.setUserField03(Strings.abbreviate(s, 64));
            }
        }
        {
            final Map<String, Object> m = xingUser.getProfessionalExperience();
            if (null != m && !m.isEmpty()) {
                final Map<String, Object> primaryCompany = (Map<String, Object>) m.get("primary_company");
                if (null != primaryCompany && !primaryCompany.isEmpty()) {
                    // Name
                    Object s = primaryCompany.get("name");
                    if (isNotNull(s)) {
                        oxContact.setCompany(Strings.abbreviate(s.toString(), 512));
                    }

                    // Title
                    s = primaryCompany.get("title");
                    if (isNotNull(s)) {
                        oxContact.setPosition(Strings.abbreviate(s.toString(), 128));
                    }
                }
            }
        }
        {
            final String s = xingUser.getOrganisationMember();
            if (isNotNull(s)) {
                oxContact.setUserField04(Strings.abbreviate(s, 64));
            }
        }
        {
            final String s = xingUser.getPermalink();
            if (isNotNull(s)) {
                oxContact.setURL(Strings.abbreviate(s, 128));
            }
        }
        {
            final Date d = xingUser.getBirthDate();
            if (null != d) {
                oxContact.setBirthday(d);
            }
        }
        boolean email2Set = false;
        {
            final Address a = xingUser.getBusinessAddress();
            if (null != a) {
                String s = a.getCity();
                if (isNotNull(s)) {
                    oxContact.setCityBusiness(Strings.abbreviate(s, 64));
                }
                s = a.getCountry();
                if (isNotNull(s)) {
                    oxContact.setCountryBusiness(Strings.abbreviate(s, 64));
                }
                s = a.getEmail();
                if (isNotNull(s)) {
                    if (email1Set) {
                        oxContact.setEmail2(Strings.abbreviate(s, 256));
                        email2Set = true;
                    } else {
                        oxContact.setEmail1(Strings.abbreviate(s, 256));
                        email1Set = true;
                    }
                }
                s = a.getFax();
                if (isNotNull(s)) {
                    oxContact.setFaxBusiness(Strings.abbreviate(s, 64));
                }
                s = a.getMobilePhone();
                if (isNotNull(s)) {
                    oxContact.setCellularTelephone1(Strings.abbreviate(s, 64));
                }
                s = a.getPhone();
                if (isNotNull(s)) {
                    oxContact.setTelephoneBusiness1(Strings.abbreviate(s, 64));
                }
                s = a.getProvince();
                if (isNotNull(s)) {
                    oxContact.setStateBusiness(Strings.abbreviate(s, 64));
                }
                s = a.getStreet();
                if (isNotNull(s)) {
                    oxContact.setStreetBusiness(Strings.abbreviate(s, 64));
                }
                s = a.getZipCode();
                if (isNotNull(s)) {
                    oxContact.setPostalCodeBusiness(Strings.abbreviate(s, 64));
                }
            }
        }
        {
            final Address a = xingUser.getPrivateAddress();
            if (null != a) {
                String s = a.getCity();
                if (isNotNull(s)) {
                    oxContact.setCityHome(Strings.abbreviate(s, 64));
                }
                s = a.getCountry();
                if (isNotNull(s)) {
                    oxContact.setCountryHome(Strings.abbreviate(s, 64));
                }
                s = a.getEmail();
                if (isNotNull(s)) {
                    if (email1Set) {
                        if (email2Set) {
                            oxContact.setEmail3(Strings.abbreviate(s, 256));
                        } else {
                            oxContact.setEmail2(Strings.abbreviate(s, 256));
                            email2Set = true;
                        }
                    } else {
                        oxContact.setEmail1(Strings.abbreviate(s, 256));
                        email1Set = true;
                    }
                }
                s = a.getFax();
                if (isNotNull(s)) {
                    oxContact.setFaxHome(Strings.abbreviate(s, 64));
                }
                s = a.getMobilePhone();
                if (isNotNull(s)) {
                    oxContact.setCellularTelephone2(Strings.abbreviate(s, 64));
                }
                s = a.getPhone();
                if (isNotNull(s)) {
                    oxContact.setTelephoneHome1(Strings.abbreviate(s, 64));
                }
                s = a.getProvince();
                if (isNotNull(s)) {
                    oxContact.setStateHome(Strings.abbreviate(s, 64));
                }
                s = a.getStreet();
                if (isNotNull(s)) {
                    oxContact.setStreetHome(Strings.abbreviate(s, 64));
                }
                s = a.getZipCode();
                if (isNotNull(s)) {
                    oxContact.setPostalCodeHome(Strings.abbreviate(s, 64));
                }
            }
        }
        {
            final Map<String, String> instantMessagingAccounts = xingUser.getInstantMessagingAccounts();
            if (null != instantMessagingAccounts) {
                final String skypeId = instantMessagingAccounts.get("skype");
                if (isNotNull(skypeId)) {
                    oxContact.setInstantMessenger1(Strings.abbreviate(skypeId, 64));
                }
                for (final Map.Entry<String, String> e : instantMessagingAccounts.entrySet()) {
                    if (!"skype".equals(e.getKey()) && !"null".equals(e.getValue())) {
                        oxContact.setInstantMessenger2(Strings.abbreviate(e.getValue(), 64));
                        break;
                    }
                }
            }

        }
        if (null != optPhotoHandler) {
            try {
                optPhotoHandler.handlePhoto(xingUser, oxContact, session);
            } catch (final Exception e) {
                LOG.warn("Could not handle photo from XING contact {} ({}).", xingUser.getDisplayName(), xingUser.getId());
            }
        }
        return oxContact;
    }

    protected boolean isNotNull(Object s) {
        return null != s && !"null".equals(s.toString());
    }
}
