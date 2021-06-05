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

package com.openexchange.admin.rmi.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import com.google.common.collect.ImmutableMap;
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
import com.openexchange.admin.rmi.exceptions.RemoteExceptionUtils;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.PermissionConfigurationChecker;
import com.openexchange.log.LogProperties;

/**
 * {@link OXCommonImpl} - General abstraction class used by all impl classes
 *
 * @author d7
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class OXCommonImpl {

    private static final String CONFIG_NAMESPACE = "config";

    private final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXCommonImpl.class);

    protected final OXToolStorageInterface tool;

    private static final Map<Class<?>, ExceptionHandler> exceptionHandlers;
    static {
        Map<Class<?>, ExceptionHandler> eh = new HashMap<>();
        eh.put(AbstractAdminRmiException.class, (t, credentials) -> logAndReturnException(LOGGER, ((AbstractAdminRmiException) t), credentials));
        eh.put(RemoteException.class, (t, credentials) -> enhanceException(credentials, (RemoteException) t));
        eh.put(Exception.class, (t, credentials) -> enhanceException(credentials, RemoteExceptionUtils.convertException((Exception) t)));
        exceptionHandlers = ImmutableMap.copyOf(eh);
    }

    /**
     * Initializes a new {@link OXCommonImpl}.
     * 
     * @throws StorageException In case the PermissionConfigurationChecker service is unavailable
     */
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

    /**
     * Checks if the given user has attributes which contain illegal properties
     *
     * @param user The user to check
     * @throws InvalidDataException in case the given user has Attributes which contain invalid properties
     */
    protected void checkUserAttributes(final User user) throws InvalidDataException {
        if (user != null && user.getUserAttributes() != null) {
            checkUserAttributes(user.getUserAttributes());
        }
    }

    /**
     * Checks if the capabilities contain illegal capabilities
     *
     * @param capsToAdd The capabilities to check
     * @param capsToRemove The capabilities to check
     * @throws InvalidDataException in case an illegal capability has been found
     */
    protected void checkCapabilities(final Optional<Set<String>> capsToAdd, final Optional<Set<String>> capsToRemove) throws InvalidDataException {
        try {
            Set<String> capsToCheck;
            if (capsToAdd.isPresent()) {
                if (capsToRemove.isPresent()) {
                    capsToCheck = Stream.concat(capsToAdd.get().stream(), capsToRemove.get().stream()).collect(Collectors.toSet());
                } else {
                    capsToCheck = capsToAdd.get();
                }
            } else {
                if (capsToRemove.isPresent() == false) {
                    // Nothing to do
                    return;
                }
                capsToCheck = capsToRemove.get();
            }
            AdminServiceRegistry.getInstance().getService(PermissionConfigurationChecker.class, true).checkCapabilities(capsToCheck);
        } catch (OXException e) {
            throw new InvalidDataException(e);
        }
    }

    /**
     * Checks if the given user attributes contain any illegal properties
     *
     * @param attributes The user attributes to check
     * @throws InvalidDataException in case the given user Attributes contain invalid properties
     */
    protected void checkUserAttributes(final Map<String, Map<String, String>> attributes) throws InvalidDataException {
        if (attributes != null) {
            Map<String, String> map = attributes.get(CONFIG_NAMESPACE);
            if (map != null && map.isEmpty() == false) {
                try {
                    AdminServiceRegistry.getInstance().getService(PermissionConfigurationChecker.class, true).checkAttributes(map);
                } catch (OXException e) {
                    throw new InvalidDataException(e);
                }
            }
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
     * Performs a null check and throws an exception if any of the passed objects is <code>null</code>
     *
     * @param objects The objects to perform the null check
     * @throws InvalidDataException if any of the passed objects is <code>null</code>
     */
    protected static void doNullCheck(Object... objects) throws InvalidDataException {
        for (Object object : objects) {
            if (object == null) {
                throw new InvalidDataException();
            }
        }
    }

    /**
     * Performs a null check and logs any exceptions before throwing
     *
     * @param logger The logger to log the exceptions
     * @param credentials The credentials
     * @param objects The objects to perform the null check
     * @throws InvalidDataException if any of the passed objects is <code>null</code>
     */
    protected static void doNullCheck(Logger logger, Credentials credentials, Object... objects) throws InvalidDataException {
        try {
            doNullCheck(objects);
        } catch (InvalidDataException e) {
            log(LogLevel.ERROR, logger, credentials, e, "Invalid data sent by client!");
            throw e;
        }
    }

    /**
     * Checks whether the context exists and updates the schema if needed
     * 
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
        return logAndReturnException(logger, e, e.getExceptionId(), creds, contextId, objectId);
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
        return logAndReturnException(logger, e, e.getExceptionId(), creds, contextId, null);
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
        return logAndReturnException(logger, e, e.getExceptionId(), creds, null, null);
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

    /**
     * Enhances and logs the specified {@link Throwable}
     *
     * @param t the {@link Throwable} to enhance and log
     * @param credentials The credentials
     */
    protected static void enhanceAndLogException(Throwable t, Credentials credentials) {
        ExceptionHandler exceptionHandler = exceptionHandlers.get(t.getClass().getSuperclass());
        if (exceptionHandler == null) {
            LOGGER.error("", t);
            return;
        }
        exceptionHandler.handle(t, credentials);
    }

    /**
     * Enhances the specified remote exception with the admin name and logs it
     *
     * @param credentials The credentials
     * @param remoteException The remote exception to enhance
     */
    private static void enhanceException(Credentials credentials, RemoteException remoteException) {
        String exceptionId = AbstractAdminRmiException.generateExceptionId();
        RemoteExceptionUtils.enhanceRemoteException(remoteException, exceptionId);
        logAndReturnException(LOGGER, remoteException, exceptionId, credentials);
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

    /**
     * {@link ExceptionHandler}
     */
    @FunctionalInterface
    private interface ExceptionHandler {

        /**
         * Handles the specified {@link Throwable}
         *
         * @param t The {@link Throwable} to handle
         * @param credentials The credentials to optionally decorate the exception with the admin name
         */
        void handle(Throwable t, Credentials credentials);
    }
}
