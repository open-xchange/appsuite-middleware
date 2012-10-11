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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.java.Java7ConcurrentLinkedQueue;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DefaultDispatcher} - The default {@link Dispatcher dispatcher} implementation.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultDispatcher implements Dispatcher {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(DefaultDispatcher.class));

    private final ConcurrentMap<String, AJAXActionServiceFactory> actionFactories;

    private final Queue<AJAXActionCustomizerFactory> customizerFactories;

    /**
     * Initializes a new {@link DefaultDispatcher}.
     */
    public DefaultDispatcher() {
        super();
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
        } catch (final IllegalStateException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
    }

    private static final Pattern SPLIT_SLASH = Pattern.compile("/");

    private AJAXActionServiceFactory lookupFactory(final String module) {
        AJAXActionServiceFactory serviceFactory = actionFactories.get(module);
        if (serviceFactory == null && module.contains("/")) {
            // Fallback for backwards compatibility. File Download Actions sometimes append the filename to the module.
            serviceFactory = actionFactories.get(SPLIT_SLASH.split(module, 0)[0]);
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
                synchronized (actionFactories) {
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

	@Override
	public boolean mayUseFallbackSession(final String module, final String action) throws OXException {
		final AJAXActionServiceFactory factory = lookupFactory(module);
        if (factory == null) {
            return false;
        }
        final DispatcherNotes actionMetadata = getActionMetadata(factory.createActionService(action));
        if (actionMetadata == null) {
        	return false;
        }
		return actionMetadata.allowPublicSession();
	}
	
	@Override
    public boolean mayOmitSession(final String module, final String action) throws OXException {
		final AJAXActionServiceFactory factory = lookupFactory(module);
        if (factory == null) {
            return false;
        }
        final DispatcherNotes actionMetadata = getActionMetadata(factory.createActionService(action));
        if (actionMetadata == null) {
        	return false;
        }
		return actionMetadata.noSession();
	}

}
