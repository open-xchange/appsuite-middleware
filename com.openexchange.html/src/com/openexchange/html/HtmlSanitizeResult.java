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
}
