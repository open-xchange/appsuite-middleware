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

package com.openexchange.subscribe.json;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.i18n.I18nService;

/**
 * Registry for all found {@link I18nService} instances.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class I18nServices {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(I18nServices.class);
    private static final I18nServices SINGLETON = new I18nServices();

    private final Map<Locale, I18nService> services = new ConcurrentHashMap<Locale, I18nService>();

    private I18nServices() {
        super();
    }

    public static I18nServices getInstance() {
        return SINGLETON;
    }

    public void addService(final I18nService service) {
        if (null != services.put(service.getLocale(), service)) {
            LOG.warn("Another i18n translation service discovered for {}", service.getLocale());
        }
    }

    public void removeService(final I18nService service) {
        if (null == services.remove(service.getLocale())) {
            LOG.warn("Unknown i18n translation service shut down for {}", service.getLocale());
        }
    }

    private static final Locale DEFAULT_LOCALE = Locale.US;

    public I18nService getService(final Locale locale) {
        final Locale loc = null == locale ? DEFAULT_LOCALE : locale;
        final I18nService retval = services.get(loc);
        if (null == retval && !"en".equalsIgnoreCase(loc.getLanguage())) {
            LOG.warn("No i18n service for locale {}.", loc);
        }
        return retval;
    }
}
