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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.publish.microformats;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.java.Strings;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.publish.microformats.osgi.StringTranslator;
import com.openexchange.publish.microformats.tools.UncloseableWriter;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;

/**
 * {@link MicroformatServlet}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MicroformatServlet extends OnlinePublicationServlet {

    private static final long serialVersionUID = 6727750981539640363L;

    private static final String DISCLAIMER_PRIVACY = "Privacy:\n"
        +   "The data and any references contained in this webpage are shared with you under the assumption "
        +   "that the owner of the data was entitled to make them available to you. Neither Open-Xchange, "
        +   "nor any of its subsidiaries or affiliates shall be liable for its publication or "
        +   "re-publication.\n"
        +   "Any unauthorized use or dissemination of this data is prohibited. If you intend to store, "
        +   "process, or pass on this data, please make sure that you have the right to do so.\n"
        +   "If you are one of the people listed or responsible for a resource listed on this page and you "
        +   "don’t agree with the publication, please send an email containing the URL (the link) to this "
        +   "webpage to %s and the publisher of the data:\n"
        +   "Data published by %s on %s\n";
    
    private static final Map<String, OXMFPublicationService> publishers = new HashMap<String, OXMFPublicationService>();

    private static final Log LOG = LogFactory.getLog(MicroformatServlet.class);

    private static final String MODULE = "module";

    private static final String SITE = "site";

    private static final String CONTEXTID = "ctx";

    private static PublicationDataLoaderService dataLoader = null;


    private static Map<String, Map<String, Object>> additionalTemplateVariables = new HashMap<String, Map<String, Object>>();

    private static UserService userService;

    private static StringTranslator translator;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd H:m:s.S z");
    static {
        TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static void setPublicationDataLoaderService(PublicationDataLoaderService service) {
        dataLoader = service;
    }


    public static void setUserService(UserService service) {
        userService = service;
    }

    public static void setStringTranslator(StringTranslator trans) {
        translator = trans;
    }

    public static void registerType(String module, OXMFPublicationService publisher, Map<String, Object> additionalVars) {
        publishers.put(module, publisher);
        additionalTemplateVariables.put(module, additionalVars);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            resp.setContentType("text/html; charset=UTF-8");
            Map<String, String> args = getPublicationArguments(req);
            String module = args.get(MODULE);

            OXMFPublicationService publisher = publishers.get(module);
            if (publisher == null) {
                resp.getWriter().println("Don't know how to handle module " + module);
                return;
            }
            Context ctx = contexts.getContext(Integer.parseInt(args.get(CONTEXTID)));
            Publication publication = publisher.getPublication(ctx, args.get(SITE));
            if (publication == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().println("Don't know site " + args.get(SITE));

                return;
            }
            if (!checkProtected(publication, args, resp)) {
                return;
            }

            Collection<? extends Object> loaded = dataLoader.load(publication);

            HashMap<String, Object> variables = new HashMap<String, Object>();
            User user = getUser(publication);
            variables.put(getCollectionName(module), loaded);
            variables.put("publication", publication);
            variables.put("request", req);
            variables.put("dateFormat", DATE_FORMAT);
            variables.put("timeFormat", TIME_FORMAT);
            variables.put("privacy", formatPrivacyText(getPrivacyText(user), user, new Date()));

            if (additionalTemplateVariables.containsKey(module)) {
                variables.putAll(additionalTemplateVariables.get(module));
            }
            
            OXTemplate template = publisher.loadTemplate(publication);
            template.process(variables, new UncloseableWriter(resp.getWriter()));

        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
            resp.getWriter().println("An exception occurred: ");
            t.printStackTrace(resp.getWriter());
        }
    }

    private String formatPrivacyText(String privacyText, User user, Date creationDate) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(creationDate);
        String retVal = String.format(privacyText, "revoke@ox.io", user.getMail(), date);
        return "<p>" +retVal.replaceAll("\n", "</p><p>") + "</p>";
    }

    private String getCollectionName(String module) {
        return module;
    }

    private Map<String, String> getPublicationArguments(HttpServletRequest req) throws UnsupportedEncodingException {
        String[] path = req.getPathInfo().split("/");
        List<String> normalized = new ArrayList<String>(path.length);
        for (int i = 0; i < path.length; i++) {
            if (!path[i].equals("")) {
                normalized.add(path[i]);
            }
        }

        int startIndex = normalized.indexOf("publications");
        if (startIndex == -1) {
            throw new IllegalArgumentException("This does not look like a valid path: " + req.getPathInfo());
        }
        String site = Strings.join(decode(normalized.subList(startIndex + 3, normalized.size()), req), "/");
        Map<String, String> args = new HashMap<String, String>();
        args.put(MODULE, normalized.get(startIndex + 1));
        args.put(CONTEXTID, normalized.get(startIndex + 2));
        args.put(SITE, site);
        args.put(SECRET, req.getParameter(SECRET));
        return args;
    }

    private User getUser(Publication publication) throws UserException {
        Context context = publication.getContext();
        int uid = publication.getUserId();
        User user = userService.getUser(uid, context);
        return user;
    }
    
    private String getPrivacyText(User user){
        return translator.translate(user.getLocale(), DISCLAIMER_PRIVACY);
    }

}
