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

package com.openexchange.log;

import org.apache.commons.logging.Log;
import com.openexchange.log.Loggable.Level;

/**
 * {@link LogService} - The logging service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface LogService {

    /**
     * Logs specified {@link Loggable} instance.
     *
     * @param loggable The loggable to log
     */
    public void log(Loggable loggable);

    /**
     * Logs specified {@link Throwable} instance.
     *
     * @param level The log level
     * @param log The logger
     * @param throwable The throwable to log
     */
    public void log(Level level, Log log, Throwable throwable);

    /**
     * Creates a new {@link Loggable} with specified arguments.
     *
     * @param level The log level
     * @param log The log
     * @param message The message
     * @param throwable The optional throwable
     * @return The newly created {@link Loggable}
     */
    public Loggable loggableFor(Level level, Log log, Object message, Throwable throwable);

    /**
     * Creates a new {@link Loggable} with specified arguments.
     *
     * @param level The log level
     * @param log The log
     * @param message The message
     * @return The newly created {@link Loggable}
     */
    public Loggable loggableFor(Level level, Log log, Object message);

}
