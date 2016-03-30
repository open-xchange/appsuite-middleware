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

package com.openexchange.mail.json;

import java.io.Closeable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailServletInterface;

/**
 * {@link MailActionConstants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MailActionConstants {

    /**
     * The property name for {@link MailServletInterface} instance.
     */
    public static final String PROPERTY_MAIL_IFACE = "com.openexchange.mail.json.mailInterface";

    /**
     * The property name for the collection of {@link Closeable} instances.
     */
    public static final String PROPERTY_CLOSEABLES = "com.openexchange.mail.json.closeables";

    /**
     * A set containing all log property names.
     */
    public static final Set<LogProperties.Name> ALL_LOG_PROPERTIES = Collections.unmodifiableSet(EnumSet.of(
        LogProperties.Name.MAIL_ACCOUNT_ID,
        LogProperties.Name.MAIL_FULL_NAME,
        LogProperties.Name.MAIL_HOST,
        LogProperties.Name.MAIL_MAIL_ID,
        LogProperties.Name.MAIL_LOGIN));

}
