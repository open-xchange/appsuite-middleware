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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.requesthandler.jobqueue;

import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXActionCustomizer;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXState;
import com.openexchange.ajax.requesthandler.DefaultDispatcher;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.exception.OXException;
import com.openexchange.framework.request.RequestContext;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * The default dispatcher job simply delegating to {@link DefaultDispatcher#doPerform(AJAXActionService, AJAXActionServiceFactory, AJAXRequestData, AJAXState, List, ServerSession) DefaultDispatcher.doPerform()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DefaultJob implements Job {

    /**
     * Creates a new builder instance.
     *
     * @param trackable Whether this job is supposed to be tracked by watcher
     * @param dispatcher The default dispatcher instance
     * @return A new builder instance
     */
    public static Builder builder(boolean trackable, DefaultDispatcher dispatcher) {
        return new Builder(trackable, dispatcher);
    }

    /** The builder for an instance of <code>DefaultJob</code> */
    public static class Builder {

        private final boolean trackable;
        private final DefaultDispatcher dispatcher;
        private AJAXActionService action;
        private AJAXActionServiceFactory factory;
        private AJAXRequestData requestData;
        private AJAXState state;
        private List<AJAXActionCustomizer> customizers;
        private RequestContext requestContext;
        private ServerSession session;
        private JobKey key;

        Builder(boolean trackable, DefaultDispatcher dispatcher) {
            super();
            this.trackable = trackable;
            this.dispatcher = dispatcher;
        }

        /**
         * Sets the key
         */
        public Builder setKey(JobKey key) {
            this.key = key;
            return this;
        }

        /**
         * Sets the action
         */
        public Builder setAction(AJAXActionService action) {
            this.action = action;
            return this;
        }

        /**
         * Sets the action factory
         */
        public Builder setFactory(AJAXActionServiceFactory factory) {
            this.factory = factory;
            return this;
        }

        /**
         * Sets the requestData
         */
        public Builder setRequestData(AJAXRequestData requestData) {
            this.requestData = requestData;
            return this;
        }

        /**
         * Sets the state
         */
        public Builder setState(AJAXState state) {
            this.state = state;
            return this;
        }

        /**
         * Sets the customizers
         */
        public Builder setCustomizers(List<AJAXActionCustomizer> customizers) {
            this.customizers = customizers;
            return this;
        }

        /**
         * Sets the request context
         */
        public Builder setRequestContext(RequestContext requestContext) {
            this.requestContext = requestContext;
            return this;
        }

        /**
         * Sets the session
         */
        public Builder setSession(ServerSession session) {
            this.session = session;
            return this;
        }

        /**
         * Creates the {@code DefaultJob} instance from this builder's arguments.
         *
         * @return The {@code DefaultJob} instance
         */
        public DefaultJob build() {
            return new DefaultJob(trackable, key, dispatcher, action, factory, requestData, state, customizers, requestContext, session);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    private final boolean trackable;
    private final DefaultDispatcher dispatcher;
    private final AJAXActionService action;
    private final AJAXActionServiceFactory factory;
    private final AJAXRequestData requestData;
    private final AJAXState state;
    private final List<AJAXActionCustomizer> customizers;
    private final RequestContext requestContext;
    private final ServerSession session;
    private final JobKey key;

    /**
     * Initializes a new {@link DefaultJob}.
     */
    DefaultJob(boolean trackable, JobKey key, DefaultDispatcher dispatcher, AJAXActionService action, AJAXActionServiceFactory factory, AJAXRequestData requestData, AJAXState state, List<AJAXActionCustomizer> customizers, RequestContext requestContext, ServerSession session) {
        super();
        this.trackable = trackable;
        this.key = key;
        this.dispatcher = dispatcher;
        this.action = action;
        this.factory = factory;
        this.requestData = requestData;
        this.state = state;
        this.customizers = customizers;
        this.requestContext = requestContext;
        this.session = session;
    }

    @Override
    public boolean isTrackable() {
        return trackable;
    }

    @Override
    public JobKey getOptionalKey() {
        return key;
    }

    @Override
    public AJAXRequestData getRequestData() {
        return requestData;
    }

    @Override
    public ServerSession getSession() {
        return session;
    }

    @Override
    public AJAXRequestResult perform() throws OXException {
        RequestContextHolder.set(requestContext);
        AJAXRequestResult result = null;
        Exception exc = null;
        try {
            return dispatcher.doPerform(action, factory, requestData, state, customizers, session);
        } catch (OXException e) {
            exc = e;
            throw e;
        } catch (RuntimeException e) {
            exc = e;
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            RequestContextHolder.reset();
            Dispatchers.signalDone(result, exc);
            if (null != state) {
                state.close();
            }
        }
    }

}
