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

package com.openexchange.oauth.impl.internal;

import static com.openexchange.oauth.OAuthConstants.OAUTH_PROBLEM_PERMISSION_DENIED;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.openexchange.http.deferrer.CustomRedirectURLDetermination;
import com.openexchange.oauth.CallbackRegistry;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.impl.internal.hazelcast.PortableCallbackRegistryFetch;
import com.openexchange.oauth.impl.services.Services;

/**
 * {@link CallbackRegistryImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CallbackRegistryImpl implements CustomRedirectURLDetermination, Runnable, CallbackRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CallbackRegistryImpl.class);

    /** A value kept in managed map */
    private static final class UrlAndStamp {

        final String callbackUrl;
        final long stamp;

        protected UrlAndStamp(final String callbackUrl, final long stamp) {
            super();
            this.callbackUrl = callbackUrl;
            this.stamp = stamp;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(128);
            builder.append("[");
            if (callbackUrl != null) {
                builder.append("callbackUrl=").append(callbackUrl).append(", ");
            }
            builder.append("stamp=").append(stamp).append("]");
            return builder.toString();
        }
    }

    // ----------------------------------------------------------------------------------- //

    private final ConcurrentMap<String, UrlAndStamp> tokenMap;

    /**
     * Initializes a new {@link CallbackRegistryImpl}.
     */
    public CallbackRegistryImpl() {
        super();
        tokenMap = new ConcurrentHashMap<String, UrlAndStamp>();
    }

    /**
     * Clears this registry.
     */
    public void clear() {
        tokenMap.clear();
    }

    @Override
    public void add(final String token, final String callbackUrl) {
        if (null != token && null != callbackUrl) {
            tokenMap.put(token, new UrlAndStamp(callbackUrl, System.currentTimeMillis()));
        }
    }

    @Override
    public String getURL(final HttpServletRequest req) {
        if (tokenMap.isEmpty()) {
            return null;
        }

        String token = req.getParameter("oauth_token");
        if (null != token) {
            // By OAuth token
            return getByToken(token);
        }

        token = req.getParameter("state");
        if (null != token && token.startsWith("__ox")) {
            return getByToken(token);
        }

        token = req.getParameter("denied");
        if (null != token) {
            // Denied...
            String callbackUrl = getByToken(token);
            if (null == callbackUrl) {
                return null;
            }

            StringBuilder callback = new StringBuilder(callbackUrl);
            callback.append(callbackUrl.indexOf('?') > 0 ? '&' : '?');
            callback.append(OAuthConstants.URLPARAM_OAUTH_PROBLEM).append('=').append(OAUTH_PROBLEM_PERMISSION_DENIED);
            return callback.toString();
        }

        return null;
    }

    private String getByToken(String token) {
        UrlAndStamp urlAndStamp = tokenMap.remove(token);
        if (null != urlAndStamp) {
            // Local hit
            return urlAndStamp.callbackUrl;
        }

        // Try remote look-up
        HazelcastInstance hazelcastInstance = Services.optService(HazelcastInstance.class);
        return null == hazelcastInstance ? null : tryGetByTokenFromRemote(token, hazelcastInstance);
    }

    /**
     * Gets the call-back URL by specified token
     *
     * @param token The token
     * @return The associated call-back URL or <code>null</code>
     */
    public String getLocalUrlByToken(String token) {
        if (null == token) {
            return null;
        }

        UrlAndStamp urlAndStamp = tokenMap.remove(token);
        return null == urlAndStamp ? null : urlAndStamp.callbackUrl;
    }

    @Override
    public void run() {
        try {
            final long threshhold = System.currentTimeMillis() - 600000;
            for (final Iterator<UrlAndStamp> iter = tokenMap.values().iterator(); iter.hasNext();) {
                if (threshhold > iter.next().stamp) {
                    // Older than threshold
                    iter.remove();
                }
            }
        } catch (final Exception e) {
            final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CallbackRegistryImpl.class);
            logger.error("", e);
        }
    }

    /**
     * Tries to obtain the call-back URL associated with given token from remote nodes (if any)
     *
     * @param token The token to look-up
     * @param hazelcastInstance The Hazelcast instance to use
     * @return The remotely looked-up call-back URL or <code>null</code>
     */
    private String tryGetByTokenFromRemote(String token, HazelcastInstance hazelcastInstance) {
        Cluster cluster = hazelcastInstance.getCluster();

        // Get local member
        Member localMember = cluster.getLocalMember();

        // Determine other cluster members
        Set<Member> otherMembers = getOtherMembers(cluster.getMembers(), localMember);
        if (otherMembers.isEmpty()) {
            return null;
        }

        IExecutorService executor = hazelcastInstance.getExecutorService("default");
        Map<Member, Future<String>> futureMap = executor.submitToMembers(new PortableCallbackRegistryFetch(token), otherMembers);
        for (Iterator<Entry<Member, Future<String>>> it = futureMap.entrySet().iterator(); it.hasNext();) {
            Future<String> future = it.next().getValue();
            // Check Future's return value
            int retryCount = 3;
            while (retryCount-- > 0) {
                try {
                    String callbackUrl = future.get();
                    retryCount = 0;
                    return callbackUrl;
                } catch (InterruptedException e) {
                    // Interrupted - Keep interrupted state
                    Thread.currentThread().interrupt();
                } catch (CancellationException e) {
                    // Canceled
                    retryCount = 0;
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
                    if (retryCount <= 0) {
                        // No further retry
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

    /**
     * Cancels given {@link Future} safely
     *
     * @param future The {@code Future} to cancel
     */
    static <V> void cancelFutureSafe(Future<V> future) {
        if (null != future) {
            try {
                future.cancel(true);
            } catch (Exception e) {
            /* Ignore */}
        }
    }

}
