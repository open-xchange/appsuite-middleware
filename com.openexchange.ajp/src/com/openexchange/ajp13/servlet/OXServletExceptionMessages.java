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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajp13.servlet;


/**
 * OXServletExceptionMessages
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXServletExceptionMessages {

    private OXServletExceptionMessages() {
        super();
    }

    // Missing property %s in 'system.properties'
    public static final String MISSING_SERVLET_DIR_MSG = "Missing property %s in 'system.properties'";

    // Servlet mapping directory does not exist: %s
    public static final String DIR_NOT_EXISTS_MSG = "Servlet mapping directory does not exist: %s";

    // File is not a directory: %s
    public static final String NO_DIRECTORY_MSG = "File is not a directory: %s";

    // Servlet mappings could not be loaded due to following error: %s
    public static final String SERVLET_MAPPINGS_NOT_LOADED_MSG = "Servlet mappings could not be loaded due to following error: %s";

    // No servlet class name found for key "%s". Please check servlet mappings.
    public static final String NO_CLASS_NAME_FOUND_MSG = "No servlet class name found for key \"%s\". Please check servlet mappings.";

    // Name "%s" already mapped to "%s". Ignoring servlet class "%s"
    public static final String ALREADY_PRESENT_MSG = "Name \"%s\" already mapped to \"%s\". Ignoring servlet class \"%s\"";

    // SecurityException while loading servlet class "%s"
    public static final String SECURITY_ERR_MSG = "SecurityException while loading servlet class \"%s\"";

    // Couldn't find servlet class "%s"
    public static final String CLASS_NOT_FOUND_MSG = "Couldn't find servlet class \"%s\"";

    // No default constructor specified in servlet class "%s"
    public static final String NO_DEFAULT_CONSTRUCTOR_MSG = "No default constructor specified in servlet class \"%s\"";

}
