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


/**
 * Contains the result information (e. g. content, truncated) of sanitizing HTML emails based on the provided information.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class HtmlSanitizeResult {

    /**
     * Content of the mail to display
     */
    private String content;

    /**
     * Marker if the mail was truncated
     */
    private boolean truncated;

    /**
     * Marker if <code>&lt;body&gt;</code> has already been replaced with a <code>&lt;div&gt;</code> tag for embedded display
     */
    private boolean bodyReplacedWithDiv;

    /**
     * Initializes a new {@link HtmlSanitizeResult}.
     *
     * @param content
     */
    public HtmlSanitizeResult(String content) {
        this(content, false);
    }

    /**
     * Initializes a new {@link HtmlSanitizeResult}.
     *
     * @param content
     * @param truncated
     */
    public HtmlSanitizeResult(String content, boolean truncated) {
        this.content = content;
        this.truncated = truncated;
        this.bodyReplacedWithDiv = false;
    }

    /**
     * Gets the content
     *
     * @return The content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content
     *
     * @param content The content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the truncated
     *
     * @return The truncated
     */
    public boolean isTruncated() {
        return truncated;
    }

    /**
     * Sets the truncated
     *
     * @param truncated The truncated to set
     */
    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    /**
     * Checks if <code>&lt;body&gt;</code> has already been replaced with a <code>&lt;div&gt;</code> tag for embedded display.
     *
     * @return <code>true</code> if already replaced; otherwise <code>false</code>
     */
    public boolean isBodyReplacedWithDiv() {
        return bodyReplacedWithDiv;
    }

    /**
     * Sets whether <code>&lt;body&gt;</code> has already been replaced with a <code>&lt;div&gt;</code> tag for embedded display
     *
     * @param bodyReplacedWithDiv <code>true</code> if already replaced; otherwise <code>false</code>
     */
    public void setBodyReplacedWithDiv(boolean bodyReplacedWithDiv) {
        this.bodyReplacedWithDiv = bodyReplacedWithDiv;
    }

}
