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
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.mail.compose.mailstorage.MailStorageCompositionSpaceConfig;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;


/**
 * {@link AssociationStorageManager}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class AssociationStorageManager implements IAssociationStorageManager {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(IAssociationStorageManager.class);
    }

    private final LoadingCache<UserAndContext, AssociationStorage> user2Storages;
    private final AtomicReference<CompositionSpaceServiceFactory> compositionSpaceServiceFactoryReference;

    /**
     * Initializes a new {@link AssociationStorageManager}.
     *
     * @throws OXException If initialization fails
     */
    public AssociationStorageManager() throws OXException {
        super();
        AtomicReference<CompositionSpaceServiceFactory> compositionSpaceServiceFactoryReference = new AtomicReference<CompositionSpaceServiceFactory>(null);
        this.compositionSpaceServiceFactoryReference = compositionSpaceServiceFactoryReference;

        long maxIdleSeconds = MailStorageCompositionSpaceConfig.getInstance().getInMemoryCacheMaxIdleSeconds();
        user2Storages = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(maxIdleSeconds))
            .removalListener((RemovalNotification<UserAndContext, AssociationStorage> notification) -> {
                if (notification.wasEvicted()) {
                    CompositionSpaceServiceFactory compositionSpaceServiceFactory = compositionSpaceServiceFactoryReference.get();
                    if (compositionSpaceServiceFactory == null) {
                        UserAndContext uac = notification.getKey();
                        LoggerHolder.LOG.error("Could not save drafts of user {} in context {} because of absent CompositionSpaceServiceFactory on eviction of user-associated composition spaces", I(uac.getUserId()), I(uac.getContextId()));
                        return;
                    }

                    AssociationStorage activeUserStorage = notification.getValue();
                    handleEvictedStorage(activeUserStorage, compositionSpaceServiceFactory);
                }
            })
            .build(new CacheLoader<UserAndContext, AssociationStorage>() {

                @Override
                public AssociationStorage load(UserAndContext key) throws Exception {
                    return new AssociationStorage(maxIdleSeconds, compositionSpaceServiceFactoryReference);
                }
            });
    }

    void handleEvictedStorage(AssociationStorage activeUserStorage, CompositionSpaceServiceFactory compositionSpaceServiceFactory) {
        activeUserStorage.signalEviction(compositionSpaceServiceFactory);
    }

    /**
     * Sets the composition space service factory to use,
     *
     * @param compositionSpaceServiceFactory The composition space service factory to use
     */
    public void setCompositionSpaceServiceFactory(CompositionSpaceServiceFactory compositionSpaceServiceFactory) {
        compositionSpaceServiceFactoryReference.set(compositionSpaceServiceFactory);
    }

    /**
     * Signals shut-down to this association storage manager.
     */
    public void shutDown() {
        CompositionSpaceServiceFactory compositionSpaceServiceFactory = compositionSpaceServiceFactoryReference.get();
        if (compositionSpaceServiceFactory == null) {
            return;
        }

        for (AssociationStorage activeUserStorage : user2Storages.asMap().values()) {
            handleEvictedStorage(activeUserStorage, compositionSpaceServiceFactory);
        }
        user2Storages.invalidateAll();
    }

    @Override
    public IAssociationStorage getStorageFor(Session session) throws OXException {
        return user2Storages.getUnchecked(UserAndContext.newInstance(session));
    }

    @Override
    public Optional<IAssociationStorage> optStorageFor(Session session) {
        return Optional.ofNullable(user2Storages.getIfPresent(UserAndContext.newInstance(session)));
    }

}
