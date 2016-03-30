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

package com.openexchange.threadpool;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * {@link CompletionFuture} - Consumers <tt>take</tt> completed tasks and process their results in the order they complete. A
 * <tt>CompletionFuture</tt> can for example be used to manage asynchronous IO, in which tasks that perform reads are submitted in one part
 * of a program or system, and then acted upon in a different part of the program when the reads complete, possibly in a different order
 * than they were requested..
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CompletionFuture<V> {

    /**
     * Retrieves and removes the Future representing the next completed task, waiting if none are yet present.
     *
     * @return The Future representing the next completed task
     * @throws InterruptedException If interrupted while waiting.
     */
    Future<V> take() throws InterruptedException;

    /**
     * Retrieves and removes the Future representing the next completed task or <tt>null</tt> if none are present.
     *
     * @return The Future representing the next completed task, or <tt>null</tt> if none are present.
     */
    Future<V> poll();

    /**
     * Retrieves and removes the Future representing the next completed task, waiting if necessary up to the specified wait time if none are
     * yet present.
     *
     * @param timeout How long to wait before giving up, in units of <tt>unit</tt>
     * @param unit A <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter
     * @return The Future representing the next completed task or <tt>null</tt> if the specified waiting time elapses before one is present.
     * @throws InterruptedException If interrupted while waiting.
     */
    Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;

}
