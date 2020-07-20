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

package com.openexchange.mail.compose.mailstorage.association;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.util.UUIDs.getUnformattedString;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.mail.compose.mailstorage.MailStorageCompositionSpaceConfig;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;

/**
 * {@link AssociationStorage} - The association storage implementation backed by Google Cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class AssociationStorage implements IAssociationStorage {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AssociationStorage.class);
    }

    private final LoadingCache<UserAndContext, Cache<UUID, CompositionSpaceToDraftAssociation>> user2Spaces;
    final AtomicReference<CompositionSpaceServiceFactory> compositionSpaceServiceFactoryReference;

    /**
     * Initializes a new {@link AssociationStorage}.
     * @throws OXException
     */
    public AssociationStorage() throws OXException {
        super();
        long maxIdleSeconds = MailStorageCompositionSpaceConfig.getInstance().getInMemoryCacheMaxIdleSeconds();
        AtomicReference<CompositionSpaceServiceFactory> compositionSpaceServiceFactoryReference = new AtomicReference<CompositionSpaceServiceFactory>(null);
        this.compositionSpaceServiceFactoryReference = compositionSpaceServiceFactoryReference;
        user2Spaces = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(maxIdleSeconds))
            .removalListener((RemovalNotification<UserAndContext, Cache<UUID, CompositionSpaceToDraftAssociation>> notification) -> {
                if (notification.wasEvicted()) {
                    CompositionSpaceServiceFactory compositionSpaceServiceFactory = compositionSpaceServiceFactoryReference.get();
                    if (compositionSpaceServiceFactory == null) {
                        UserAndContext uac = notification.getKey();
                        LoggerHolder.LOG.error("Could not save drafts of user {} in context {} because of absent CompositionSpaceServiceFactory on eviction of user-associated composition spaces", I(uac.getUserId()), I(uac.getContextId()));
                        return;
                    }

                    Cache<UUID, CompositionSpaceToDraftAssociation> activeUserSpaces = notification.getValue();
                    for (CompositionSpaceToDraftAssociation association : activeUserSpaces.asMap().values()) {
                        handleEvictedAssociation(association, compositionSpaceServiceFactory);
                    }
                }
            })
            .build(new CacheLoader<UserAndContext, Cache<UUID, CompositionSpaceToDraftAssociation>>() {

                @Override
                public Cache<UUID, CompositionSpaceToDraftAssociation> load(UserAndContext key) throws Exception {
                    return CacheBuilder.newBuilder()
                        .expireAfterAccess(Duration.ofSeconds(maxIdleSeconds))
                        .removalListener((RemovalNotification<UUID, CompositionSpaceToDraftAssociation> notification) -> {
                            if (notification.wasEvicted()) {
                                CompositionSpaceServiceFactory compositionSpaceServiceFactory = compositionSpaceServiceFactoryReference.get();
                                if (compositionSpaceServiceFactory == null) {
                                    LoggerHolder.LOG.error("Could not save draft because of absent CompositionSpaceServiceFactory on eviction of space {}", notification.getKey());
                                    return;
                                }

                                CompositionSpaceToDraftAssociation association = notification.getValue();
                                handleEvictedAssociation(association, compositionSpaceServiceFactory);
                            }
                        })
                        .build();
                }
            });
    }

    /**
     * Handles an evicted association by saving linked composition space as draft mail and cleaning up the cache file behind it.
     *
     * @param association The association that is about being evicted
     * @param compositionSpaceServiceFactory The factory to use
     */
    static void handleEvictedAssociation(CompositionSpaceToDraftAssociation association, CompositionSpaceServiceFactory compositionSpaceServiceFactory) {
        try {
            CompositionSpaceService compositionSpaceService = compositionSpaceServiceFactory.createServiceFor(association.getSession());
            compositionSpaceService.saveCompositionSpaceToDraftMail(association.getCompositionSpaceId(), Optional.empty(), true);
            LoggerHolder.LOG.debug("Saved draft for evicted composition space association: {}", UUIDs.getUnformattedString(association.getCompositionSpaceId()));
        } catch (Exception e) {
            LoggerHolder.LOG.error("Error while saving draft on eviction of composition space asociation: {}", UUIDs.getUnformattedString(association.getCompositionSpaceId()), e);
        }

        association.getFileCacheReference().ifPresent(r -> {
            if (r.isValid()) {
                r.cleanUp();
                LoggerHolder.LOG.debug("Cleaned up file cache reference of evicted composition space association {}: {}", UUIDs.getUnformattedString(association.getCompositionSpaceId()), r);
            } else {
                LoggerHolder.LOG.debug("File cache reference of evicted composition space association is already invalid: {}", UUIDs.getUnformattedString(association.getCompositionSpaceId()), r);
            }
        });
    }

    /**
     * Sets the composition space service factory to use,
     *
     * @param compositionSpaceServiceFactory The composition space service factory to use
     */
    public void setCompositionSpaceServiceFactory(CompositionSpaceServiceFactory compositionSpaceServiceFactory) {
        compositionSpaceServiceFactoryReference.set(compositionSpaceServiceFactory);
    }

    @Override
    public void store(CompositionSpaceToDraftAssociation association) {
        Cache<UUID, CompositionSpaceToDraftAssociation> activeUserSpaces = user2Spaces.getUnchecked(UserAndContext.newInstance(association.getSession()));
        activeUserSpaces.put(association.getCompositionSpaceId(), association);
    }

    @Override
    public CompositionSpaceToDraftAssociation get(UUID compositionSpaceId, Session session) throws OXException {
        Optional<CompositionSpaceToDraftAssociation> optionalAssociation = opt(compositionSpaceId, session);
        if (optionalAssociation.isPresent()) {
            return optionalAssociation.get();
        }
        throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUnformattedString(compositionSpaceId));
    }

    @Override
    public List<CompositionSpaceToDraftAssociation> getAllForUser(Session session) throws OXException {
        Cache<UUID, CompositionSpaceToDraftAssociation> activeUserSpaces = user2Spaces.getIfPresent(UserAndContext.newInstance(session));
        if (null == activeUserSpaces) {
            return Collections.emptyList();
        }
        return activeUserSpaces.size() <= 0 ? Collections.emptyList() : new ArrayList<>(activeUserSpaces.asMap().values());
    }

    @Override
    public Optional<CompositionSpaceToDraftAssociation> opt(UUID compositionSpaceId, Session session) {
        Cache<UUID, CompositionSpaceToDraftAssociation> activeUserSpaces = user2Spaces.getIfPresent(UserAndContext.newInstance(session));
        if (null == activeUserSpaces) {
            return Optional.empty();
        }
        CompositionSpaceToDraftAssociation association = activeUserSpaces.getIfPresent(compositionSpaceId);
        if (null == association) {
            return Optional.empty();
        }
        return Optional.of(association);
    }

    @Override
    public Optional<CompositionSpaceToDraftAssociation> delete(UUID compositionSpaceId, Session session, boolean ensureExistent) throws OXException {
        Cache<UUID, CompositionSpaceToDraftAssociation> activeUserSpaces = user2Spaces.getIfPresent(UserAndContext.newInstance(session));
        if (null == activeUserSpaces) {
            if (ensureExistent) {
                throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUnformattedString(compositionSpaceId));
            }
            return Optional.empty();
        }

        CompositionSpaceToDraftAssociation removedAssociation = activeUserSpaces.asMap().remove(compositionSpaceId);
        if (null == removedAssociation) {
            if (ensureExistent) {
                throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUnformattedString(compositionSpaceId));
            }
        } else {
            removedAssociation.getFileCacheReference().ifPresent(r -> r.cleanUp());
        }

        return Optional.ofNullable(removedAssociation);
    }

}
