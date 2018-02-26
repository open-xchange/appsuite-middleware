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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.html;

import com.openexchange.session.Session;

/**
 * {@link HtmlSanitizeOptions} - The options when sanitizing HTML content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class HtmlSanitizeOptions {

    /**
     * Creates a new builder instance.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>HtmlSanitizeOptions</code> */
    public static class Builder {

        private String optConfigName;
        private boolean dropExternalImages;
        private boolean[] modified;
        private String cssPrefix;
        private int maxContentSize;
        private boolean suppressLinks;
        private boolean replaceBodyWithDiv;
        private boolean prettyPrint;
        private Session session;

        Builder() {
            super();
            optConfigName = null;
            dropExternalImages = false;
            modified = null;
            cssPrefix = null;
            maxContentSize = 0;
            suppressLinks = false;
            replaceBodyWithDiv = false;
            session = null;
            prettyPrint = true;
        }


        /** Sets the session */
        public Builder setSession(Session session) {
            this.session = session;
            return this;
        }

        public Builder setOptConfigName(String optConfigName) {
            this.optConfigName = optConfigName;
            return this;
        }

        /** Sets whether to drop image URLs */
        public Builder setDropExternalImages(boolean dropExternalImages) {
            this.dropExternalImages = dropExternalImages;
            return this;
        }

        /** Sets an optional <code>boolean</code> array with length <code>1</code> to store modified status */
        public Builder setModified(boolean[] modified) {
            this.modified = modified;
            return this;
        }

        /** Sets the optional CSS prefix to use. */
        public Builder setCssPrefix(String cssPrefix) {
            this.cssPrefix = cssPrefix;
            return this;
        }

        /** Sets the max. content size, which is the maximum number of bytes that will be returned for content. Less than or equal to 0 (zero) means unlimited. Below <code>10000</code> will be ignored. */
        public Builder setMaxContentSize(int maxContentSize) {
            this.maxContentSize = maxContentSize;
            return this;
        }

        /** Sets whether to suppress links. */
        public Builder setSuppressLinks(boolean suppressLinks) {
            this.suppressLinks = suppressLinks;
            return this;
        }

        /** Sets whether <code>&lt;body&gt;</code> is supposed to be replaced with a <code>&lt;div&gt;</code> tag for embedded display */
        public Builder setReplaceBodyWithDiv(boolean replaceBodyWithDiv) {
            this.replaceBodyWithDiv = replaceBodyWithDiv;
            return this;
        }

        /** Set whether to 'pretty-print' the html or not */
        public Builder setPrettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }

        /** Builds the instance from this builder's arguments */
        public HtmlSanitizeOptions build() {
            return new HtmlSanitizeOptions(session, optConfigName, dropExternalImages, modified, cssPrefix, maxContentSize, suppressLinks, replaceBodyWithDiv, prettyPrint);
        }
    }

    // ------------------------------------------------------------------------------------------------------------

    private final String optConfigName;
    private final boolean dropExternalImages;
    private final boolean[] modified;
    private final String cssPrefix;
    private final int maxContentSize;
    private final boolean suppressLinks;
    private final boolean replaceBodyWithDiv;
    private final boolean prettyPrint;
    private final Session session;

    HtmlSanitizeOptions(Session session, String optConfigName, boolean dropExternalImages, boolean[] modified, String cssPrefix, int maxContentSize, boolean suppressLinks, boolean replaceBodyWithDiv, boolean prettyPrint) {
        super();
        this.session = session;
        this.optConfigName = optConfigName;
        this.dropExternalImages = dropExternalImages;
        this.modified = modified;
        this.cssPrefix = cssPrefix;
        this.maxContentSize = maxContentSize;
        this.suppressLinks = suppressLinks;
        this.replaceBodyWithDiv = replaceBodyWithDiv;
        this.prettyPrint = prettyPrint;
    }

    /**
     * Gets the optional session
     *
     * @return The session or <code>null</code>
     */
    public Session optSession() {
        return session;
    }

    /**
     * Gets the optional configuration name to read whitelist from
     *
     * @return The optional configuration name to read whitelist from
     */
    public String getOptConfigName() {
        return optConfigName;
    }

    /**
     * Whether to drop image URLs
     *
     * @return <code>true</code> to drop; otherwise <code>false</code>
     */
    public boolean isDropExternalImages() {
        return dropExternalImages;
    }

    /**
     * Gets an optional <code>boolean</code> array with length <code>1</code> to store modified status
     *
     * @return A <code>boolean</code> array with length <code>1</code> to store modified status
     */
    public boolean[] getModified() {
        return modified;
    }

    /**
     * Gets the cssPrefix
     *
     * @return The cssPrefix
     */
    public String getCssPrefix() {
        return cssPrefix;
    }

    /**
     * Gets the max. content size, which is the maximum number of bytes that will be returned for content. Less than or equal to 0 (zero) means unlimited. Below <code>10000</code> will be ignored.
     *
     * @return The max. content size
     */
    public int getMaxContentSize() {
        return maxContentSize;
    }

    /**
     * Checks whether to suppress links.
     *
     * @return <code>true</code> to suppress links; otherwise <code>false</code>
     */
    public boolean isSuppressLinks() {
        return suppressLinks;
    }

    /**
     * Checks whether <code>&lt;body&gt;</code> is supposed to be replaced with a <code>&lt;div&gt;</code> tag for embedded display
     *
     * @return <code>true</code> to replace; otherwise <code>false</code>
     */
    public boolean isReplaceBodyWithDiv() {
        return replaceBodyWithDiv;
    }

    /**
     * Checks whether to 'pretty-print' the html or not
     * 
     * @return <code>true</code> to pretty-print, otherwise <code>false</code>
     */
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

}
