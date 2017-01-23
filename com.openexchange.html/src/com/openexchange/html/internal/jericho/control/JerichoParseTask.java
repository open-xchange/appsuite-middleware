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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.html.internal.jericho.control;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.html.internal.jericho.JerichoHandler;
import com.openexchange.html.internal.jericho.JerichoParser;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link JerichoParseTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class JerichoParseTask extends AbstractTask<Void> implements Delayed {

    /** The special poison task to stop taking from queue */
    public static final JerichoParseTask POISON = new JerichoParseTask(null, null, false, 0, null) {

        @Override
        public int compareTo(Delayed o) {
            return -1;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0L;
        }
    };

    // ---------------------------------------------------------------------------------------------------------------------------

    private final String html;
    private final JerichoHandler handler;
    private final boolean checkSize;
    private final JerichoParser parser;
    private final int timeoutSec;
    private volatile long stamp;
    private volatile Thread worker;

    /**
     * Initializes a new {@link JerichoParseTask}.
     *
     * @param html The HTML content to parse
     * @param handler The handler to call-back
     * @param checkSize Whether to check the size of the HTML content
     * @param timeoutSec The timeout seconds
     * @param parser The parser instance
     */
    public JerichoParseTask(String html, JerichoHandler handler, boolean checkSize, int timeoutSec, JerichoParser parser) {
        super();
        this.html = html;
        this.handler = handler;
        this.checkSize = checkSize;
        this.timeoutSec = timeoutSec;
        this.parser = parser;
    }

    @Override
    public int compareTo(Delayed o) {
        long thisStamp = this.stamp;
        long otherStamp = ((JerichoParseTask) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long toGo = stamp - System.currentTimeMillis();
        return unit.convert(toGo, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setThreadName(ThreadRenamer threadRenamer) {
        threadRenamer.renamePrefix("JerichoParser");
    }

    @Override
    public Void call() {
        JerichoParseControl control = JerichoParseControl.getInstance();
        stamp = System.currentTimeMillis() + (timeoutSec * 1000L);
        worker = Thread.currentThread();
        control.add(this);
        try {
            parser.doParse(html, handler, checkSize);
            return null;
        } finally {
            control.remove(this);
        }
    }

    /**
     * Interrupts this task (if currently processed).
     */
    public void interrupt() {
        Thread worker = this.worker;
        if (null != worker) {
            worker.interrupt();
        }
    }

}
