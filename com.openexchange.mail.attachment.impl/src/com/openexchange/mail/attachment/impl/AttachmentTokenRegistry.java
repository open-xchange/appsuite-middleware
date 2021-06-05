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

package com.openexchange.mail.attachment.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.hazelcast.Hazelcasts;
import com.openexchange.mail.attachment.AttachmentToken;
import com.openexchange.mail.attachment.AttachmentTokenConstants;
import com.openexchange.mail.attachment.AttachmentTokenService;
import com.openexchange.mail.attachment.impl.portable.PortableAttachmentToken;
import com.openexchange.mail.attachment.impl.portable.PortableCheckTokenExistence;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link AttachmentTokenRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AttachmentTokenRegistry implements AttachmentTokenConstants, AttachmentTokenService {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentTokenRegistry.class);

    private static volatile AttachmentTokenRegistry instance;

    /**
     * Gets the instance
     *
     * @return The instance or <code>null</code> if not initialized
     */
    public static AttachmentTokenRegistry getInstance() {
        return instance;
    }

    /**
     * Initializes the instance
     *
     * @param services The services look-up
     * @return The initialized instance
     */
    public static AttachmentTokenRegistry initInstance(ServiceLookup services) {
        AttachmentTokenRegistry atr = new AttachmentTokenRegistry(services);
        instance = atr;
        return atr;
    }

    /**
     * Releases the instance.
     */
    public static void releaseInstance() {
        AttachmentTokenRegistry atr = instance;
        if (null != atr) {
            instance = null;
            atr.dispose();
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;
    private final ConcurrentMap<UserAndContext, ConcurrentMap<String, AttachmentToken>> map;
    private final ConcurrentMap<String, AttachmentToken> tokens;
    private final ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link AttachmentTokenRegistry}.
     */
    private AttachmentTokenRegistry(ServiceLookup services) {
        super();
        this.services = services;
        map = new ConcurrentHashMap<UserAndContext, ConcurrentMap<String, AttachmentToken>>(256, 0.9f, 1);
        tokens = new ConcurrentHashMap<String, AttachmentToken>(512, 0.9f, 1);
        timerTask = services.getService(TimerService.class).scheduleWithFixedDelay(new CleanExpiredTokensRunnable(map, tokens), CLEANER_FREQUENCY, CLEANER_FREQUENCY);
    }

    /**
     * Disposes this registry.
     */
    private void dispose() {
        ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            timerTask.cancel(false);
        }
        tokens.clear();
        map.clear();
    }

    @Override
    public void dropFor(final int userId, final int contextId) {
        final ConcurrentMap<String, AttachmentToken> userTokens = map.remove(keyFor(userId, contextId));
        if (null != userTokens) {
            for (final Iterator<AttachmentToken> iter = userTokens.values().iterator(); iter.hasNext();) {
                final AttachmentToken token = iter.next();
                tokens.remove(token.getId());
                iter.remove();

            }
        }
        LOG.debug("Cleaned user-sensitive attachment tokens for user {} in context {}", I(userId), I(contextId));
    }

    @Override
    public void dropFor(final Session session) {
        dropFor(session.getUserId(), session.getContextId());
    }

    @Override
    public void removeToken(final String tokenId) {
        final AttachmentToken attachmentToken = tokens.remove(tokenId);
        if (null == attachmentToken) {
            return;
        }
        /*
         * Clean from other map, too
         */
        final UserAndContext key = keyFor(attachmentToken.getUserId(), attachmentToken.getContextId());
        final ConcurrentMap<String, AttachmentToken> userTokens = map.get(key);
        if (null != userTokens) {
            userTokens.remove(tokenId);
            if (userTokens.isEmpty()) {
                map.remove(key);
            }
        }
    }

    @Override
    public AttachmentToken getToken(String tokenId, boolean chunked) {
        return getToken(tokenId, chunked, true);
    }

    /**
     * Gets the token
     *
     * @param tokenId The token identifier
     * @param chunked <code>true</code> if a chunk-wise retrieval is performed; otherwise <code>false</code>
     * @param considerRemote Whether to perform look-up if locally absent
     * @return The token or <code>null</code>
     */
    public AttachmentToken getToken(String tokenId, boolean chunked, boolean considerRemote) {
        AttachmentToken attachmentToken = tokens.get(tokenId);
        if (null == attachmentToken) {
            if (considerRemote) {
                HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
                if (null != hzInstance) {
                    return getFromRemote(tokenId, chunked, hzInstance);
                }
            }
            return null;
        }
        if (attachmentToken.isExpired()) {
            removeToken(tokenId);
            return null;
        }
        if (attachmentToken.isOneTime() && !chunked) {
            removeToken(tokenId);
            return attachmentToken;
        }
        return attachmentToken.touch();
    }

    private AttachmentToken getFromRemote(String tokenId, boolean chunked, HazelcastInstance hzInstance) {
        // Determine other cluster members
        Set<Member> otherMembers = Hazelcasts.getRemoteMembers(hzInstance);
        if (otherMembers.isEmpty()) {
            // No other cluster members
            return null;
        }

        Hazelcasts.Filter<PortableAttachmentToken, AttachmentToken> filter = new Hazelcasts.Filter<PortableAttachmentToken, AttachmentToken>() {

            @Override
            public AttachmentToken accept(PortableAttachmentToken p) {
                if (!p.isValid()) {
                    return null;
                }

                AttachmentToken token = new AttachmentToken(AttachmentTokenConstants.DEFAULT_TIMEOUT);
                token.setAccountId(p.getAccountId());
                token.setAttachmentId(p.getAttachmentId());
                token.setCheckIp(p.isCheckIp());
                token.setClient(p.getClient());
                token.setClientIp(p.getClientIp());
                token.setContextId(p.getContextId());
                token.setFolderPath(p.getFolderPath());
                token.setJsessionId(p.getJsessionId());
                token.setMailId(p.getMailId());
                token.setOneTime(p.isOneTime());
                token.setSessionId(p.getSessionId());
                token.setUserAgent(p.getUserAgent());
                token.setUserId(p.getUserId());
                return token;
            }
        };
        try {
            return Hazelcasts.executeByMembersAndFilter(new PortableCheckTokenExistence(tokenId, chunked), otherMembers, hzInstance.getExecutorService("default"), filter);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }

    @Override
    public void putToken(final AttachmentToken token, final Session session) {
        final UserAndContext key = keyFor(session);
        ConcurrentMap<String, AttachmentToken> userTokens = map.get(key);
        if (null == userTokens) {
            final ConcurrentMap<String, AttachmentToken> newmap = new ConcurrentHashMap<String, AttachmentToken>();
            userTokens = map.putIfAbsent(key, newmap);
            if (null == userTokens) {
                userTokens = newmap;
            }
        }
        userTokens.put(token.getId(), token);
        tokens.put(token.getId(), token);
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    private static UserAndContext keyFor(Session session) {
        return UserAndContext.newInstance(session);
    }

    protected static UserAndContext keyFor(int user, int context) {
        return UserAndContext.newInstance(user, context);
    }

    private static final class CleanExpiredTokensRunnable implements Runnable {

        private final ConcurrentMap<UserAndContext, ConcurrentMap<String, AttachmentToken>> rmap;
        private final ConcurrentMap<String, AttachmentToken> rtokens;

        CleanExpiredTokensRunnable(final ConcurrentMap<UserAndContext, ConcurrentMap<String, AttachmentToken>> rmap, final ConcurrentMap<String, AttachmentToken> rtokens) {
            super();
            this.rmap = rmap;
            this.rtokens = rtokens;
        }

        @Override
        public void run() {
            try {
                for (Iterator<AttachmentToken> iterator = rtokens.values().iterator(); iterator.hasNext();) {
                    AttachmentToken token = iterator.next();
                    if (token.isExpired()) {
                        iterator.remove();
                        ConcurrentMap<String, AttachmentToken> userTokens = rmap.get(keyFor(token.getUserId(), token.getContextId()));
                        if (null != userTokens) {
                            userTokens.remove(token.getId());
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    } // End of class CleanExpiredTokensRunnable

}
