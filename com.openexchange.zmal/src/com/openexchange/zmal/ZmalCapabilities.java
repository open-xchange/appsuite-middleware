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

package com.openexchange.zmal;

import com.openexchange.mail.api.MailCapabilities;

/**
 * {@link ZmalCapabilities} - The capabilities of underlying Zimbra mail server with {@link #hasTimeStamps()} hard-coded to return
 * <code>false</code>.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ZmalCapabilities extends MailCapabilities {

    /**
     * ACL
     */
    public static final String CAP_ACL = "ACL";

    /**
     * THREAD=REFERENCES
     */
    public static final String CAP_THREAD_REFERENCES = "THREAD=REFERENCES";

    /**
     * THREAD=ORDEREDSUBJECT
     */
    public static final String CAP_THREAD_ORDEREDSUBJECT = "THREAD=ORDEREDSUBJECT";

    /**
     * QUOTA
     */
    public static final String CAP_QUOTA = "QUOTA";

    /**
     * IMAP4
     */
    public static final String CAP_IMAP4 = "IMAP4";

    /**
     * IMAP4rev1
     */
    public static final String CAP_IMAP4_REV1 = "IMAP4REV1";

    /**
     * UIDPLUS
     */
    public static final String CAP_UIDPLUS = "UIDPLUS";

    /**
     * SORT
     */
    public static final String CAP_SORT = "SORT";

    /**
     * NAMESPACE
     */
    public static final String CAP_NAMESPACE = "NAMESPACE";

    /**
     * IDLE
     */
    public static final String CAP_IDLE = "IDLE";

    /**
     * CHILDREN
     */
    public static final String CAP_CHILDREN = "CHILDREN";

    /*-
     * Members
     */

    private boolean hasACL;

    private boolean hasQuota;

    private boolean hasThreadReferences;

    private boolean hasSort;

    private boolean hasSubscription;

    /**
     * Initializes a new {@link ZmalCapabilities}
     */
    public ZmalCapabilities() {
        super();
    }

    @Override
    public boolean hasPermissions() {
        return hasACL;
    }

    public void setACL(final boolean hasACL) {
        this.hasACL = hasACL;
    }

    @Override
    public boolean hasQuota() {
        return hasQuota;
    }

    public void setQuota(final boolean hasQuota) {
        this.hasQuota = hasQuota;
    }

    @Override
    public boolean hasSort() {
        return hasSort;
    }

    public void setSort(final boolean hasSort) {
        this.hasSort = hasSort;
    }

    @Override
    public boolean hasThreadReferences() {
        return hasThreadReferences;
    }

    public void setThreadReferences(final boolean hasThreadReferences) {
        this.hasThreadReferences = hasThreadReferences;
    }

    @Override
    public boolean hasSubscription() {
        return hasSubscription;
    }

    public void setHasSubscription(final boolean hasSubscription) {
        this.hasSubscription = hasSubscription;
    }

    @Override
    public final int getCapabilities() {
        int retval = 0;
        retval |= hasACL ? BIT_PERMISSIONS : 0;
        retval |= hasQuota ? BIT_QUOTA : 0;
        retval |= hasSort ? BIT_SORT : 0;
        retval |= hasThreadReferences ? BIT_THREAD_REFERENCES : 0;
        retval |= hasSubscription ? BIT_SUBSCRIPTION : 0;
        return retval;
    }

    @Override
    public String toString() {
        return new StringBuilder(64).append(MailCapabilities.class.getSimpleName()).append(": hasPermissions=").append(hasPermissions()).append(
            ", hasQuota=").append(hasQuota()).append(", hasSort=").append(hasSort()).append(", hasSubscription=").append(hasSubscription()).append(
            ", hasThreadReferences=").append(hasThreadReferences()).toString();
    }

}
