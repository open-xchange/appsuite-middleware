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

package com.openexchange.groupware.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.configuration.SystemConfig;

/**
 * 
 * UpdateTaskCollection
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class UpdateTaskCollection {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(UpdateTaskCollection.class);

	private UpdateTaskCollection() {
		super();
	}

	private static final Lock VERSION_LOCK = new ReentrantLock();

	private static final AtomicInteger version = new AtomicInteger(-1);

	private static final String PROPERTYNAME = "UPDATETASKSCFG";

	private static final ArrayList<UpdateTask> updateTaskList = new ArrayList<UpdateTask>();

	static {
		final String propStr;
		if ((propStr = SystemConfig.getProperty(PROPERTYNAME)) == null) {
			LOG.error("Missing property 'UPDATETASKSCFG' in system.properties");
		} else {
			final File updateTasksFile = new File(propStr);
			if (!updateTasksFile.exists() || !updateTasksFile.isFile()) {
				LOG.error("Missing file " + propStr);
			} else {
				BufferedReader reader = null;
				try {
					final Class[] parameterTypes = new Class[] {};
					final Object[] initArgs = new Object[] {};
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(updateTasksFile)));
					String line = null;
					while ((line = reader.readLine()) != null) {
						final String l = line.trim();
						if (l.length() == 0 || l.charAt(0) == '#') {
							continue;
						}
						try {
							updateTaskList.add((UpdateTask) Class.forName(l).getConstructor(parameterTypes)
									.newInstance(initArgs));
						} catch (ClassNotFoundException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (IllegalArgumentException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (SecurityException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (InstantiationException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (IllegalAccessException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (InvocationTargetException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (NoSuchMethodException e) {
							LOG.error(e.getMessage(), e);
							continue;
						}
					}
				} catch (FileNotFoundException e) {
					LOG.error(e.getMessage(), e);
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							LOG.error(e.getMessage(), e);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a list of <code>UpdateTask</code> instances that apply to
	 * current database version
	 * 
	 * @param dbVersion -
	 *            current database version
	 * @return list of <code>UpdateTask</code> instances
	 */
	public static final List<UpdateTask> getFilteredAndSortedUpdateTasks(final int dbVersion) {
		final List<UpdateTask> retval = (List<UpdateTask>) updateTaskList.clone();
		/*
		 * Filter
		 */
		final int size = retval.size();
		final Iterator<UpdateTask> iter = retval.iterator();
		for (int i = 0; i < size; i++) {
			final UpdateTask ut = iter.next();
			if (ut.addedWithVersion() <= dbVersion) {
				iter.remove();
			}
		}
		/*
		 * Sort
		 */
		Collections.sort(retval, new UpdateTaskComparator());
		return retval;
	}

	/**
	 * Iterates all implementations of <code>UpdateTask</code> and determines
	 * the highest version number indicated through method
	 * <code>UpdateTask.addedWithVersion()</code>.
	 * 
	 * @return the highest version number
	 */
	public static final int getHighestVersion() {
		if (version.get() == -1) {
			VERSION_LOCK.lock();
			try {
				/*
				 * Check again
				 */
				if (version.get() == -1) {
					final int size = updateTaskList.size();
					final Iterator<UpdateTask> iter = updateTaskList.iterator();
					version.set(0);
					for (int i = 0; i < size; i++) {
						version.set(Math.max(version.get(), iter.next().addedWithVersion()));
					}
				}
			} finally {
				VERSION_LOCK.unlock();
			}
		}
		return version.get();
	}

	/**
	 * 
	 * UpdateTaskComparator - sorts instances of <code>UpdateTask</code> by
	 * their version in first order and by their priority in second order
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 * 
	 */
	private static final class UpdateTaskComparator implements Comparator<UpdateTask> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final UpdateTask o1, final UpdateTask o2) {
			if (o1.addedWithVersion() > o2.addedWithVersion()) {
				return 1;
			} else if (o1.addedWithVersion() < o2.addedWithVersion()) {
				return -1;
			} else if (o1.getPriority() > o2.getPriority()) {
				return 1;
			} else if (o1.getPriority() < o2.getPriority()) {
				return -1;
			}
			return 0;
		}

	}

}
