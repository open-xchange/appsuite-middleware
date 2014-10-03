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

package com.openexchange.appsuite;

import static com.openexchange.ajax.requesthandler.AJAXRequestDataBuilder.request;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AppSuiteLoginRampUp}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AppSuiteLoginRampUp implements LoginRampUpService {

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

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AppSuiteLoginRampUp}.
     *
     * @param services The service look-up
     */
    public AppSuiteLoginRampUp(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public boolean contributesTo(String client) {
        return "open-xchange-appsuite".equals(client);
    }

    /** The ramp-up keys. Keep order! */
    private static final RampUpKey[] KEYS = RampUpKey.values();

    @Override
    public JSONObject getContribution(final ServerSession session, final AJAXRequestData loginRequest) throws OXException {
        int numberOfKeys = KEYS.length;

        final ConcurrentMap<String, Object> rampUp = new ConcurrentHashMap<String, Object>(numberOfKeys);
        final Dispatcher ox = services.getService(Dispatcher.class);

        ThreadPoolService threads = services.getService(ThreadPoolService.class);
        ThreadPoolCompletionService<Void> completionService = new ThreadPoolCompletionService<Void>(threads);

        completionService.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                try {
                    JSONObject folderlist = new JSONObject(2);
                    folderlist.put("1", ox.perform(request().module("folders").action("list").params("parent", "1", "tree", "0", "altNames", "true", "timezone", "UTC", "columns", "1,2,3,4,5,6,20,23,300,301,302,304,305,306,307,308,309,310,311,312,313,314,315,316,317,3010,3020,3030").format("json").build(), null, session).getResultObject());
                    rampUp.put(RampUpKey.FOLDER_LIST.key, folderlist);
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                }
                return null;
            }
        });

        completionService.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                JSONObject folder = new JSONObject(3);
                try {
                    folder.put("1", ox.perform(request().module("folders").action("get").params("id", "1", "tree", "1", "altNames", "true", "timezone", "UTC").format("json").build(), null, session).getResultObject());
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                }
                try {
                    folder.put("default0/INBOX", ox.perform(request().module("folders").action("get").params("id", "default0/INBOX", "tree", "1", "altNames", "true", "timezone", "UTC").format("json").build(), null, session).getResultObject());
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                }
                rampUp.put(RampUpKey.FOLDER.key, folder);
                return null;
            }
        });

        completionService.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                try {
                    JSONObject jslobs = new JSONObject();
                    JSONArray lobs = (JSONArray) ox.perform(request()
                        .module("jslob")
                        .action("list")
                        .data(new JSONArray(Arrays.asList("io.ox/core", "io.ox/core/updates", "io.ox/mail", "io.ox/contacts", "io.ox/calendar", "io.ox/caldav", "io.ox/files", "io.ox/tours", "io.ox/mail/emoji", "io.ox/tasks", "io.ox/office")
                            ), "json"
                        ).format("json").build(), null, session).getResultObject();
                    for(int i = 0, size = lobs.length(); i < size; i++) {
                        JSONObject lob = lobs.getJSONObject(i);
                        jslobs.put(lob.getString("id"), lob);
                    }
                    rampUp.put(RampUpKey.JSLOBS.key, jslobs);
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                }
                return null;
            }
        });

        completionService.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                AJAXRequestData manifestRequest = request().module("apps/manifests").action("config").format("json").hostname(loginRequest.getHostname()).build();
                try {
                    rampUp.put(RampUpKey.SERVER_CONFIG.key, ox.perform(manifestRequest, null, session).getResultObject());
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                }
                return null;
            }
        });

        completionService.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                JSONObject oauth = new JSONObject(3);
                rampUp.put(RampUpKey.OAUTH.key, oauth);

                try {
                    oauth.put("services", ox.perform(request().module("oauth/services").action("all").format("json").build(), null, session).getResultObject());
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                }
                try {
                    oauth.put("accounts", ox.perform(request().module("oauth/accounts").action("all").format("json").build(), null, session).getResultObject());
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                }

                try {
                    oauth.put("secretCheck", ox.perform(request().module("recovery/secret").action("check").format("json").build(), null, session).getResultObject());
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                }
                return null;
            }
        });

        completionService.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                try {
                    rampUp.put(RampUpKey.USER.key, ox.perform(request().module("user").action("get").params("timezone", "utc", "id", "" + session.getUserId()).format("json").build(), null, session).getResultObject());
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                }
                return null;
            }
        });

        completionService.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                try {
                    rampUp.put(RampUpKey.ACCOUNTS.key, ox.perform(request().module("account").action("all").format("json").params("columns", "1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018,1019,1020,1021,1022,1023,1024,1025,1026,1027,1028,1029,1030,1031,1032,1033,1034,1035,1036,1037,1038,1039,1040,1041,1042,1043").build(), null, session).getResultObject());
                } catch (OXException x) {
                    // Omit result on error. Let the UI deal with this
                }
                return null;
            }

        });

        for (int i = numberOfKeys; i-- > 0;) {
            try {
                completionService.take();
            } catch (InterruptedException e) {
                // Keep interrupted state
                Thread.currentThread().interrupt();
                Logger logger = org.slf4j.LoggerFactory.getLogger(AppSuiteLoginRampUp.class);
                logger.warn("Failed ramp-up", e);
                return new JSONObject();
            }
        }

        try {
            JSONObject jo = new JSONObject(numberOfKeys);
            for (RampUpKey rampUpKey : KEYS) {
                jo.put(rampUpKey.key, JSONCoercion.coerceToJSON(rampUp.get(rampUpKey.key)));
            }
            return jo;
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            Logger logger = org.slf4j.LoggerFactory.getLogger(AppSuiteLoginRampUp.class);
            logger.warn("Failed ramp-up", t);
            return new JSONObject();
        }
    }


}
