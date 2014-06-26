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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.continuation.Continuation;
import com.openexchange.continuation.ContinuationException;
import com.openexchange.continuation.ContinuationExceptionCodes;
import com.openexchange.continuation.ContinuationRegistryService;
import com.openexchange.continuation.ContinuationResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Java7ConcurrentLinkedQueue;
import com.openexchange.log.LogProperties;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DefaultDispatcher} - The default {@link Dispatcher dispatcher} implementation.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultDispatcher implements Dispatcher {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultDispatcher.class);

    private final ConcurrentMap<StrPair, Boolean> fallbackSessionActionsCache;
    private final ConcurrentMap<StrPair, Boolean> publicSessionAuthCache;
    private final ConcurrentMap<StrPair, Boolean> omitSessionActionsCache;
    private final ConcurrentMap<StrPair, Boolean> noSecretCallbackCache;

    private final ConcurrentMap<String, AJAXActionServiceFactory> actionFactories;
    private final Queue<AJAXActionCustomizerFactory> customizerFactories;

    /**
     * Initializes a new {@link DefaultDispatcher}.
     */
    public DefaultDispatcher() {
        super();
        fallbackSessionActionsCache = new ConcurrentHashMap<StrPair, Boolean>(128);
        publicSessionAuthCache = new ConcurrentHashMap<StrPair, Boolean>(128);
        omitSessionActionsCache = new ConcurrentHashMap<StrPair, Boolean>(128);
        noSecretCallbackCache = new ConcurrentHashMap<StrPair, Boolean>(128);

        actionFactories = new ConcurrentHashMap<String, AJAXActionServiceFactory>();
        customizerFactories = new Java7ConcurrentLinkedQueue<AJAXActionCustomizerFactory>();
    }

    @Override
    public AJAXState begin() throws OXException {
        return new AJAXState();
    }

    @Override
    public void end(final AJAXState state) {
        if (null != state) {
            state.close();
        }
    }

    @Override
    public boolean handles(final String module) {
        return actionFactories.containsKey(module);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final AJAXState state, final ServerSession session) throws OXException {
        if (null == session) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_SESSION);
        }
        addLogProperties(requestData, false);
        try {
            List<AJAXActionCustomizer> outgoing = new ArrayList<AJAXActionCustomizer>(customizerFactories.size());
            final List<AJAXActionCustomizer> todo = new LinkedList<AJAXActionCustomizer>();
            /*
             * Create customizers
             */
            for (final AJAXActionCustomizerFactory customizerFactory : customizerFactories) {
                final AJAXActionCustomizer customizer = customizerFactory.createCustomizer(requestData, session);
                if (customizer != null) {
                    todo.add(customizer);
                }
            }
            /*
             * Iterate customizers for AJAXRequestData
             */
            AJAXRequestData modifiedRequestData = requestData;
            while (!todo.isEmpty()) {
                final Iterator<AJAXActionCustomizer> iterator = todo.iterator();
                while (iterator.hasNext()) {
                    final AJAXActionCustomizer customizer = iterator.next();
                    try {
                        final AJAXRequestData modified = customizer.incoming(modifiedRequestData, session);
                        if (modified != null) {
                            modifiedRequestData = modified;
                        }
                        outgoing.add(customizer);
                        iterator.remove();
                    } catch (final FlowControl.Later l) {
                        // Remains in list and is therefore retried
                    }
                }
            }
            /*
             * Look-up appropriate factory for request's module
             */
            final AJAXActionServiceFactory factory = lookupFactory(modifiedRequestData.getModule());
            if (factory == null) {
                throw AjaxExceptionCodes.UNKNOWN_MODULE.create(modifiedRequestData.getModule());
            }
            /*
             * Get associated action
             */
            final AJAXActionService action = factory.createActionService(modifiedRequestData.getAction());
            if (action == null) {
                throw AjaxExceptionCodes.UNKNOWN_ACTION_IN_MODULE.create(modifiedRequestData.getAction(), modifiedRequestData.getModule());
            }
            /*
             * Is it possible to serve request by ETag?
             */
            {
                final String eTag = modifiedRequestData.getETag();
                if (null != eTag && (action instanceof ETagAwareAJAXActionService) && ((ETagAwareAJAXActionService) action).checkETag(eTag, modifiedRequestData, session)) {
                    final AJAXRequestResult etagResult = new AJAXRequestResult();
                    etagResult.setType(AJAXRequestResult.ResultType.ETAG);
                    final long newExpires = modifiedRequestData.getExpires();
                    if (newExpires > 0) {
                        etagResult.setExpires(newExpires);
                    }
                    return etagResult;
                }
            }
            /*
             * Check for Action annotation
             */
            if (modifiedRequestData.getFormat() == null) {
                final DispatcherNotes actionMetadata = getActionMetadata(action);
                modifiedRequestData.setFormat(actionMetadata == null ? "apiResponse" : actionMetadata.defaultFormat());
            }
            /*
             * State already initialized for module?
             */
            if (factory instanceof AJAXStateHandler) {
                final AJAXStateHandler handler = (AJAXStateHandler) factory;
                if (state.addInitializer(modifiedRequestData.getModule(), handler)) {
                    handler.initialize(state);
                }
            }
            modifiedRequestData.setState(state);
            /*
             * Perform request
             */
            AJAXRequestResult result;
            try {
                result = action.perform(modifiedRequestData, session);
                if (null == result) {
                    // Huh...?!
                    addLogProperties(modifiedRequestData, true);
                    throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(AJAXRequestResult.class.getSimpleName(), "null");
                }
            } catch (final IllegalStateException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof OXException) {
                    throw (OXException) cause;
                }
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (final ContinuationException e) {
                result = handleContinuationException(e, session);
            } finally {
                modifiedRequestData.cleanUploads();
            }
            /*
             * Check for direct result type
             */
            if (AJAXRequestResult.ResultType.DIRECT == result.getType()) {
                // No further processing
                return result;
            }
            /*
             * Iterate customizers in reverse oder for request data and result pair
             */
            Collections.reverse(outgoing);
            outgoing = new LinkedList<AJAXActionCustomizer>(outgoing);
            while (!outgoing.isEmpty()) {
                final Iterator<AJAXActionCustomizer> iterator = outgoing.iterator();

                while (iterator.hasNext()) {
                    final AJAXActionCustomizer customizer = iterator.next();
                    try {
                        final AJAXRequestResult modified = customizer.outgoing(modifiedRequestData, result, session);
                        if (modified != null) {
                            result = modified;
                        }
                        iterator.remove();
                    } catch (final FlowControl.Later l) {
                        // Remains in list and is therefore retried
                    }
                }
            }
            return result;
        } catch (final RuntimeException e) {
            if ("org.mozilla.javascript.WrappedException".equals(e.getClass().getName())) {
                // Handle special Rhino wrapper error
                final Throwable wrapped = e.getCause();
                if (wrapped instanceof OXException) {
                    throw (OXException) wrapped;
                }
            }

            // Wrap unchecked exception
            addLogProperties(requestData, true);
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles specified <code>ContinuationException</code> instance.
     *
     * @param e The exception to handle
     * @param session The associated session
     * @return The AJAX result
     * @throws OXException If <code>ContinuationException</code> does not signal special error code <code>CONTINUATION-0003</code>
     *             (Scheduled for continuation: &lt;uuid&gt;)
     */
    private AJAXRequestResult handleContinuationException(final ContinuationException e, final ServerSession session) throws OXException {
        if (!ContinuationExceptionCodes.SCHEDULED_FOR_CONTINUATION.equals(e)) {
            throw e;
        }
        final UUID uuid = e.getUuid();
        if (null == uuid) {
            throw e;
        }
        final ContinuationRegistryService continuationRegistry = ServerServiceRegistry.getInstance().getService(ContinuationRegistryService.class);
        if (null == continuationRegistry) {
            throw e;
        }
        final Continuation<Object> continuation = continuationRegistry.getContinuation(uuid, session);
        if (null == continuation) {
            throw e;
        }
        try {
            final ContinuationResponse<Object> cr = continuation.getNextResponse(1000, TimeUnit.NANOSECONDS);
            return new AJAXRequestResult(cr.getValue(), cr.getTimeStamp(), cr.getFormat()).setContinuationUuid(cr.isCompleted() ? null : uuid);
        } catch (final InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(ie, ie.getMessage());
        }
    }

    private void addLogProperties(final AJAXRequestData requestData, final boolean withQueryString) {
        if (null != requestData) {
            LogProperties.putProperty(LogProperties.Name.AJAX_ACTION, requestData.getAction());
            LogProperties.putProperty(LogProperties.Name.AJAX_MODULE, requestData.getModule());

            if (withQueryString) {
                final Map<String, String> parameters = requestData.getParameters();
                if (null != parameters) {
                    final StringBuilder sb = new StringBuilder(256);
                    sb.append('"');
                    boolean first = true;
                    for (final Entry<String, String> entry : parameters.entrySet()) {
                        if (first) {
                            sb.append('?');
                            first = false;
                        } else {
                            sb.append('&');
                        }
                        sb.append(entry.getKey()).append('=').append(entry.getValue());
                    }
                    sb.append('"');
                    LogProperties.putProperty(LogProperties.Name.SERVLET_QUERY_STRING, sb.toString());
                }
            }
        }
    }

    // private static final Pattern SPLIT_SLASH = Pattern.compile("/");

    private AJAXActionServiceFactory lookupFactory(final String module) {
        AJAXActionServiceFactory serviceFactory = actionFactories.get(module);
        if (null == serviceFactory) {
            final int pos = module.indexOf('/');
            if (pos > 0) {
                // Fallback for backwards compatibility. File Download Actions sometimes append the filename to the module.
                serviceFactory = actionFactories.get(module.substring(0, pos));
            }
        }
        return serviceFactory;
    }

    private DispatcherNotes getActionMetadata(final AJAXActionService action) {
        if (null == action) {
            return null;
        }
        return action.getClass().getAnnotation(DispatcherNotes.class);
    }

    /**
     * Registers specified factory under given module.
     *
     * @param module The module
     * @param factory The factory (possibly annotated with {@link Module})
     */
    public void register(final String module, final AJAXActionServiceFactory factory) {
        synchronized (actionFactories) {
            AJAXActionServiceFactory current = actionFactories.putIfAbsent(module, factory);
            if (null != current) {
                try {
                    current = actionFactories.get(module);
                    final Module moduleAnnotation = current.getClass().getAnnotation(Module.class);
                    if (null == moduleAnnotation) {
                        final StringBuilder sb = new StringBuilder(512).append("There is already a factory associated with module \"");
                        sb.append(module).append("\": ").append(current.getClass().getName());
                        sb.append(". Therefore registration is denied for factory \"").append(factory.getClass().getName());
                        sb.append("\". Unless these two factories provide the \"").append(Module.class.getName()).append(
                            "\" annotation to specify what actions are supported by each factory.");
                        LOG.warn(sb.toString());
                    } else {
                        final CombinedActionFactory combinedFactory;
                        if (current instanceof CombinedActionFactory) {
                            combinedFactory = (CombinedActionFactory) current;
                        } else {
                            combinedFactory = new CombinedActionFactory();
                            combinedFactory.add(current);
                            actionFactories.put(module, combinedFactory);
                        }
                        combinedFactory.add(factory);
                    }
                } catch (final IllegalArgumentException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }

    /**
     * Adds specified customizer factory.
     *
     * @param factory The customizer factory
     */
    public void addCustomizer(final AJAXActionCustomizerFactory factory) {
        this.customizerFactories.add(factory);
    }

    /**
     * Releases specified factory from given module.
     *
     * @param module The module
     * @param factory The factory (possibly annotated with {@link Module})
     */
    public void remove(final String module, final AJAXActionServiceFactory factory) {
        synchronized (actionFactories) {
            final AJAXActionServiceFactory removed = actionFactories.remove(module);
            if (removed instanceof CombinedActionFactory) {
                final CombinedActionFactory combinedFactory = (CombinedActionFactory) removed;
                combinedFactory.remove(factory);
                if (!combinedFactory.isEmpty()) {
                    actionFactories.put(module, combinedFactory);
                }
            }
        }
    }

    private AJAXActionService getActionServiceSafe(final String action, final AJAXActionServiceFactory factory) {
        try {
            return factory.createActionService(action);
        } catch (final Exception e) {
            return null;
        }
    }

	@Override
	public boolean mayUseFallbackSession(final String module, final String action) throws OXException {
	    final StrPair key = new StrPair(module, action);
        Boolean ret = fallbackSessionActionsCache.get(key);
	    if (null == ret) {
	        final AJAXActionServiceFactory factory = lookupFactory(module);
	        if (factory == null) {
	            return false;
	        }
            final DispatcherNotes actionMetadata = getActionMetadata(getActionServiceSafe(action, factory));
            ret = actionMetadata == null ? Boolean.FALSE : Boolean.valueOf(actionMetadata.allowPublicSession());
	        fallbackSessionActionsCache.put(key, ret);
        }
	    return ret.booleanValue();
	}

	@Override
	public boolean mayPerformPublicSessionAuth(final String module, final String action) throws OXException {
	    final StrPair key = new StrPair(module, action);
        Boolean ret = publicSessionAuthCache.get(key);
        if (null == ret) {
            final AJAXActionServiceFactory factory = lookupFactory(module);
            if (factory == null) {
                return false;
            }
            final DispatcherNotes actionMetadata = getActionMetadata(getActionServiceSafe(action, factory));
            ret = actionMetadata == null ? Boolean.FALSE : Boolean.valueOf(actionMetadata.publicSessionAuth());
            publicSessionAuthCache.put(key, ret);
        }
        return ret.booleanValue();
	}

	@Override
    public boolean mayOmitSession(final String module, final String action) throws OXException {
	    final StrPair key = new StrPair(module, action);
        Boolean ret = omitSessionActionsCache.get(key);
        if (null == ret) {
            final AJAXActionServiceFactory factory = lookupFactory(module);
            if (factory == null) {
                return false;
            }
            final DispatcherNotes actionMetadata = getActionMetadata(getActionServiceSafe(action, factory));
            ret = actionMetadata == null ? Boolean.FALSE : Boolean.valueOf(actionMetadata.noSession());
            omitSessionActionsCache.put(key, ret);
        }
        return ret.booleanValue();
	}

	@Override
	public boolean noSecretCallback(String module, String action) throws OXException {
	    final StrPair key = new StrPair(module, action);
        Boolean ret = noSecretCallbackCache.get(key);
        if (null == ret) {
            final AJAXActionServiceFactory factory = lookupFactory(module);
            if (factory == null) {
                ret = Boolean.FALSE;
            } else {
                final DispatcherNotes actionMetadata = getActionMetadata(getActionServiceSafe(action, factory));
                ret = actionMetadata == null ? Boolean.FALSE : Boolean.valueOf(actionMetadata.noSecretCallback());
            }
            noSecretCallbackCache.put(key, ret);
        }
        return ret.booleanValue();
	}

	private static final class StrPair {
	    private final String str1;
	    private final String str2;
	    private final int hash;

        StrPair(String str1, String str2) {
            super();
            this.str1 = str1;
            this.str2 = str2;
            final int prime = 31;
            int result = 1;
            result = prime * result + ((str1 == null) ? 0 : str1.hashCode());
            result = prime * result + ((str2 == null) ? 0 : str2.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof StrPair)) {
                return false;
            }
            StrPair other = (StrPair) obj;
            if (str1 == null) {
                if (other.str1 != null) {
                    return false;
                }
            } else if (!str1.equals(other.str1)) {
                return false;
            }
            if (str2 == null) {
                if (other.str2 != null) {
                    return false;
                }
            } else if (!str2.equals(other.str2)) {
                return false;
            }
            return true;
        }

	} // End of class Strings

}
