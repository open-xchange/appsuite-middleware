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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.cache.dynamic.impl.OXObjectFactory;
import com.openexchange.cache.dynamic.impl.Refresher;
import com.openexchange.caching.Cache;
import com.openexchange.groupware.AbstractOXException;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
final class ContextReloader extends Refresher<ContextExtended> implements ContextExtended {

    /**
     * Cached delegate;
     */
    private ContextExtended delegate;

    /**
     * Default constructor.
     * @throws AbstractOXException if some problem occurs with refreshing.
     */
    public ContextReloader(final OXObjectFactory<ContextExtended> factory,
        final Cache cache) throws AbstractOXException {
        super(factory, cache);
        this.delegate = refresh();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        try {
            this.delegate = refresh();
        } catch (final AbstractOXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return delegate.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ContextReloader: " + delegate.toString();
    }

    /**
     * {@inheritDoc}
     */
    public void setUpdating(final boolean updating) {
        try {
            this.delegate = refresh();
        } catch (final AbstractOXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        delegate.setUpdating(updating);
    }

    /**
     * {@inheritDoc}
     */
    public int getContextId() {
        return delegate.getContextId();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getFileStorageAuth() {
        try {
            this.delegate = refresh();
        } catch (final AbstractOXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return delegate.getFileStorageAuth();
    }

    /**
     * {@inheritDoc}
     */
    public long getFileStorageQuota() {
        try {
            this.delegate = refresh();
        } catch (final AbstractOXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return delegate.getFileStorageQuota();
    }

    /**
     * {@inheritDoc}
     */
    public int getFilestoreId() {
        try {
            this.delegate = refresh();
        } catch (final AbstractOXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return delegate.getFilestoreId();
    }

    /**
     * {@inheritDoc}
     */
    public String getFilestoreName() {
        try {
            this.delegate = refresh();
        } catch (final AbstractOXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return delegate.getFilestoreName();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getLoginInfo() {
        try {
            this.delegate = refresh();
        } catch (final AbstractOXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return delegate.getLoginInfo();
    }

    /**
     * {@inheritDoc}
     */
    public int getMailadmin() {
        try {
            this.delegate = refresh();
        } catch (final AbstractOXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return delegate.getMailadmin();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnabled() {
        try {
            this.delegate = refresh();
        } catch (final AbstractOXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return delegate.isEnabled();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUpdating() {
        try {
            this.delegate = refresh();
        } catch (final AbstractOXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return delegate.isUpdating();
    }
}
