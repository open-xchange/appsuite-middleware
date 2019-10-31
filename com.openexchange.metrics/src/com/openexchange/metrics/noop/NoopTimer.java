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

package com.openexchange.metrics.noop;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import com.openexchange.metrics.types.Timeable;
import com.openexchange.metrics.types.Timer;


/**
 * {@link NoopTimer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class NoopTimer implements Timer {

    private static final NoopTimer INSTANCE = new NoopTimer();

    private NoopTimer() {
        super();
    }

    public static NoopTimer getInstance() {
        return INSTANCE;
    }

    @Override
    public void update(long duration, TimeUnit timeUnit) {

    }

    @Override
    public <T> T time(Callable<T> event) throws Exception {
        return event.call();
    }

    @Override
    public <T, E extends Exception> T time(Timeable<T, E> timeable) throws E {
        return timeable.call();
    }

    @Override
    public <T> T timeSupplier(Supplier<T> event) {
        return event.get();
    }

    @Override
    public void time(Runnable event) {
        event.run();
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public double getFifteenMinuteRate() {
        return 0;
    }

    @Override
    public double getFiveMinuteRate() {
        return 0;
    }

    @Override
    public double getMeanRate() {
        return 0;
    }

    @Override
    public double getOneMinuteRate() {
        return 0;
    }

}
