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

package com.openexchange.imap;

import com.openexchange.mail.api.MailCapabilities;

/**
 * {@link IMAPCapabilities} - The capabilities of underlying IMAP server with {@link #hasTimeStamps()} hard-coded to return
 * <code>false</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IMAPCapabilities extends MailCapabilities {

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

    /**
     * SORTY BY DISPLAYNAME
     */
    public static final String CAP_SORT_DISPLAY = "SORT=DISPLAY";

    /**
     * SEARCH BY ATTACHMENT FILE NAME
     */
    public static final String CAP_SEARCH_FILENAME = "SEARCH=X-MIMEPART";

    /*-
     * IMAP bit constants
     */

    private static final int BIT_THREAD_ORDEREDSUBJECT = 1 << NEXT_SHIFT_OPERAND;

    private static final int BIT_IMAP4 = 1 << (NEXT_SHIFT_OPERAND + 1);

    private static final int BIT_IMAP4_REV1 = 1 << (NEXT_SHIFT_OPERAND + 2);

    private static final int BIT_UIDPLUS = 1 << (NEXT_SHIFT_OPERAND + 3);

    private static final int BIT_NAMESPACE = 1 << (NEXT_SHIFT_OPERAND + 4);

    private static final int BIT_IDLE = 1 << (NEXT_SHIFT_OPERAND + 5);

    private static final int BIT_CHILDREN = 1 << (NEXT_SHIFT_OPERAND + 6);

    private static final int BIT_SORT_DISPLAY = 1 << (NEXT_SHIFT_OPERAND + 7);

    /*-
     * Members
     */

    private boolean hasACL;
    private boolean hasQuota;
    private boolean hasThreadReferences;
    private boolean hasThreadOrderedSubject;
    private boolean hasSort;
    private boolean hasIMAP4;
    private boolean hasIMAP4rev1;
    private boolean hasUIDPlus;
    private boolean hasSubscription;
    private boolean hasNamespace;
    private boolean hasIdle;
    private boolean hasChildren;
    private boolean hasSortDisplay;
    private boolean hasFileNameSearch;

    /**
     * Initializes a new {@link IMAPCapabilities}
     */
    public IMAPCapabilities() {
        super();
    }

    @Override
    public boolean hasPermissions() {
        return hasACL;
    }

    public void setACL(final boolean hasACL) {
        this.hasACL = hasACL;
    }

    public boolean hasIMAP4() {
        return hasIMAP4;
    }

    public void setIMAP4(final boolean hasIMAP4) {
        this.hasIMAP4 = hasIMAP4;
    }

    public boolean hasIMAP4rev1() {
        return hasIMAP4rev1;
    }

    public void setIMAP4rev1(final boolean hasIMAP4rev1) {
        this.hasIMAP4rev1 = hasIMAP4rev1;
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

    public boolean hasThreadOrderedSubject() {
        return hasThreadOrderedSubject;
    }

    public void setThreadOrderedSubject(final boolean hasThreadOrderedSubject) {
        this.hasThreadOrderedSubject = hasThreadOrderedSubject;
    }

    @Override
    public boolean hasThreadReferences() {
        return hasThreadReferences;
    }

    public void setThreadReferences(final boolean hasThreadReferences) {
        this.hasThreadReferences = hasThreadReferences;
    }

    public boolean hasUIDPlus() {
        return hasUIDPlus;
    }

    public void setUIDPlus(final boolean hasUIDPlus) {
        this.hasUIDPlus = hasUIDPlus;
    }

    @Override
    public boolean hasSubscription() {
        return hasSubscription;
    }

    public void setHasSubscription(final boolean hasSubscription) {
        this.hasSubscription = hasSubscription;
    }

    public boolean hasNamespace() {
        return hasNamespace;
    }

    public void setNamespace(final boolean hasNamespace) {
        this.hasNamespace = hasNamespace;
    }

    public boolean hasIdle() {
        return hasIdle;
    }

    public void setIdle(final boolean hasIdle) {
        this.hasIdle = hasIdle;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public void setChildren(final boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean hasSortDisplay() {
        return hasSortDisplay;
    }

    public void setSortDisplay(final boolean hasSortDisplay) {
        this.hasSortDisplay = hasSortDisplay;
    }

    @Override
    public final int getCapabilities() {
        int retval = 0;
        retval |= hasACL ? BIT_PERMISSIONS : 0;
        retval |= hasIMAP4 ? BIT_IMAP4 : 0;
        retval |= hasIMAP4rev1 ? BIT_IMAP4_REV1 : 0;
        retval |= hasQuota ? BIT_QUOTA : 0;
        retval |= hasSort ? BIT_SORT : 0;
        retval |= hasThreadOrderedSubject ? BIT_THREAD_ORDEREDSUBJECT : 0;
        retval |= hasThreadReferences ? BIT_THREAD_REFERENCES : 0;
        retval |= hasUIDPlus ? BIT_UIDPLUS : 0;
        retval |= hasSubscription ? BIT_SUBSCRIPTION : 0;
        retval |= hasNamespace ? BIT_NAMESPACE : 0;
        retval |= hasIdle ? BIT_IDLE : 0;
        retval |= hasChildren ? BIT_CHILDREN : 0;
        retval |= hasSortDisplay ? BIT_SORT_DISPLAY : 0;
        return retval;
    }

    @Override
    public String toString() {
        return new StringBuilder(64).append(MailCapabilities.class.getSimpleName()).append(": hasPermissions=").append(hasPermissions()).append(
            ", hasQuota=").append(hasQuota()).append(", hasSort=").append(hasSort()).append(", hasSubscription=").append(hasSubscription()).append(
            ", hasThreadReferences=").append(hasThreadReferences()).append(", hasChildren=").append(hasChildren()).append(", hasIMAP4=").append(
            hasIMAP4()).append(", hasIMAP4rev1=").append(hasIMAP4rev1()).append(", hasNamespace=").append(hasNamespace()).append(
            ", hasThreadOrderedSubject=").append(hasThreadOrderedSubject()).append(", hasUIDPlus=").append(hasUIDPlus()).append(
            ", hasSortDisplay=").append(hasSortDisplay()).append(", hasFileNameSearch=").append(hasFileNameSearch()).toString();
    }

    /**
     * Sets whether file name search is supported
     *
     * @param hasFileNameSearch <code>true</code> to indicate support for file name search; otherwise <code>false</code>
     */
    public void setFileNameSearch(boolean hasFileNameSearch) {
        this.hasFileNameSearch = hasFileNameSearch;
    }

    @Override
    public boolean hasFileNameSearch() {
        return hasFileNameSearch;
    }

    @Override
    public boolean hasFolderValidity() {
        return true;
    }

}
