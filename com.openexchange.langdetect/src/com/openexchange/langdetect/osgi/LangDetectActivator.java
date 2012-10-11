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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.langdetect.osgi;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.langdetect.LanguageDetectionExceptionCodes;
import com.openexchange.langdetect.LanguageDetectionService;
import com.openexchange.langdetect.internal.Lc4jLanguageDetectionService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link LangDetectActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LangDetectActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(LangDetectActivator.class));
        logger.info("Starting bundle: com.openexchange.langdetect");
        try {
            /*
             * Open tracker
             */
            track(ConfigurationService.class, new ServiceTrackerCustomizer<ConfigurationService, ConfigurationService>() {

                    @Override
                    public ConfigurationService addingService(final ServiceReference<ConfigurationService> reference) {
                        final ConfigurationService service = context.getService(reference);
                        final Lc4jLanguageDetectionService languageDetectionService = Lc4jLanguageDetectionService.getInstance();
                        /*
                         * Load language codes
                         */
                        try {
                            final String propName = "com.openexchange.langdetect.languageCodesFile";
                            final String languageCodesFile = service.getProperty(propName);
                            if (null == languageCodesFile) {
                                throw LanguageDetectionExceptionCodes.UNEXPECTED_ERROR.create("Missing property: " + propName);
                            }
                            languageDetectionService.loadLanguageCodes(service.getFileByName("language-codes.properties"));
                        } catch (final OXException e) {
                            logger.error(e.getMessage(), e);
                            return null;
                        }
                        /*
                         * Set language model directory
                         */
                        try {
                            final String propName = "com.openexchange.langdetect.languageModelsDir";
                            final String languageModelsDir = service.getProperty(propName);
                            if (null == languageModelsDir) {
                                throw LanguageDetectionExceptionCodes.UNEXPECTED_ERROR.create("Missing property: " + propName);
                            }
                            languageDetectionService.setLanguageModelsDir(service.getDirectory(languageModelsDir));
                        } catch (final OXException e) {
                            logger.error(e.getMessage(), e);
                            return null;
                        }
                        registerService(LanguageDetectionService.class, languageDetectionService, null);
                        return service;
                    }

                    @Override
                    public void modifiedService(final ServiceReference<ConfigurationService> reference, final ConfigurationService service) {
                        // Nope
                    }

                    @Override
                    public void removedService(final ServiceReference<ConfigurationService> reference, final ConfigurationService service) {
                        context.ungetService(reference);
                    }
                });
            openTrackers();
        } catch (final Exception e) {
            cleanUp();
            logger.error("Starting bundle failed: com.openexchange.langdetect", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(LangDetectActivator.class));
        logger.info("Stopping bundle: com.openexchange.langdetect");
        try {
            unregisterServices();
            closeTrackers();
            cleanUp();
        } catch (final Exception e) {
            logger.error("Stopping bundle failed: com.openexchange.langdetect", e);
            throw e;
        }
    }

}
