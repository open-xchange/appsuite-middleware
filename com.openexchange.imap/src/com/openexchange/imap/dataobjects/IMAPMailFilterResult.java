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

package com.openexchange.imap.dataobjects;

import com.openexchange.mail.dataobjects.MailFilterResult;
import com.sun.mail.imap.FilterResult;


/**
 * {@link IMAPMailFilterResult} - The mail filter result backed by an IMAP filter result.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class IMAPMailFilterResult extends MailFilterResult {

    private final FilterResult filterResult;

    /**
     * Initializes a new {@link IMAPMailFilterResult}.
     *
     * @param filterResult The IMAP filter result
     */
    public IMAPMailFilterResult(FilterResult filterResult) {
        super(filterResult.getUid() < 0 ? null : Long.toString(filterResult.getUid(), 10));
        this.filterResult = filterResult;
    }

    @Override
    public boolean isOK() {
        return filterResult.isOK();
    }

    @Override
    public boolean hasWarnings() {
        return filterResult.hasWarnings();
    }

    @Override
    public boolean hasErrors() {
        return filterResult.hasErrors();
    }

    @Override
    public String getErrors() {
        return filterResult.getErrors();
    }

    @Override
    public String getWarnings() {
        return filterResult.getWarnings();
    }

}
