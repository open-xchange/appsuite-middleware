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

package com.openexchange.mail.autoconfig;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link AutoconfigException}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AutoconfigException extends OXException implements LocalizableStrings {

    private static final long serialVersionUID = 4909345337720686173L;

    private static final String PREFIX = "MAIL-AUTOCONFIG";

    public static final String INVALID_MAIL = "The E-Mail address %1$s is invalid.";

    public static OXException unexpected(String logMessage) {
        return new OXException(1).setPrefix(PREFIX).setLogMessage(logMessage);
    }

    public static OXException unexpected(String logMessage, Throwable cause) {
        return new OXException(1, OXExceptionStrings.DEFAULT_MESSAGE, cause, new Object[0]).setPrefix(PREFIX).setLogMessage(logMessage);
    }

    public static OXException invalidMail(String mail) {
        return new OXException(2, INVALID_MAIL, mail).setPrefix(PREFIX);
    }

    public static OXException xml(XmlPullParserException e) {
        return new OXException(3, null, e).setPrefix(PREFIX);
    }

    public static OXException io(IOException e) {
        return new OXException(4, null, e).setPrefix(PREFIX);
    }

}
