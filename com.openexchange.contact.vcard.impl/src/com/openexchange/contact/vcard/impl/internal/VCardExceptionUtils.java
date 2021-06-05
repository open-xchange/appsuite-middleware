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

package com.openexchange.contact.vcard.impl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import com.openexchange.exception.OXException;
import ezvcard.ValidationWarning;
import ezvcard.ValidationWarnings;
import ezvcard.io.ParseWarning;
import ezvcard.property.VCardProperty;

/**
 * {@link VCardExceptionUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class VCardExceptionUtils {

    public static List<OXException> getParserWarnings(List<ParseWarning> parserWarnings) {
        if (null != parserWarnings && 0 < parserWarnings.size()) {
            List<OXException> warnings = new ArrayList<OXException>();
            for (ParseWarning warning : parserWarnings) {
                warnings.add(VCardExceptionCodes.PARSER_ERROR.create(warning.toString()));
            }
            return warnings;
        }
        return Collections.emptyList();
    }

    public static List<OXException> getValidationWarnings(ValidationWarnings validationWarnings) {
        if (null != validationWarnings && false == validationWarnings.isEmpty()) {
            List<OXException> warnings = new ArrayList<OXException>();
            for (Entry<VCardProperty, List<ValidationWarning>> entry : validationWarnings) {
                VCardProperty property = entry.getKey();
                List<ValidationWarning> propViolations = entry.getValue();
                String propertyName = null != property ? property.getClass().getSimpleName() : "";
                if (null != propViolations && 0 < propViolations.size()) {
                    for (ValidationWarning propViolation : propViolations) {
                        warnings.add(VCardExceptionCodes.VALIDATION_FAILED.create(propertyName, propViolation.getMessage(), propViolation.getCode()));
                    }
                }
            }
            return warnings;
        }
        return Collections.emptyList();
    }

}