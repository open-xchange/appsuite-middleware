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

package com.openexchange.publish.json;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link PublicationJSONExceptionMessage}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class PublicationJSONExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link PublicationJSONExceptionMessage}.
     */
    private PublicationJSONExceptionMessage() {
        super();
    }

    public final static String THROWABLE_MSG = "An unexpected error occurred: %s";

    public final static String MISSING_PARAMETER_MSG = "Missing value for parameter %s";

    public final static String UNKNOWN_ACTION_MSG = "Unknown action: %s";

    public final static String UNKOWN_ENTITY_MODULE_MSG = "Unknown entity module: %s";

    public final static String UNKNOWN_COLUMN_MSG = "Unknown column: %s";

    public final static String UNKNOWN_TARGET_MSG = "Unknown target: %s";

    public final static String THROWABLE_HELP = "Please try again later.";

    public final static String MISSING_PARAMETER_HELP = "Please correct the client program";

    public final static String UNKNOWN_ACTION_HELP = "Please correct the client program";

    public final static String UNKOWN_ENTITY_MODULE_HELP = "Please use only entity modules known to the server";

    public final static String UNKNOWN_COLUMN_HELP = "Please ask only for columns known to the server";

    public final static String UNKNOWN_TARGET_HELP = "Please check the spelling of the id, or load a list of known targets";

}
