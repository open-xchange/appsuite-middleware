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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.subscribe.crawler.offering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.publish.microformats.tools.ContactTemplateUtils;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateException;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link CrawlerOfferingServlet}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class CrawlerOfferingServlet extends HttpServlet {

    // TODO: Authentication ?

    private static final long serialVersionUID = -6668834083007607601L;

    private static final Log LOG = LogFactory.getLog(CrawlerOfferingServlet.class);

    private static SubscriptionSourceDiscoveryService sources = null;

    private static TemplateService templateService = null;

    private static ConfigurationService configService;

    private static final String LIST_TEMPLATE = "offering_list.tmpl";

    private static final String SOURCE_TEMPLATE = "offering_source.tmpl";

    private static final String CONTACTS_TEMPLATE = "offering_contacts.tmpl";

    private static final String INFOSTORE_TEMPLATE = "offering_infostore.tmpl";

    public static void setSources(SubscriptionSourceDiscoveryService service) {
        sources = service;
    }

    public static void setTemplateService(TemplateService service) {
        templateService = service;
    }

    public static void setConfigService(ConfigurationService service) {
        configService = service;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!auth(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            String parameter = req.getParameter("action");
            if (parameter.equals("list")) {
                doList(req, resp);
            } else if (parameter.equals("source")) {
                doSource(req, resp);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(!auth(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String sourceName = req.getParameter("crawler");
        SubscriptionSource source = sources.getSource(sourceName);
        if(source == null) {
            sourceNotFound(resp, sourceName);
            return;
        }
        Map<String, Object> parameters = collectParameters(req, source);

        Subscription subscription = new Subscription();
        subscription.setSource(source);
        subscription.setConfiguration(parameters);

        try {
            resp.setContentType("text/html;charset=UTF-8");

            Collection<?> content = source.getSubscribeService().getContent(subscription);

            switch (source.getFolderModule()) {
            case FolderObject.CONTACT:
                OXTemplate template = templateService.loadTemplate(CONTACTS_TEMPLATE);
                fillResultTemplate(template, content, "contacts", resp);
                break;
            case FolderObject.INFOSTORE:
                break;
            }
        } catch (AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            resp.setContentType("text/html;charset=UTF-8");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Map<String, Object> collectParameters(HttpServletRequest req, SubscriptionSource source) {
        Map<String, Object> retval = new HashMap<String, Object>();

        for (FormElement element : source.getFormDescription()) {
            switch (element.getWidget()) {
            case INPUT:
            case TEXT:
            case PASSWORD:
            case LINK:
                retval.put(element.getName(), req.getParameter(element.getName()));
                break;
            case CHECKBOX:
                Boolean value = Boolean.valueOf(req.getParameter(element.getName()));
                retval.put(element.getName(), value);
                break;
            }
        }

        return retval;
    }

    private void doList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            OXTemplate template = templateService.loadTemplate(LIST_TEMPLATE);
            resp.setContentType("text/html;charset=UTF-8");
            fillListTemplate(template, req, resp);
        } catch (AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            resp.setContentType("text/html;charset=UTF-8");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void doSource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            OXTemplate template = templateService.loadTemplate(SOURCE_TEMPLATE);
            resp.setContentType("text/html;charset=UTF-8");
            fillSourceTemplate(template, req, resp);
        } catch (AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            resp.setContentType("text/html;charset=UTF-8");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void fillListTemplate(OXTemplate template, HttpServletRequest req, HttpServletResponse resp) throws TemplateException, IOException {
        List<Map<String, String>> links = new ArrayList<Map<String, String>>();

        sources.getSources();
        for (SubscriptionSource source : sources.getSources()) {
            Map<String, String> link = new HashMap<String, String>();
            link.put("link", getProtocol(req) + req.getServerName() + "/publications/crawler?action=source&crawler=" + source.getId());
            link.put("name", source.getDisplayName());
            links.add(link);
        }

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("LINKS", links);

        template.process(values, resp.getWriter());
    }

    private void fillSourceTemplate(OXTemplate template, HttpServletRequest req, HttpServletResponse resp) throws TemplateException, IOException {
        List<Map<String, String>> elements = new ArrayList<Map<String, String>>();

        String sourceName = req.getParameter("crawler");
        SubscriptionSource source = sources.getSource(sourceName);
        if(source == null) {
            sourceNotFound(resp, sourceName);
        }
        DynamicFormDescription formDescription = source.getFormDescription();

        for (FormElement element : formDescription) {
            Map<String, String> e = new HashMap<String, String>();
            e.put("id", element.getName());
            e.put("displayName", element.getDisplayName());
            e.put("type", element.getWidget().getKeyword());
            elements.add(e);
        }

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("ELEMENTS", elements);
        values.put("ACTION", getProtocol(req) + req.getServerName() + "/publications/crawler?action=crawl&crawler=" + source.getId());
        values.put("SOURCE", source);
        values.put("MODULE", Module.getModuleString(source.getFolderModule(), -1));
        
        template.process(values, resp.getWriter());
    }

    private void fillResultTemplate(OXTemplate template, Collection<?> content, String types, HttpServletResponse resp) throws TemplateException, IOException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("utils", new ContactTemplateUtils());
        values.put(types, content);
        template.process(values, resp.getWriter());
    }

    private String getProtocol(HttpServletRequest req) {
        return Tools.getProtocol(req);
    }

    private boolean auth(HttpServletRequest req) {
        Authentication authentication = new Whitelist(configService);
        return authentication.auth(req);
    }
    
    private void sourceNotFound(HttpServletResponse res, String sourceName) throws IOException {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().println("Source "+sourceName+" was not found on this server.");
    }

}
