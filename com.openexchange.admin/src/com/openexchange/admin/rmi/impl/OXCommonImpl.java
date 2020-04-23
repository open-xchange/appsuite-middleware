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

package com.openexchange.admin.rmi.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.NameAndIdObject;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.AbstractAdminRmiException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.exception.LogLevel;
import com.openexchange.java.Reflections;
import com.openexchange.log.LogProperties;

/**
 * General abstraction class used by all impl classes
 *
 * @author d7
 */
public abstract class OXCommonImpl {

    private final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXCommonImpl.class);

    protected final OXToolStorageInterface tool;

    public OXCommonImpl() {
        tool = OXToolStorageInterface.getInstance();
    }

    /**
     * Checks whether the given context exists or not
     *
     * @param ctx The context to check
     * @throws NoSuchContextException In case the context doesn't exist
     * @throws StorageException In case of errors with the storage
     */
    protected void checkExistence(Context ctx) throws NoSuchContextException, StorageException {
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException(i(ctx.getId()));
        }
    }

    protected final void contextcheck(final Context ctx) throws InvalidCredentialsException {
        if (null == ctx || null == ctx.getId()) {
            final InvalidCredentialsException e = new InvalidCredentialsException("Client sent invalid context data object");
            LOGGER.error("", e);
            throw e;
        }
    }

    protected void setUserIdInArrayOfUsers(final Context ctx, final User[] users) throws InvalidDataException, StorageException, NoSuchObjectException {
        for (final User user : users) {
            setIdOrGetIDFromNameAndIdObject(ctx, user);
        }
    }

    protected void setIdOrGetIDFromNameAndIdObject(final Context ctx, final NameAndIdObject nameandid) throws StorageException, InvalidDataException, NoSuchObjectException {
        final Integer id = nameandid.getId();
        if (null == id || 0 == id.intValue()) {
            final String name = nameandid.getName();
            if (null != name) {
                if (nameandid instanceof User) {
                    try {
                        nameandid.setId(I(tool.getUserIDByUsername(ctx, name)));
                    } catch (NoSuchUserException e) {
                        throw new NoSuchObjectException(e);
                    }
                } else if (nameandid instanceof Group) {
                    try {
                        nameandid.setId(I(tool.getGroupIDByGroupname(ctx, name)));
                    } catch (NoSuchGroupException e) {
                        throw new NoSuchObjectException(e);
                    }
                } else if (nameandid instanceof Resource) {
                    try {
                        nameandid.setId(I(tool.getResourceIDByResourcename(ctx, name)));
                    } catch (NoSuchResourceException e) {
                        throw new NoSuchObjectException(e);
                    }
                } else if (nameandid instanceof Context) {
                    nameandid.setId(I(tool.getContextIDByContextname(name)));
                } else if (nameandid instanceof Database) {
                    nameandid.setId(I(tool.getDatabaseIDByDatabasename(name)));
                } else if (nameandid instanceof Server) {
                    nameandid.setId(I(tool.getServerIDByServername(name)));
                }
            } else {
                final String simpleName = nameandid.getClass().getSimpleName().toLowerCase();
                throw new InvalidDataException("One " + simpleName + "object has no " + simpleName + "id or " + simpleName + "name");
            }
        }
    }

    /**
     * @param objects
     * @throws InvalidDataException
     */
    protected final static void doNullCheck(final Object... objects) throws InvalidDataException {
        for (final Object object : objects) {
            if (object == null) {
                throw new InvalidDataException();
            }
        }
    }

    /**
     * Checks whether the context exists and updates the schema if needed
     * @param ctx
     * @throws StorageException
     * @throws com.openexchange.admin.rmi.exceptions.DatabaseUpdateException
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException
     */
    protected void checkContextAndSchema(final Context ctx) throws StorageException, DatabaseUpdateException, NoSuchContextException {
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException("The context " + ctx.getId() + " does not exist!");
        }
        if (tool.checkAndUpdateSchemaIfRequired(ctx)) {
            DatabaseUpdateException e = tool.generateDatabaseUpdateException(ctx.getId().intValue());
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets the admin/reseller name from given credentials
     *
     * @param credentials The credentials
     * @return The admin/reseller name
     */
    protected static String getAdminName(Credentials credentials) {
        return null == credentials ? null : credentials.getLogin();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Logs provisioning exception with exception identifier in MDC, enhances exception message by identifier and returns exception with enhanced message
     *
     * @param logger The logger to log the exception
     * @param e The exception to log and enhance
     * @param creds The credentials used for this call
     * @param contextId The identifier of the affected context
     * @param objectId The identifier of the affected object
     * @return The enhanced exception
     */
    protected static <E extends AbstractAdminRmiException> E logAndReturnException(org.slf4j.Logger logger, E e, Credentials creds, String contextId, String objectId) {
        // Enhance by provisioning log properties & log exception
        LogProperties.putProvisioningLogProperties(getAdminName(creds), e.getExceptionId(), contextId, objectId);
        try {
            logger.error("", e);
        } finally {
            LogProperties.removeProvisioningLogProperties();
        }

        // Return exception instance as-is
        return e;
    }

    /**
     * Logs provisioning exception with exception identifier in MDC, enhances exception message by identifier and returns exception with enhanced message
     *
     * @param logger The logger to log the exception
     * @param e The exception to log and enhance
     * @param creds The credentials used for this call
     * @param contextId The identifier of the affected context
     * @return The enhanced exception
     */
    protected static <E extends AbstractAdminRmiException> E logAndReturnException(org.slf4j.Logger logger, E e, Credentials creds, String contextId) {
        return logAndReturnException(logger, e, creds, contextId, null);
    }

    /**
     * Logs provisioning exception with exception identifier in MDC, enhances exception message by identifier and returns exception with enhanced message
     *
     * @param logger The logger to log the exception
     * @param e The exception to log and enhance
     * @param creds The credentials used for this call
     * @return The enhanced exception
     */
    protected static <E extends AbstractAdminRmiException> E logAndReturnException(org.slf4j.Logger logger, E e, Credentials creds) {
        return logAndReturnException(logger, e, creds, null, null);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Logs given exception with exception identifier in MDC, enhances exception message by identifier and returns exception with enhanced message
     *
     * @param logger The logger to log the exception
     * @param e The exception to log and enhance
     * @param exceptionId The exception identifier
     * @param creds The credentials used for this call
     * @param contextId The identifier of the affected context
     * @param objectId The identifier of the affected object
     * @return The enhanced exception
     */
    protected static <E extends Exception> E logAndReturnException(org.slf4j.Logger logger, E e, String exceptionId, Credentials creds, String contextId, String objectId) {
        // Enhance by provisioning log properties & log exception
        LogProperties.putProvisioningLogProperties(getAdminName(creds), exceptionId, contextId, objectId);
        try {
            logger.error("", e);
        } finally {
            LogProperties.removeProvisioningLogProperties();
        }

        // Return exception instance as-is
        return e;
    }

    /**
     * Logs given exception with exception identifier in MDC, enhances exception message by identifier and returns exception with enhanced message
     *
     * @param logger The logger to log the exception
     * @param e The exception to log and enhance
     * @param exceptionId The exception identifier
     * @param creds The credentials used for this call
     * @param contextId The identifier of the affected context
     * @return The enhanced exception
     */
    protected static <E extends Exception> E logAndReturnException(org.slf4j.Logger logger, E e, String exceptionId, Credentials creds, String contextId) {
        return logAndReturnException(logger, e, exceptionId, creds, contextId, null);
    }

    /**
     * Logs given exception with exception identifier in MDC, enhances exception message by identifier and returns exception with enhanced message
     *
     * @param logger The logger to log the exception
     * @param e The exception to log and enhance
     * @param exceptionId The exception identifier
     * @param creds The credentials used for this call
     * @return The enhanced exception
     */
    protected static <E extends Exception> E logAndReturnException(org.slf4j.Logger logger, E e, String exceptionId, Credentials creds) {
        return logAndReturnException(logger, e, exceptionId, creds, null, null);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a comma-separated list of object identifiers
     *
     * @param objects The array of object
     * @return The identifiers as comma-separated list
     */
    protected static String getObjectIds(NameAndIdObject[] objects) {
        if (null == objects || 0 >= objects.length) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(objects[0].getId()).append(",");
        for (int j = 1; j < objects.length; j++) {
            sb.append(',').append(objects[j].getId());
        }
        return sb.toString();
    }

    /**
     * Log a message with given log properties in MDC
     *
     * @param level The log level
     * @param logger The logger
     * @param creds The credentials to put in MDC
     * @param contextId The affected context's identifier to put in MDC
     * @param objectId The affected object's identifier to put in MDC
     * @param e The exception to log
     * @param message The message to log
     * @param args The arguments to log
     */
    public static void log(LogLevel level, org.slf4j.Logger logger, Credentials creds, String contextId, String objectId, Exception e, String message, Object... args) {
        if (null == level || null == logger) {
            LOGGER.error("LogLevel and logger instance must not be null!");
            return;
        }
        LogProperties.putProvisioningLogProperties(getAdminName(creds), null, contextId, objectId);
        switch (level) {
            case TRACE:
                logger.trace(message, args, e);
                break;
            case DEBUG:
                logger.debug(message, args, e);
                break;
            case INFO:
                logger.info(message, args, e);
                break;
            case WARNING:
                logger.warn(message, args, e);
                break;
            case ERROR:
                logger.error(message, args, e);
                break;
            default:
                LOGGER.error("Invalid log level {}", level.name());
                break;
        }
    }

    /**
     * Log a message with given log properties in MDC
     *
     * @param level The log level
     * @param logger The logger
     * @param creds The credentials to put in MDC
     * @param contextId The affected context's identifier to put in MDC
     * @param objectId The affected object's identifier to put in MDC
     * @param e The exception to log
     * @param message The message to log
     */
    public static void log(LogLevel level, org.slf4j.Logger logger, Credentials creds, String contextId, String objectId, Exception e, String message) {
        log(level, logger, creds, contextId, objectId, e, message, new Object[] {});
    }

    /**
     * Log a message with given log properties in MDC
     *
     * @param level The log level
     * @param logger The logger
     * @param creds The credentials to put in MDC
     * @param contextId The affected context's identifier to put in MDC
     * @param e The exception to log
     * @param message The message to log
     * @param args The arguments to log
     */
    public static void log(LogLevel level, org.slf4j.Logger logger, Credentials creds, String contextId, Exception e, String message, Object... args) {
        log(level, logger, creds, contextId, null, e, message, args);
    }

    /**
     * Log a message with given log properties in MDC
     *
     * @param level The log level
     * @param logger The logger
     * @param creds The credentials to put in MDC
     * @param contextId The affected context's identifier to put in MDC
     * @param e The exception to log
     * @param message The message to log
     */
    public static void log(LogLevel level, org.slf4j.Logger logger, Credentials creds, String contextId, Exception e, String message) {
        log(level, logger, creds, contextId, null, e, message, new Object[] {});
    }

    /**
     * Log a message with given log properties in MDC
     *
     * @param level The log level
     * @param logger The logger
     * @param creds The credentials to put in MDC
     * @param e The exception to log
     * @param message The message to log
     * @param args The arguments to log
     */
    public static void log(LogLevel level, org.slf4j.Logger logger, Credentials creds, Exception e, String message, Object... args) {
        log(level, logger, creds, null, null, e, message, args);
    }

    /**
     * Log a message with given log properties in MDC
     *
     * @param level The log level
     * @param logger The logger
     * @param creds The credentials to put in MDC
     * @param e The exception to log
     * @param message The message to log
     */
    public static void log(LogLevel level, org.slf4j.Logger logger, Credentials creds, Exception e, String message) {
        log(level, logger, creds, null, null, e, message, new Object[] {});
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final CachedConstructor NON_EXISTENT = new CachedConstructor(null);

    private static class CachedConstructor {

        final Constructor<?> constructor;

        /**
         * Initializes a new {@link CachedConstructor}.
         *
         * @param constructor The cached constructor
         */
        CachedConstructor(Constructor<?> constructor) {
            super();
            this.constructor = constructor;
        }
    }

    private static final Field FIELD_DETAIL_MESSAGE;

    static {
        Field detailMessageField = null;
        try {
            detailMessageField = Throwable.class.getDeclaredField("detailMessage");
            Reflections.makeModifiable(detailMessageField);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        FIELD_DETAIL_MESSAGE = detailMessageField;
    }

}
