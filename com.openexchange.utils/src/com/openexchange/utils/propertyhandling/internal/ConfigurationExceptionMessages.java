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

package com.openexchange.utils.propertyhandling.internal;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link ConfigurationExceptionMessages}
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public class ConfigurationExceptionMessages implements LocalizableStrings {

    // The value given in the property %1$s is no integer value
    public static final String NO_INTEGER_VALUE_MSG = "The value given in the property %1$s is no integer value.";

    // Property %1$s not set but required.
    public static final String REQUIRED_PROPERTIY_NOT_SET_MSG = "Property %1$s not set but required.";

    // Property %1$s claims to have condition but condition not set.
    public static final String CONDITION_NOT_SET_MSG = "Property %1$s claims to have condition but condition not set.";

    // Property %1$s must be set if %2$s is set to %3$s
    public static final String MUST_BE_SET_TO_MSG = "Property %1$s must be set if %2$s is set to %3$s";

    // The class %1$s cannot be used as a property type at the moment
    public static final String UNKNOWN_TYPE_CLASS_MSG = "The %1$s cannot be used as a property type in the property %2$s";
}
