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

package com.openexchange.snippet;

import java.util.Collection;
import java.util.Map;

/**
 * {@link Snippet} - Represents arbitrary (textual) content.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Snippet {

    /**
     * The property name for the identifier.
     */
    public static final String PROP_ID = "com.openexchange.snippet.id";

    /**
     * The property name for the account identifier.
     */
    public static final String PROP_ACCOUNT_ID = "com.openexchange.snippet.accountid";

    /**
     * The property name for the type; e.g. <code>"signature"</code>.
     */
    public static final String PROP_TYPE = "com.openexchange.snippet.type";

    /**
     * The property name for the display name.
     */
    public static final String PROP_DISPLAY_NAME = "com.openexchange.snippet.displayname";

    /**
     * The property name for the module identifier; e.g. <code>"com.openexchange.mail"</code>.
     */
    public static final String PROP_MODULE = "com.openexchange.snippet.module";

    /**
     * The property name for the creator.
     */
    public static final String PROP_CREATED_BY = "com.openexchange.snippet.createdby";

    /**
     * The property name for the shared flag.
     */
    public static final String PROP_SHARED = "com.openexchange.snippet.shared";

    /**
     * The property name for the optional miscellaneous JSON data.
     */
    public static final String PROP_MISC = "com.openexchange.snippet.misc";

    /**
     * Gets the identifier.
     * 
     * @return The identifier.
     */
    int getId();

    /**
     * Gets the account identifier (if applicable for associated module).
     * 
     * @return The account identifier or <code>-1</code>.
     */
    int getAccountId();

    /**
     * Gets the module identifier; e.g. <code>"com.openexchange.mail"</code>.
     * 
     * @return The module identifier
     */
    String getModule();

    /**
     * Gets the type; e.g. <code>"signature"</code>.
     * 
     * @return The type
     */
    String getType();

    /**
     * Gets the display name.
     * 
     * @return The display name
     */
    String getDisplayName();

    /**
     * Gets the textual content.
     * 
     * @return The content
     */
    String getContent();

    /**
     * Gets the collection of attachments.
     * 
     * @return The attachments or an empty collection if none attached
     */
    Collection<Attachment> getAttachments();

    /**
     * Gets miscellaneous JSON data.
     * <p>
     * Allowed is any object returned by:
     * 
     * <pre>
     * String sJson = &quot;...&quot;; // Any JSON representation
     * new org.json.JSONTokener(sJson).next();
     * </pre>
     * 
     * @return The JSON data or <code>null</code> if absent
     */
    Object getMisc();

    /**
     * Signals whether this snippet is shared to others.
     * 
     * @return <code>true</code> if shared; otherwise <code>false</code>
     */
    boolean isShared();

    /**
     * Gets the identifier of this snippet's creator.
     * 
     * @return The creator identifier or <code>-1</code> if absent
     */
    int getCreatedBy();

    /**
     * Gets this snippet's properties.
     * 
     * @return The properties
     */
    Map<String, Object> getProperties();
}
