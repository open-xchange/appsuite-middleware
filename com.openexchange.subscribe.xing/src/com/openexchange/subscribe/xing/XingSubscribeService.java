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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.FileTypeMap;
import org.apache.commons.logging.Log;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Streams;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.xing.session.XingOAuthAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.xing.Address;
import com.openexchange.xing.Contacts;
import com.openexchange.xing.User;
import com.openexchange.xing.UserField;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingUnlinkedException;

/**
 * {@link XingSubscribeService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class XingSubscribeService extends AbstractSubscribeService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(XingSubscribeService.class);

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
        source.setId("com.openexchange.subscribe.socialplugin.xing");
        source.setSubscribeService(this);

        final DynamicFormDescription form = new DynamicFormDescription();

        final FormElement oauthAccount = FormElement.custom("oauthAccount", "account", FormStrings.ACCOUNT_LABEL);
        oauthAccount.setOption("type", services.getService(OAuthServiceMetaData.class).getId());
        form.add(oauthAccount);

        source.setFormDescription(form);
        this.source = source;
    }

    private OAuthAccount getXingOAuthAccount(final Session session) throws OXException {
        OAuthAccount defaultAccount = (OAuthAccount) session.getParameter("com.openexchange.subscribe.xing.defaultAccount");
        if (null != defaultAccount) {
            return defaultAccount;
        }
        // Determine default XING access
        final OAuthService oAuthService = services.getService(OAuthService.class);
        defaultAccount = oAuthService.getDefaultAccount(API.XING, session);
        if (null != defaultAccount) {
            // Cache in session
            session.setParameter("com.openexchange.subscribe.xing.defaultAccount", defaultAccount);
        }
        return defaultAccount;
    }

    @Override
    public Collection<?> getContent(final Subscription subscription) throws OXException {
        try {
            final ServerSession session = subscription.getSession();
            final OAuthAccount xingOAuthAccount = getXingOAuthAccount(session);

            final XingOAuthAccess xingOAuthAccess = XingOAuthAccess.accessFor(xingOAuthAccount, session);
            final Contacts xingContacts = xingOAuthAccess.getXingAPI().getContactsFrom(
                xingOAuthAccess.getXingUserId(),
                UserField.ID,
                Arrays.asList(UserField.values()));

            final List<Contact> ret = new ArrayList<Contact>(xingContacts.getTotal());
            for (final User xingContact : xingContacts.getUsers()) {
                ret.add(convert(xingContact));
            }
            return ret;
        } catch (final XingUnlinkedException e) {
            throw XingSubscribeExceptionCodes.UNLINKED_ERROR.create();
        } catch (final XingException e) {
            throw XingSubscribeExceptionCodes.XING_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw XingSubscribeExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
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
                    LOG.error("subscription.getConfiguration().get(\"account\") is null. Complete configuration is : " + subscription.getConfiguration());
                }
            } else {
                LOG.error("subscription.getConfiguration() is null");
            }
        } else {
            LOG.error("subscription is null");
        }
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
            if (isEmpty(displayName)) {
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

    private Contact convert(final User xingUser) {
        if (null == xingUser) {
            return null;
        }
        final Contact oxContact = new Contact();
        {
            final String s = xingUser.getId();
            if (isNotNull(s)) {
                oxContact.setUserField20(s);
            }
        }
        {
            final String s = xingUser.getActiveMail();
            if (isNotNull(s)) {
                oxContact.setEmail1(s);
            }
        }
        {
            final String s = xingUser.getDisplayName();
            if (isNotNull(s)) {
                oxContact.setDisplayName(s);
            }
        }
        {
            final String s = xingUser.getFirstName();
            if (isNotNull(s)) {
                oxContact.setGivenName(s);
            }
        }
        {
            final String s = xingUser.getLastName();
            if (isNotNull(s)) {
                oxContact.setSurName(s);
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
                oxContact.setUserField02(s);
            }
        }
        {
            final String s = xingUser.getInterests();
            if (isNotNull(s)) {
                oxContact.setUserField01(s);
            }
        }
        {
            final String s = xingUser.getWants();
            if (isNotNull(s)) {
                oxContact.setUserField03(s);
            }
        }
        {
            final String s = xingUser.getOrganisationMember();
            if (isNotNull(s)) {
                oxContact.setPosition(s);
            }
        }
        {
            final String s = xingUser.getPermalink();
            if (isNotNull(s)) {
                oxContact.setURL(s);
            }
        }
        {
            final Date d = xingUser.getBirthDate();
            if (null != d) {
                oxContact.setBirthday(d);
            }
        }
        {
            final Address a = xingUser.getPrivateAddress();
            if (null != a) {
                String s = a.getCity();
                if (isNotNull(s)) {
                    oxContact.setCityHome(s);
                }
                s = a.getCountry();
                if (isNotNull(s)) {
                    oxContact.setCountryHome(s);
                }
                s = a.getEmail();
                if (isNotNull(s)) {
                    oxContact.setEmail3(s);
                }
                s = a.getFax();
                if (isNotNull(s)) {
                    oxContact.setFaxHome(s);
                }
                s = a.getMobilePhone();
                if (isNotNull(s)) {
                    oxContact.setCellularTelephone2(s);
                }
                s = a.getPhone();
                if (isNotNull(s)) {
                    oxContact.setTelephoneHome1(s);
                }
                s = a.getProvince();
                if (isNotNull(s)) {
                    oxContact.setStateHome(s);
                }
                s = a.getStreet();
                if (isNotNull(s)) {
                    oxContact.setStreetHome(s);
                }
                s = a.getZipCode();
                if (isNotNull(s)) {
                    oxContact.setPostalCodeHome(s);
                }
            }
        }
        {
            final Address a = xingUser.getBusinessAddress();
            if (null != a) {
                String s = a.getCity();
                if (isNotNull(s)) {
                    oxContact.setCityBusiness(s);
                }
                s = a.getCountry();
                if (isNotNull(s)) {
                    oxContact.setCountryBusiness(s);
                }
                s = a.getEmail();
                if (isNotNull(s)) {
                    oxContact.setEmail2(s);
                }
                s = a.getFax();
                if (isNotNull(s)) {
                    oxContact.setFaxBusiness(s);
                }
                s = a.getMobilePhone();
                if (isNotNull(s)) {
                    oxContact.setCellularTelephone1(s);
                }
                s = a.getPhone();
                if (isNotNull(s)) {
                    oxContact.setTelephoneBusiness1(s);
                }
                s = a.getProvince();
                if (isNotNull(s)) {
                    oxContact.setStateBusiness(s);
                }
                s = a.getStreet();
                if (isNotNull(s)) {
                    oxContact.setStreetBusiness(s);
                }
                s = a.getZipCode();
                if (isNotNull(s)) {
                    oxContact.setPostalCodeBusiness(s);
                }
            }
        }
        {
            final Map<String, String> instantMessagingAccounts = xingUser.getInstantMessagingAccounts();
            if (null != instantMessagingAccounts) {
                final String skypeId = instantMessagingAccounts.get("skype");
                if (isNotNull(skypeId)) {
                    oxContact.setInstantMessenger1(skypeId);
                }
                for (final Map.Entry<String, String> e : instantMessagingAccounts.entrySet()) {
                    if (!"skype".equals(e.getKey()) && !"null".equals(e.getValue())) {
                        oxContact.setInstantMessenger2(e.getValue());
                        break;
                    }
                }
            }

        }
        {
            final Map<String, Object> photoUrls = xingUser.getPhotoUrls();
            if (null != photoUrls) {
                final Object pic = photoUrls.get("maxi_thumb");
                if (null != pic && !"null".equals(pic.toString())) {
                    try {
                        final String sUrl = pic.toString();
                        loadImageFromURL(oxContact, sUrl);
                    } catch (final OXException e) {
                        final Throwable cause = e.getCause();
                        LOG.warn("Couldn't load XING user's image", null == cause ? e : cause);
                    }
                }
            }
        }
        return oxContact;
    }

    private boolean isNotNull(final String s) {
        return null != s && !"null".equals(s);
    }

    private boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection and put into given {@link Contact contact container}.
     *
     * @param contact The contact container to fill
     * @param url The URI parameter's value
     * @throws OXException If converting image's data fails
     */
    private void loadImageFromURL(final Contact contact, final String url) throws OXException {
        try {
            loadImageFromURL(contact, new URL(url));
        } catch (final MalformedURLException e) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection and put into given {@link Contact contact container}.
     *
     * @param contact The contact container to fill
     * @param url The image URL
     * @throws OXException If converting image's data fails
     */
    private void loadImageFromURL(final Contact contact, final URL url) throws OXException {
        String mimeType = null;
        byte[] bytes = null;
        try {
            final URLConnection urlCon = url.openConnection();
            urlCon.setConnectTimeout(2500);
            urlCon.setReadTimeout(2500);
            urlCon.connect();
            mimeType = urlCon.getContentType();
            final InputStream in = urlCon.getInputStream();
            try {
                final ByteArrayOutputStream buffer = Streams.newByteArrayOutputStream(in.available());
                transfer(in, buffer);
                bytes = buffer.toByteArray();
            } finally {
                Streams.close(in);
            }
        } catch (final SocketTimeoutException e) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        if (mimeType == null) {
            mimeType = ImageTypeDetector.getMimeType(bytes);
            if ("application/octet-stream".equals(mimeType)) {
                mimeType = getMimeType(url.toString());
            }
        }
        if (bytes != null && isValidImage(bytes)) {
            // Mime type should be of image type. Otherwise web server send some error page instead of 404 error code.
            contact.setImage1(bytes);
            contact.setImageContentType(mimeType);
        }
    }

    private static final FileTypeMap DEFAULT_FILE_TYPE_MAP = FileTypeMap.getDefaultFileTypeMap();

    private String getMimeType(final String filename) {
        return DEFAULT_FILE_TYPE_MAP.getContentType(filename);
    }

    private boolean isValidImage(final byte[] data) {
        java.awt.image.BufferedImage bimg = null;
        try {
            bimg = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(data));
        } catch (final Exception e) {
            return false;
        }
        return (bimg != null);
    }

    private static void transfer(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        out.flush();
    }

}
