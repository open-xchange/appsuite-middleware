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



package com.openexchange.custom.parallels.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.custom.parallels.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * Servlet which returns needed Data for the Parallels direct links manipulation
 * and also returns some data like username,password etc.
 *
 * Also does jobs for the other GUI Plugins
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 *
 */
public final class ParallelsInfoServlet extends PermissionServlet {

    /**
     *
     */
    private static final long serialVersionUID = -6454818806420432111L;

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ParallelsInfoServlet.class);

    /**
     * Initializes
     */
    public ParallelsInfoServlet() {
        super();
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }

    @Override
    protected void doGet(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException,
        IOException {

        try {
            actionGetData(req, resp);
        } catch (final JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     * Performs the GET request!
     *
     * This will return a Json Object with username,password,context
     * name,context-id,branding-url. Can then be used in IFRAME Plugins and
     * for manipulating direct urls.
     *
     * obj.put("login"
		obj.put("password",
		obj.put("context_id"
		obj.put("context_name"
		obj.put("mail"
		obj.put("branding_host"
     *
     *
     * @param req
     *            The servlet request
     * @param resp
     *            The servlet response
     * @throws JSONException
     *             If JSON data cannot be composed
     * @throws IOException
     *             If an I/O error occurs
     */
    private void actionGetData(final HttpServletRequest req,
        final HttpServletResponse resp) throws JSONException, IOException {
        /*
         * Some variables we need here to handle the request
         */
        final Response response = new Response();
        final Session session = getSessionObject(req);

        String login = session.getLogin();

        final String[] tmp = login.split("@");
        login = tmp[0];
        final String password = session.getPassword();
        final int contextid = session.getContextId();
        String context_name = "UNRESOLVED";
        String mail = null;
        String branded_host = null;
        try {

            final Context ctx = ContextStorage.getInstance().getContext(contextid);
            final User userobject = getUserObjectFromSession(session);
            context_name = ctx.getName();

            // resolve the branding URL
            branded_host = getBrandingHostFromLoginMappings(ctx);

            mail = userobject.getMail();
        } catch (final OXException e) {
            LOG.error("", e);
        } catch (final ServiceException e) {
            LOG.error("Error resolving branded url for context {}", contextid);
        }

        final JSONObject obj = new JSONObject();

        obj.put("login", login);
        obj.put("password", password);
        obj.put("context_id", contextid);
        obj.put("context_name", context_name);
        obj.put("mail", mail);
        obj.put("branding_host", branded_host);

        response.setData(obj);

        /*
         * Close response and flush print writer
         */
        ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
    }

    public static User getUserObjectFromSession(final Session session) throws OXException{

        final int contextid = session.getContextId();
        final Context ctx = ContextStorage.getInstance().getContext(contextid);

        return UserStorage.getInstance().getUser(session.getUserId(),ctx);
    }



    private String getBrandingHostFromLoginMappings(final Context ctx) throws OXException {

        final String[] login_mappings = ctx.getLoginInfo();
        final ConfigurationService configservice = Services.getService(ConfigurationService.class);

        String branded_host = null;

        // load suffix for branding string dynamically in loginmappings
        final String suffix_branded = configservice.getProperty(ParallelsOptions.PROPERTY_BRANDING_SUFFIX);
        LOG.debug("Loaded loginmappings {} for context {}", Arrays.toString(login_mappings), ctx.getContextId());
        boolean found_host = false;
        if( null != suffix_branded && suffix_branded.length() != 0) {
            for (final String login_mapping : login_mappings) {
                if(login_mapping.startsWith(suffix_branded)){
                    /**
                     *
                     *  We found our mapping which contains the branded host!
                     *
                     *  Now split up the string to get the host part
                     *
                     */
                    final String[] URL_ = login_mapping.split("\\|\\|"); // perhaps replace with substring(start,end) if would be faster
                    if(URL_.length!=3){
                        LOG.error("getBrandingHostFromLoginMappings: Could not split up branded host {} login mapping for context {}", login_mapping, ctx.getContextId());
                    }else{
                        branded_host = URL_[2];
                        LOG.debug("Successfully resolved HOST to {} for branded context {}", branded_host, ctx.getContextId());
                    }
                }
            }
        }
        if(!found_host){
            // now host was provisioned, load fallback from configuration
            branded_host = configservice.getProperty(ParallelsOptions.PROPERTY_BRANDING_FALLBACKHOST);
            // use systems getHostname() if no fallbackhost is set
            if( null == branded_host || branded_host.length() == 0 ) {
                try {
                    branded_host = InetAddress.getLocalHost().getCanonicalHostName();
                } catch (UnknownHostException e) { }
            }
            if( null == branded_host || branded_host.length() == 0 ) {
                LOG.warn("getHostname: Unable to determine any hostname for context {}", ctx.getContextId());
            }
        }

        return branded_host;

    }





}
