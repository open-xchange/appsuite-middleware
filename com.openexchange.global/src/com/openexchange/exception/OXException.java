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

package com.openexchange.exception;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import com.openexchange.exception.Category.EnumType;
import com.openexchange.exception.internal.I18n;
import com.openexchange.i18n.Localizable;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXException.class);

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

    /**
     * The generic types.
     */
    public static enum Generic {
        /**
         * No generic type set.
         */
        NONE,
        /**
         * Not found.
         */
        NOT_FOUND,
        /**
         * The exception was caused due to missing permissions needed to access a certain module and/or module.
         */
        NO_PERMISSION,
        /**
         * A mandatory (request) field is missing.
         */
        MANDATORY_FIELD,
        /**
         * A conflicting operation (tries to update data but offers an out-dated time stamp).
         */
        CONFLICT,
    }

    /**
     * Creates a general exception.
     *
     * @param logMessage The log message
     * @return A general exception.
     */
    public static OXException general(String logMessage) {
        return OXExceptions.general(logMessage);
    }

    /**
     * Creates a general exception.
     *
     * @param logMessage The log message
     * @param cause The cause
     * @return A general exception.
     */
    public static OXException general(String logMessage, Throwable cause) {
        return OXExceptions.general(logMessage, cause);
    }

    /**
     * Creates a not-found exception.
     *
     * @param id The identifier of the missing object
     * @return A not-found exception.
     */
    public static OXException notFound(String id) {
        return OXExceptions.notFound(id);
    }

    /**
     * Creates a module-denied exception.
     *
     * @param module The identifier of the module
     * @return A module-denied exception.
     */
    public static OXException noPermissionForModule(String module) {
        return OXExceptions.noPermissionForModule(module);
    }

    /**
     * Creates a folder-denied exception.
     *
     * @return A folder-denied exception.
     */
    public static OXException noPermissionForFolder() {
        return OXExceptions.noPermissionForFolder();
    }

    /**
     * Creates a missing-field exception.
     *
     * @param name The field name
     * @return A missing-field exception.
     */
    public static OXException mandatoryField(String name) {
        return OXExceptions.mandatoryField(name);
    }

    /**
     * Creates a missing-field exception.
     *
     * @param code The code number
     * @param name The field name
     * @return A missing-field exception.
     */
    public static OXException mandatoryField(int code, String name) {
        return OXExceptions.mandatoryField(code, name);
    }

    /**
     * Creates a general conflict exception.
     *
     * @return A general conflict exception.
     */
    public static OXException conflict() {
        return OXExceptions.conflict();
    }

    /*-
     * ------------------------------------- Member stuff -------------------------------------
     */

    private final int count;
    private final Map<String, String> properties;
    private final List<Category> categories;
    private final List<ProblematicAttribute> problematics;
    private final Map<String, Object> arguments;

    private Object[] displayArgs;
    private int code;
    private boolean lightWeight;
    private String displayMessage;
    private String logMessage;
    private Object[] logArgs;
    private String exceptionId;
    private String prefix;
    private boolean interceptable;
    private Generic generic;
    private OXExceptionCode exceptionCode;

    /**
     * Initializes a default {@link OXException}.
     */
    public OXException() {
        super();
        interceptable = true;
        generic = Generic.NONE;
        code = CODE_DEFAULT;
        count = COUNTER.incrementAndGet();
        properties = new HashMap<String, String>(4);
        arguments = new HashMap<String, Object>(4);
        categories = new LinkedList<Category>();
        displayMessage = OXExceptionStrings.MESSAGE;
        logMessage = null;
        displayArgs = MESSAGE_ARGS_EMPTY;
        problematics = new LinkedList<ProblematicAttribute>();
        lightWeight = false;
    }

    /**
     * Initializes a default {@link OXException}.
     *
     * @param cause The cause
     */
    public OXException(Throwable cause) {
        super(cause);
        interceptable = true;
        generic = Generic.NONE;
        code = CODE_DEFAULT;
        count = COUNTER.incrementAndGet();
        properties = new HashMap<String, String>(4);
        arguments = new HashMap<String, Object>(4);
        categories = new LinkedList<Category>();
        displayMessage = OXExceptionStrings.MESSAGE;
        logMessage = null;
        displayArgs = MESSAGE_ARGS_EMPTY;
        problematics = new LinkedList<ProblematicAttribute>();
        lightWeight = false;
    }

    /**
     * Initializes a {@link OXException} cloned from specified {@link OXException} including stack trace.
     *
     * @param cloneMe The <code>OXException</code> instance to clone
     */
    public OXException(OXException cloneMe) {
        super();
        interceptable = true;
        setStackTrace(cloneMe.getStackTrace());
        this.generic = cloneMe.generic;
        this.count = cloneMe.count;
        this.code = cloneMe.code;
        this.categories = null == cloneMe.categories ? new LinkedList<Category>() : new ArrayList<Category>(cloneMe.categories);
        this.displayArgs = cloneMe.displayArgs;
        this.displayMessage = cloneMe.displayMessage;
        this.exceptionId = cloneMe.exceptionId;
        this.logMessage = cloneMe.logMessage;
        this.prefix = cloneMe.prefix;
        this.problematics = null == cloneMe.problematics ? new LinkedList<ProblematicAttribute>() : new ArrayList<ProblematicAttribute>(cloneMe.problematics);
        this.properties = null == cloneMe.properties ? new HashMap<String, String>(4) : new HashMap<String, String>(cloneMe.properties);
        this.arguments = null == cloneMe.arguments ? new HashMap<String, Object>(4) : new HashMap<String, Object>(cloneMe.arguments);
        this.lightWeight = cloneMe.lightWeight;
    }

    /**
     * Initializes a new {@link OXException}.
     *
     * @param code The numeric error code
     */
    public OXException(int code) {
        super();
        interceptable = true;
        generic = Generic.NONE;
        count = COUNTER.incrementAndGet();
        properties = new HashMap<String, String>(4);
        arguments = new HashMap<String, Object>(4);
        categories = new LinkedList<Category>();
        this.code = code;
        this.displayMessage = OXExceptionStrings.MESSAGE;
        this.displayArgs = MESSAGE_ARGS_EMPTY;
        problematics = new LinkedList<ProblematicAttribute>();
        lightWeight = false;
    }

    /**
     * Initializes a new {@link OXException}.
     *
     * @param code The numeric error code
     * @param displayMessage The printf-style display message (usually a constant from a class implementing {@link LocalizableStrings})
     * @param displayArgs The arguments for display message
     */
    public OXException(int code, String displayMessage, Object... displayArgs) {
        super();
        interceptable = true;
        generic = Generic.NONE;
        count = COUNTER.incrementAndGet();
        properties = new HashMap<String, String>(4);
        arguments = new HashMap<String, Object>(4);
        categories = new LinkedList<Category>();
        this.code = code;
        this.displayMessage = null == displayMessage ? OXExceptionStrings.MESSAGE : displayMessage;
        this.displayArgs = null == displayArgs ? MESSAGE_ARGS_EMPTY : displayArgs;
        problematics = new LinkedList<ProblematicAttribute>();
        lightWeight = false;
    }

    /**
     * Initializes a new {@link OXException}.
     *
     * @param code The numeric error code
     * @param displayMessage The printf-style display message (usually a constant from a class implementing {@link LocalizableStrings})
     * @param cause The optional cause for this {@link OXException}
     * @param displayArgs The arguments for display message
     */
    public OXException(int code, String displayMessage, Throwable cause, Object... displayArgs) {
        super(cause);
        interceptable = true;
        generic = Generic.NONE;
        count = COUNTER.incrementAndGet();
        properties = new HashMap<String, String>(4);
        arguments = new HashMap<String, Object>(4);
        categories = new LinkedList<Category>();
        this.code = code;
        this.displayMessage = null == displayMessage ? OXExceptionStrings.MESSAGE : displayMessage;
        this.displayArgs = displayArgs;
        problematics = new ArrayList<ProblematicAttribute>(1);
        lightWeight = false;
    }

    /**
     * Copies from specified {@link OXException}.
     *
     * @param e The {@link OXException} to copy from
     */
    public void copyFrom(OXException e) {
        this.interceptable = e.interceptable;
        this.code = e.code;
        this.generic = e.generic;
        this.categories.clear();
        this.categories.addAll(e.getCategories());
        this.displayArgs = e.displayArgs;
        this.displayMessage = e.displayMessage;
        this.exceptionId = e.exceptionId;
        this.logMessage = e.logMessage;
        this.prefix = e.prefix;
        this.problematics.clear();
        if (null != e.problematics) {
            this.problematics.addAll(e.problematics);
        }
        this.properties.clear();
        this.properties.putAll(e.properties);
        this.arguments.clear();
        this.arguments.putAll(e.arguments);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return lightWeight ? this : super.fillInStackTrace();
    }

    /**
     * Gets the possible cause that is no Open-Xchange error.
     *
     * @return The possible non-OXException cause or <code>null</code>
     */
    public Throwable getNonOXExceptionCause() {
        Throwable cause = getCause();
        return cause instanceof OXException ? ((OXException) cause).getNonOXExceptionCause() : cause;
    }

    /**
     * Checks if the error code from this {@link OXException} instance matches the given code.
     *
     * @param code The error code number
     * @param prefix The error code prefix
     * @return <code>true</code> if error code matches; otherwise <code>false</code>
     * @throws NullPointerException If <code>prefix</code> argument is <code>null</code>
     */
    public boolean equalsCode(int code, String prefix) {
        // Check error code number
        if (this.code != code) {
            return false;
        }

        // Check error code prefix
        if (null == prefix) {
            throw new NullPointerException("prefix is null");
        }
        return prefix.equals(this.prefix);
    }

    /**
     * Gets the numeric code.
     *
     * @return The code
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the generic
     *
     * @return The generic
     */
    public Generic getGeneric() {
        return generic;
    }

    /**
     * Checks generic equality.
     *
     * @param generic The generic
     * @return <code>true</code> if equal; otherwise <code>false</code>
     */
    public boolean isGeneric(Generic generic) {
        return null != generic && generic.equals(this.generic);
    }

    /**
     * Checks if generic is set to {@link Generic#NOT_FOUND}.
     *
     * @return <code>true</code> for {@link Generic#NOT_FOUND}; otherwise <code>false</code>
     */
    public boolean isNotFound() {
        return Generic.NOT_FOUND.equals(generic);
    }

    /**
     * Checks if generic is set to {@link Generic#NO_PERMISSION}.
     *
     * @return <code>true</code> for {@link Generic#NO_PERMISSION}; otherwise <code>false</code>
     */
    public boolean isNoPermission() {
        return Generic.NO_PERMISSION.equals(generic);
    }

    /**
     * Checks if generic is set to {@link Generic#MANDATORY_FIELD}.
     *
     * @return <code>true</code> for {@link Generic#MANDATORY_FIELD}; otherwise <code>false</code>
     */
    public boolean isMandatory() {
        return Generic.MANDATORY_FIELD.equals(generic);
    }

    /**
     * Checks if generic is set to {@link Generic#CONFLICT}.
     *
     * @return <code>true</code> for {@link Generic#CONFLICT}; otherwise <code>false</code>
     */
    public boolean isConflict() {
        return Generic.CONFLICT.equals(generic);
    }

    /**
     * Marks this {@link OXException} as light-weight.
     * <p>
     * Light-weight in terms of {@link #fillInStackTrace()} is implemented in a lazy way.
     * <pre>
     *   public synchronized Throwable fillInStackTrace() {
     *      return this;
     *   }
     * </pre>
     */
    public OXException markLightWeight() {
        this.lightWeight = true;
        return this;
    }

    /**
     * Sets the generic
     *
     * @param generic The generic to set
     * @return This {@link OXException} with generic applied
     */
    public OXException setGeneric(Generic generic) {
        this.generic = generic;
        return this;
    }

    /**
     * Sets the error message which appears in log and is <b>not</b> shown to user.
     *
     * @param logMessage The log error message
     * @return This exception with log error message applied (for chained invocations)
     */
    public OXException setLogMessage(String logMessage) {
        this.logMessage = logMessage;
        return this;
    }

    /**
     * Sets the error message which appears in log and is <b>not</b> shown to user.
     *
     * @param displayFormat The printf-style log error message
     * @param args The printf arguments
     * @return This exception with log error message applied (for chained invocations)
     */
    public OXException setLogMessage(String displayFormat, Object... args) {
        if (null == displayFormat) {
            return this;
        }
        try {
            this.logMessage = String.format(Locale.US, displayFormat, args);
            logArgs = args;
        } catch (NullPointerException e) {
            this.logMessage = null;
        } catch (IllegalFormatException e) {
            LOG.error("Illegal format: >>{}<<, params: {}, code: {}", displayFormat, (null == args || 0 == args.length ? "<none>" : Arrays.toString(args)), getErrorCode(), e);
            this.logMessage = null;
        }
        return this;
    }

    /**
     * Sets the message intended for being displayed to the user.
     *
     * @param displayMessage The displayable message
     * @param displayArgs The optional arguments
     * @return This {@link OXException} with display message applied
     */
    public OXException setDisplayMessage(String displayMessage, Object... displayArgs) {
        this.displayMessage = null == displayMessage ? OXExceptionStrings.MESSAGE : displayMessage;
        this.displayArgs = null == displayArgs ? MESSAGE_ARGS_EMPTY : displayArgs;
        return this;
    }

    /**
     * Gets the plain log message.
     *
     * @return The plain log message.
     */
    public String getPlainLogMessage() {
        return logMessage;
    }

    /**
     * Gets the arguments used to compose log message.
     *
     * @return The log arguments
     * @see #getPlainLogMessage()
     */
    public Object[] getLogArgs() {
        return logArgs;
    }

    /**
     * Gets the arguments for display message.
     *
     * @return The display arguments
     */
    public Object[] getDisplayArgs() {
        return displayArgs;
    }

    /**
     * Logs this exception - if allowed - in best-fitting log level using specified logger.
     * <p>
     * This method basically performs:
     * <pre>
     *  switch (e.getCategories().get(0).getLogLevel()) {
     *    case TRACE:
     *        LOGGER.trace("", e);
     *        break;
     *    case DEBUG:
     *        LOGGER.debug("", e);
     *        break;
     *    case INFO:
     *        LOGGER.info("", e);
     *        break;
     *    case WARNING:
     *        LOGGER.warn("", e);
     *        break;
     *    case ERROR:
     *        LOGGER.error("", e);
     *        break;
     *    default:
     *        break;
     *  }
     * </pre>
     *
     * @param logger The logger
     * @deprecated Please perform the actual logging in the class, in which the logger instance is defined
     */
    @Deprecated
    public void log(org.slf4j.Logger logger) {
        switch (getCategories().get(0).getLogLevel()) {
            case TRACE:
                logger.trace("", this);
                break;
            case DEBUG:
                logger.debug("", this);
                break;
            case INFO:
                logger.info("", this);
                break;
            case WARNING:
                logger.warn("", this);
                break;
            case ERROR:
                logger.error("", this);
                break;
            default:
                break;
        }
    }

    /**
     * Gets the log message appropriate for specified log level.
     * <p>
     * This is a convenience method that invokes {@link #getLogMessage(LogLevel, String)} with latter argument set to <code>null</code>.
     *
     * @param logLevel The log level
     * @return The log message for specified log level or <code>null</code> if not loggable.
     * @see #getLogMessage(LogLevel, String)
     * @deprecated Just use {@link #getLogMessage()}
     */
    @Deprecated
    public String getLogMessage(LogLevel logLevel) {
        return getLogMessage(logLevel, null);
    }

    /**
     * Gets the log message appropriate for specified log level.
     *
     * @param logLevel The log level
     * @param defaultLog The default logging to return if this exception is not loggable for specified log level
     * @return The log message for specified log level or <code>defaultLog</code> if not loggable.
     * @deprecated Just use {@link #getLogMessage()}
     */
    @Deprecated
    public String getLogMessage(LogLevel logLevel, String defaultLog) {
        if (!isLoggable()) {
            return defaultLog;
        }
        return getLogMessage();
    }

    /**
     * Gets the composed log message (also returned by {@link #getMessage()};<br>
     * e.g. <pre>OX-0001 Categories=ERROR Message="Huston, we have a problem" exceptionID=147</pre>
     *
     * @return The log message
     */
    public String getLogMessage() {
        /*
         * Log details
         */
        StringBuilder sb = new StringBuilder(256).append(getErrorCode());
        /*
         * Iterate categories
         */
        sb.append(" Categories=");
        {
            List<Category> cats = getCategories();
            sb.append(cats.get(0));
            int size = cats.size();
            for (int i = 1; i < size; i++) {
                sb.append(',').append(cats.get(i));
            }
        }
        /*
         * Append message
         */
        sb.append(" Message=");
        if (null == logMessage) {
            String str = getDisplayMessage0(Locale.US);
            if (null == str) {
                sb.append(EMPTY_MSG);
            } else {
                sb.append('\'').append(str).append('\'');
            }
        } else {
            sb.append('\'').append(logMessage).append('\'');
        }
        /*
         * Exception identifier
         */
        sb.append(" exceptionID=").append(getExceptionId());
        /*
         * Properties
         */
        if (!properties.isEmpty()) {
            for (String propertyName : new TreeSet<String>(properties.keySet())) {
                sb.append(DELIM).append(propertyName).append(": ").append(properties.get(propertyName));
            }
        }
        /*
         * Finally return
         */
        return dropSubsequentWhitespaces(sb.toString());
    }

    /**
     * Gets the sole message.
     *
     * @return The sole message
     */
    public String getSoleMessage() {
        /*
         * Log details
         */
        StringBuilder sb = new StringBuilder(256);
        /*
         * Append message
         */
        String logMessage = this.logMessage;
        if (null == logMessage) {
            String str = getDisplayMessage0(Locale.US);
            if (null == str) {
                sb.append(EMPTY_MSG);
            } else {
                sb.append(str);
            }
        } else {
            sb.append(logMessage);
        }
        /*
         * Finally return
         */
        return dropSubsequentWhitespaces(sb.toString());
    }

    /**
     * Checks if this {@link OXException} is loggable for specified log level.
     *
     * @return <code>true</code> if this {@link OXException} is loggable for specified log level; otherwise <code>false</code>
     * @deprecated
     */
    @Deprecated
    public boolean isLoggable() {
        return true;
    }

    /**
     * Gets the first category.
     *
     * @return The first category.
     */
    public Category getCategory() {
        return getCategories().get(0);
    }

    /**
     * Gets the (sorted) categories.
     *
     * @return The (sorted) categories
     */
    public List<Category> getCategories() {
        if (categories.isEmpty()) {
            /*
             * No category specified. Fall back to default one.
             */
            return Collections.<Category> singletonList(CATEGORY_ERROR);
        }
        if (1 == categories.size()) {
            return Collections.unmodifiableList(categories);
        }
        // Sort before return
        sortCategories(categories);
        return Collections.unmodifiableList(categories);
    }

    /**
     * Sorts specified categories.
     *
     * @param categories The categories to sort
     */
    public static void sortCategories(List<Category> categories) {
        List<ComparableCategory> comparables = toComparables(categories);
        Collections.sort(comparables);
        categories.clear();
        for (ComparableCategory comparable : comparables) {
            if (null != comparable) {
                categories.add(comparable.category);
            }
        }
    }

    private static List<ComparableCategory> toComparables(List<Category> categories) {
        List<ComparableCategory> ret = new ArrayList<ComparableCategory>(categories.size());
        for (Category category : categories) {
            if (null != category) {
                ret.add(new ComparableCategory(category));
            }
        }
        return ret;
    }

    private static final class ComparableCategory implements Comparable<ComparableCategory> {

        protected final Category category;

        protected ComparableCategory(Category category) {
            super();
            this.category = category;
        }

        @Override
        public int compareTo(ComparableCategory other) {
            return category.compareTo(other.category);
        }

    }

    /**
     * Adds specified category.
     *
     * @param category The category to add
     * @return This exception with category added (for chained invocations)
     */
    public final OXException addCategory(Category category) {
        if (null != category) {
            if (categories.isEmpty() && EnumType.TRY_AGAIN.equals(category.getType()) && OXExceptionStrings.MESSAGE.equals(displayMessage)) {
                displayMessage = OXExceptionStrings.MESSAGE_RETRY;
            }
            categories.add(category);
        }
        return this;
    }

    /**
     * Sets specified category and drops all existing categories.
     *
     * @param category The category to set
     * @return This exception with category set (for chained invocations)
     */
    public OXException setCategory(Category category) {
        if (null != category) {
            categories.clear();
            /*-
             *
            if (EnumType.TRY_AGAIN.equals(category.getType()) && OXExceptionStrings.MESSAGE.equals(displayMessage)) {
                displayMessage = OXExceptionStrings.MESSAGE_RETRY;
            }
            */
            categories.add(category);
        }
        return this;
    }

    /**
     * Removes specified category.
     *
     * @param category The category to remove
     */
    public void removeCategory(Category category) {
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
    public void setExceptionId(String exceptionId) {
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
     * Checks if this {@link OXException}'s prefix equals specified expected prefix.
     *
     * @param expected The expected prefix to check against
     * @return <code>true</code> if prefix equals specified expected prefix; otherwise <code>false</code>
     */
    public boolean isPrefix(String expected) {
        return (null == prefix ? PREFIX_GENERAL : prefix).equals(expected);
    }

    /**
     * Sets the prefix for the compound error code: &lt;prefix&gt; + "-" + &lt;code&gt;
     *
     * @param prefix The prefix
     * @return This {@link OXException} with new prefix applied (for chained invocations)
     */
    public OXException setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Gets the interceptable
     *
     * @return The interceptable
     */
    public boolean isInterceptable() {
        return interceptable;
    }

    /**
     * Sets the interceptable
     *
     * @param interceptable The interceptable to set
     */
    public OXException setInterceptable(boolean interceptable) {
        this.interceptable = interceptable;
        return this;
    }

    /**
     * Gets the exception code
     *
     * @return The exception code or <code>null</code>
     */
    public OXExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    /**
     * Sets the exception code
     *
     * @param exceptionCode The exception code to set
     * @return This {@link OXException} with exception code applied
     */
    public OXException setExceptionCode(OXExceptionCode exceptionCode) {
        this.exceptionCode = exceptionCode;
        return this;
    }

    /**
     * Gets the compound error code: &lt;prefix&gt; + "-" + &lt;code&gt;
     *
     * @return The compound error code
     */
    public final String getErrorCode() {
        return new StringBuilder(getPrefix()).append('-').append(String.format("%04d", Integer.valueOf(code))).toString();
    }

    /**
     * Gets the internationalized message intended for being displayed to user.
     *
     * @param locale The locale providing the language to which the message shall be translated
     * @return The internationalized message
     */
    public String getDisplayMessage(Locale locale) {
        String msg = getDisplayMessage0(locale);
        return msg == null ? EMPTY_MSG : msg;
    }

    private String getDisplayMessage0(Locale locale) {
        Locale lcl = null == locale ? Locale.US : locale;
        I18n i18n = I18n.getInstance();

        // Translate format string
        String msg = i18n.translate(lcl, displayMessage);

        // Generate formatted string using the specified locale, format string, and arguments.
        if (msg != null && displayArgs != null) {
            int length = displayArgs.length;
            if (length > 0) {
                try {
                    Object[] args = new Object[length];
                    for (int i = length; i-- > 0;) {
                        Object arg = displayArgs[i];
                        args[i] = (arg instanceof Localizable) ? i18n.translate(lcl, ((Localizable) arg).getArgument()) : arg;
                    }
                    msg = String.format(lcl, msg, args);
                } catch (NullPointerException e) {
                    msg = null;
                } catch (MissingFormatArgumentException e) {
                    LOG.debug("Missing format argument: >>{}<<", msg, e);
                } catch (IllegalFormatException e) {
                    LOG.error("Illegal message format: >>{}<<", msg, e);
                }
            }
        }

        return dropSubsequentWhitespaces(msg);
    }

    /**
     * Checks if this exception is similar to specified exception code regarding category and code number.
     *
     * @param exceptionCode The exception code to check against
     * @return <code>true</code> if this exception is similar to specified exception code; otherwise <code>false</code>
     */
    public final boolean similarTo(OXExceptionCode exceptionCode) {
        return (exceptionCode.getCategory() == this.getCategory() && exceptionCode.getNumber() == this.getCode());
    }

    /**
     * Checks if this exception is similar to specified exception regarding category and code number.
     *
     * @param other The exception to check against
     * @return <code>true</code> if this exception is similar to specified exception; otherwise <code>false</code>
     */
    public final boolean similarTo(OXException other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other.getCategory() == this.getCategory() && other.getCode() == this.getCode()) {
            return true;
        }
        return false;
    }

    @Override
    public String getMessage() {
        return getLogMessage();
    }

    @Override
    public String toString() {
        return getLogMessage();
    }

    /*-
     * ----------------------------- ProblematicAttribute stuff -----------------------------
     */

    /**
     * Generic interface for a problematic attribute.
     */
    public static interface ProblematicAttribute {
        // Different implementations will have different methods.
    }

    /**
     * Interface for a truncated attribute.
     */
    public static interface Truncated extends ProblematicAttribute {

        /**
         * @return the column identifier of the truncated attribute.
         */
        int getId();

        /**
         * @return the maximum allowed size in bytes for the truncated attribute.
         */
        int getMaxSize();

        /**
         * @return the actual length of the value of the truncated attribute.
         */
        int getLength();
    }

    /**
     * Interface for an incorrect string in an attribute.
     */
    public static interface IncorrectString extends ProblematicAttribute {

        /**
         * Gets the column identifier of the attribute containing the incorrect string.
         *
         * @return The column identifier of the truncated attribute
         */
        int getId();

        /**
         * Gets the incorrect (sub-)string.
         *
         * @return The incorrect string
         */
        String getIncorrectString();

    }

    /**
     * Interface for an attribute which could not be parsed orderly.
     */
    public static interface Parsing extends ProblematicAttribute {

        /**
         * @return the JSON attribute name that can not be parsed.
         */
        String getAttribute();
    }

    /**
     * Adds an attribute identifier that has been truncated.
     *
     * @param truncatedId identifier of the truncated attribute.
     * @deprecated use {@link #addProblematic(ProblematicAttribute)}
     */
    @Deprecated
    public void addTruncatedId(final int truncatedId) {
        problematics.add(new Truncated() {

            @Override
            public int getId() {
                return truncatedId;
            }

            @Override
            public int getLength() {
                return -1;
            }

            @Override
            public int getMaxSize() {
                return -1;
            }
        });
    }

    /**
     * Adds a problematic attribute.
     */
    public void addProblematic(ProblematicAttribute problematic) {
        problematics.add(problematic);
    }

    private static final ProblematicAttribute[] EMPTY_PROBLEMATICS = new ProblematicAttribute[0];

    /**
     * Gets the problematic attributes.
     *
     * @return The problematic attributes
     */
    public final ProblematicAttribute[] getProblematics() {
        return problematics.isEmpty() ? EMPTY_PROBLEMATICS : problematics.toArray(new ProblematicAttribute[problematics.size()]);
    }

    /*-
     * ----------------------------- Arguments related methods -----------------------------
     */

    /**
     * Checks for existence of specified argument.
     *
     * @param name The argument name
     * @return <code>true</code> if such an argument exists; otherwise <code>false</code>
     */
    public boolean containsArgument(String name) {
        return arguments.containsKey(name);
    }

    /**
     * Gets the value of the denoted argument.
     *
     * @param name The argument name
     * @return The argument value or <code>null</code> if absent
     */
    public Object getArgument(String name) {
        return arguments.get(name);
    }

    /**
     * Sets the value of denoted argument.
     *
     * @param name The argument name
     * @param value The argument value
     * @return The value previously associated with argument value or <code>null</code> if not present before
     */
    public Object setArgument(String name, Object value) {
        if (null == value) {
            return arguments.remove(name);
        }
        return arguments.put(name, value);
    }

    /**
     * Removes the value of the denoted argument.
     *
     * @param name The argument name
     * @return The removed argument value or <code>null</code> if absent
     */
    public Object removeArgument(String name) {
        return arguments.remove(name);
    }

    /**
     * Gets the {@link Set} view for contained argument names.<br>
     * <b>Note</b>: Changes in returned set do not affect original arguments!
     *
     * @return The argument names
     */
    public Set<String> getArgumentNames() {
        return Collections.unmodifiableSet(arguments.keySet());
    }

    /**
     * Gets the unmodifiable {@link Map} view for contained arguments.<br>
     *
     * @return The arguments
     */
    public Map<String, Object> getArguments() {
        return Collections.unmodifiableMap(arguments);
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
    public boolean containsProperty(String name) {
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
    public String getProperty(String name) {
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
    public String setProperty(String name, String value) {
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
    public OXException setSessionProperties(Session session) {
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
    public String removeProperty(String name) {
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

    private static final Pattern P = Pattern.compile("(\\p{L}) {2,}(\\p{L})");

    /** Drops multiple subsequent white-spaces from given message */
    private static String dropSubsequentWhitespaces(String message) {
        if (null == message || message.indexOf("  ") < 0) {
            return message;
        }
        return P.matcher(message).replaceAll("$1 $2");
    }

}
