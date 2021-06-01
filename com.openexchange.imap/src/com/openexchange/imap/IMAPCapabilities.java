/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.imap;

import com.openexchange.mail.PreviewMode;
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

    /**
     * TEXT PREVIEW: <code>"SNIPPET=FUZZY"</code>
     */
    public static final String CAP_TEXT_PREVIEW = PreviewMode.SNIPPET_FUZZY.getCapabilityName();

    /**
     * TEXT PREVIEW: <code>"PREVIEW=FUZZY"</code>
     */
    public static final String CAP_TEXT_PREVIEW_NEW = PreviewMode.PREVIEW_FUZZY.getCapabilityName();

    /**
     * RFC8970-conform TEXT PREVIEW: <code>"PREVIEW"</code>
     */
    public static final String CAP_TEXT_PREVIEW_RFC8970 = PreviewMode.PREVIEW_RFC8970.getCapabilityName();

    /**
     * CONDSTORE support according to <a href="https://tools.ietf.org/html/rfc7162">https://tools.ietf.org/html/rfc7162</a>
     */
    public static final String CAP_CONDSTORE = "CONDSTORE";

    /**
     * Filters applicable to existing messages: <code>"FILTER=SIEVE"</code>
     */
    public static final String CAP_FILTER_SIEVE = "FILTER=SIEVE";

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
    private boolean hasAttachmentSearch;
    private boolean hasTextPreview;
    private boolean hasMailFilterApplication;
    private boolean hasPublicFolders;
    private boolean hasSharedFolders;

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

    public void setACL(boolean hasACL) {
        this.hasACL = hasACL;
    }

    public boolean hasIMAP4() {
        return hasIMAP4;
    }

    public void setIMAP4(boolean hasIMAP4) {
        this.hasIMAP4 = hasIMAP4;
    }

    public boolean hasIMAP4rev1() {
        return hasIMAP4rev1;
    }

    public void setIMAP4rev1(boolean hasIMAP4rev1) {
        this.hasIMAP4rev1 = hasIMAP4rev1;
    }

    @Override
    public boolean hasQuota() {
        return hasQuota;
    }

    public void setQuota(boolean hasQuota) {
        this.hasQuota = hasQuota;
    }

    @Override
    public boolean hasSort() {
        return hasSort;
    }

    public void setSort(boolean hasSort) {
        this.hasSort = hasSort;
    }

    public boolean hasThreadOrderedSubject() {
        return hasThreadOrderedSubject;
    }

    public void setThreadOrderedSubject(boolean hasThreadOrderedSubject) {
        this.hasThreadOrderedSubject = hasThreadOrderedSubject;
    }

    @Override
    public boolean hasThreadReferences() {
        return hasThreadReferences;
    }

    public void setThreadReferences(boolean hasThreadReferences) {
        this.hasThreadReferences = hasThreadReferences;
    }

    public boolean hasUIDPlus() {
        return hasUIDPlus;
    }

    public void setUIDPlus(boolean hasUIDPlus) {
        this.hasUIDPlus = hasUIDPlus;
    }

    @Override
    public boolean hasSubscription() {
        return hasSubscription;
    }

    public void setHasSubscription(boolean hasSubscription) {
        this.hasSubscription = hasSubscription;
    }

    public boolean hasNamespace() {
        return hasNamespace;
    }

    public void setNamespace(boolean hasNamespace) {
        this.hasNamespace = hasNamespace;
    }

    public boolean hasIdle() {
        return hasIdle;
    }

    public void setIdle(boolean hasIdle) {
        this.hasIdle = hasIdle;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public void setChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean hasSortDisplay() {
        return hasSortDisplay;
    }

    public void setSortDisplay(boolean hasSortDisplay) {
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
            ", hasSortDisplay=").append(hasSortDisplay()).append(", hasFileNameSearch=").append(hasFileNameSearch())
            .append(", hasAttachmentSearch=").append(hasAttachmentMarker()).toString();
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

    /**
     * Sets whether text previews are supported
     *
     * @param hasTextPreview <code>true</code> to signal support for text previews; otherwise <code>false</code>
     */
    public void setTextPreview(boolean hasTextPreview) {
        this.hasTextPreview = hasTextPreview;
    }

    @Override
    public boolean hasTextPreview() {
        return hasTextPreview;
    }

    @Override
    public boolean hasAttachmentMarker() {
        return hasAttachmentSearch;
    }

    public void setAttachmentSearchEnabled(boolean enabled) {
        this.hasAttachmentSearch = enabled;
    }

    /**
     * Sets if mail system supports public folders
     *
     * @param supported <code>true</code> if public folders are supported; otherwise <code>false</code>
     */
    public void setPublicFolders(boolean supported) {
        this.hasPublicFolders = supported;
    }

    @Override
    public boolean hasPublicFolders() {
        return hasPublicFolders;
    }

    /**
     * Sets if mail system supports shared folders
     *
     * @param supported <code>true</code> if shared folders are supported; otherwise <code>false</code>
     */
    public void setSharedFolders(boolean supported) {
        this.hasSharedFolders = supported;
    }

    @Override
    public boolean hasSharedFolders() {
        return hasSharedFolders;
    }

    @Override
    public boolean hasMailFilterApplication() {
        return hasMailFilterApplication;
    }

    public void setMailFilterApplication(boolean enabled) {
        this.hasMailFilterApplication = enabled;
    }

    @Override
    public boolean hasFolderValidity() {
        return true;
    }

}
