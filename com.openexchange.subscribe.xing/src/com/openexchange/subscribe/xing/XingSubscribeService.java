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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.xing.session.XingOAuthAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.xing.Contacts;
import com.openexchange.xing.OrderBy;
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
    private final SubscriptionSource source = new SubscriptionSource();

    /**
     * Initializes a new {@link XingSubscribeService}.
     * 
     * @param services The service look-up
     */
    public XingSubscribeService(final ServiceLookup services) {
        super();
        this.services = services;

        source.setDisplayName("XING");
        source.setFolderModule(FolderObject.CONTACT);
        source.setId("com.openexchange.subscribe.socialplugin.xing");
        source.setSubscribeService(this);

        final DynamicFormDescription form = new DynamicFormDescription();

        final FormElement oauthAccount = FormElement.custom("oauthAccount", "account", FormStrings.ACCOUNT_LABEL);
        oauthAccount.setOption("type", services.getService(OAuthServiceMetaData.class).getId());
        form.add(oauthAccount);

        source.setFormDescription(form);
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
                OrderBy.ID,
                Arrays.asList(UserField.values()));

            // TODO: Convert to contacts

            return null;
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

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
