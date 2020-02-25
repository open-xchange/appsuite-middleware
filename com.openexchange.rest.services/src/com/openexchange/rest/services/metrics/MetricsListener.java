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

package com.openexchange.rest.services.metrics;

import java.time.Duration;
import java.util.List;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEvent.Type;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.glassfish.jersey.uri.UriTemplate;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

/**
 * {@link MetricsListener} - a {@link ApplicationEventListener} which records metrics for the rest servlets
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
@Provider
public class MetricsListener implements ApplicationEventListener  {

    private static final String NAME = "appsuite.restapi.requests.timer";
    
    @Override
    public void onEvent(ApplicationEvent event) {
        // do nothing
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return new RequestListener(System.currentTimeMillis());
    }
    
    /**
     * 
     * {@link RequestListener}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.4
     */
    private static class RequestListener implements RequestEventListener {
        
        private static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
        private static final String INVALID = "INVALID";
        private final long start;

        /**
         * Initializes a new {@link MetricsListener.RequestListener}.
         * 
         * @param startMillis The start time in milliseconds
         */
        public RequestListener(long startMillis) {
            super();
            this.start = startMillis;
        }
        
        @Override
        public void onEvent(RequestEvent event) {
            Type type = event.getType();
            if (type.equals(Type.FINISHED) || type.equals(Type.ON_EXCEPTION)) {
                List<UriTemplate> templates = event.getContainerRequest().getUriInfo().getMatchedTemplates();
                String path = null;
                if (templates.isEmpty()) {
                    path = "/" + event.getUriInfo().getPath();
                } else {
                    path = templates.get(0).getTemplate();
                }
                StatusType status = Status.INTERNAL_SERVER_ERROR;
                if(type.equals(Type.ON_EXCEPTION) || event.getException() != null) {
                    Throwable exception = event.getException();
                    if(exception instanceof WebApplicationException) {
                        WebApplicationException we = (WebApplicationException) exception;
                        if(we instanceof NotFoundException) {
                            Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);
                            getTimer(INVALID, METHOD_NOT_ALLOWED, 404).record(duration);
                            return;
                        }
                        status = we.getResponse().getStatusInfo();
                    }
                } else {
                    if(event.getContainerResponse() != null) {
                        status = event.getContainerResponse().getStatusInfo();
                    }
                }
                Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);
                getTimer(path, status.equals(Status.METHOD_NOT_ALLOWED) ? METHOD_NOT_ALLOWED : event.getContainerRequest().getMethod(), status.getStatusCode()).record(duration);
            }
        }
        
        /**
         * Gets the timer with the given values
         *
         * @param path The template path
         * @param method The method
         * @param status The status code
         * @return The Timer
         */
        private Timer getTimer(String path, String method, int status) {
            // @formatter:off
            return Timer.builder(NAME)
                        .tags("path", path, "method", method, "status", String.valueOf(status))
                        .description("Records the duration of rest calls.")
                        .register(Metrics.globalRegistry);
            // @formatter:on
        }
    }

}
