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
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.NameAndIdObject;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;
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
import com.openexchange.log.LogProperties;

/**
 * General abstraction class used by all impl classes
 *
 * @author d7
 */
public abstract class OXCommonImpl {

    private final static int EXCEPTION_ID = new SecureRandom().nextInt();
    private final static AtomicInteger COUNTER = new AtomicInteger(0);

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
        if (null != credentials) {
            return credentials.getLogin();
        }
        return null;
    }

    /**
     * Add exception identfier to exception message, helping to trace errors across client and server
     *
     * @param e The exception whose message should be enhanced
     * @param exceptionId The exception identifier
     * @return The exception message plus exception identifier
     */
    public static String enhanceExceptionMessage(Exception e, String exceptionId) {
        StringBuilder sb = new StringBuilder(e.getMessage()).append("; exceptionId ").append(exceptionId);
        return sb.toString();
    }

    /**
     * Adds the exception identifier to exception message
     *
     * @param <T> The exception type
     * @param e The exception to enhance
     * @param exceptionId The exception identifier
     * @return The enhanced exception
     */
    private static <T extends Exception> Exception enhanceException(T e, String exceptionId) {
        if (null == e) {
            return null;
        }
        String message = enhanceExceptionMessage(e, exceptionId);
        Constructor<T> constructor;
        try {
            constructor = (Constructor<T>) e.getClass().getConstructor(String.class, Throwable.class);
            T result = constructor.newInstance(message, e.getCause());
            result.setStackTrace(e.getStackTrace());
            return result;
        } catch (NoSuchMethodException x) {
            // Maybe OXResellerException, wrap in RemoteExeption
            LOGGER.debug("", x);
            return new RemoteException(message, e);
        } catch (Exception x) {
            LOGGER.error("", x);
            return null;
        }
    }

    /**
     * Logs provisioning exception with exception identifier in MDC, enhances exception message by identifier and returns exception with enhanced message
     *
     * @param logger The logger to log the exception
     * @param e The exception to log and enhance
     * @param creds The credentials used for this call
     * @param contextId The affected context identifier
     * @param objectId The affected object identifier in context
     * @return The enhanced exception
     */
    protected static Exception logAndEnhanceException(org.slf4j.Logger logger, Exception e, Credentials creds, String contextId, String objectId) {
        String exceptionId = new StringBuilder().append(EXCEPTION_ID).append("-").append(COUNTER.incrementAndGet()).toString();
        LogProperties.putProvisioningLogProperties(getAdminName(creds), exceptionId, contextId, objectId);
        logger.error("", e);
        LogProperties.removeProvisioningLogProperties();
        return enhanceException(e, exceptionId);
    }

    /**
     * Logs provisioning exception with exception identifier in MDC, enhances exception message by identifier and returns exception with enhanced message
     *
     * @param logger The logger to log the exception
     * @param e The exception to log and enhance
     * @param creds The credentials used for this call
     * @param contextId The affected context identifier
     * @return The enhanced exception
     */
    protected static Exception logAndEnhanceException(org.slf4j.Logger logger, Exception e, Credentials creds, String contextId) {
        return logAndEnhanceException(logger, e, creds, contextId, null);
    }

    /**
     * Logs provisioning exception with exception identifier in MDC, enhances exception message by identifier and returns exception with enhanced message
     *
     * @param logger The logger to log the exception
     * @param e The exception to log and enhance
     * @param creds The credentials used for this call
     * @return The enhanced exception
     */
    protected Exception logAndEnhanceException(org.slf4j.Logger logger, Exception e, Credentials creds) {
        return logAndEnhanceException(logger, e, creds, null, null);
    }

    /**
     * Creates a comma-separated list of object identifiers 
     *
     * @param objects The array of object
     * @return The identifiers as comma-separated list
     */
    protected String getObjectIds(NameAndIdObject[] objects) {
        StringBuilder sb = new StringBuilder();
        if (null != objects && 0 < objects.length) {
            for (NameAndIdObject object : objects) {
                sb.append(object.getId()).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
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

}
