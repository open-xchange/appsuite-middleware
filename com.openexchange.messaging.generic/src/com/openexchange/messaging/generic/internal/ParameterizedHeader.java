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

package com.openexchange.messaging.generic.internal;

import java.io.Serializable;
import java.util.Iterator;

/**
 * {@link ParameterizedHeader} - Super class for headers which can hold a parameter list such as <code>Content-Type</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public abstract class ParameterizedHeader implements Serializable, Comparable<ParameterizedHeader>, com.openexchange.messaging.ParameterizedMessagingHeader {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -1094716342843794294L;

    /**
     * The delegatee.
     */
    protected final com.openexchange.mail.mime.ParameterizedHeader delegate;

    /**
     * Initializes a new {@link ParameterizedHeader}
     */
    protected ParameterizedHeader(final com.openexchange.mail.mime.ParameterizedHeader delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public int compareTo(final ParameterizedHeader other) {
        if (this == other) {
            return 0;
        }
        return delegate.compareTo(other.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ParameterizedHeader)) {
            return false;
        }
        final ParameterizedHeader other = (ParameterizedHeader) obj;
        if (delegate == null) {
            if (other.delegate != null) {
                return false;
            }
        } else if (!delegate.equals(other.delegate)) {
            return false;
        }
        return true;
    }

    @Override
    public HeaderType getHeaderType() {
        return HeaderType.PARAMETERIZED;
    }

    /**
     * Adds specified value to given parameter name. If existing, the parameter is treated as a contiguous parameter according to RFC2231.
     *
     * @param key The parameter name
     * @param value The parameter value to add
     */
    @Override
    public final void addParameter(final String key, final String value) {
        delegate.addParameter(key, value);
    }

    /**
     * Sets the given parameter. Existing value is overwritten.
     *
     * @param key The parameter name
     * @param value The parameter value
     */
    @Override
    public final void setParameter(final String key, final String value) {
        delegate.setParameter(key, value);
    }

    /**
     * Gets specified parameter's value
     *
     * @param key The parameter name
     * @return The parameter's value or <code>null</code> if not existing
     */
    @Override
    public final String getParameter(final String key) {
        return delegate.getParameter(key);
    }

    /**
     * Removes specified parameter and returns its value
     *
     * @param key The parameter name
     * @return The parameter's value or <code>null</code> if not existing
     */
    @Override
    public final String removeParameter(final String key) {
        return delegate.removeParameter(key);
    }

    /**
     * Checks if parameter is present
     *
     * @param key the parameter name
     * @return <code>true</code> if parameter is present; otherwise <code>false</code>
     */
    @Override
    public final boolean containsParameter(final String key) {
        return delegate.containsParameter(key);
    }

    /**
     * Gets all parameter names wrapped in an {@link Iterator}
     *
     * @return All parameter names wrapped in an {@link Iterator}
     */
    @Override
    public final Iterator<String> getParameterNames() {
        return delegate.getParameterNames();
    }

}
