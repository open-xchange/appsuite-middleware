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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.share.impl.subscription;

import java.util.Iterator;
import org.osgi.framework.BundleContext;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.session.Session;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult;
import com.openexchange.share.subscription.ShareLinkState;
import com.openexchange.share.subscription.ShareSubscriptionExceptions;
import com.openexchange.share.subscription.ShareSubscriptionInformation;
import com.openexchange.share.subscription.ShareSubscriptionProvider;
import com.openexchange.share.subscription.ShareSubscriptionRegistry;

/**
 * {@link ShareSubscriptionRegistryImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ShareSubscriptionRegistryImpl extends RankingAwareNearRegistryServiceTracker<ShareSubscriptionProvider> implements ShareSubscriptionRegistry {

    private static final ShareLinkAnalyzeResult UNRESOLVABLE = new ShareLinkAnalyzeResult(ShareLinkState.UNRESOLVABLE, null);

    /**
     * Initializes a new {@link ShareSubscriptionRegistryImpl}.
     * 
     * @param context The bundle context
     */
    public ShareSubscriptionRegistryImpl(BundleContext context) {
        super(context, ShareSubscriptionProvider.class, 100);
    }

    @Override
    public ShareLinkAnalyzeResult analyze(Session session, String shareLink) throws OXException {
        checkLinkIsUsable(shareLink);
        ShareSubscriptionProvider provider = getProvider(session, shareLink);
        if (null == provider) {
            return UNRESOLVABLE;
        }
        ShareLinkAnalyzeResult infos = provider.analyze(session, shareLink);
        if (null != infos) {
            return infos;
        }
        return UNRESOLVABLE;
    }

    @Override
    public ShareSubscriptionInformation mount(Session session, String shareLink, String shareName, String password) throws OXException {
        checkLinkIsUsable(shareLink);
        ShareSubscriptionProvider provider = getProvider(session, shareLink);
        if (null != provider) {
            return provider.mount(session, shareLink, shareName, password);
        }
        throw ShareSubscriptionExceptions.NOT_USABLE.create(shareLink);
    }

    @Override
    public ShareSubscriptionInformation remount(Session session, String shareLink, String shareName, String password) throws OXException {
        checkLinkIsUsable(shareLink);
        ShareSubscriptionProvider provider = getProvider(session, shareLink);
        if (null != provider) {
            return provider.remount(session, shareLink, shareName, password);
        }
        throw ShareSubscriptionExceptions.MISSING_SUBSCRIPTION.create(shareLink);
    }

    @Override
    public void unmount(Session session, String shareLink) throws OXException {
        checkLinkIsUsable(shareLink);
        /*
         * Try all providers because the link might not be marked supported by the actual provider anymore
         */
        for (Iterator<ShareSubscriptionProvider> iterator = iterator(); iterator.hasNext();) {
            if (iterator.next().unmount(session, shareLink)) {
                return;
            }
        }
        throw ShareSubscriptionExceptions.MISSING_SUBSCRIPTION.create(shareLink);
    }

    /*
     * ============================== HELPERS ==============================
     */

    private void checkLinkIsUsable(String shareLink) throws OXException {
        if (Strings.isEmpty(shareLink)) {
            throw ShareSubscriptionExceptions.MISSING_LINK.create();
        }
    }

    /**
     * Returns the provider for the link
     *
     * @param shareLink The share link
     * @return A provider or <code>null</code>
     */
    private ShareSubscriptionProvider getProvider(Session session, String shareLink) {
        for (Iterator<ShareSubscriptionProvider> iterator = iterator(); iterator.hasNext();) {
            ShareSubscriptionProvider provider = iterator.next();
            if (provider.isSupported(session, shareLink)) {
                return provider;
            }
        }
        return null;
    }

}
