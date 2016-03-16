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

package com.openexchange.i18n.impl;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nTranslator;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;


/**
 * If strings shall be translated, you need to use an appropriate {@link I18nService},
 * according to the desired locale. The {@link I18nTranslatorFactory} helps you to
 * keep track of all {@link I18nService} instances and creates new {@link Translator}
 * instances based on given locales.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class I18nTranslatorFactory extends ServiceTracker<I18nService, I18nService> implements TranslatorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(I18nTranslatorFactory.class);

    private final ConcurrentMap<Locale, I18nService> services = new ConcurrentHashMap<Locale, I18nService>();


    /**
     * Initializes a new {@link I18nTranslatorFactory}.
     *
     * @param context The bundle context
     */
    public I18nTranslatorFactory(BundleContext context) {
        super(context, I18nService.class, null);
    }

    @Override
    public Translator translatorFor(Locale locale) {
        if (locale == null) {
            return I18nTranslator.EMPTY;
        }

        I18nService service = services.get(locale);
        if (service == null) {
            return I18nTranslator.EMPTY;
        }

        return new I18nTranslator(service);
    }

    @Override
    public I18nService addingService(ServiceReference<I18nService> reference) {
        I18nService service = super.addingService(reference);
        if (service != null) {
            I18nService existing = services.putIfAbsent(service.getLocale(), service);
            if (existing != null) {
                LOG.warn("Ignoring duplicate I18nService for locale {}.", service.getLocale());
                context.ungetService(reference);
                return null;
            }
        }
        return service;
    }

    @Override
    public void removedService(ServiceReference<I18nService> reference, I18nService service) {
        if (services.remove(service.getLocale(), service)) {
            super.removedService(reference, service);
        }
    }

}
