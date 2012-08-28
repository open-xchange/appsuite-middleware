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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.snippet;

/**
 * {@link SnippetProperties} - The snippet's properties.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum SnippetProperties {

    /**
     * The snippet's properties.
     */
    PROPERTIES(null),
    /**
     * The snippet's content.
     */
    CONTENT(null),
    /**
     * The snippet's attachments.
     */
    ATTACHMENTS(null),
    /**
     * The property for the identifier.
     */
    ID("snpt.id"),
    /**
     * The property for the account identifier.
     */
    ACCOUNT_ID("snpt.accountid"),
    /**
     * The property for the type; e.g. <code>"signature"</code>.
     */
    TYPE("snpt.type"),
    /**
     * The property for the display name.
     */
    DISPLAY_NAME("snpt.displayname"),
    /**
     * The property for the module identifier; e.g. <code>"com.openexchange.mail"</code>.
     */
    MODULE("snpt.module"),
    /**
     * The property for the creator.
     */
    CREATED_BY("snpt.createdby"),
    /**
     * The property for the shared flag.
     */
    SHARED("snpt.shared"),
    /**
     * The property for the optional miscellaneous JSON data.
     */
    MISC("snpt.misc"),

    ;

    private final String propName;

    private SnippetProperties(final String propName) {
        this.propName = propName;
    }

    /**
     * Gets the property name
     * 
     * @return The property name or <code>null</code> if no property is associated
     */
    public String getPropName() {
        return propName;
    }

    @Override
    public String toString() {
        return propName;
    }
}
