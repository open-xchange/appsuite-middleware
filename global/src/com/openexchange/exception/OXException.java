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
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import com.openexchange.exception.internal.I18n;
import com.openexchange.i18n.LocalizableStrings;
import com.openexchange.session.Session;

/**
 * {@link OXException} - The (super) exception class for all kinds of <a href="http://www.open-xchange.com">Open-Xchange</a> exceptions.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXException extends Exception implements OXExceptionConstants {

    // ([A-Z_]+)\((".*").*,
    // public static final String $1_MSG = $2;
    
    
    // /\*\*([\s*])*(\S.*\S)([\s*])*/
    // // $2
    
    
    
    // ([A-Za-z_]+)\((".*"),
    // $1($1_MSG,

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(OXException.class);

    private static final long serialVersionUID = 2058371531364916608L;

    private static final String DELIM = "\n\t";

    private static final int SERVER_ID = new SecureRandom().nextInt();

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

    private final Object[] messageArgs;

    private final int code;

    private final String message;

    private String detailedMessage;

    private String exceptionId;

    private String prefix;

    /**
     * Initializes a default {@link OXException}.
     */
    public OXException() {
        super();
        code = CODE_DEFAULT;
        count = COUNTER.incrementAndGet();
        properties = new HashMap<String, String>(8);
        categories = new LinkedList<Category>();
        message = OXExceptionStrings.MESSAGE;
        detailedMessage = null;
        messageArgs = MESSAGE_ARGS_EMPTY;
    }

    /**
     * Initializes a new {@link OXException}.
     * 
     * @param code The numeric error code
     * @param message The displayable printf-style error message (usually a constant from a class implementing {@link LocalizableStrings})
     * @param messageArgs The message arguments
     */
    public OXException(final int code, final String message, final Object... messageArgs) {
        super();
        count = COUNTER.incrementAndGet();
        properties = new HashMap<String, String>(8);
        categories = new LinkedList<Category>();
        this.code = code;
        this.message = message;
        this.messageArgs = messageArgs;
    }

    /**
     * Initializes a new {@link OXException}.
     * 
     * @param code The numeric error code
     * @param message The displayable printf-style error message (usually a constant from a class implementing {@link LocalizableStrings})
     * @param cause The optional cause for this {@link OXException}
     * @param messageArgs The message arguments
     */
    public OXException(final int code, final String message, final Throwable cause, final Object... messageArgs) {
        super(cause);
        count = COUNTER.incrementAndGet();
        properties = new HashMap<String, String>(8);
        categories = new LinkedList<Category>();
        this.code = code;
        this.message = message;
        this.messageArgs = messageArgs;
    }

    /**
     * Sets the detailed error message which appears in log and is <b>not</b> shown to user.
     * 
     * @param detailedMessage The detailed error message
     * @return This exception with detailed error message applied (for chained invocations)
     */
    public OXException setDetailedMessage(final String detailedMessage) {
        this.detailedMessage = detailedMessage;
        return this;
    }

    /**
     * Sets the detailed error message which appears in log and is <b>not</b> shown to user.
     * 
     * @param messageFormat The printf-style detailed error message
     * @param args The printf arguments
     * @return This exception with detailed error message applied (for chained invocations)
     */
    public OXException setDetailedMessage(final String messageFormat, final Object... args) {
        if (null == messageFormat) {
            return this;
        }
        try {
            this.detailedMessage = String.format(Locale.US, messageFormat, args);
        } catch (final NullPointerException e) {
            this.detailedMessage = null;
        } catch (final IllegalFormatException e) {
            LOG.error(e.getMessage(), e);
            final Exception logMe = new Exception(super.getMessage());
            logMe.setStackTrace(super.getStackTrace());
            LOG.error("Illegal message format.", logMe);
            this.detailedMessage = null;
        }
        return this;
    }

    /**
     * Gets the arguments for displayable message.
     * 
     * @return The message arguments to set
     */
    public Object[] getMessageArgs() {
        return messageArgs;
    }

    /**
     * Logs this exception - if allowed - using specified logger.
     * 
     * @param log The logger
     */
    public void log(final Log log) {
        final LogLevel logLevel = getCategories().get(0).getLogLevel();
        if (!logLevel.appliesTo(log)) {
            return;
        }
        final String loggable = getLogMessage(logLevel, null);
        if (null == loggable) {
            return;
        }
        logLevel.log(loggable, this, log);
    }

    /**
     * Gets the detailed message for specified log level.
     * <p>
     * This is a convenience method that invokes {@link #getLogMessage(LogLevel, String)} with latter argument set to
     * <code>null</code>.
     * 
     * @param logLevel The log level
     * @return The detailed message for specified log level or <code>null</code> if not loggable.
     * @see #getLogMessage(LogLevel, String)
     */
    public String getLogMessage(final LogLevel logLevel) {
        return getLogMessage(logLevel, null);
    }

    /**
     * Gets the detailed message for specified log level.
     * 
     * @param logLevel The log level
     * @param defaultLog The default logging to return if this exception is not loggable for specified log level
     * @return The detailed message for specified log level or <code>defaultLog</code> if not loggable.
     */
    public String getLogMessage(final LogLevel logLevel, final String defaultLog) {
        if (!isLoggable(logLevel)) {
            return defaultLog;
        }
        return getDetailedMessage();
    }

    /**
     * Gets the composed detail message.
     * 
     * @return The detail message
     */
    public String getDetailedMessage() {
        /*
         * Log details
         */
        final StringBuilder sb = new StringBuilder(256).append(getErrorCode());
        /*
         * Iterate categories
         */
        sb.append(" Categories=");
        {
            final List<Category> cats = getCategories();
            sb.append(cats.get(0));
            final int size = cats.size();
            for (int i = 1; i < size; i++) {
                sb.append(',').append(cats.get(i));
            }
        }
        /*
         * Append message
         */
        sb.append(" Message=");
        if (null == detailedMessage) {
            final String str = getDisplayMessage0(Locale.US);
            if (null == str) {
                sb.append(EMPTY_MSG);
            } else {
                sb.append('\'').append(str).append('\'');
            }
        } else {
            sb.append('\'').append(detailedMessage).append('\'');
        }
        /*
         * Exception identifier
         */
        sb.append(" exceptionID=").append(getExceptionId());
        /*
         * Properties
         */
        if (!properties.isEmpty()) {
            for (final String propertyName : new TreeSet<String>(properties.keySet())) {
                sb.append(DELIM).append(propertyName).append(": ").append(properties.get(propertyName));
            }
        }
        /*
         * Finally return
         */
        return sb.toString();
    }

    /**
     * Checks if this {@link OXException} is loggable for specified log level.
     * 
     * @param logLevel The log level
     * @return <code>true</code> if this {@link OXException} is loggable for specified log level; otherwise <code>false</code>
     */
    private boolean isLoggable(final LogLevel logLevel) {
        for (final Category category : getCategories()) {
            if (logLevel.includes(category)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the (sorted) categories.
     * 
     * @return The (sorted) categories
     */
    public List<Category> getCategories() {
        if (this.categories.isEmpty()) {
            /*
             * No category specified. Fall back to default one.
             */
            return Collections.<Category> singletonList(CATEGORY_ERROR);
        }
        Collections.sort(this.categories);
        return Collections.unmodifiableList(this.categories);
    }

    /**
     * Adds specified category.
     * 
     * @param category The category to add
     * @return This exception with category added (for chained invocations)
     */
    public OXException addCategory(final Category category) {
        if (null != category) {
            categories.add(category);
        }
        return this;
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
            exceptionId = new StringBuilder(16).append(SERVER_ID).append('-').append(count).toString();
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
     * <p>
     * This method is intended for being overridden by subclasses.
     * 
     * @return The prefix
     * @see #getErrorCode()
     */
    public String getPrefix() {
        return null == prefix ? PREFIX_GENERAL : prefix;
    }

    /**
     * Sets the prefix for the compound error code: &lt;prefix&gt; + "-" + &lt;code&gt;
     * 
     * @param prefix The prefix
     * @return This {@link OXException} with new prefix applied (for chained invocations)
     */
    public OXException setPrefix(final String prefix) {
        this.prefix = prefix;
        return this;
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
     * Gets the internationalized message intended for being displayed to user.
     * 
     * @param locale The locale providing the language to which the message shall be translated
     * @return The internationalized message
     */
    public String getDisplayMessage(final Locale locale) {
        final String msg = getDisplayMessage0(locale);
        return msg == null ? EMPTY_MSG : msg;
    }

    private String getDisplayMessage0(final Locale locale) {
        final Locale lcl = null == locale ? Locale.US : locale;
        String msg = I18n.getInstance().translate(lcl, message);
        if (msg != null) {
            try {
                msg = String.format(lcl, msg, messageArgs);
            } catch (final NullPointerException e) {
                msg = null;
            } catch (final IllegalFormatException e) {
                LOG.error(e.getMessage(), e);
                final Exception logMe = new Exception(super.getMessage());
                logMe.setStackTrace(super.getStackTrace());
                LOG.error("Illegal message format.", logMe);
                msg = null;
            }
        }
        return msg;
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
     * Sets the session properties:
     * <ul>
     * <li> {@link OXExceptionConstants#PROPERTY_SESSION}</li>
     * <li> {@link OXExceptionConstants#PROPERTY_CLIENT}</li>
     * <li> {@link OXExceptionConstants#PROPERTY_AUTH_ID}</li>
     * <li> {@link OXExceptionConstants#PROPERTY_LOGIN}</li>
     * <li> {@link OXExceptionConstants#PROPERTY_USER}</li>
     * <li> {@link OXExceptionConstants#PROPERTY_CONTEXT}</li>
     * </ul>
     * 
     * @param session The session
     * @return The exception with session properties applide (for chained invocations)
     */
    public OXException setSessionProperties(final Session session) {
        properties.put(PROPERTY_SESSION, session.getSessionID());
        properties.put(PROPERTY_CLIENT, session.getClient());
        properties.put(PROPERTY_AUTH_ID, session.getAuthId());
        properties.put(PROPERTY_LOGIN, session.getLogin());
        properties.put(PROPERTY_CONTEXT, String.valueOf(session.getContextId()));
        properties.put(PROPERTY_USER, String.valueOf(session.getContextId()));
        return this;
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
        return Collections.unmodifiableSet(properties.keySet());
    }

}
