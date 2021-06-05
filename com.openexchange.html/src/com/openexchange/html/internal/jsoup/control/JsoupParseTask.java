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

package com.openexchange.html.internal.jsoup.control;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.html.internal.jsoup.JsoupHandler;
import com.openexchange.html.internal.jsoup.JsoupParser;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link JsoupParseTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class JsoupParseTask extends AbstractTask<Void> implements Delayed {

    /** The special poison task to stop taking from queue */
    public static final JsoupParseTask POISON = new JsoupParseTask(null, null, 0, false, null) {

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
    private final JsoupHandler handler;
    private final JsoupParser parser;
    private final int timeoutSec;
    private final boolean prettyPrint;
    private volatile long stamp;
    private volatile Thread worker;

    /**
     * Initializes a new {@link JsoupParseTask}.
     *
     * @param html The HTML content to parse
     * @param handler The handler to call-back
     * @param checkSize Whether to check the size of the HTML content
     * @param timeoutSec The timeout seconds
     * @param prettyPrint Whether resulting HTML content is supposed to be pretty-printed
     * @param parser The parser instance
     */
    public JsoupParseTask(String html, JsoupHandler handler, int timeoutSec, boolean prettyPrint, JsoupParser parser) {
        super();
        this.html = html;
        this.handler = handler;
        this.timeoutSec = timeoutSec;
        this.prettyPrint = prettyPrint;
        this.parser = parser;
    }

    @Override
    public int compareTo(Delayed o) {
        long thisStamp = this.stamp;
        long otherStamp = ((JsoupParseTask) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long toGo = stamp - System.currentTimeMillis();
        return unit.convert(toGo, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setThreadName(ThreadRenamer threadRenamer) {
        threadRenamer.renamePrefix("JsoupParser");
    }

    @Override
    public Void call() throws OXException {
        JsoupParseControl control = JsoupParseControl.getInstance();
        stamp = System.currentTimeMillis() + (timeoutSec * 1000L);
        worker = Thread.currentThread();
        control.add(this);
        try {
            parser.doParse(html, handler, prettyPrint);
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
