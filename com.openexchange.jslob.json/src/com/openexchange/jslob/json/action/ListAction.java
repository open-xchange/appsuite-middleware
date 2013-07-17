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

package com.openexchange.jslob.json.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobExceptionCodes;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.json.JSlobRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ListAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
@Action(name = "list", description = "Get a list of JSlobs associated with the current user and context.", method = RequestMethod.PUT, parameters = { @Parameter(name = "serviceId", description = "Optional identifier for the JSlob. Default is <tt>com.openexchange.jslob.config</tt>", optional = true) }, requestBody = "An array containing JSlob identifiers.", responseDescription = "An array containing JSlobs.")
public final class ListAction extends JSlobAction {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ListAction.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link ListAction}.
     *
     * @param services The service look-up
     */
    public ListAction(final ServiceLookup services, final Map<String, JSlobAction> actions) {
        super(services, actions);
    }

    @Override
    protected AJAXRequestResult perform(final JSlobRequest jslobRequest) throws OXException, JSONException {
        try {
            String serviceId = jslobRequest.getParameter("serviceId", String.class);
            if (null == serviceId) {
                serviceId = DEFAULT_SERVICE_ID;
            }
            final JSlobService jslobService = getJSlobService(serviceId);
            final JSONArray ids = (JSONArray) jslobRequest.getRequestData().requireData();
            final int length = ids.length();
            // Check length
            if (length <= 1) {
                if (0 == length) {
                    return new AJAXRequestResult(Collections.<JSlob> emptyList(), "jslob");
                }
                return new AJAXRequestResult(Collections.<JSlob> singletonList(jslobService.get(ids.getString(0), jslobRequest.getSession())), "jslob");
            }
            // More than one to load
            final ServerSession session = jslobRequest.getSession();
            // Create completion service and result map
            final CompletionService<Void> completionService = new ThreadPoolCompletionService<Void>(ThreadPools.getThreadPool());
            final ConcurrentMap<String, JSlob> results = new ConcurrentHashMap<String, JSlob>(length);
            // Submit tasks
            final int numTasks = length - 1;
            for (int i = 0; i < numTasks; i++) {
                final String id = ids.getString(i);
                completionService.submit(new Callable<Void>() {

                    @Override
                    public Void call() throws OXException {
                        try {
                            results.put(id, jslobService.get(id, session));
                            return null;
                        } catch (final RuntimeException e) {
                            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                        }
                    }
                });
            }
            // Perform last with current thread
            {
                final String id = ids.getString(numTasks);
                results.put(id, jslobService.get(id, session));
            }
            // Await completion
            for (int i = 0; i < numTasks; i++) {
                completionService.take();
            }
            // Assign to list
            final List<JSlob> jslobs = new ArrayList<JSlob>(length);
            for (int i = 0; i < length; i++) {
                final JSlob jSlob = results.get(ids.getString(i));
                if (null != jSlob) {
                    jslobs.add(jSlob);
                }
            }
            return new AJAXRequestResult(jslobs, "jslob");
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getAction() {
        return "list";
    }

}
