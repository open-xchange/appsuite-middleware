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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Hazelcasts;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.openexchange.http.deferrer.CustomRedirectURLDetermination;
import com.openexchange.oauth.CallbackRegistry;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.impl.internal.hazelcast.PortableCallbackRegistryFetch;
import com.openexchange.oauth.impl.internal.hazelcast.PortableMultipleCallbackRegistryFetch;
import com.openexchange.oauth.impl.services.Services;
import com.openexchange.threadpool.ThreadPoolService;

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
        List<String> remoteLookUps = new ArrayList<String>(2);

        String token = req.getParameter("oauth_token");
        if (null != token) {
            UrlAndStamp urlAndStamp = tokenMap.remove(token);
            if (null != urlAndStamp) {
                // Local hit
                return urlAndStamp.callbackUrl;
            }
            remoteLookUps.add(token);
        }

        token = req.getParameter("state");
        if (null != token && token.startsWith("__ox")) {
            UrlAndStamp urlAndStamp = tokenMap.remove(token);
            if (null != urlAndStamp) {
                // Local hit
                return urlAndStamp.callbackUrl;
            }
            remoteLookUps.add(token);
        }

        if (false == remoteLookUps.isEmpty()) {
            // Try remote look-up
            HazelcastInstance hazelcastInstance = Services.optService(HazelcastInstance.class);
            return null == hazelcastInstance ? null : tryGetByTokensFromRemote(remoteLookUps, hazelcastInstance);
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
        return null == hazelcastInstance ? null : tryGetByTokensFromRemote(Collections.singletonList(token), hazelcastInstance);
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
     * @param tokens The tokens to look-up
     * @param hazelcastInstance The Hazelcast instance to use
     * @return The remotely looked-up call-back URL or <code>null</code>
     */
    private String tryGetByTokensFromRemote(List<String> tokens, HazelcastInstance hazelcastInstance) {
        int size = tokens.size();
        if (size <= 0) {
            return null;
        }

        // Determine other cluster members
        Set<Member> otherMembers = Hazelcasts.getRemoteMembers(hazelcastInstance);
        if (otherMembers.isEmpty()) {
            return null;
        }

        Hazelcasts.Filter<String, String> filter = new Hazelcasts.Filter<String, String>() {

            @Override
            public String accept(String callbackUrl) {
                // "return null == callbackUrl ? null : callbackUrl" is simply:
                return callbackUrl;
            }
        };

        ThreadPoolService threadPool = Services.optService(ThreadPoolService.class);
        IExecutorService executor = hazelcastInstance.getExecutorService("default");
        Callable<String> remoteTask = size == 1 ? new PortableCallbackRegistryFetch(tokens.get(0)) : new PortableMultipleCallbackRegistryFetch(tokens.toArray(new String[size]));
        try {
            return Hazelcasts.executeByMembersAndFilter(remoteTask, otherMembers, executor, filter, threadPool.getExecutor());
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

}
