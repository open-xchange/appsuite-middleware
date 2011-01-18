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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.groupware.contexts.impl;

import java.util.Map;
import java.util.Set;
import com.openexchange.cache.dynamic.impl.OXObjectFactory;
import com.openexchange.cache.dynamic.impl.Refresher;
import com.openexchange.groupware.AbstractOXException;

/**
 * Context object delegator that reloads the backed delegate newly into cache if it times out.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
final class ContextReloader extends Refresher<ContextExtended> implements ContextExtended {

    private static final long serialVersionUID = -2022359916415524347L;

    private ContextExtended delegate;

    /**
     * Default constructor.
     * @throws AbstractOXException if some problem occurs with refreshing.
     */
    public ContextReloader(OXObjectFactory<ContextExtended> factory, String regionName) throws AbstractOXException {
        super(factory, regionName);
        this.delegate = refresh();
    }

    @Override
    public boolean equals(final Object obj) {
        updateDelegate();
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        updateDelegate();
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return "ContextReloader: " + delegate.toString();
    }

    public void setUpdating(final boolean updating) {
        updateDelegate();
        delegate.setUpdating(updating);
    }

    public void setReadOnly(boolean readOnly) {
        updateDelegate();
        delegate.setReadOnly(readOnly);
    }

    public int getContextId() {
        return delegate.getContextId();
    }

    public String getName() {
        updateDelegate();
        return delegate.getName();
    }

    public String[] getFileStorageAuth() {
        updateDelegate();
        return delegate.getFileStorageAuth();
    }

    public long getFileStorageQuota() {
        updateDelegate();
        return delegate.getFileStorageQuota();
    }

    public int getFilestoreId() {
        updateDelegate();
        return delegate.getFilestoreId();
    }

    public String getFilestoreName() {
        updateDelegate();
        return delegate.getFilestoreName();
    }

    public String[] getLoginInfo() {
        updateDelegate();
        return delegate.getLoginInfo();
    }

    public int getMailadmin() {
        updateDelegate();
        return delegate.getMailadmin();
    }

    public boolean isEnabled() {
        updateDelegate();
        return delegate.isEnabled();
    }

    public boolean isUpdating() {
        updateDelegate();
        return delegate.isUpdating();
    }

    public boolean isReadOnly() {
        updateDelegate();
        return delegate.isReadOnly();
    }

    /**
     * @throws RuntimeException if refreshing fails.
     */
    private void updateDelegate() throws RuntimeException {
        try {
            this.delegate = refresh();
        } catch (final AbstractOXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Map<String, Set<String>> getAttributes() {
        updateDelegate();
        return delegate.getAttributes();
    }
}
