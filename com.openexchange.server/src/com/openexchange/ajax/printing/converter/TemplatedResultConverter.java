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

package com.openexchange.ajax.printing.converter;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.osgi.framework.ServiceReference;
import com.openexchange.ajax.printing.TemplateHelperFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.server.ServiceLookup;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link TemplatedResultConverter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TemplatedResultConverter implements ResultConverter, SimpleRegistryListener<TemplateHelperFactory> {

    private final ServiceLookup services;
    private final List<TemplateHelperFactory> helperFactories = new LinkedList<TemplateHelperFactory>();
    private TemplateService templates = null;

    public TemplatedResultConverter(ServiceLookup services, TemplateService templates) {
        super();
        this.services = services;
        this.templates = templates;
    }

    @Override
    public String getInputFormat() {
        return "native";
    }

    @Override
    public String getOutputFormat() {
        return "template";
    }

    @Override
    public Quality getQuality() {
        return Quality.BAD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {


        OXTemplate template = templates.loadTemplate(requestData.getParameter("template"), requestData.getParameter("template"), session, false);

        Map<String, Object> rootObject = new HashMap<String, Object>();

        rootObject.put("templates", templates.createHelper(rootObject, session, false));
        rootObject.put("data", result.getResultObject());

        Locale locale = session.getUser().getLocale();
        StringHelper strings = StringHelper.valueOf(locale);
        rootObject.put("strings", strings);
        rootObject.put("locale", locale);
        rootObject.put("dates", new Dates(locale));

        rootObject.put("JSON", new JSONHelper());

        rootObject.put("assets", new AssetHelper(requestData.constructURL(null, requestData.getPrefix() + "/templating", false, "action=provide&name=").toString()));

        Map<String, Object> helper = new HashMap<String, Object>();
        rootObject.put("helper", helper);

        if (template.getProperty("requires") != null) {
            String requires = template.getProperty("requires");
            for (String require: requires.split(",\\s+")) {
                for(TemplateHelperFactory factory: helperFactories) {
                    if (factory.getName().equalsIgnoreCase(require)) {
                        helper.put(require, factory.create(requestData, result, session, converter, rootObject));
                    }
                }
            }
        }


        rootObject.put("objects", new NativeBuilderFactory());
        rootObject.put("ox", new WhitelistedDispatcher(services.getService(Dispatcher.class), session, template.isTrusted()));
        if (template.isTrusted()) {
            rootObject.put("session", session.getSessionID());
        }


        rootObject.put("req", requestData);

        StringWriter writer = new StringWriter();

        template.process(rootObject, writer);

        String html = null;

        if (!template.isTrusted()) {
            html = services.getService(HtmlService.class).sanitize(writer.toString(), "templating", false, null, null);
        } else {
            html = writer.toString();
        }

        result.setResultObject(html, "template");


        if (template.isTrusted()) {
            result.setHeader("Content-Type", template.getProperty("contentType", "text/html"));
        } else {
            result.setHeader("Content-Type", "text/html");
        }
    }

    private static final class NativeBuilderFactory {
        public NativeBuilder build() {
            return new NativeBuilder();
        }
    }

    @Override
    public void added(ServiceReference<TemplateHelperFactory> ref, TemplateHelperFactory service) {
        helperFactories.add(service);
    }

    @Override
    public void removed(ServiceReference<TemplateHelperFactory> ref, TemplateHelperFactory service) {
        helperFactories.remove(service);
    }

}
