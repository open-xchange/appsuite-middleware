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

package com.openexchange.subscribe.oauth;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.association.OAuthAccountAssociation;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionStorage;

/**
 * {@link AbstractSubscribeOAuthAccountAssociationProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractSubscribeOAuthAccountAssociationProvider implements OAuthAccountAssociationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSubscribeOAuthAccountAssociationProvider.class);

    private static final String TREE_ID = "1";
    private final String sourceId;
    protected final ServiceLookup services;

    /**
     * Initialises a new {@link AbstractSubscribeOAuthAccountAssociationProvider}.
     */
    public AbstractSubscribeOAuthAccountAssociationProvider(String sourceId, ServiceLookup services) {
        super();
        this.sourceId = sourceId;
        this.services = services;
    }

    @Override
    public Collection<OAuthAccountAssociation> getAssociationsFor(int accountId, Session session) throws OXException {
        Collection<OAuthAccountAssociation> associations = null;
        FolderService folderService = services.getService(FolderService.class);
        for (Subscription subscription : getSubscriptionsOfUser(session)) {
            try {
                if (OAuthUtil.getAccountId(subscription.getConfiguration()) != accountId) {
                    continue;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.debug("No association found between subscription {} and oauth account {} for user {} in context {}.", I(subscription.getId()), I(accountId), I(session.getUserId()), I(session.getContextId()), e);
                continue;
            }
            if (null == associations) {
                associations = new LinkedList<>();
            }
            // Unfortunately the display name of the folder is not stored in the Subscription metadata
            // hence we have to fetch it from the folder service.
            Folder folder = folderService.getFolder(TREE_ID, subscription.getFolderId(), session, null);
            associations.add(createAssociation(accountId, session.getUserId(), session.getContextId(), folder.getName(), subscription));
        }
        return null == associations ? Collections.<OAuthAccountAssociation> emptyList() : associations;
    }

    /**
     * Creates a new {@link OAuthAccountAssociation} for the specified {@link Subscription}
     *
     * @param accountId The OAuthAccount identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param subscription The subscription
     */
    protected abstract OAuthAccountAssociation createAssociation(int accountId, int userId, int contextId, String folderName, Subscription subscription);

    /**
     * Returns a {@link List} with all {@link Subscription}s of the user
     *
     * @param session the groupware {@link Session}
     * @return A {@link List} with all {@link Subscription}s of the user.
     * @throws OXException if an error is occurred
     */
    protected List<Subscription> getSubscriptionsOfUser(Session session) throws OXException {
        SubscriptionStorage subscriptionStorage = AbstractSubscribeService.STORAGE.get();
        ContextService contextService = services.getService(ContextService.class);
        return subscriptionStorage.getSubscriptionsOfUser(contextService.getContext(session.getContextId()), session.getUserId(), sourceId);
    }
}
