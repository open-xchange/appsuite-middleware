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

package com.openexchange.templating.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.OXTemplateExceptionHandler;
import com.openexchange.templating.TemplateErrorMessage;
import com.openexchange.tools.strings.StringParser;
import freemarker.template.Template;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class OXTemplateImpl implements OXTemplate{

    public static ServiceLookup services = null;

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXTemplateImpl.class);

	private final Map<String, String> properties = new HashMap<String, String>();
	private final OXTemplateExceptionHandler exceptionHandler;
    private Template template;
    private TemplateLevel level = TemplateLevel.USER;
    private boolean trusted;


    /**
     * Initializes a new {@link OXTemplateImpl}.
     */
    public OXTemplateImpl() {
        this(OXTemplateExceptionHandler.RETHROW_HANDLER);
    }

    public OXTemplateImpl(final OXTemplateExceptionHandler exceptionHandler) {
        super();
        if (exceptionHandler == null) {
            throw new IllegalStateException("ExceptionHandler must be set!");
        }
        this.exceptionHandler = exceptionHandler;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(final Template template) {
        this.template = template;
    }

    @Override
    public void process(final Object rootObject, final Writer writer) throws OXException {
        try {
            template.process(rootObject, writer);
        } catch (final freemarker.template.TemplateException e) {
            exceptionHandler.handleTemplateException(TemplateErrorMessage.UnderlyingException.create(e, e.getMessage()), writer);
        } catch (final IOException e) {
            final OXException x = TemplateErrorMessage.IOException.create(e);
            LOG.error("", x);
            throw x;
        }
    }

    @Override
    public TemplateLevel getLevel() {
        return level;
    }

    public void setLevel(final TemplateLevel level) {
        this.level = level;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    @Override
    public boolean isTrusted() {
        return trusted;
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public <T> T getProperty(String name, Class<T> klass) {
        return getProperty(name, klass, null);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        String string = properties.get(name);
        if (string == null) {
            return defaultValue;
        }
        return string;
    }

    @Override
    public <T> T getProperty(String name, Class<T> klass, T defaultValue) {
        String string = properties.get(name);
        if (string == null) {
            return defaultValue;
        }
        return services.getService(StringParser.class).parse(string, klass);
    }

    public void setProperties(Properties props) {
        properties.clear();
        for(Object key: props.keySet()) {
            properties.put(key.toString(), props.getProperty(key.toString()));
        }
    }

}
