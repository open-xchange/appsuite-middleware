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

package com.openexchange.mail.attachment.impl;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.openexchange.mail.attachment.AttachmentToken;
import com.openexchange.mail.attachment.AttachmentTokenConstants;
import com.openexchange.mail.attachment.AttachmentTokenService;
import com.openexchange.mail.attachment.impl.portable.PortableAttachmentToken;
import com.openexchange.mail.attachment.impl.portable.PortableCheckTokenExistence;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
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
    private final ConcurrentMap<Key, ConcurrentMap<String, AttachmentToken>> map;
    private final ConcurrentMap<String, AttachmentToken> tokens;
    private final ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link AttachmentTokenRegistry}.
     */
    private AttachmentTokenRegistry(ServiceLookup services) {
        super();
        this.services = services;
        map = new ConcurrentHashMap<Key, ConcurrentMap<String, AttachmentToken>>(256, 0.9f, 1);
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
        LOG.debug("Cleaned user-sensitive attachment tokens for user {} in context {}", userId, contextId);
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
        final Key key = keyFor(attachmentToken.getUserId(), attachmentToken.getContextId());
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
        // Get local member
        Cluster cluster = hzInstance.getCluster();
        Member localMember = cluster.getLocalMember();

        // Determine other cluster members
        Set<Member> otherMembers = getOtherMembers(cluster.getMembers(), localMember);

        if (otherMembers.isEmpty()) {
            // No other cluster members
            return null;
        }

        IExecutorService executor = hzInstance.getExecutorService("default");
        Map<Member, Future<PortableAttachmentToken>> futureMap = executor.submitToMembers(new PortableCheckTokenExistence(tokenId, chunked), otherMembers);
        for (Entry<Member, Future<PortableAttachmentToken>> entry : futureMap.entrySet()) {
            Member member = entry.getKey();
            Future<PortableAttachmentToken> future = entry.getValue();
            // Check Future's return value
            int retryCount = 3;
            while (retryCount-- > 0) {
                try {
                    PortableAttachmentToken p = future.get();
                    retryCount = 0;
                    if (p.isValid()) {
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
                } catch (InterruptedException e) {
                    // Interrupted - Keep interrupted state
                    Thread.currentThread().interrupt();
                    LOG.warn("Interrupted while performing remote look-up for attachment token {}", tokenId, e);
                    return null;
                } catch (CancellationException e) {
                    // Canceled
                    LOG.warn("Canceled while performing remote look-up for attachment token {}", tokenId, e);
                    return null;
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();

                    // Check for Hazelcast timeout
                    if (!(cause instanceof com.hazelcast.core.OperationTimeoutException)) {
                        if (cause instanceof RuntimeException) {
                            throw ((RuntimeException) cause);
                        }
                        if (cause instanceof Error) {
                            throw (Error) cause;
                        }
                        throw new IllegalStateException("Not unchecked", cause);
                    }

                    // Timeout while awaiting remote result
                    if (retryCount > 0) {
                        LOG.info("Timeout while performing remote look-up for attachment token {} from cluster member \"{}\". Retry...", tokenId, member);
                    } else {
                        // No further retry
                        LOG.info("Giving up remote look-up for attachment token {} from cluster member \"{}\".",tokenId, member);
                        cancelFutureSafe(future);
                    }
                }
            }
        }


        return null;
    }

    private Set<Member> getOtherMembers(Set<Member> allMembers, Member localMember) {
        Set<Member> otherMembers = new LinkedHashSet<Member>(allMembers);
        if (!otherMembers.remove(localMember)) {
            LOG.warn("Couldn't remove local member from cluster members.");
        }
        return otherMembers;
    }

    private <V> void cancelFutureSafe(Future<V> future) {
        if (null != future) {
            try { future.cancel(true); } catch (Exception e) {/*Ignore*/}
        }
    }

    @Override
    public void putToken(final AttachmentToken token, final Session session) {
        final Key key = keyFor(session);
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

    private static Key keyFor(Session session) {
        return keyFor(session.getUserId(), session.getContextId());
    }

    protected static Key keyFor(int user, int context) {
        return new Key(user, context);
    }

    private static final class Key {

        private final int cid;
        private final int user;
        private final int hash;

        Key(final int user, final int cid) {
            super();
            this.user = user;
            this.cid = cid;

            int prime = 31;
            int result = prime * 1 + cid;
            result = prime * result + user;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (cid != other.cid) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            return true;
        }

    } // End of class Key

    private static final class CleanExpiredTokensRunnable implements Runnable {

        private final ConcurrentMap<Key, ConcurrentMap<String, AttachmentToken>> rmap;
        private final ConcurrentMap<String, AttachmentToken> rtokens;

        CleanExpiredTokensRunnable(final ConcurrentMap<Key, ConcurrentMap<String, AttachmentToken>> rmap, final ConcurrentMap<String, AttachmentToken> rtokens) {
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
            } catch (final Exception e) {
                LOG.error("", e);
            }
        }
    } // End of class CleanExpiredTokensRunnable

}
