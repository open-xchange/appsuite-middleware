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
import java.util.concurrent.atomic.AtomicBoolean;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.Initialization;

/**
 * {@link UpdateTaskCollectionInit}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class UpdateTaskCollectionInit implements Initialization {

	private static final UpdateTaskCollectionInit instance = new UpdateTaskCollectionInit();

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(UpdateTaskCollectionInit.class);

	private static final String PROPERTYNAME = "UPDATETASKSCFG";

	public static UpdateTaskCollectionInit getInstance() {
		return instance;
	}

	private final AtomicBoolean started = new AtomicBoolean();

	private UpdateTaskCollectionInit() {
		super();
	}

	private void init() {
		final String propStr;
		if ((propStr = SystemConfig.getProperty(PROPERTYNAME)) == null) {
			LOG.error("Missing property 'UPDATETASKSCFG' in system.properties");
		} else {
			final File updateTasksFile = new File(propStr);
			if (!updateTasksFile.exists() || !updateTasksFile.isFile()) {
				LOG.error("Missing file " + propStr);
			} else {
				final ArrayList<UpdateTask> updateTaskList = new ArrayList<UpdateTask>();
				BufferedReader reader = null;
				try {
					final Class[] parameterTypes = new Class[] {};
					final Object[] initArgs = new Object[] {};
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(updateTasksFile)));
					String line = null;
					while ((line = reader.readLine()) != null) {
						final String l = line.trim();
						if ((l.length() == 0) || (l.charAt(0) == '#')) {
							continue;
						}
						try {
							updateTaskList.add((UpdateTask) Class.forName(l).getConstructor(parameterTypes)
									.newInstance(initArgs));
						} catch (final ClassNotFoundException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (final IllegalArgumentException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (final SecurityException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (final InstantiationException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (final IllegalAccessException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (final InvocationTargetException e) {
							LOG.error(e.getMessage(), e);
							continue;
						} catch (final NoSuchMethodException e) {
							LOG.error(e.getMessage(), e);
							continue;
						}
					}
					UpdateTaskCollection.setUpdateTaskList(updateTaskList);
				} catch (final FileNotFoundException e) {
					LOG.error(e.getMessage(), e);
				} catch (final IOException e) {
					LOG.error(e.getMessage(), e);
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (final IOException e) {
							LOG.error(e.getMessage(), e);
						}
					}
				}
			}
		}
	}

	private void reset() {
		UpdateTaskCollection.setUpdateTaskList(null);
	}

	public void start() throws AbstractOXException {
		if (started.get()) {
			LOG.error("UpdateTaskCollection has already been started", new Throwable());
		}
		init();
		started.set(true);
		if (LOG.isInfoEnabled()) {
			LOG.info("UpdateTaskCollection successfully started");
		}
	}

	public void stop() throws AbstractOXException {
		if (!started.get()) {
			LOG.error("UpdateTaskCollection cannot be stopped since it has not been started before", new Throwable());
		}
		reset();
		started.set(false);
		if (LOG.isInfoEnabled()) {
			LOG.info("UpdateTaskCollection successfully stopped");
		}
	}
}
