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

package com.openexchange.log;

import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.slf4j.spi.LocationAwareLogger;


/**
 * {@link Slf4jLogger} - java.util.logging.Logger implementation delegating to SLF4j.
 * <p>
 * Methods {@link java.util.logging.Logger#setParent(Logger)}, {@link java.util.logging.Logger#getParent()},
 * {@link java.util.logging.Logger#setUseParentHandlers(boolean)} and {@link java.util.logging.Logger#getUseParentHandlers()} are not
 * overridden.
 * <p>
 * Level mapping inspired by {@link org.slf4j.bridge.SLF4JBridgeHandler}:
 *
 * <pre>
 * FINEST  -&gt; TRACE
 * FINER   -&gt; TRACE
 * FINE    -&gt; DEBUG
 * CONFIG  -&gt; DEBUG
 * INFO    -&gt; INFO
 * WARN ING -&gt; WARN
 * SEVER   -&gt; ERROR
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Slf4jLogger extends AbstractDelegatingLogger {

    private static final String FQCN = Slf4jLogger.class.getName();

    private final org.slf4j.Logger logger;
    private final LocationAwareLogger locationAwareLogger;

    /**
     * Initializes a new {@link Slf4jLogger}.
     *
     * @param clazz The class
     */
    public Slf4jLogger(final Class<?> clazz) {
        super(clazz.getName(), null);
        logger = org.slf4j.LoggerFactory.getLogger(clazz);
        if (logger instanceof LocationAwareLogger) {
            locationAwareLogger = (LocationAwareLogger) logger;
        } else {
            locationAwareLogger = null;
        }
    }

    /**
     * Initializes a new {@link Slf4jLogger}.
     */
    public Slf4jLogger(final String name, final String resourceBundleName) {
        super(name, resourceBundleName);
        logger = org.slf4j.LoggerFactory.getLogger(name);
        if (logger instanceof LocationAwareLogger) {
            locationAwareLogger = (LocationAwareLogger) logger;
        } else {
            locationAwareLogger = null;
        }
    }

    @Override
    public void log(final Level level, final String msg) {
        if (Level.FINE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(msg);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, null);
            }
        } else if (Level.INFO.equals(level)) {
            if (locationAwareLogger == null) {
                logger.info(msg);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, msg, null, null);
            }
        } else if (Level.WARNING.equals(level)) {
            if (locationAwareLogger == null) {
                logger.warn(msg);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, null);
            }
        } else if (Level.FINER.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(msg);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, null);
            }
        } else if (Level.FINEST.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(msg);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, msg, null, null);
            }
        } else if (Level.ALL.equals(level)) {
            // should never occur, all is used to configure java.util.logging
            // but not accessible by the API Logger.xxx() API
            if (locationAwareLogger == null) {
                logger.error(msg);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, null);
            }
        } else if (Level.SEVERE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.error(msg);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, null);
            }
        } else if (Level.CONFIG.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(msg);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, null);
            }
        } else if (Level.OFF.equals(level)) {
            // don't log
        }
    }

    @Override
    public void log(final Level level, final String msg, final Object param1) {
        if (Level.FINE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, new Object[] {param1}, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, new Object[] {param1}, (ResourceBundle) null), null, null);
            }
        } else if (Level.INFO.equals(level)) {
            if (locationAwareLogger == null) {
                logger.info(formatMessage(msg, new Object[] {param1}, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, formatMessage(msg, new Object[] {param1}, (ResourceBundle) null), null, null);
            }
        } else if (Level.WARNING.equals(level)) {
            if (locationAwareLogger == null) {
                logger.warn(formatMessage(msg, new Object[] {param1}, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, formatMessage(msg, new Object[] {param1}, (ResourceBundle) null), null, null);
            }
        } else if (Level.FINER.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, new Object[] {param1}, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, new Object[] {param1}, (ResourceBundle) null), null, null);
            }
        } else if (Level.FINEST.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, new Object[] {param1}, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, formatMessage(msg, new Object[] {param1}, (ResourceBundle) null), null, null);
            }
        } else if (Level.ALL.equals(level)) {
            // should never occur, all is used to configure java.util.logging
            // but not accessible by the API Logger.xxx() API
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, new Object[] {param1}, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, new Object[] {param1}, (ResourceBundle) null), null, null);
            }
        } else if (Level.SEVERE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, new Object[] {param1}, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, new Object[] {param1}, (ResourceBundle) null), null, null);
            }
        } else if (Level.CONFIG.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, new Object[] {param1}, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, new Object[] {param1}, (ResourceBundle) null), null, null);
            }
        } else if (Level.OFF.equals(level)) {
            // don't log
        }
    }

    @Override
    public void log(final Level level, final String msg, final Object[] params) {
        if (Level.FINE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, params, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, params, (ResourceBundle) null), null, null);
            }
        } else if (Level.INFO.equals(level)) {
            if (locationAwareLogger == null) {
                logger.info(formatMessage(msg, params, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, formatMessage(msg, params, (ResourceBundle) null), null, null);
            }
        } else if (Level.WARNING.equals(level)) {
            if (locationAwareLogger == null) {
                logger.warn(formatMessage(msg, params, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, formatMessage(msg, params, (ResourceBundle) null), null, null);
            }
        } else if (Level.FINER.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, params, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, params, (ResourceBundle) null), null, null);
            }
        } else if (Level.FINEST.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, params, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, formatMessage(msg, params, (ResourceBundle) null), null, null);
            }
        } else if (Level.ALL.equals(level)) {
            // should never occur, all is used to configure java.util.logging
            // but not accessible by the API Logger.xxx() API
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, params, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, params, (ResourceBundle) null), null, null);
            }
        } else if (Level.SEVERE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, params, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, params, (ResourceBundle) null), null, null);
            }
        } else if (Level.CONFIG.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, params, (ResourceBundle) null));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, params, (ResourceBundle) null), null, null);
            }
        } else if (Level.OFF.equals(level)) {
            // don't log
        }
    }

    @Override
    public void log(final Level level, final String msg, final Throwable t) {
        if (Level.FINE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, t);
            }
        } else if (Level.INFO.equals(level)) {
            if (locationAwareLogger == null) {
                logger.info(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, msg, null, t);
            }
        } else if (Level.WARNING.equals(level)) {
            if (locationAwareLogger == null) {
                logger.warn(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, t);
            }
        } else if (Level.FINER.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, t);
            }
        } else if (Level.FINEST.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, msg, null, t);
            }
        } else if (Level.ALL.equals(level)) {
            // should never occur, all is used to configure java.util.logging
            // but not accessible by the API Logger.xxx() API
            if (locationAwareLogger == null) {
                logger.error(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, t);
            }
        } else if (Level.SEVERE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.error(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, t);
            }
        } else if (Level.CONFIG.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, t);
            }
        } else if (Level.OFF.equals(level)) {
            // don't log
        }
    }

    @Override
    public void info(final String msg) {
        if (locationAwareLogger == null) {
            logger.info(msg);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, msg, null, null);
        }
    }

    @Override
    public void fine(final String msg) {
        if (locationAwareLogger == null) {
            logger.debug(msg);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, null);
        }
    }

    @Override
    public void finer(final String msg) {
        if (locationAwareLogger == null) {
            logger.trace(msg);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, msg, null, null);
        }
    }

    @Override
    public void finest(final String msg) {
        if (locationAwareLogger == null) {
            logger.trace(msg);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, msg, null, null);
        }
    }

    @Override
    public void warning(final String msg) {
        if (locationAwareLogger == null) {
            logger.warn(msg);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, null);
        }
    }

    @Override
    public void severe(final String msg) {
        if (locationAwareLogger == null) {
            logger.error(msg);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, null);
        }
    }

    @Override
    public void config(final String msg) {
        if (locationAwareLogger == null) {
            logger.debug(msg);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, null);
        }
    }

    @Override
    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        if (locationAwareLogger == null) {
            logger.trace("", thrown);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, thrown.getMessage(), null, thrown);
        }
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg) {
        log(level, msg);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Object param1) {
        log(level, msg, param1);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Object[] params) {
        log(level, msg, params);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Throwable t) {
        log(level, msg, t);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg) {
        if (Level.FINE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, null, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, null, bundleName), null, null);
            }
        } else if (Level.INFO.equals(level)) {
            if (locationAwareLogger == null) {
                logger.info(formatMessage(msg, null, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, formatMessage(msg, null, bundleName), null, null);
            }
        } else if (Level.WARNING.equals(level)) {
            if (locationAwareLogger == null) {
                logger.warn(formatMessage(msg, null, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, formatMessage(msg, null, bundleName), null, null);
            }
        } else if (Level.FINER.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, null, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, null, bundleName), null, null);
            }
        } else if (Level.FINEST.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, null, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, formatMessage(msg, null, bundleName), null, null);
            }
        } else if (Level.ALL.equals(level)) {
            // should never occur, all is used to configure java.util.logging
            // but not accessible by the API Logger.xxx() API
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, null, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, null, bundleName), null, null);
            }
        } else if (Level.SEVERE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, null, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, null, bundleName), null, null);
            }
        } else if (Level.CONFIG.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, null, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, null, bundleName), null, null);
            }
        } else if (Level.OFF.equals(level)) {
            // don't log
        }
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg, final Object param1) {
        if (Level.FINE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, new Object[] { param1 }, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, new Object[] { param1 }, bundleName), null, null);
            }
        } else if (Level.INFO.equals(level)) {
            if (locationAwareLogger == null) {
                logger.info(formatMessage(msg, new Object[] { param1 }, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, formatMessage(msg, new Object[] { param1 }, bundleName), null, null);
            }
        } else if (Level.WARNING.equals(level)) {
            if (locationAwareLogger == null) {
                logger.warn(formatMessage(msg, new Object[] { param1 }, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, formatMessage(msg, new Object[] { param1 }, bundleName), null, null);
            }
        } else if (Level.FINER.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, new Object[] { param1 }, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, new Object[] { param1 }, bundleName), null, null);
            }
        } else if (Level.FINEST.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, new Object[] { param1 }, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, formatMessage(msg, new Object[] { param1 }, bundleName), null, null);
            }
        } else if (Level.ALL.equals(level)) {
            // should never occur, all is used to configure java.util.logging
            // but not accessible by the API Logger.xxx() API
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, new Object[] { param1 }, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, new Object[] { param1 }, bundleName), null, null);
            }
        } else if (Level.SEVERE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, new Object[] { param1 }, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, new Object[] { param1 }, bundleName), null, null);
            }
        } else if (Level.CONFIG.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, new Object[] { param1 }, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, new Object[] { param1 }, bundleName), null, null);
            }
        } else if (Level.OFF.equals(level)) {
            // don't log
        }
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg, final Object[] params) {
        if (Level.FINE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, params, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, params, bundleName), null, null);
            }
        } else if (Level.INFO.equals(level)) {
            if (locationAwareLogger == null) {
                logger.info(formatMessage(msg, params, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, formatMessage(msg, params, bundleName), null, null);
            }
        } else if (Level.WARNING.equals(level)) {
            if (locationAwareLogger == null) {
                logger.warn(formatMessage(msg, params, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, formatMessage(msg, params, bundleName), null, null);
            }
        } else if (Level.FINER.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, params, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, params, bundleName), null, null);
            }
        } else if (Level.FINEST.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, params, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, formatMessage(msg, params, bundleName), null, null);
            }
        } else if (Level.ALL.equals(level)) {
            // should never occur, all is used to configure java.util.logging
            // but not accessible by the API Logger.xxx() API
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, params, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, params, bundleName), null, null);
            }
        } else if (Level.SEVERE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, params, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, params, bundleName), null, null);
            }
        } else if (Level.CONFIG.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, params, bundleName));
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, params, bundleName), null, null);
            }
        } else if (Level.OFF.equals(level)) {
            // don't log
        }
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg, final Throwable t) {
        if (Level.FINE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, null, bundleName), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, null, bundleName), null, t);
            }
        } else if (Level.INFO.equals(level)) {
            if (locationAwareLogger == null) {
                logger.info(formatMessage(msg, null, bundleName), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, formatMessage(msg, null, bundleName), null, t);
            }
        } else if (Level.WARNING.equals(level)) {
            if (locationAwareLogger == null) {
                logger.warn(formatMessage(msg, null, bundleName), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, formatMessage(msg, null, bundleName), null, t);
            }
        } else if (Level.FINER.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, null, bundleName), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, null, bundleName), null, t);
            }
        } else if (Level.FINEST.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, null, bundleName), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, formatMessage(msg, null, bundleName), null, t);
            }
        } else if (Level.ALL.equals(level)) {
            // should never occur, all is used to configure java.util.logging
            // but not accessible by the API Logger.xxx() API
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, null, bundleName), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, null, bundleName), null, t);
            }
        } else if (Level.SEVERE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, null, bundleName), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, null, bundleName), null, t);
            }
        } else if (Level.CONFIG.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, null, bundleName), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, null, bundleName), null, t);
            }
        } else if (Level.OFF.equals(level)) {
            // don't log
        }
    }

    @Override
    public void log(final LogRecord record) {
        final Level level = record.getLevel();
        final String msg = record.getMessage();
        final Throwable t = record.getThrown();
        final Object[] params = record.getParameters();
        final ResourceBundle resourceBundle = record.getResourceBundle();

        if (Level.FINE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, params, resourceBundle), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, params, resourceBundle), null, t);
            }
        } else if (Level.INFO.equals(level)) {
            if (locationAwareLogger == null) {
                logger.info(formatMessage(msg, params, resourceBundle), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, formatMessage(msg, params, resourceBundle), null, t);
            }
        } else if (Level.WARNING.equals(level)) {
            if (locationAwareLogger == null) {
                logger.warn(formatMessage(msg, params, resourceBundle), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, formatMessage(msg, params, resourceBundle), null, t);
            }
        } else if (Level.FINER.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, params, resourceBundle), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, params, resourceBundle), null, t);
            }
        } else if (Level.FINEST.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(formatMessage(msg, params, resourceBundle), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, formatMessage(msg, params, resourceBundle), null, t);
            }
        } else if (Level.ALL.equals(level)) {
            // should never occur, all is used to configure java.util.logging
            // but not accessible by the API Logger.xxx() API
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, params, resourceBundle), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, params, resourceBundle), null, t);
            }
        } else if (Level.SEVERE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.error(formatMessage(msg, params, resourceBundle), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, formatMessage(msg, params, resourceBundle), null, t);
            }
        } else if (Level.CONFIG.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(formatMessage(msg, params, resourceBundle), t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, formatMessage(msg, params, resourceBundle), null, t);
            }
        } else if (Level.OFF.equals(level)) {
            // don't log
        }
    }

    @Override
    protected boolean supportsHandlers() {
        return true;
    }

    @Override
    public Level getLevel() {
        Level level;
        // Verify from the wider (trace) to the narrower (error)
        if (logger.isTraceEnabled()) {
            level = Level.FINEST;
        } else if (logger.isDebugEnabled()) {
            // map to the lowest between FINER, FINE and CONFIG
            level = Level.FINER;
        } else if (logger.isInfoEnabled()) {
            level = Level.INFO;
        } else if (logger.isWarnEnabled()) {
            level = Level.WARNING;
        } else if (logger.isErrorEnabled()) {
            level = Level.SEVERE;
        } else {
            level = Level.OFF;
        }
        return level;
    }

    @Override
    public boolean isLoggable(final Level level) {
        final int i = level.intValue();
        if (i == Level.OFF.intValue()) {
            return false;
        } else if (i >= Level.SEVERE.intValue()) {
            return logger.isErrorEnabled();
        } else if (i >= Level.WARNING.intValue()) {
            return logger.isWarnEnabled();
        } else if (i >= Level.INFO.intValue()) {
            return logger.isInfoEnabled();
        } else if (i >= Level.FINER.intValue()) {
            return logger.isDebugEnabled();
        }
        return logger.isTraceEnabled();
    }


    @Override
    protected void internalLogFormatted(final String msg, final LogRecord record) {
        final Level level = record.getLevel();
        final Throwable t = record.getThrown();

        final Handler targets[] = getHandlers();
        if (targets != null) {
            for (final Handler h : targets) {
                h.publish(record);
            }
        }
        if (!getUseParentHandlers()) {
            return;
        }

        /*
         * As we can not use a "switch ... case" block but only a "if ... else if ..." block, the order of the
         * comparisons is important. We first try log level FINE then INFO, WARN, FINER, etc
         */
        if (Level.FINE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, t);
            }
        } else if (Level.INFO.equals(level)) {
            if (locationAwareLogger == null) {
                logger.info(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, msg, null, t);
            }
        } else if (Level.WARNING.equals(level)) {
            if (locationAwareLogger == null) {
                logger.warn(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, t);
            }
        } else if (Level.FINER.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, t);
            }
        } else if (Level.FINEST.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, msg, null, t);
            }
        } else if (Level.ALL.equals(level)) {
            // should never occur, all is used to configure java.util.logging
            // but not accessible by the API Logger.xxx() API
            if (locationAwareLogger == null) {
                logger.error(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, t);
            }
        } else if (Level.SEVERE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.error(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, t);
            }
        } else if (Level.CONFIG.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, t);
            }
        } else if (Level.OFF.equals(level)) {
            // don't log
        }
    }

}
