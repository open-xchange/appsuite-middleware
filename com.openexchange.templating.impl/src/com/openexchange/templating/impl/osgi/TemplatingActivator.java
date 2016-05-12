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

package com.openexchange.templating.impl.osgi;

import java.lang.reflect.Field;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.html.HtmlService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.templating.TemplateService;
import com.openexchange.templating.impl.OXIntegration;
import com.openexchange.templating.impl.OXTemplateImpl;
import com.openexchange.templating.impl.TemplateServiceImpl;
import com.openexchange.tools.strings.StringParser;
import freemarker.log.OXFreemarkerLoggerFactory;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class TemplatingActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TemplatingActivator.class);

    @Override
    public void startBundle() throws Exception {
        // Set freemarker's logger
        {
            final Field extendedProviderField = freemarker.log.Logger.class.getDeclaredField("factory");
            extendedProviderField.setAccessible(true);
            extendedProviderField.set(null, new OXFreemarkerLoggerFactory());
        }

        ConfigurationService config = getService(ConfigurationService.class);
        final OXIntegration integration = new OXIntegration(getService(InfostoreFacade.class));
        final TemplateServiceImpl templates = new TemplateServiceImpl(config);
        templates.setOXFolderHelper(integration);
        templates.setInfostoreHelper(integration);
        registerService(TemplateService.class, templates);

        OXTemplateImpl.services = this;

        final boolean hasProperty = config.getProperty(TemplateServiceImpl.PATH_PROPERTY) != null;
        if (!hasProperty) {
            final IllegalStateException exception = new IllegalStateException("Missing Property " + TemplateServiceImpl.PATH_PROPERTY);
            exception.fillInStackTrace();

            LOG.error(TemplateServiceImpl.PATH_PROPERTY + " is not set. Templating will remain inactive.", exception);
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, InfostoreFacade.class, StringParser.class, HtmlService.class };
    }

}
