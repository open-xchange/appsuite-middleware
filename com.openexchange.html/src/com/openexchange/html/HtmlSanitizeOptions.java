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

package com.openexchange.html;

import com.openexchange.session.Session;

/**
 * {@link HtmlSanitizeOptions} - The options when sanitizing HTML content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class HtmlSanitizeOptions {

    /** HTML parser preference */
    public static enum ParserPreference {
        /**
         * No HTML parser preference.
         */
        NONE,
        /**
         * Preference for <a href="http://jericho.htmlparser.net/docs/index.html">Jericho HTML parser</a>
         */
        JERICHO,
        /**
         * Preference for <a href="https://jsoup.org/">jsoup HTML parser</a>
         */
        JSOUP;
    }

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
        private boolean sanitize;
        private boolean prettyPrint;
        private Session session;
        private ParserPreference parserPreference;

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
            sanitize = true;
            prettyPrint = true;
            parserPreference = ParserPreference.NONE;
        }

        /** Sets this builder arguments according to specified <code>HtmlSanitizeOptions</code> instance */
        public Builder copyFrom(HtmlSanitizeOptions options) {
            this.session = options.optSession();
            this.sanitize = options.isSanitize();
            this.optConfigName = options.getOptConfigName();
            this.dropExternalImages = options.isDropExternalImages();
            this.modified = options.getModified();
            this.cssPrefix = options.getCssPrefix();
            this.maxContentSize = options.getMaxContentSize();
            this.suppressLinks = options.isSuppressLinks();
            this.replaceBodyWithDiv = options.isReplaceBodyWithDiv();
            this.parserPreference = options.getParserPreference();
            this.prettyPrint = options.isPrettyPrint();
            return this;
        }

        /** Sets the parser preference */
        public Builder setParserPreference(ParserPreference parserPreference) {
            this.parserPreference = parserPreference;
            return this;
        }

        /** Sets the session */
        public Builder setSession(Session session) {
            this.session = session;
            return this;
        }

        /** Sets whether HTML/CSS content is supposed to be sanitized (against white-list). If <code>false</code> only CSS is processed to keep possible CSS prefix */
        public Builder setSanitize(boolean sanitize) {
            this.sanitize = sanitize;
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
            return new HtmlSanitizeOptions(session, sanitize, optConfigName, dropExternalImages, modified, cssPrefix, maxContentSize, suppressLinks, replaceBodyWithDiv, parserPreference, prettyPrint);
        }
    }

    // ------------------------------------------------------------------------------------------------------------

    private final boolean sanitize;
    private final String optConfigName;
    private final boolean dropExternalImages;
    private final boolean[] modified;
    private final String cssPrefix;
    private final int maxContentSize;
    private final boolean suppressLinks;
    private final boolean replaceBodyWithDiv;
    private final boolean prettyPrint;
    private final Session session;
    private final ParserPreference parserPreference;

    HtmlSanitizeOptions(Session session, boolean sanitize, String optConfigName, boolean dropExternalImages, boolean[] modified, String cssPrefix, int maxContentSize, boolean suppressLinks, boolean replaceBodyWithDiv,
        ParserPreference parserPreference, boolean prettyPrint) {
        super();
        this.session = session;
        this.sanitize = sanitize;
        this.optConfigName = optConfigName;
        this.dropExternalImages = dropExternalImages;
        this.modified = modified;
        this.cssPrefix = cssPrefix;
        this.maxContentSize = maxContentSize;
        this.suppressLinks = suppressLinks;
        this.replaceBodyWithDiv = replaceBodyWithDiv;
        this.prettyPrint = prettyPrint;
        this.parserPreference = null == parserPreference ? ParserPreference.NONE : parserPreference;
    }

    /**
     * Gets the HTML parser preference
     *
     * @return The preference
     */
    public ParserPreference getParserPreference() {
        return parserPreference;
    }

    /**
     * Checks whether HTML/CSS content is supposed to be sanitized (against white-list). If <code>false</code> only CSS is processed to keep possible CSS prefix
     *
     * @return <code>true</code> to sanitize; otherwise <code>false</code>
     */
    public boolean isSanitize() {
        return sanitize;
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
