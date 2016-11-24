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

package com.openexchange.publish.microformats;

import java.io.IOException;
import java.io.PrintWriter;
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
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.html.HtmlService;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Strings;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.publish.EscapeMode;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.publish.Publications;
import com.openexchange.publish.microformats.osgi.Services;
import com.openexchange.publish.microformats.osgi.StringTranslator;
import com.openexchange.publish.microformats.tools.CustomizableHttpServletRequest;
import com.openexchange.publish.microformats.tools.HTMLUtils;
import com.openexchange.publish.tools.PublicationSession;
import com.openexchange.templating.OXTemplate;
import com.openexchange.user.UserService;

/**
 * {@link MicroformatServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - HTML whitelisting, same-origin-policy stuff
 */
public class MicroformatServlet extends OnlinePublicationServlet {

    private static final long serialVersionUID = 6727750981539640363L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MicroformatServlet.class);

    private static final Map<String, OXMFPublicationService> PUBLISHERS = new ConcurrentHashMap<String, OXMFPublicationService>();

    private static final String MODULE = OXMFConstants.MODULE;

    private static final String SITE = OXMFConstants.SITE;

    private static final String CONTEXTID = OXMFConstants.CONTEXTID;

    private static final String SITE_NAME = OXMFConstants.SITE_NAME;

    /**
     * Property to get the configured name of the hoster to be displayed in disclaimer of the default template
     */
    private static final String PROPERTY_LEGAL_HOSTER_NAME = "com.openexchange.publish.legalHosterName";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd H:m:s.S z");
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final Map<String, Map<String, Object>> ADDITONAL_TEMPLATE_VARIABLES = new ConcurrentHashMap<String, Map<String, Object>>();

    private static volatile PublicationDataLoaderService dataLoader = null;
    private static volatile UserService userService;
    private static volatile StringTranslator translator;
    private static volatile ConfigurationService configService;
    private static volatile ContactService contacts;
    public static volatile HtmlService htmlService;

    public static void setPublicationDataLoaderService(final PublicationDataLoaderService service) {
        dataLoader = service;
    }

    public static void setUserService(final UserService service) {
        userService = service;
    }

    public static void setStringTranslator(final StringTranslator trans) {
        translator = trans;
    }

    public static void setConfigService(final ConfigurationService confService) {
        configService = confService;
    }

    public static void registerType(final String module, final OXMFPublicationService publisher, final Map<String, Object> additionalVars) {
        PUBLISHERS.put(module, publisher);
        ADDITONAL_TEMPLATE_VARIABLES.put(module, additionalVars);
    }

    public static void setContactService(final ContactService service) {
        contacts = service;
    }

    public static void setHtmlService(final HtmlService htmlService2) {
        htmlService = htmlService2;
    }

    // ----------------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link MicroformatServlet}.
     */
    public MicroformatServlet() {
        super();
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            resp.setContentType("text/html; charset=UTF-8");
            final Map<String, String> args = getPublicationArguments(req);
            if(args==null){
                final PrintWriter writer = resp.getWriter();
                writer.println("The publication request is missing some or all parameters.");
                writer.flush();
                return;
            }
            final String module = args.get(MODULE);

            final OXMFPublicationService publisher = PUBLISHERS.get(module);
            if (publisher == null) {
                final PrintWriter writer = resp.getWriter();
                String escaped = Publications.escape(module, EscapeMode.HTML);
                writer.println("The publication has either been revoked in the meantime or module \"" + escaped + "\" is unknown.");
                writer.flush();
                return;
            }
            final Context ctx = contexts.getContext(Integer.parseInt(args.get(CONTEXTID)));
            final Publication publication = publisher.getPublication(ctx, args.get(SITE));
            if (publication == null || !publication.isEnabled()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                final PrintWriter writer = resp.getWriter();
                final HtmlService htmlService = MicroformatServlet.htmlService;
                writer.println("Unknown site " + (null == htmlService ? "" : htmlService.encodeForHTML(args.get(SITE))));
                writer.flush();
                return;
            }
            if (!checkProtected(publication, args, resp)) {
                return;
            }

            if (!checkPublicationPermission(publication, resp)) {
                return;
            }

            Collection<? extends Object> loaded = dataLoader.load(publication, EscapeMode.HTML);
            User user = getUser(publication);
            Contact userContact = getContact(new PublicationSession(publication));

            // Sanitize publication
            sanitizePublication(publication);

            // Compose variables for template processing
            Map<String, Object> variables = new HashMap<String, Object>(12, 0.9F);
            variables.put(getCollectionName(module), loaded);
            variables.put("publication", publication);
            {
                HttpServletRequest httpRequest = req;

                HostnameService hostnameService = Services.getService(HostnameService.class);
                if (null != hostnameService) {
                    try {
                        String hostName = hostnameService.getHostname(publication.getUserId(), publication.getContext().getContextId());
                        if (false == Strings.isEmpty(hostName)) {
                            // Set server name as returned by HostnameService
                            httpRequest = new CustomizableHttpServletRequest(httpRequest).setServerName(hostName);
                        }
                    } catch (Exception e) {
                        // Unable to check for proper host name
                    }
                }

                variables.put("request", httpRequest);
            }
            variables.put("dateFormat", DATE_FORMAT);
            variables.put("timeFormat", TIME_FORMAT);
            {
                String admin = configService.getProperty("PUBLISH_REVOKE");
                if (admin == null || admin.equals("")) {
                    admin = userService.getUser(ctx.getMailadmin(), ctx).getMail();
                }
                String legalHosterName = configService.getProperty(MicroformatServlet.PROPERTY_LEGAL_HOSTER_NAME);
                if (legalHosterName == null || legalHosterName.equals("")) {
                    legalHosterName = MicroformatStrings.DISCLAIMER_ALTERNATIV_HOSTER_NAME_WORDING;
                }
                String privacyText = formatPrivacyText(getPrivacyText(user), legalHosterName, admin, user, new Date(publication.getCreated()));
                variables.put("privacy", privacyText); // TODO Use lastmodified once someone implements this.
            }
            variables.put("userContact", userContact);
            variables.put("htmlService", new HTMLUtils(htmlService));

            if (ADDITONAL_TEMPLATE_VARIABLES.containsKey(module)) {
                variables.putAll(ADDITONAL_TEMPLATE_VARIABLES.get(module));
            }

            final OXTemplate template = publisher.loadTemplate(publication);
            final AllocatingStringWriter htmlWriter = new AllocatingStringWriter();
            template.process(variables, htmlWriter);
            String html = htmlWriter.toString();
            if (!template.isTrusted()) {
                //html = htmlService.getConformHTML(html, Charset.defaultCharset().toString());
                html = htmlService.sanitize(html, "microformatWhitelist", false, null, null);
            }
            final PrintWriter writer = resp.getWriter();
            writer.write(html);
            writer.flush();
        } catch (final OXException x) {
            LOG.error("", x);
            final PrintWriter writer = resp.getWriter();
            writer.println("Publishing failed. Please try again later. Exception ID: " + x.getExceptionId());
            writer.flush();
        } catch (final Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("", t);
            final PrintWriter writer = resp.getWriter();
            writer.println("Publishing failed. Please try again later.");
            writer.flush();
        }
    }

    private void sanitizePublication(final Publication publication) {
        Map<String, Object> configuration = publication.getConfiguration();
        Object siteObj = configuration.get(SITE_NAME);
        if (null != siteObj) {
            configuration.put(SITE_NAME, saneSiteName(siteObj.toString()));
        }
    }

    private Contact getContact(final PublicationSession publicationSession) throws OXException {
        return contacts.getUser(publicationSession, publicationSession.getUserId());
    }

    private String formatPrivacyText(final String privacyText, final String company, final String adminAddress, final User user, final Date creationDate) {
        final String date = new SimpleDateFormat("yyyy-MM-dd").format(creationDate);
        final String retVal = String.format(privacyText, company, adminAddress, user.getMail(), date);
        return "<p>" + retVal.replaceAll("\n", "</p><p>") + "</p>";
    }

    private String getCollectionName(final String module) {
        return module;
    }

    private Map<String, String> getPublicationArguments(final HttpServletRequest req) throws UnsupportedEncodingException {
        final String[] path;
        if(req.getPathInfo()==null){
            path = new String[0];
        } else {
            path = SPLIT.split(req.getPathInfo(), 0);
        }
        final List<String> normalized = new ArrayList<String>(path.length);
        for (int i = 0; i < path.length; i++) {
            if (!path[i].equals("")) {
                normalized.add(path[i]);
            }
        }

        if(normalized.size()<2){
            return null;
        }
        final String site = Strings.join(HelperClass.decode(normalized.size() > 2 ? normalized.subList(2, normalized.size()) : normalized, req, SPLIT2), "/");
        final Map<String, String> args = new HashMap<String, String>();
        args.put(MODULE, normalized.get(0));
        args.put(CONTEXTID, normalized.get(1));
        args.put(SITE, site);
        args.put(SECRET, req.getParameter(SECRET));
        return args;
    }

    private User getUser(final Publication publication) throws OXException {
        final Context context = publication.getContext();
        final int uid = publication.getUserId();
        final User user = userService.getUser(uid, context);
        return user;
    }

    private String getPrivacyText(final User user) {
        return translator.translate(user.getLocale(), MicroformatStrings.DISCLAIMER_PRIVACY);
    }

    private String saneSiteName(final String site) {
        if (Strings.isEmpty(site)) {
            return site;
        }
        return AJAXUtility.encodeUrl(site, true, false);
    }

}
