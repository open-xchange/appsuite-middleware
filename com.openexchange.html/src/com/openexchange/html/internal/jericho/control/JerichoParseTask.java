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

package com.openexchange.html.internal.jericho.control;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
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
    public Void call() throws OXException {
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
