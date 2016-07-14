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

package com.openexchange.login;

import static com.openexchange.ajax.requesthandler.AJAXRequestDataBuilder.request;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptions;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DefaultAppSuiteLoginRampUp} - The default ramp-up implementation.
 * <p>
 * <div style="background-color:#FFDDDD; padding:6px; margin:0px;">
 * <b>Note</b><br>
 * The passed {@link ServiceLookup} instance needs to track the following singleton services:
 * <ul>
 * <li>com.openexchange.threadpool.ThreadPoolService</li>
 * <li>com.openexchange.ajax.requesthandler.Dispatcher</li>
 * </ul>
 * </div>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class DefaultAppSuiteLoginRampUp implements LoginRampUpService {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAppSuiteLoginRampUp.class);

    private static enum RampUpKey {

        SERVER_CONFIG("serverConfig"),
        JSLOBS("jslobs"),
        OAUTH("oauth"),
        FOLDER("folder"),
        FOLDER_LIST("folderlist"),
        USER("user"),
        ACCOUNTS("accounts"),

        ;

        final String key;
        private RampUpKey(String key) {
            this.key = key;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------ //

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link DefaultAppSuiteLoginRampUp}.
     *
     * @param services The service look-up
     */
    protected DefaultAppSuiteLoginRampUp(ServiceLookup services) {
        super();
        this.services = services;
    }

    static void handleException(OXException e, String key) {
        if (OXExceptions.isCategory(Category.CATEGORY_PERMISSION_DENIED, e)) {
            LOG.debug("Permission error during {} ramp-up", key, e);
        } else {
            LOG.error("Error during {} ramp-up", key, e);
        }
    }

    static void handleException(Exception e, String key) {
        LOG.error("Error during {} ramp-up", key, e);
    }

    /** The ramp-up keys. Keep order! */
    private static final RampUpKey[] KEYS = RampUpKey.values();

    @Override
    public JSONObject getContribution(final ServerSession session, final AJAXRequestData loginRequest) throws OXException {
        int numberOfKeys = KEYS.length;

        ConcurrentMap<String, Future<Object>> rampUps = new ConcurrentHashMap<String, Future<Object>>(numberOfKeys);
        ThreadPoolService threads = services.getService(ThreadPoolService.class);

        final Dispatcher ox = services.getService(Dispatcher.class);

        rampUps.put(RampUpKey.FOLDER_LIST.key, threads.submit(new AbstractTask<Object>() {

            @Override
            public Object call() throws Exception {
                AJAXRequestResult requestResult = null;
                Exception exc = null;
                try {
                    JSONObject folderlist = new JSONObject(2);
                    AJAXRequestData requestData = request().session(session).module("folders").action("list").params("parent", "1", "tree", "0", "altNames", "true", "timezone", "UTC", "columns", "1,2,3,4,5,6,20,23,300,301,302,304,305,306,307,308,309,310,311,312,313,314,315,316,317,318,3010,3020,3030").format("json").build(loginRequest);
                    requestResult = ox.perform(requestData, null, session);
                    folderlist.put("1", requestResult.getResultObject());
                    return folderlist;
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.FOLDER_LIST.key);
                } catch (RuntimeException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.FOLDER_LIST.key);
                } finally {
                    Dispatchers.signalDone(requestResult, exc);
                }
                return null;
            }
        }));

        rampUps.put(RampUpKey.FOLDER.key, threads.submit(new AbstractTask<Object>() {

            @Override
            public Object call() throws Exception {
                if (null == session.getUserPermissionBits() || false == session.getUserPermissionBits().hasWebMail()) {
                    return null;
                }
                JSONObject folder = new JSONObject(3);
                {
                    AJAXRequestResult requestResult = null;
                    Exception exc = null;
                    try {
                        AJAXRequestData requestData = request().session(session).module("folders").action("get").params("id", "1", "tree", "1", "altNames", "true", "timezone", "UTC").format("json").build(loginRequest);
                        requestResult = ox.perform(requestData, null, session);
                        folder.put("1", requestResult.getResultObject());
                    } catch (OXException x) {
                        // Omit result on error. Let the UI deal with this
                        exc = x;
                        handleException(x, RampUpKey.FOLDER.key);
                    } catch (RuntimeException x) {
                        // Omit result on error. Let the UI deal with this
                        exc = x;
                        handleException(x, RampUpKey.FOLDER.key);
                    } finally {
                        Dispatchers.signalDone(requestResult, exc);
                    }
                }
                {
                    AJAXRequestResult requestResult = null;
                    Exception exc = null;
                    try {
                        AJAXRequestData requestData = request().session(session).module("folders").action("get").params("id", "default0/INBOX", "tree", "1", "altNames", "true", "timezone", "UTC").format("json").build(loginRequest);
                        requestResult = ox.perform(requestData, null, session);
                        folder.put("default0/INBOX", requestResult.getResultObject());
                    } catch (OXException x) {
                        // Omit result on error. Let the UI deal with this
                        exc = x;
                        handleException(x, RampUpKey.FOLDER.key);
                    } catch (RuntimeException x) {
                        // Omit result on error. Let the UI deal with this
                        exc = x;
                        handleException(x, RampUpKey.FOLDER.key);
                    } finally {
                        Dispatchers.signalDone(requestResult, exc);
                    }
                }
                return folder;
            }
        }));

        rampUps.put(RampUpKey.JSLOBS.key, threads.submit(new AbstractTask<Object>() {

            @Override
            public Object call() throws Exception {
                AJAXRequestResult requestResult = null;
                Exception exc = null;
                try {
                    JSONObject jslobs = new JSONObject();
                    AJAXRequestData requestData = request().session(session).module("jslob").action("list").data(new JSONArray(Arrays.asList("io.ox/core", "io.ox/core/updates", "io.ox/mail", "io.ox/contacts", "io.ox/calendar", "io.ox/caldav", "io.ox/files", "io.ox/tours", "io.ox/mail/emoji", "io.ox/tasks", "io.ox/office")), "json").format("json").build(loginRequest);
                    requestResult = ox.perform(requestData, null, session);
                    JSONArray lobs = (JSONArray) requestResult.getResultObject();
                    for (int i = 0, size = lobs.length(); i < size; i++) {
                        JSONObject lob = lobs.getJSONObject(i);
                        jslobs.put(lob.getString("id"), lob);
                    }
                    return jslobs;
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.JSLOBS.key);
                } catch (RuntimeException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.JSLOBS.key);
                } finally {
                    Dispatchers.signalDone(requestResult, exc);
                }
                return null;
            }
        }));

        rampUps.put(RampUpKey.SERVER_CONFIG.key, threads.submit(new AbstractTask<Object>() {

            @Override
            public Object call() throws Exception {
                AJAXRequestResult requestResult = null;
                Exception exc = null;
                try {
                    AJAXRequestData requestData = request().session(session).module("apps/manifests").action("config").format("json").hostname(loginRequest.getHostname()).build(loginRequest);
                    requestResult = ox.perform(requestData, null, session);
                    return requestResult.getResultObject();
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.SERVER_CONFIG.key);
                } catch (RuntimeException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.SERVER_CONFIG.key);
                } finally {
                    Dispatchers.signalDone(requestResult, exc);
                }
                return null;
            }
        }));

        rampUps.put(RampUpKey.OAUTH.key, threads.submit(new AbstractTask<Object>() {

            @Override
            public Object call() throws Exception {
                if (session.isAnonymous() || session.getUser().isGuest()) {
                    return null;
                }
                JSONObject oauth = new JSONObject(3);

                AJAXRequestResult requestResult = null;
                Exception exc = null;
                try {
                    AJAXRequestData requestData = request().session(session).module("oauth/services").action("all").format("json").build(loginRequest);
                    requestResult = ox.perform(requestData, null, session);
                    oauth.put("services", requestResult.getResultObject());
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.OAUTH.key);
                } catch (RuntimeException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.OAUTH.key);
                } finally {
                    Dispatchers.signalDone(requestResult, exc);
                }

                requestResult = null;
                exc = null;
                try {
                    AJAXRequestData requestData = request().session(session).module("oauth/accounts").action("all").format("json").build(loginRequest);
                    requestResult = ox.perform(requestData, null, session);
                    oauth.put("accounts", requestResult.getResultObject());
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.OAUTH.key);
                } catch (RuntimeException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.OAUTH.key);
                } finally {
                    Dispatchers.signalDone(requestResult, exc);
                }

                requestResult = null;
                exc = null;
                try {
                    AJAXRequestData requestData = request().session(session).module("recovery/secret").action("check").format("json").build(loginRequest);
                    requestResult = ox.perform(requestData, null, session);
                    oauth.put("secretCheck", requestResult.getResultObject());
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.OAUTH.key);
                } catch (RuntimeException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.OAUTH.key);
                } finally {
                    Dispatchers.signalDone(requestResult, exc);
                }

                return oauth;
            }
        }));

        rampUps.put(RampUpKey.USER.key, threads.submit(new AbstractTask<Object>() {

            @Override
            public Object call() throws Exception {
                AJAXRequestResult requestResult = null;
                Exception exc = null;
                try {
                    AJAXRequestData requestData = request().session(session).module("user").action("get").params("timezone", "utc", "id", Integer.toString(session.getUserId())).format("json").build(loginRequest);
                    requestResult = ox.perform(requestData, null, session);
                    return requestResult.getResultObject();
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.USER.key);
                } catch (RuntimeException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.USER.key);
                } finally {
                    Dispatchers.signalDone(requestResult, exc);
                }
                return null;
            }
        }));

        rampUps.put(RampUpKey.ACCOUNTS.key, threads.submit(new AbstractTask<Object>() {

            @Override
            public Object call() throws Exception {
                AJAXRequestResult requestResult = null;
                Exception exc = null;
                try {
                    AJAXRequestData requestData = request().session(session).module("account").action("all").format("json").params("columns", "all").build(loginRequest);
                    requestResult = ox.perform(requestData, null, session);
                    return requestResult.getResultObject();
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.ACCOUNTS.key);
                } catch (RuntimeException x) {
                    // Omit result on error. Let the UI deal with this
                    exc = x;
                    handleException(x, RampUpKey.ACCOUNTS.key);
                } finally {
                    Dispatchers.signalDone(requestResult, exc);
                }
                return null;
            }

        }));

        try {
            JSONObject jo = new JSONObject(numberOfKeys);
            for (RampUpKey rampUpKey : KEYS) {
                Object value = rampUps.get(rampUpKey.key).get();
                jo.put(rampUpKey.key, JSONCoercion.coerceToJSON(value));
            }
            return jo;
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            Logger logger = org.slf4j.LoggerFactory.getLogger(DefaultAppSuiteLoginRampUp.class);
            logger.warn("Failed ramp-up", t);
            return new JSONObject();
        }
    }


}
