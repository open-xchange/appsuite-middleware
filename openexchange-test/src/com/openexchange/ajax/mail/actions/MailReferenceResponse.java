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

package com.openexchange.ajax.mail.actions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * {@link MailReferenceResponse}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MailReferenceResponse extends AbstractAJAXResponse {

    private static final Pattern MAIL_REF_PATTERN = Pattern.compile("^default([0-9]+)(.).*");

    private String separator;

    private int accountID;

    private String folder;

    private String mailID;

    private String reference;

    /**
     * Initializes a new {@link MailReferenceResponse}.
     *
     * @param response
     */
    protected MailReferenceResponse(Response response) {
        super(response);

        reference = (String) response.getData();
        Matcher matcher = MAIL_REF_PATTERN.matcher(reference);
        if (matcher.matches()) {
            accountID = Integer.parseInt(matcher.group(1));
            separator = matcher.group(2);
            int firstSeparatorIndex = reference.indexOf(separator);
            int lastSeparatorIndex = reference.lastIndexOf(separator);
            String folderName = reference.substring(firstSeparatorIndex + 1, lastSeparatorIndex);
            mailID = reference.substring(lastSeparatorIndex + 1);
            folder = "default" + accountID + separator + folderName;
        }
    }

    public String getSeparator() {
        return separator;
    }

    public int getAccountID() {
        return accountID;
    }

    public String getFolder() {
        return folder;
    }

    public String getMailID() {
        return mailID;
    }

    public String getMailReference() {
        return reference;
    }

}
