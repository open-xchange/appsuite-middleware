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

package com.openexchange.configjump;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link ConfigJumpExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class ConfigJumpExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link ConfigJumpExceptionMessage}.
     */
    private ConfigJumpExceptionMessage() {
        super();
    }
    
    // Unknown problem: "%s".
    public final static String UNKNOWN_MSG = "Unknown problem: \"%s\".";

    // Too few (%d) login attributes.
    public final static String MISSING_ATTRIBUTES_MSG = "Too few (%d) login attributes.";

    // Problem while communicating with external authorization.
    public final static String COMMUNICATION_MSG = "Problem while communicating with external " + "authorization.";

    // Instantiating the class failed.
    public final static String INSTANTIATION_FAILED_MSG = "Instantiating the class failed.";

    // Class %1$s can not be found.
    public final static String CLASS_NOT_FOUND_MSG = "Class %1$s can not be found.";

    // Missing property %1$s in system.properties.
    public final static String MISSING_SETTING_MSG = "Missing property %1$s in system.properties.";

    // URL "%s" is malformed.
    public final static String MALFORMED_URL_MSG = "URL \"%s\" is malformed.";

    // Link is not implemented.
    public final static String NOT_IMPLEMENTED_MSG = "Extras link is not implemented.";

}
