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

import static com.openexchange.java.util.UUIDs.getUnformattedString;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;

/**
 * {@link AssociationStorage} - The association storage for a certain user implementation backed by Google Cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class AssociationStorage implements IAssociationStorage {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AssociationStorage.class);
    }

    private final Cache<UUID, CompositionSpaceToDraftAssociation> associations;

    /**
     * Initializes a new {@link AssociationStorage}.
     *
     * @param maxIdleSeconds The max. idle seconds
     */
    public AssociationStorage(long maxIdleSeconds) {
        super();
        associations = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(maxIdleSeconds))
            .removalListener((RemovalNotification<UUID, CompositionSpaceToDraftAssociation> notification) -> {
                if (notification.wasEvicted()) {
                    CompositionSpaceToDraftAssociation association = notification.getValue();
                    handleEvictedAssociation(association);
                }
            })
            .build();
    }

    /**
     * Handles an evicted association by saving linked composition space as draft mail and cleaning up the cache file behind it.
     *
     * @param association The association that is about being evicted
     */
    static void handleEvictedAssociation(CompositionSpaceToDraftAssociation association) {
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
     * Signals eviction to this association storage
     *
     * @param compositionSpaceServiceFactory The composition space service factory to use
     */
    public void signalEviction() {
        for (CompositionSpaceToDraftAssociation association : associations.asMap().values()) {
            handleEvictedAssociation(association);
        }
        associations.invalidateAll();
    }

    @Override
    public CompositionSpaceToDraftAssociation update(CompositionSpaceToDraftAssociationUpdate associationUpdate) throws OXException {
        if (associationUpdate == null) {
            throw new IllegalArgumentException("Association update must not be null");
        }
        UUID compositionSpaceId = associationUpdate.getCompositionSpaceId();
        CompositionSpaceToDraftAssociation association = associations.getIfPresent(compositionSpaceId);
        if (association == null) {
            throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUnformattedString(compositionSpaceId));
        }
        association.updateVariants(associationUpdate);
        return association;
    }

    @Override
    public CompositionSpaceToDraftAssociation storeIfAbsent(CompositionSpaceToDraftAssociation association) {
        ConcurrentMap<UUID, CompositionSpaceToDraftAssociation> activeUserAssociations = associations.asMap();
        return activeUserAssociations.putIfAbsent(association.getCompositionSpaceId(), association);
    }

    @Override
    public CompositionSpaceToDraftAssociation get(UUID compositionSpaceId) throws OXException {
        Optional<CompositionSpaceToDraftAssociation> optionalAssociation = opt(compositionSpaceId);
        if (optionalAssociation.isPresent()) {
            return optionalAssociation.get();
        }
        throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUnformattedString(compositionSpaceId));
    }

    @Override
    public List<CompositionSpaceToDraftAssociation> getAll() throws OXException {
        return associations.size() <= 0 ? Collections.emptyList() : new ArrayList<>(associations.asMap().values());
    }

    @Override
    public Optional<CompositionSpaceToDraftAssociation> opt(UUID compositionSpaceId) {
        return Optional.ofNullable(associations.getIfPresent(compositionSpaceId));
    }

    @Override
    public Optional<CompositionSpaceToDraftAssociation> delete(UUID compositionSpaceId, boolean ensureExistent) throws OXException {
        CompositionSpaceToDraftAssociation removedAssociation = associations.asMap().remove(compositionSpaceId);

        if (null == removedAssociation) {
            // No such association
            if (ensureExistent) {
                throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUnformattedString(compositionSpaceId));
            }
            return Optional.empty();
        }

        removedAssociation.getFileCacheReference().ifPresent(r -> r.cleanUp());
        removedAssociation.invalidate();
        return Optional.of(removedAssociation);
    }

}
