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
import com.openexchange.api2.OXException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.MicroformatStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.java.Strings;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.publish.microformats.osgi.StringTranslator;
import com.openexchange.publish.microformats.tools.UncloseableWriter;
import com.openexchange.publish.tools.PublicationSession;
import com.openexchange.templating.OXTemplate;
import com.openexchange.user.UserService;

/**
 * {@link MicroformatServlet}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MicroformatServlet extends OnlinePublicationServlet {

    private static final long serialVersionUID = 6727750981539640363L;

    private static final Map<String, OXMFPublicationService> publishers = new HashMap<String, OXMFPublicationService>();

    private static final Log LOG = LogFactory.getLog(MicroformatServlet.class);

    private static final String MODULE = "module";

    private static final String SITE = "site";

    private static final String CONTEXTID = "ctx";

    private static PublicationDataLoaderService dataLoader = null;


    private static Map<String, Map<String, Object>> additionalTemplateVariables = new HashMap<String, Map<String, Object>>();

    private static UserService userService;

    private static StringTranslator translator;

    private static ConfigurationService configService;

    private static ContactInterfaceDiscoveryService contacts;

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
    
    public static void setConfigService(ConfigurationService confService) {
        configService = confService;
    }

    public static void registerType(String module, OXMFPublicationService publisher, Map<String, Object> additionalVars) {
        publishers.put(module, publisher);
        additionalTemplateVariables.put(module, additionalVars);
    }

    public static void setContactInterfaceDiscoveryService(ContactInterfaceDiscoveryService service) {
        contacts = service;
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
            Contact userContact = getContact(new PublicationSession(publication), publication.getContext(), user.getContactId());
            
            variables.put(getCollectionName(module), loaded);
            variables.put("publication", publication);
            variables.put("request", req);
            variables.put("dateFormat", DATE_FORMAT);
            variables.put("timeFormat", TIME_FORMAT);
            String admin = configService.getProperty("PUBLISH_REVOKE");
            if(admin == null || admin.equals(""))
                admin = userService.getUser(ctx.getMailadmin(), ctx).getMail();
            String privacyText = formatPrivacyText(
                getPrivacyText(user),
                admin,
                user,
                new Date());
            variables.put("privacy", privacyText ); //TODO Use lastmodified once someone implements this.
            variables.put("userContact", userContact); 

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

    private Contact getContact(PublicationSession publicationSession, Context context, int contactId) throws OXException {
        ContactInterface contactInterface = contacts.getContactInterfaceProvider(FolderObject.SYSTEM_LDAP_FOLDER_ID, context.getContextId()).newContactInterface(publicationSession);
        Contact contact = contactInterface.getObjectById(contactId, FolderObject.SYSTEM_LDAP_FOLDER_ID);
        return contact;
    }


    private String formatPrivacyText(String privacyText, String adminAddress, User user, Date creationDate) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(creationDate);
        String retVal = String.format(privacyText, adminAddress, user.getMail(), date);
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
        return translator.translate(user.getLocale(), MicroformatStrings.DISCLAIMER_PRIVACY);
    }


}
