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

package com.openexchange.groupware.contexts;

import java.io.Serializable;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ContextImpl implements ContextExtended, Serializable {

    /**
     * Serialization.
     */
    private static final long serialVersionUID = 8570995404471786200L;

    /**
     * Unique identifier of the context.
     */
    private final int contextId;

    /**
     * The login informations of a context.
     */
    private String[] loginInfo;

    /**
     * Unique identifier of the contexts mailadmin.
     */
    private int mailadmin = -1;

    /**
     * Identifier of the file store.
     */
    private int filestoreId = -1;

    /**
     * Name where to place the file storage inside the file store.
     */
    private String filestoreName;

    /**
     * Authentication of the file storage.
     */
    private String[] filestorageAuth;

    /**
     * Quota of the file storage.
     */
    private long fileStorageQuota;

    /**
     * Is the context enabled.
     */
    private boolean enabled = true;

    /**
     * Default constructor.
     * @param contextId Unique identifier.
     */
    public ContextImpl(final int contextId) {
        this.contextId = contextId;
    }

    /**
     * {@inheritDoc}
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ContextImpl)) {
            return false;
        }
        return contextId == ((ContextImpl) obj).contextId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return contextId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ContextImpl cid: " + contextId;
    }

    /**
     * {@inheritDoc}
     */
    public int getMailadmin() {
        return mailadmin;
    }

    /**
     * {@inheritDoc}
     */
    public long getFileStorageQuota() {
        return fileStorageQuota;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param mailadmin the mailadmin to set
     */
    public void setMailadmin(final int mailadmin) {
        this.mailadmin = mailadmin;
    }

    /**
     * @param fileStorageQuota the fileStorageQuota to set
     */
    public void setFileStorageQuota(final long fileStorageQuota) {
        this.fileStorageQuota = fileStorageQuota;
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the filestoreId
     */
    public int getFilestoreId() {
        return filestoreId;
    }

    /**
     * @param filestoreId the filestoreId to set
     */
    public void setFilestoreId(final int filestoreId) {
        this.filestoreId = filestoreId;
    }

    /**
     * @return the filestoreName
     */
    public String getFilestoreName() {
        return filestoreName;
    }

    /**
     * @param filestoreName the filestoreName to set
     */
    public void setFilestoreName(final String filestoreName) {
        this.filestoreName = filestoreName;
    }

    /**
     * @param filestoreAuth the filestoreAuth to set
     */
    public void setFilestoreAuth(final String[] filestoreAuth) {
        this.filestorageAuth = filestoreAuth;
    }

    /**
     * @return the filestoreAuth
     */
    public String[] getFileStorageAuth() {
        return filestorageAuth.clone();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getLoginInfo() {
        return loginInfo.clone();
    }

    /**
     * @param loginInfo the loginInfo to set
     */
    protected void setLoginInfo(final String[] loginInfo) {
        this.loginInfo = loginInfo.clone();
    }
}
