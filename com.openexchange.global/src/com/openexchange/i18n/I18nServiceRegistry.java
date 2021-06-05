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

package com.openexchange.i18n;

import java.util.Collection;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.internal.NOOPI18nService;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link I18nServiceRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
@SingletonService
public interface I18nServiceRegistry {

    /**
     * Gets the currently available i18n services.
     *
     * @return The currently available i18n services
     * @throws OXException If i18n services cannot be returned
     */
    Collection<I18nService> getI18nServices() throws OXException;

    /**
     * Gets the i18n service, which fits at best the specified locale.
     *
     * @param locale The locale
     * @return The i18n service or the default {@link NOOPI18nService} if no such locale is currently available
     *         never <code>null</code>.
     */
    I18nService getI18nService(Locale locale);

    /**
     * Gets the i18n service, which fits at best the specified locale.
     * 
     * @param locale The locale
     * @param exactMatch Whether the {@link I18nService} must match the given locale perfectly (exact match).
     * @return The i18n service or the default {@link NOOPI18nService} if no such locale is currently available
     *         never <code>null</code>.
     */
    I18nService getI18nService(Locale locale, boolean exactMatch);

}
