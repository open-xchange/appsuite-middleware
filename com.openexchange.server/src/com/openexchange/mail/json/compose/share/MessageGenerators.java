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

package com.openexchange.mail.json.compose.share;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.templating.TemplateService;

/**
 * {@link MessageGenerators} - Utility class for {@link com.openexchange.mail.json.compose.share.spi.MessageGenerator message generators}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class MessageGenerators {

    /**
     * Initializes a new {@link MessageGenerators}.
     */
    private MessageGenerators() {
        super();
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private static final AtomicReference<TranslatorFactory> TRANSLATOR_FACTORY_REF = new AtomicReference<TranslatorFactory>(null);

    /**
     * Sets the translator factory.
     *
     * @param factory The factory to set or <code>null</code> to clear
     */
    public static void setTranslatorFactory(TranslatorFactory factory) {
        TRANSLATOR_FACTORY_REF.set(factory);
    }

    /**
     * Gets the translator factory.
     *
     * @return The translator factory or <code>null</code>
     */
    public static TranslatorFactory getTranslatorFactory() {
        return TRANSLATOR_FACTORY_REF.get();
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private static final AtomicReference<TemplateService> TEMPLATE_SERVICE_REF = new AtomicReference<TemplateService>(null);

    /**
     * Sets the template service.
     *
     * @param templateService The template service to set or <code>null</code> to clear
     */
    public static void setTemplateService(TemplateService templateService) {
        TEMPLATE_SERVICE_REF.set(templateService);
    }

    /**
     * Gets the template service.
     *
     * @return The template service or <code>null</code>
     */
    public static TemplateService getTemplateService() {
        return TEMPLATE_SERVICE_REF.get();
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private static final AtomicReference<RegionalSettingsService> REGIONAL_SERVICE_REF = new AtomicReference<RegionalSettingsService>(null);

    /**
     * Sets the regional settings service.
     *
     * @param regionalSettingsService The regional settings service to set or <code>null</code> to clear
     */
    public static void setRegionalSettingsService(RegionalSettingsService regionalSettingsService) {
        REGIONAL_SERVICE_REF.set(regionalSettingsService);
    }

    /**
     * Gets the regional settings service.
     *
     * @return The regional settings service or <code>null</code>
     */
    public static RegionalSettingsService getRegionalSettingsService() {
        return REGIONAL_SERVICE_REF.get();
    }

}
