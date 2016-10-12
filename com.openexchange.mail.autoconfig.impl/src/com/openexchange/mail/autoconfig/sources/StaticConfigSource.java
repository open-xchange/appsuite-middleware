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

package com.openexchange.mail.autoconfig.sources;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;
import com.openexchange.mail.autoconfig.Autoconfig;

/**
 * {@link StaticConfigSource} - An abstract class for static auto-config sources.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.0
 */
public abstract class StaticConfigSource implements ConfigSource {

    /**
     * Checks if a given domain is supported.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     * @since 7.4.0
     */
    public static interface DomainFilter {

        /**
         * Checks if specified domain is supported; e.g. <code>"yahoo.com"</code>.
         *
         * @param emailDomain The domain to check
         * @return <code>true</code> if accepted; otherwise <code>false</code>
         */
        public boolean accept(String emailDomain);
    }

    // ---------------------------------------------------------------- //

    private final DomainFilter filter;

    /**
     * Initializes a new {@link StaticConfigSource}.
     *
     * @param filter The domain filter
     * @throws NullPointerException If <code>filter</code> is <code>null</code>
     */
    protected StaticConfigSource(final DomainFilter filter) {
        super();
        if (null == filter) {
            throw new NullPointerException("filter rmust not be null.");
        }
        this.filter = filter;
    }

    @Override
    public final Autoconfig getAutoconfig(final String emailLocalPart, final String emailDomain, final String password, final User user, final Context context) throws OXException {
        return getAutoconfig(emailLocalPart, emailDomain, password, user, context, true);
    }

    @Override
    public final DefaultAutoconfig getAutoconfig(final String emailLocalPart, final String emailDomain, final String password, final User user, final Context context, boolean forceSecure) throws OXException {
        if (null != filter && filter.accept(emailDomain)) {
            return getStaticAutoconfig(emailLocalPart, emailDomain, password, user, context, forceSecure);
        }
        return null;
    }

    /**
     * Gets the static auto-config.
     *
     * @param emailLocalPart The local part
     * @param emailDomain The domain part
     * @param password Tha password
     * @param user The associated user
     * @param context The context
     * @param forceSecure <code>true</code> if a secure connection should be enforced; otherwise <code>false</code> to also allow plain ones
     * @return The auto-config or <code>null</code>
     * @throws OXException If an error occurs
     */
    protected abstract DefaultAutoconfig getStaticAutoconfig(String emailLocalPart, String emailDomain, String password, User user, Context context, boolean forceSecure) throws OXException;

}
