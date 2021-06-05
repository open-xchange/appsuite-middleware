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

package com.openexchange.sms;

import java.util.Locale;
import com.openexchange.exception.OXException;

/**
 * {@link PhoneNumberParserService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public interface PhoneNumberParserService {
    
    /**
     * Parse phone number for SMS service. Number must be in international format (e. g. +491234567890)
     * 
     * @param phoneNumber
     * @return
     * @throws OXException
     */
    String parsePhoneNumber(String phoneNumber) throws OXException;

    /**
     * Parse phone number for SMS service in notation identified by locale.
     * 
     * @param phoneNumber
     * @param locale
     * @return
     * @throws OXException
     */
    String parsePhoneNumber(String phoneNumber, Locale locale) throws OXException;

}
