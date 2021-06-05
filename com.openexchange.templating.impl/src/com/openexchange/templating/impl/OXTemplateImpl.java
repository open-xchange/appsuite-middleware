/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        } catch (freemarker.template.TemplateException e) {
            exceptionHandler.handleTemplateException(TemplateErrorMessage.UnderlyingException.create(e, e.getMessage()), writer);
        } catch (IOException e) {
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
