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

package com.openexchange.share.impl.subscription;

import java.util.Iterator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ShareSubscriptionRegistryImpl.class);

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
            LOGGER.trace("Found no provider for share link {}", shareLink);
            return unsupported(shareLink);
        }
        ShareLinkAnalyzeResult infos = provider.analyze(session, shareLink);
        if (null != infos) {
            return infos;
        }
        LOGGER.trace("Provider {} was unable to produce information about the share link {}", provider.getClass(), shareLink);
        return unsupported(shareLink);
    }

    @Override
    public ShareSubscriptionInformation subscribe(Session session, String shareLink, String shareName, String password) throws OXException {
        checkLinkIsUsable(shareLink);
        ShareSubscriptionProvider provider = getProvider(session, shareLink);
        if (null != provider) {
            return provider.subscribe(session, shareLink, shareName, password);
        }
        throw ShareSubscriptionExceptions.NOT_USABLE.create(shareLink);
    }

    @Override
    public ShareSubscriptionInformation resubscribe(Session session, String shareLink, String shareName, String password) throws OXException {
        checkLinkIsUsable(shareLink);
        ShareSubscriptionProvider provider = getProvider(session, shareLink);
        if (null != provider) {
            return provider.resubscribe(session, shareLink, shareName, password);
        }
        throw ShareSubscriptionExceptions.MISSING_SUBSCRIPTION.create(shareLink);
    }

    @Override
    public void unsubscribe(Session session, String shareLink) throws OXException {
        checkLinkIsUsable(shareLink);
        /*
         * Try all providers because the link might not be marked supported by the actual provider anymore
         */
        for (Iterator<ShareSubscriptionProvider> iterator = iterator(); iterator.hasNext();) {
            if (iterator.next().unsubscribe(session, shareLink)) {
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

    private ShareLinkAnalyzeResult unsupported(String shareLink) {
        return new ShareLinkAnalyzeResult(ShareLinkState.UNSUPPORTED, ShareSubscriptionExceptions.NOT_USABLE.create(shareLink), null);
    }

}
