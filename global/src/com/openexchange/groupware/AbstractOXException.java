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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exceptions.ErrorMessage;

/**
 * This should be the super class of all exceptions that are sent as error codes to the client.
 * 
 * @author <a href="thorben.betten@open-xchange.org">Thorben Betten</a>
 */
public class AbstractOXException extends Exception {

    public static final int SERVER_ID = new Random().nextInt();

    private static AtomicInteger instanceCounter = new AtomicInteger(0);

    private final int counter = instanceCounter.incrementAndGet();

    private final Map<String, String> details = new HashMap<String, String>();

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(AbstractOXException.class);

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 1960902715206993957L;

    public static enum Category {
        /**
         * An error resulting from wrong or missing input from front-end (e.g. mandatory field missing)
         * 
         * @value 1
         */
        USER_INPUT(1),
        /**
         * An error strictly related to user configuration which denies requested operation
         * 
         * @value 2
         */
        USER_CONFIGURATION(2),
        /**
         * An error related to insufficient permission settings
         * 
         * @value 3
         */
        PERMISSION(3),
        /**
         * A requested operation could not be accomplished cause a needed resource is temporary down or missing (e.g. imap server rejects
         * connection because of too many established connections)
         * 
         * @value 4
         */
        TRY_AGAIN(4),
        /**
         * A subsystem or third party service is down and therefore does not respond (e.g. database is down).
         * 
         * @value 5
         */
        SUBSYSTEM_OR_SERVICE_DOWN(5),
        /**
         * The underlying socket connection is corrupt, empty or closed. Only a temporary error that does not affect the whole system.
         * 
         * @value 6
         */
        SOCKET_CONNECTION(6),
        /**
         * An internal java-related (runtime) exception
         * 
         * @value 7
         */
        INTERNAL_ERROR(7),
        /**
         * A programming error which was caused by incorrect programm code.
         * 
         * @value 8
         */
        CODE_ERROR(8),
        /**
         * A concurrent modification
         * 
         * @value 9
         */
        CONCURRENT_MODIFICATION(9),
        /**
         * Error in system setup detected.
         * 
         * @value 10
         */
        SETUP_ERROR(10),
        /**
         * The requested operation could not be performed cause an underlying resource is full or busy (e.g. IMAP folder exceeds quota)
         * 
         * @value 11
         */
        EXTERNAL_RESOURCE_FULL(11),
        /**
         * The given data could not be stored into the database because an attribute contains a too long value.
         * 
         * @value 12
         */
        TRUNCATED(12),
        /**
         * This is not an error but a partial success with an attached warning.
         * 
         * @value 13
         */
        WARNING(13);

        private final int code;

        private Category(final int code) {
            this.code = code;
        }

        /**
         * @return the code
         */
        public int getCode() {
            return code;
        }

        /**
         * @param code number of the category.
         * @return the category according to the code.
         */
        public static Category byCode(final int code) {
            return CODES.get(Integer.valueOf(code));
        }

        private static final Map<Integer, Category> CODES;

        static {
            final Map<Integer, Category> tmp = new HashMap<Integer, Category>(Category.values().length, 1F);
            for (final Category category : values()) {
                tmp.put(Integer.valueOf(category.getCode()), category);
            }
            CODES = Collections.unmodifiableMap(tmp);
        }
    }

    private Component component;

    private Category category;

    /**
     * Four-digit detail number
     */
    private int detailNumber;

    /**
     * The arguments for the message.
     */
    private Object[] messageArgs;

    /**
     * List of problematic attributes.
     */
    private final List<ProblematicAttribute> problematics;

    /**
     * The user session which provide appropiate <code>Locale</code> object
     */
    private Locale locale;

    private String exceptionID;

    public static final DecimalFormat DF = new DecimalFormat("0000");

    /**
     * @deprecated use constructor with component, category and detail number.
     */
    @Deprecated
    public AbstractOXException() {
        super();
        component = EnumComponent.NONE;
        category = Category.SUBSYSTEM_OR_SERVICE_DOWN;
        problematics = new ArrayList<ProblematicAttribute>();
    }

    /**
     * @deprecated use constructor with component, category and detail number.
     */
    @Deprecated
    public AbstractOXException(final String message) {
        this(EnumComponent.NONE, message);
    }

    /**
     * @deprecated use constructor with component, category and detail number.
     */
    @Deprecated
    public AbstractOXException(final Component component, final String message) {
        super(message);
        this.component = component;
        category = Category.SUBSYSTEM_OR_SERVICE_DOWN;
        problematics = new ArrayList<ProblematicAttribute>();
    }

    /**
     * Copy constructor.
     * 
     * @param cause Nested cause.
     */
    public AbstractOXException(final AbstractOXException cause) {
        super(cause.getOrigMessage(), cause);
        component = cause.component;
        category = cause.category;
        detailNumber = cause.detailNumber;
        messageArgs = cause.messageArgs;
        problematics = new ArrayList<ProblematicAttribute>(cause.problematics);
    }

    /**
     * @deprecated use constructor with component, category and detail number.
     */
    @Deprecated
    public AbstractOXException(final Throwable cause) {
        this(EnumComponent.NONE, cause);
    }

    /**
     * @deprecated use constructor with component, category and detail number.
     */
    @Deprecated
    public AbstractOXException(final Component component, final Throwable cause) {
        super(cause);
        this.component = component;
        category = Category.CODE_ERROR;
        problematics = new ArrayList<ProblematicAttribute>();
    }

    /**
     * @deprecated use constructor with component, category and detail number.
     */
    @Deprecated
    public AbstractOXException(final String message, final Throwable cause) {
        this(EnumComponent.NONE, message, cause);
    }

    /**
     * @deprecated use constructor with component, category and detail number.
     */
    @Deprecated
    public AbstractOXException(final Component component, final String message, final Throwable cause) {
        super(message, cause);
        this.component = component;
        category = Category.SUBSYSTEM_OR_SERVICE_DOWN;
        problematics = new ArrayList<ProblematicAttribute>();
    }

    public AbstractOXException(final Component component, final String message, final AbstractOXException cause) {
        super(message, cause);
        this.component = component;
        category = cause.category;
        detailNumber = cause.detailNumber;
        problematics = new ArrayList<ProblematicAttribute>();
    }

    /**
     * Constructor with all parameters.
     * 
     * @param component Component.
     * @param category Category.
     * @param number detail number.
     * @param message message of the exception.
     * @param cause the cause.
     */
    public AbstractOXException(final Component component, final Category category, final int detailNumber, final String message, final Throwable cause) {
        super(message, cause);
        this.component = component;
        this.category = category;
        this.detailNumber = detailNumber;
        problematics = new ArrayList<ProblematicAttribute>();
    }

    /**
     * Constructor in conjuction with {@link ErrorMessage}.
     * 
     * @param message ErrorMessage
     * @param cause the initial cause.
     */
    public AbstractOXException(final ErrorMessage message, final Throwable cause) {
        super(message.getMessage(), cause);
        component = message.getComponent();
        category = message.getCategory();
        detailNumber = message.getDetailNumber();
        problematics = new ArrayList<ProblematicAttribute>();
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(final Component component) {
        this.component = component;
    }

    public int getDetailNumber() {
        return detailNumber;
    }

    public void setDetailNumber(final int detailNumber) {
        this.detailNumber = detailNumber;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    /**
     * @param messageArgs the messageArgs to set
     */
    public void setMessageArgs(final Object... messageArgs) {
        this.messageArgs = messageArgs;
    }

    /**
     * @return the messageArgs
     */
    public Object[] getMessageArgs() {
        return messageArgs;
    }

    /**
     * Adds an attribute identifier that has been truncated.
     * 
     * @param truncatedId identifier of the truncated attribute.
     * @deprecated use {@link #addProblematic(com.openexchange.groupware.AbstractOXException.Truncated)}
     */
    @Deprecated
    public void addTruncatedId(final int truncatedId) {
        problematics.add(new Truncated() {

            public int getId() {
                return truncatedId;
            }

            public int getLength() {
                return -1;
            }

            public int getMaxSize() {
                return -1;
            }
        });
    }

    /**
     * Adds a problematic attribute.
     */
    public void addProblematic(final ProblematicAttribute problematic) {
        problematics.add(problematic);
    }

    /**
     * @return the problematics
     */
    public final ProblematicAttribute[] getProblematics() {
        return problematics.toArray(new ProblematicAttribute[problematics.size()]);
    }

    /**
     * @return the original message without replacing printf-style patterns with arguments.
     */
    public String getOrigMessage() {
        return super.getMessage();
    }

    public String getErrorCode() {
        final StringBuilder sb = new StringBuilder();
        sb.append(component.getAbbreviation());
        sb.append('-');
        sb.append(DF.format(detailNumber));
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return toString();
    }

    public void overrideExceptionID(final String exceptionID) {
        this.exceptionID = exceptionID;
    }

    public String getExceptionID() {
        if (exceptionID == null) {
            final StringBuilder builder = new StringBuilder();
            builder.append(SERVER_ID);
            builder.append('-');
            builder.append(counter);
            exceptionID = builder.toString();
        }
        return exceptionID;
    }

    /**
     * Adds additional diagnostic information usually relevant for developers.
     * 
     * @param detail
     * @param value
     */
    public void setDetail(final String detail, final String value) {
        details.put(detail, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256).append(getErrorCode()).append(" Category=").append(category.code);
        sb.append(" Message=");
        String msg = null;
        if (super.getMessage() != null) {
            try {
                msg = locale == null ? String.format(super.getMessage(), messageArgs) : String.format(
                    locale,
                    super.getMessage(),
                    messageArgs);
            } catch (final NullPointerException e) {
                LOG.error(e.getMessage(), e);
                msg = super.getMessage();
            } catch (final IllegalFormatException e) {
                LOG.error(e.getMessage(), e);
                final Exception logMe = new Exception(super.getMessage());
                logMe.setStackTrace(super.getStackTrace());
                LOG.error("Illegal message format.", logMe);
                msg = super.getMessage();
            }
        }
        sb.append(msg == null ? "[Not available]" : msg);
        sb.append(" exceptionID=");
        sb.append(getExceptionID());
        return sb.toString();
    }

    public interface ProblematicAttribute {
        // Different implementations will have different methods.
    }

    public interface Truncated extends ProblematicAttribute {

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

    public interface Parsing extends ProblematicAttribute {

        /**
         * @return the JSON attribute name that can not be parsed.
         */
        String getAttribute();
    }
}
