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

package com.openexchange.pgp.core.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link PGPCoreExceptionMessages}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v8.0.0
 */
public final class PGPCoreExceptionMessages implements LocalizableStrings {

    // No encrypted data was found for decryption
    public static final String NO_PGP_DATA_FOUND = "No encrypted items found.";

    // The right private key wasn't found to perform the action.  Key identity is listed
    public static final String PRIVATE_KEY_NOT_FOUND = "The private key for the identity '%1$s' could not be found.";

    // Bad password used
    public static final String BAD_PASSWORD = "Bad password.";

    // No items that contain a signature were found
    public static final String NO_PGP_SIGNATURE_FOUND = "No signature items found";

    // Generic IO error with error specified
    public static final String IO_EXCEPTION = "An I/O error occurred: '%1$s'";

    // Generic PGP error with error specified
    public static final String PGP_EXCEPTION = "A PGP error occurred: '%1$s'";

    private PGPCoreExceptionMessages() {}

}
