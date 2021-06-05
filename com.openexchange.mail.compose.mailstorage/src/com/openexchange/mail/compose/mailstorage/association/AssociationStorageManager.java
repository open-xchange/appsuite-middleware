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

package com.openexchange.mail.compose.mailstorage.association;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.mailstorage.MailStorageCompositionSpaceConfig;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;


/**
 * {@link AssociationStorageManager} - The association storage manager implementation backed by Google Cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class AssociationStorageManager implements IAssociationStorageManager {

    private final LoadingCache<UserAndContext, AssociationStorage> user2Storages;

    /**
     * Initializes a new {@link AssociationStorageManager}.
     *
     * @throws OXException If initialization fails
     */
    public AssociationStorageManager() throws OXException {
        super();
        long maxIdleSeconds = MailStorageCompositionSpaceConfig.getInstance().getInMemoryCacheMaxIdleSeconds();
        user2Storages = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(maxIdleSeconds))
            .removalListener((RemovalNotification<UserAndContext, AssociationStorage> notification) -> {
                if (notification.wasEvicted()) {
                    AssociationStorage activeUserStorage = notification.getValue();
                    handleEvictedStorage(activeUserStorage);
                }
            })
            .build(new CacheLoader<UserAndContext, AssociationStorage>() {

                @Override
                public AssociationStorage load(UserAndContext key) throws Exception {
                    return new AssociationStorage(maxIdleSeconds);
                }
            });
    }

    void handleEvictedStorage(AssociationStorage activeUserStorage) {
        activeUserStorage.signalEviction();
    }

    /**
     * Signals shut-down to this association storage manager.
     */
    public void shutDown() {
        for (AssociationStorage activeUserStorage : user2Storages.asMap().values()) {
            handleEvictedStorage(activeUserStorage);
        }
        user2Storages.invalidateAll();
    }

    @Override
    public IAssociationStorage getStorageFor(Session session) throws OXException {
        try {
            return user2Storages.get(UserAndContext.newInstance(session));
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw OXException.general(new StringBuilder("Failed fetching association storage for user ").append(session.getUserId()).append(" in context ").append(session.getContextId()).toString(), cause == null ? e : cause);
        } catch (RuntimeException e) {
            throw OXException.general(new StringBuilder("Failed fetching association storage for user ").append(session.getUserId()).append(" in context ").append(session.getContextId()).toString(), e);
        }
    }

    @Override
    public Optional<IAssociationStorage> optStorageFor(Session session) {
        return Optional.ofNullable(user2Storages.getIfPresent(UserAndContext.newInstance(session)));
    }

}
