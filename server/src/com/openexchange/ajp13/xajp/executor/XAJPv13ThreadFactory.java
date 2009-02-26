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

package com.openexchange.ajp13.xajp.executor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link XAJPv13ThreadFactory} - A thread factory for AJP threads taking a custom name prefix for created threads.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class XAJPv13ThreadFactory implements java.util.concurrent.ThreadFactory {

    /**
     * Appendix length: <name-prefix> + "0000001"
     */
    private static final int APPENDIX_LENGTH = 7;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final String namePrefix;

    private final int len;

    /**
     * Initializes a new {@link XAJPv13ThreadFactory}.
     */
    public XAJPv13ThreadFactory(final String namePrefix) {
        super();
        this.namePrefix = namePrefix;
        len = namePrefix.length() + APPENDIX_LENGTH;
    }

    public Thread newThread(final Runnable r) {
        final Thread t = new Thread(r, getThreadName(threadNumber.getAndIncrement(), new StringBuilder(len).append(namePrefix)));
        t.setUncaughtExceptionHandler(new XAJPv13UncaughtExceptionhandler());
        return t;
    }

    private static String getThreadName(final int threadNumber, final StringBuilder sb) {
        for (int i = threadNumber; i < 1000000; i *= 10) {
            sb.append('0');
        }
        return sb.append(threadNumber).toString();
    }

}
