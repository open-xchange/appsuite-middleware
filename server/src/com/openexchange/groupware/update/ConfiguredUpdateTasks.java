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

import static com.openexchange.tools.io.IOUtils.closeReaderStuff;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.tools.encoding.Charsets;

/**
 * This class contains the list of configured update tasks. The configuration can be done by the configuration file updatetasks.cfg.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConfiguredUpdateTasks {

    private static final ConfiguredUpdateTasks SINGLETON = new ConfiguredUpdateTasks();

    private static final Log LOG = LogFactory.getLog(ConfiguredUpdateTasks.class);

    private static final String PROPERTYNAME = "UPDATETASKSCFG";

    private boolean configured = false;

    private List<UpdateTask> taskList = new ArrayList<UpdateTask>();

    private ConfiguredUpdateTasks() {
        super();
    }

    public static ConfiguredUpdateTasks getInstance() {
        return SINGLETON;
    }

    public void loadConfiguration(ConfigurationService configService) {
        File updateTasks = getUpdateTaskFile(configService);
        if (null == updateTasks) {
            configured = false;
            return;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(updateTasks), Charsets.UTF_8));
            String line = null;
            while ((line = reader.readLine()) != null) {
                final String l = line.trim();
                if ((l.length() == 0) || (l.charAt(0) == '#')) {
                    continue;
                }
                try {
                    taskList.add(Class.forName(l).asSubclass(UpdateTask.class).newInstance());
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
                }
            }
            configured = true;
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            closeReaderStuff(reader);
        }
    }

    private File getUpdateTaskFile(ConfigurationService configService) {
        String fileName = configService.getProperty(PROPERTYNAME);
        if (null == fileName) {
            return null;
        }
        File retval = new File(fileName);
        if (!retval.exists() || !retval.isFile()) {
            return null;
        }
        return retval;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public boolean isConfigured() {
        return configured;
    }

    public UpdateTask[] getTaskList() {
        return taskList.toArray(new UpdateTask[taskList.size()]);
    }
}
