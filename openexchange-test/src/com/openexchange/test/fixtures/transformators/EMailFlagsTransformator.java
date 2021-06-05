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

package com.openexchange.test.fixtures.transformators;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * Transforms textual representations of email flags to the corresponding integer values defined
 * by the MailMessage class, e.g. "FLAG_DRAFT" becomes 4.
 *
 * @author tfriedrich
 */
public class EMailFlagsTransformator implements Transformator {

    @Override
    public Object transform(final String value) throws OXException {
        if (null == value || 1 > value.length()) {
            return I(0);
        }
        int flags = 0;
        final String[] splitted = value.split(",");
        for (final String flag : splitted) {
            if (null != flag) {
                flags |= getFlag(flag.trim());
            }
        }
        return I(flags);
    }

    private int getFlag(final String flag) {
        if ("FLAG_ANSWERED".equalsIgnoreCase(flag) || "ANSWERED".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_ANSWERED;
        } else if ("FLAG_DELETED".equalsIgnoreCase(flag) || "DELETED".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_DELETED;
        } else if ("FLAG_DRAFT".equalsIgnoreCase(flag) || "DRAFT".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_DRAFT;
        } else if ("FLAG_FLAGGED".equalsIgnoreCase(flag) || "FLAGGED".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_FLAGGED;
        } else if ("FLAG_RECENT".equalsIgnoreCase(flag) || "RECENT".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_RECENT;
        } else if ("FLAG_SEEN".equalsIgnoreCase(flag) || "SEEN".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_SEEN;
        } else if ("FLAG_USER".equalsIgnoreCase(flag) || "USER".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_USER;
        } else if ("FLAG_SPAM".equalsIgnoreCase(flag) || "SPAM".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_SPAM;
        } else if ("FLAG_FORWARDED".equalsIgnoreCase(flag) || "FORWARDED".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_FORWARDED;
        } else if ("FLAG_READ_ACK".equalsIgnoreCase(flag) || "READ_ACK".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_READ_ACK;
        } else {
            return 0;
        }
    }
}
