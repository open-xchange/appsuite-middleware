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

package com.openexchange.sms.impl;

import java.util.Locale;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.openexchange.exception.OXException;
import com.openexchange.sms.PhoneNumberParserService;
import com.openexchange.sms.SMSExceptionCode;


/**
 * {@link PhoneNumberParserServiceImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class PhoneNumberParserServiceImpl implements PhoneNumberParserService {

    public PhoneNumberParserServiceImpl() {
        super();
    }

    @Override
    public String parsePhoneNumber(String phoneNumber) throws OXException {
        return parsePhoneNumber(phoneNumber, null);
    }

    @Override
    public String parsePhoneNumber(String phoneNumber, Locale locale) throws OXException {
        PhoneNumberUtil parser = PhoneNumberUtil.getInstance();
        PhoneNumber number;
        try {
            if (null != locale) {
                number = parser.parse(phoneNumber, locale.getCountry());
            } else {
                number = parser.parse(phoneNumber, null);
            }
            StringBuilder sb = new StringBuilder(15);
            sb.append(number.getCountryCode()).append(number.getNationalNumber());
            return sb.toString();
        } catch (NumberParseException e) {
            throw SMSExceptionCode.PARSING_ERROR.create(phoneNumber);
        }
    }

}
