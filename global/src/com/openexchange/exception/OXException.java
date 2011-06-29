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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.exception;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.exception.internal.I18n;

/**
 * {@link OXException} - The (super) exception class for all kinds of <a href="http://www.open-xchange.com">Open-Xchange</a> exceptions.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXException extends RuntimeException implements OXExceptionConstants {

    private static final long serialVersionUID = 2058371531364916608L;

    private static final Random RANDOM = new SecureRandom();

    private static final int SERVER_ID = RANDOM.nextInt();

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    /**
     * Gets the server identifier.
     * 
     * @return The server identifier
     */
    public static int getServerId() {
        return SERVER_ID;
    }

    /*-
     * ------------------------------------- Member stuff -------------------------------------
     */

    private final int count;

    private final Map<String, String> properties;

    private final List<Category> categories;

    private int code;

    private String message;

    private String detailedMessage;

    private String exceptionId;

    /**
     * Initializes a new {@link OXException}.
     */
    public OXException() {
        super();
        count = COUNTER.incrementAndGet();
        properties = new HashMap<String, String>(8);
        categories = new LinkedList<Category>();
        categories.add(CATEGORY_DEFAULT);
        final int i = 9;
    }

    public String getDetailedMessage() {
        // TODO: Compose it!
        return null;
    }

    /**
     * Gets an {@link Iterator} for specified categories.
     * 
     * @return The {@link Iterator} for specified categories
     */
    public Iterator<Category> getCategories() {
        return new ArrayList<Category>(categories).iterator();
    }

    /**
     * Adds specified category.
     * 
     * @param category The category to add
     */
    public void addCategory(final Category category) {
        categories.add(category);
    }

    /**
     * Removes specified category.
     * 
     * @param category The category to remove
     */
    public void removeCategory(final Category category) {
        categories.remove(category);
    }

    /**
     * Gets this exception's identifier.
     * 
     * @return The exception identifier
     */
    public String getExceptionId() {
        if (exceptionId == null) {
            final StringBuilder builder = new StringBuilder();
            builder.append(SERVER_ID);
            builder.append('-');
            builder.append(count);
            exceptionId = builder.toString();
        }
        return exceptionId;
    }

    /**
     * Sets this exception's identifier.
     * <p>
     * <b>Note</b>: The identifier is calculated when invoking {@link #getExceptionId()}. When setting the identifier, the calculated value
     * is overridden.
     * 
     * @param exceptionId The identifier
     */
    public void setExceptionId(final String exceptionId) {
        this.exceptionId = exceptionId;
    }

    /**
     * Gets the prefix for the compound error code: &lt;prefix&gt; + "-" + &lt;code&gt;
     * 
     * @return The prefix
     * @see #getErrorCode()
     */
    public String getPrefix() {
        return PREFIX_GENERAL;
    }

    /**
     * Gets the compound error code: &lt;prefix&gt; + "-" + &lt;code&gt;
     * 
     * @return The compound error code
     */
    public final String getErrorCode() {
        return new StringBuilder(getPrefix()).append('-').append(code).toString();
    }

    /**
     * Gets the message intended for being displayed to user.
     * 
     * @param locale The locale providing the language to which the displayabe message shall be translated
     * @return The displayable message
     */
    public String getDisplayableMessage(final Locale locale) {
        return I18n.getInstance().translate(locale, message);
    }

    @Override
    public String getMessage() {
        return getDetailedMessage();
    }

    @Override
    public String toString() {
        return getDetailedMessage();
    }

    /*-
     * ----------------------------- Property related methods -----------------------------
     */

    /**
     * Checks for existence of specified property.
     * <p>
     * See {@link OXExceptionConstants} for property name constants; e.g. {@link OXExceptionConstants#PROPERTY_SESSION}.
     * 
     * @param name The property name
     * @return <code>true</code> if such a property exists; otherwise <code>false</code>
     * @see OXExceptionConstants
     */
    public boolean containsProperty(final String name) {
        return properties.containsKey(name);
    }

    /**
     * Gets the value of the denoted property.
     * <p>
     * See {@link OXExceptionConstants} for property name constants; e.g. {@link OXExceptionConstants#PROPERTY_SESSION}.
     * 
     * @param name The property name
     * @return The property value or <code>null</code> if absent
     * @see OXExceptionConstants
     */
    public String getProperty(final String name) {
        return properties.get(name);
    }

    /**
     * Sets the value of denoted property.
     * <p>
     * See {@link OXExceptionConstants} for property name constants; e.g. {@link OXExceptionConstants#PROPERTY_SESSION}.
     * 
     * @param name The property name
     * @param value The property value
     * @return The value previously associated with property name or <code>null</code> if not present before
     * @see OXExceptionConstants
     */
    public String setProperty(final String name, final String value) {
        if (null == value) {
            return properties.remove(name);
        }
        return properties.put(name, value);
    }

    /**
     * Removes the value of the denoted property.
     * <p>
     * See {@link OXExceptionConstants} for property name constants; e.g. {@link OXExceptionConstants#PROPERTY_SESSION}.
     * 
     * @param name The property name
     * @return The removed property value or <code>null</code> if absent
     * @see OXExceptionConstants
     */
    public String remove(final String name) {
        return properties.remove(name);
    }

    /**
     * Gets the {@link Set} view for contained property names.<br>
     * <b>Note</b>: Changes in returned set do not affect original properties!
     * <p>
     * See {@link OXExceptionConstants} for property name constants; e.g. {@link OXExceptionConstants#PROPERTY_SESSION}.
     * 
     * @return The property name
     * @see OXExceptionConstants
     */
    public Set<String> getPropertyNames() {
        return new HashSet<String>(properties.keySet());
    }

}
