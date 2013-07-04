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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.publish;

import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocalizableStrings;

/**
 * Messages to translate for {@link OXException}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class PublicationExceptionMessages implements LocalizableStrings {

    public static final String SQL_EXCEPTION_MSG = "A SQL error occurred.";

    public static final String PARSE_EXCEPTION_MSG = "A parsing error occurred: %1$s.";

    public static final String NO_LOADER_FOUND_MSG = "Could not load publications of type %1$s";

    public static final String ID_GIVEN_MSG = "Unable to save a given ID.";

    public static final String PUBLICATION_NOT_FOUND_MSG = "Cannot find the publication site.";

    public static final String UNIQUENESS_CONSTRAINT_VIOLATION = "%1$s has already been taken (field: %2$s)";

    public static final String ACCESS_DENIED_MSG = "You do not have the permissions to perform the chosen action (%s)";

    public static final String NOT_EXIST_MSG = "The published document has been deleted in the meantime and therefore is no longer available.";

    private PublicationExceptionMessages() {
        super();
    }
}
