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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.internal.throttle;

import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.internal.DriveServiceLookup;
import com.openexchange.exception.OXException;

/**
 * {@link ThrottlingController}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ThrottlingController {

    private static volatile Integer maxConcurrentFileTransfers;
    private static int maxConcurrentFileTransfers() {
        Integer tmp = maxConcurrentFileTransfers;
        if (null == tmp) {
            synchronized (ThrottlingController.class) {
                tmp = maxConcurrentFileTransfers;
                if (null == tmp) {
                    int defaultValue = -1;
                    ConfigurationService configService = DriveServiceLookup.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = configService.getIntProperty("com.openexchange.drive.maxConcurrentFileTransfers", defaultValue);
                    maxConcurrentFileTransfers = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile Integer maxConcurrentSyncOperations;
    private static int maxConcurrentSyncOperations() {
        Integer tmp = maxConcurrentSyncOperations;
        if (null == tmp) {
            synchronized (ThrottlingController.class) {
                tmp = maxConcurrentSyncOperations;
                if (null == tmp) {
                    int defaultValue = -1;
                    ConfigurationService configService = DriveServiceLookup.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = configService.getIntProperty("com.openexchange.drive.maxConcurrentSyncOperations", defaultValue);
                    maxConcurrentSyncOperations = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static AtomicInteger fileTransfers = new AtomicInteger();
    private static AtomicInteger syncOperations = new AtomicInteger();

    public static int getCurrentFileTransfers() {
        return fileTransfers.get();
    }

    public static void enterFileTransfer() throws OXException {
        int max = maxConcurrentFileTransfers();
        if (0 < max && max < fileTransfers.incrementAndGet()) {
            throw DriveExceptionCodes.SERVER_BUSY.create();
        }
    }

    public static void leaveFileTransfer() {
        fileTransfers.decrementAndGet();
    }

    public static int getCurrentSyncOperations() {
        return syncOperations.get();
    }

    public static void enterSyncOperation() throws OXException {
        int max = maxConcurrentSyncOperations();
        if (0 < max && max < syncOperations.incrementAndGet()) {
            throw DriveExceptionCodes.SERVER_BUSY.create();
        }
    }

    public static void leaveSyncOperation() {
        syncOperations.decrementAndGet();
    }

    /**
     * Initializes a new {@link ThrottlingController}.
     */
    private ThrottlingController() {
        super();
    }

}
