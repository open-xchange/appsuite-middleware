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

package com.openexchange.ajp13;

/**
 * {@link AJPv13ListenerThread} - A subclass of {@link Thread} enhanced with an
 * additional flag to indicate <i>dead</i> status. This flag is checked inside
 * AJP listener to prevent this thread from further running.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13ListenerThread extends Thread {

	private boolean dead;

	/**
	 * AJPv13ListenerThread
	 */
	public AJPv13ListenerThread() {
		super();
	}

	/**
	 * Constructor using field <code>target</code>
	 */
	public AJPv13ListenerThread(final Runnable target) {
		super(target);
	}

	/**
	 * Constructor using fields <code>group</code> and <code>target</code>
	 */
	public AJPv13ListenerThread(final ThreadGroup group, final Runnable target) {
		super(group, target);
	}

	/**
	 * Constructor using field <code>name</code>
	 */
	public AJPv13ListenerThread(final String name) {
		super(name);
	}

	/**
	 * Constructor using fields <code>group</code> and <code>name</code>
	 */
	public AJPv13ListenerThread(final ThreadGroup group, final String name) {
		super(group, name);
	}

	/**
	 * Constructor using fields <code>target</code> and <code>name</code>
	 */
	public AJPv13ListenerThread(final Runnable target, final String name) {
		super(target, name);
	}

	/**
	 * Constructor using fields <code>group</code>, <code>target</code> and
	 * <code>name</code>
	 */
	public AJPv13ListenerThread(final ThreadGroup group, final Runnable target, final String name) {
		super(group, target, name);
	}

	/**
	 * Constructor using fields <code>group</code>, <code>target</code>,
	 * <code>name</code> and <code>stackSize</code>
	 */
	public AJPv13ListenerThread(final ThreadGroup group, final Runnable target, final String name, final long stackSize) {
		super(group, target, name, stackSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#interrupt()
	 */
	@Override
	public void interrupt() {
		dead = true;
		super.interrupt();
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(final boolean dead) {
		this.dead = dead;
	}
}
