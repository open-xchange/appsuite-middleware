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

package com.openexchange.groupware.importexport;

import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * Result object for mail imports.
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 *
 */
public class MailImportResult {

    public static String ERROR = "Error";
    public static String FILENAME = "Filename";

    private String id;
    private MailMessage mail;
    private boolean hasError;
    private OXException exception;

    public MailImportResult() {
        super();
        id = null;
        mail = null;
        hasError = false;
        exception = null;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public MailMessage getMail() {
        return mail;
    }


    public void setMail(final MailMessage mail) {
        this.mail = mail;
    }

    /**
     *
     * @return true if contains Exception.
     */
    public boolean hasError() {
        return hasError;
    }

    public OXException getException() {
        return exception;
    }

    public void setException(final OXException exception) {
        hasError = true;
        this.exception = exception;
    }

}
