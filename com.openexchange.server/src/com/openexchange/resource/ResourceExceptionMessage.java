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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.resource;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ResourceExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class ResourceExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link ResourceExceptionMessage}.
     */
    private ResourceExceptionMessage() {
        super();
    }

    // No resource group found for identifier \"%1$d\".
    public final static String RESOURCEGROUP_NOT_FOUND_MSG_DISPLAY = "No resource group found for identifier \"%1$d\".";

    // Found resource groups with the same identifier %1$d.
    public final static String RESOURCEGROUP_CONFLICT_MSG_DISPLAY = "Found resource groups with the same identifier \"%1$d\".";

    // No resource found with identifier \"%1$d\".
    public final static String RESOURCE_NOT_FOUND_MSG_DISPLAY = "No resource found with identifier \"%1$d\".";

    // Found resource(s) with same identifier \"%1$s\".
    public final static String RESOURCE_CONFLICT_MSG_DISPLAY = "Found resource(s) with same identifier \"%1$s\".";

    // You do not have the appropriate permissions to modify resources.
    public final static String PERMISSION_MSG_DISPLAY = "You do not have the appropriate permissions to modify resources.";

    // The provided resource identifier \"%1$s\" contains invalid characters.
    public final static String INVALID_RESOURCE_IDENTIFIER_MSG_DISPLAY = "The provided resource identifier \"%1$s\" contains invalid characters.";

    // The provided E-Mail address \"%1$s\" for resource is invalid.
    public final static String INVALID_RESOURCE_MAIL_MSG_DISPLAY = "The provided E-Mail address \"%1$s\" for resource is invalid.";

    // There is already a resource with E-Mail address "%1$s". Please choose another one.
    public final static String RESOURCE_CONFLICT_MAIL_MSG_DISPLAY = "There is already a resource with E-Mail address \"%1$s\". Please choose another one.";

    // The selected resource \"%1$s\" has been changed in the meantime.
    public final static String CONCURRENT_MODIFICATION_MSG_DISPLAY = "The selected resource \"%1$s\" has been changed in the meantime.";

    // Missing mandatory field(s) in given resource.
    public static final String MANDATORY_FIELD_MSG_DISPLAY = "Missing mandatory field(s) in given resource.";

}
