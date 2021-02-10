/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.file.storage;

import java.util.Collection;
import java.util.Collections;
import com.openexchange.exception.OXException;

/**
 * 
 * {@link FileStorageResult}
 * Makes it possible to return a list of warnings in addition to any response.
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.5
 * @param <R> The type of the response.
 */
public class FileStorageResult<R> {

    /**
     * 
     * Creates a new {@link FileStorageResult} object.
     *
     * @param <R> The type of the response.
     * @param response The response.
     * @param warnings The warnings.
     * @return The {@link FileStorageResult} object.
     */
    public static <R> FileStorageResult<R> newFileStorageResult(final R response, final Collection<OXException> warnings) {
        return new FileStorageResult<R>(response, warnings);
    }

    private final R response;

    private final Collection<OXException> warnings;

    /**
     * 
     * Initializes a new {@link FileStorageResult}.
     * 
     * @param response The response.
     * @param warnings The warnings.
     */
    private FileStorageResult(final R response, final Collection<OXException> warnings) {
        this.response = response;
        this.warnings = null == warnings ? Collections.emptySet() : warnings;
    }

    /**
     * 
     * Gets the response.
     *
     * @return The response.
     */
    public R getResponse() {
        return response;
    }

    /**
     * 
     * Gets the warnings.
     *
     * @return The warnings.
     */
    public Collection<OXException> getWarnings() {
        return warnings;
    }

}