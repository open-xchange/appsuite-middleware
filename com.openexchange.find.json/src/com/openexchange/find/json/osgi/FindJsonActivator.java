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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.find.json.osgi;

import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.find.SearchService;
import com.openexchange.find.json.FindActionFactory;
import com.openexchange.find.json.converters.AutocompleteResultJSONConverter;
import com.openexchange.find.json.converters.ConfigJSONConverter;
import com.openexchange.find.json.converters.SearchResultJSONConverter;
import com.openexchange.i18n.I18nService;

/**
 *
 * {@link FindJsonActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class FindJsonActivator extends AJAXModuleActivator {

    private final String MODULE_PATH = "find";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SearchService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        I18nTracker translator = new I18nTracker(context);
        track(I18nService.class, translator);

        final ResultConverterRegistry converterRegistry = new ResultConverterRegistry(context);
        track(ResultConverter.class, converterRegistry);

        openTrackers();

        registerService(ResultConverter.class, new ConfigJSONConverter(translator));
        registerService(ResultConverter.class, new AutocompleteResultJSONConverter(translator));
        registerService(ResultConverter.class, new SearchResultJSONConverter(translator, converterRegistry));
        registerModule(new FindActionFactory(this), MODULE_PATH);
    }

}
