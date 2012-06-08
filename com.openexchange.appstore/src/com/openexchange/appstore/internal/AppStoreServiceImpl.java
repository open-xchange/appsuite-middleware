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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.appstore.internal;

import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.sql.tools.SQLTools.createLIST;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.appstore.AppException;
import com.openexchange.appstore.AppStoreService;
import com.openexchange.appstore.Application;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.IN;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.Predicate;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.UPDATE;

/**
 * {@link AppStoreServiceImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AppStoreServiceImpl implements AppStoreService {

    static final org.apache.commons.logging.Log LOG = com.openexchange.log.LogFactory.getLog(AppStoreServiceImpl.class);
    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link AppStoreServiceImpl}.
     * @param activator
     */
    public AppStoreServiceImpl(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public List<Application> list(Context context, User user) throws OXException {
        List<Application> applications;
        applications = loadFromDB(getReleased(context, user));

        ApplicationInstaller appInstaller = new ApplicationInstaller(context, user, serviceLookup);
        List<Application> userApps = appInstaller.list();
        Map<String, Application.Status> statusMap = new HashMap<String, Application.Status>();
        for (Application userApp : userApps) {
            statusMap.put(userApp.getName(), userApp.getStatus());
        }

        for (Application application : applications) {
            Application.Status status = statusMap.get(application.getName());
            if (status == null) {
                application.setStatus(Application.Status.none);
            } else {
                application.setStatus(status);
            }
        }
        return applications;
    }

    public void dropUserApplications(Context context, User user) throws OXException {
        ApplicationInstaller appInstaller = new ApplicationInstaller(context, user, serviceLookup);
        appInstaller.dropUserApplications();
    }

    @Override
    public List<Application> installed(Context context, User user) throws OXException {
        ApplicationInstaller appInstaller = new ApplicationInstaller(context, user, serviceLookup);
        List<Application> userApps = appInstaller.list();
        List<String> released = getReleased(context, user);
        final List<String> names = new ArrayList<String>();
        for (Application application : userApps) {
            if (application.getStatus() == Application.Status.installed && released.contains(application.getName())) {
                names.add(application.getName());
            }
        }

        List<Application> applications;
        applications = loadFromDB(names);

        return applications;
    }

    @Override
    public boolean install(Context context, User user, String id) throws OXException {
        ApplicationInstaller appInstaller = new ApplicationInstaller(context, user, serviceLookup);
        Application app = new Application();
        app.setName(id);
        return appInstaller.install(app);
    }

    @Override
    public List<Application> list(String category) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean uninstall(Context context, User user, String id) throws OXException {
        ApplicationInstaller appInstaller = new ApplicationInstaller(context, user, serviceLookup);
        Application app = new Application();
        app.setName(id);
        return appInstaller.uninstall(app);
    }

    private List<Application> loadAll(int userId, int contextId, FileFilter applicationFilter) throws OXException, IOException {
        ConfigViewFactory service = serviceLookup.getService(ConfigViewFactory.class);
        ConfigView view = service.getView(userId, contextId);
        String property = view.get(locationProperty, String.class);

        List<Application> retval = new ArrayList<Application>();
        if (property.endsWith("/")) {
            property = property.substring(0, property.length() - 2);
        }
        File directory = new File(property);
        // to get the relative path later i need the base path length to make a substring
        int start = view.get(locationProperty, String.class).length() - directory.getName().length();

        if (!directory.isDirectory()) {
            return retval;
        }

        List<File> apps = listAppDirectories(directory, applicationFilter);

        for (File app : apps) {
            File appFile = null;
            File manifestFile = null;
            for (File f : app.listFiles()) {
                if (f.getName().equalsIgnoreCase(Application.FILE)) {
                    appFile = f;
                }
                if (f.getName().equalsIgnoreCase(Application.MANIFEST)) {
                    manifestFile = f;
                }
            }
            FileReader fr = new FileReader(appFile);
            BufferedReader br = new BufferedReader(fr);
            try {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.getProperty("line.separator"));
                }

                Application application = new Application();
                application.setDescription(sb.toString());

                br.close();
                fr.close();
                fr = new FileReader(manifestFile);
                br = new BufferedReader(fr);
                sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.getProperty("line.separator"));
                }

                application.setManifest(sb.toString());

                // get parent path information and string length
                int end = app.getPath().length();
                // get relative path information
                String path = app.getPath().substring(start, end);

                application.setRelativePath(path);
                application.setName(app.getName());
                retval.add(application);
            } finally {
                br.close();
                fr.close();
            }
        }

        return retval;
    }

    private List<File> listAppDirectories(File rootDirectory, FileFilter applicationFilter) {
        List<File> directories = new ArrayList<File>();

        File[] children = rootDirectory.listFiles();
        for (File child : children) {
            if (isAppDirectory(child)) {
                if (applicationFilter == null || applicationFilter.accept(child)) {
                    directories.add(child);
                }
            }
            if (child.isDirectory()) {
                directories.addAll(listAppDirectories(child, applicationFilter));
            }
        }

        return directories;
    }

    private boolean isAppDirectory(File pathname) {
        if (pathname.isFile()) {
            return false;
        }
        boolean foundDescription = false;
        boolean foundManifest = false;
        for (File file : pathname.listFiles()) {
            if (file.isFile()) {
                if (file.getName().equalsIgnoreCase(Application.FILE)) {
                    foundDescription = true;
                }
                if (file.getName().equalsIgnoreCase(Application.MANIFEST)) {
                    foundManifest = true;
                }
            }
        }
        return foundDescription && foundManifest;

    }

    /**
     * Loads all Applications from the database
     * @param names if not null, this list contains the applications to load. If null, all applications are loaded.
     * @return
     * @throws OXException
     */
    private List<Application> loadFromDB(List<String> names) throws OXException {
        if (names.isEmpty()) {
            return Collections.emptyList();
        }
        SELECT select = new SELECT(ASTERISK).FROM("applications");
        if (names != null) {
            select = select.WHERE(new IN("application", createLIST(names.size(), PLACEHOLDER)));
        }

        DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        Connection con = databaseService.getReadOnly();
        StatementBuilder sb = new StatementBuilder();

        List<Application> retval = new ArrayList<Application>();
        try {
            ResultSet resultSet = sb.executeQuery(con, select, names == null ? Collections.emptyList() : names);
            while (resultSet.next()) {
                Application temp = new Application();
                temp.setName(resultSet.getString("application"));
                temp.setDescription(resultSet.getString("description"));
                temp.setManifest(resultSet.getString("manifest"));
                temp.setRelativePath(resultSet.getString("path"));
                retval.add(temp);
            }
            sb.closePreparedStatement(null, resultSet);
        } catch (SQLException e) {
            throw AppException.sqlException(e);
        }
        databaseService.backReadOnly(con);
        return retval;
    }

    @Override
    public List<Application> crawl(User user, Context context) throws OXException {
        String table = "applications";

        List<Application> applications;
        try {
            applications = loadAll(user.getId(), context.getContextId(), null);
        } catch (IOException e) {
            throw AppException.ioError();
        }

        SELECT existingSelect = new SELECT("application").FROM(table);

        List<String> existingApplications = new ArrayList<String>();
        DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        Connection con = databaseService.getWritable();

        try {
            StatementBuilder sb = new StatementBuilder();
            ResultSet rs = sb.executeQuery(con, existingSelect, Collections.emptyList());
            while (rs.next()) {
                existingApplications.add(rs.getString("application"));
            }
            sb.closePreparedStatement(null, rs);
        } catch (SQLException e) {
            throw AppException.sqlException(e);
        }

        List<Application> applicationsToAdd = new ArrayList<Application>();
        List<Application> applicationsToUpdate = new ArrayList<Application>();

        for (Application application : applications) {
            if (existingApplications.contains(application.getName())) {
                applicationsToUpdate.add(application);
                existingApplications.remove(application.getName());
            } else {
                applicationsToAdd.add(application);
            }
        }

        // Delete
        List<String> applicationsToRemove = existingApplications;
        if (!applicationsToRemove.isEmpty()) {
            DELETE delete = new DELETE().FROM(table).WHERE(new IN("applications", createLIST(applicationsToRemove.size(), PLACEHOLDER)));
            try {
                new StatementBuilder().executeStatement(con, delete, applicationsToRemove);
            } catch (SQLException e) {
                throw AppException.sqlException(e);
            }
        }

        // Insert
        INSERT insert = new INSERT().INTO(table)
            .SET("application", PLACEHOLDER)
            .SET("path", PLACEHOLDER)
            .SET("description", PLACEHOLDER)
            .SET("manifest", PLACEHOLDER);
        for (Application application : applicationsToAdd) {
            List<Object> values = new ArrayList<Object>();
            values.add(application.getName());
            values.add(application.getRelativePath());
            values.add(application.getDescription());
            values.add(application.getManifest());
            try {
                new StatementBuilder().executeStatement(con, insert, values);
            } catch (SQLException e) {
                throw AppException.sqlException(e);
            }
        }

        // Update
        for (Application application : applicationsToUpdate) {
            UPDATE update = new UPDATE(table)
                .SET("path", PLACEHOLDER)
                .SET("description", PLACEHOLDER)
                .SET("manifest", PLACEHOLDER)
                .WHERE(new EQUALS("application", PLACEHOLDER));
            List<Object> values = new ArrayList<Object>();
            values.add(application.getRelativePath());
            values.add(application.getDescription());
            values.add(application.getManifest());
            values.add(application.getName());
            try {
                new StatementBuilder().executeStatement(con, update, values);
            } catch (SQLException e) {
                throw AppException.sqlException(e);
            }
        }

        databaseService.backWritable(con);

        return applications;
    }

    //private List<Application> get

    @Override
    public void release(Integer contextId, Integer userId, Application application) throws OXException {
        if (userId != null && contextId == null) {
            throw AppException.unexpected("Missing context");
        }

        String table = "applicationReleases";
        DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        Connection con = databaseService.getWritable();
        SELECT select = new SELECT(ASTERISK).FROM(table).WHERE(new EQUALS("application", PLACEHOLDER).AND(new EQUALS("userId", PLACEHOLDER).AND(new EQUALS("cid", PLACEHOLDER))));
        List<Object> values = new ArrayList<Object>();
        values.add(application.getName());
        values.add(userId != null ? userId : 0);
        values.add(contextId != null ? contextId : 0);
        try {
            StatementBuilder sb = new StatementBuilder();
            ResultSet rs = sb.executeQuery(con, select, values);
            if (rs.next()) {
                LOG.info("Application already released: " + application.getName());
                sb.closePreparedStatement(null, rs);
                databaseService.backWritable(con);
                return;
            }
        } catch (SQLException e) {
            throw AppException.sqlException(e);
        }

        INSERT insert = new INSERT().INTO(table)
            .SET("application", PLACEHOLDER)
            .SET("state", PLACEHOLDER);

        values = new ArrayList<Object>();
        values.add(application.getName());
        values.add("released");

        if (contextId != null) {
            insert = insert.SET("cid", PLACEHOLDER);
            values.add(contextId);
        }

        if (userId != null) {
            insert = insert.SET("userId", PLACEHOLDER);
            values.add(userId);
        }

        try {
            new StatementBuilder().executeStatement(con, insert, values);
        } catch (SQLException e) {
            throw AppException.sqlException(e);
        }

        databaseService.backWritable(con);
    }

    private List<String> getReleased(Context context, User user) throws OXException {
        Predicate where = new EQUALS("state", PLACEHOLDER);
        List<Object> values = new ArrayList<Object>();
        values.add("released");
        if (context != null) {
            if (user != null) {
                where = where.AND(new EQUALS("userId", PLACEHOLDER).OR(new EQUALS("cid", PLACEHOLDER)).OR(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("userId", PLACEHOLDER))));
                values.add(user.getId());
                values.add(context.getContextId());
                values.add(0);
                values.add(0);
            } else {
                where = where.AND(new EQUALS("cid", PLACEHOLDER).OR(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("userId", PLACEHOLDER))));
                values.add(context.getContextId());
                values.add(0);
                values.add(0);
            }
        }

        SELECT select = new SELECT(ASTERISK).FROM("applicationReleases").WHERE(where);

        DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        Connection con = databaseService.getReadOnly();
        StatementBuilder sb = new StatementBuilder();
        List<String> retval = new ArrayList<String>();
        try {
            ResultSet resultSet = sb.executeQuery(con, select, values);
            while (resultSet.next()) {
                retval.add(resultSet.getString("application"));
            }
        } catch (SQLException e) {
            throw AppException.sqlException(e);
        }

        return retval;
    }

    @Override
    public void revoke(Integer contextId, Integer userId, Application application) throws OXException {
        if (userId != null && contextId == null) {
            throw AppException.unexpected("Missing context");
        }

        String table = "applicationReleases";
        DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        Connection con = databaseService.getWritable();

        DELETE delete = new DELETE().FROM(table).WHERE(new EQUALS("application", PLACEHOLDER).AND(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("userId", PLACEHOLDER).AND(new EQUALS("state", PLACEHOLDER)))));
        List<Object> values = new ArrayList<Object>();
        values.add(application.getName());
        values.add(contextId != null ? contextId : 0);
        values.add(userId != null ? userId : 0);
        values.add("released");

        try {
            new StatementBuilder().executeStatement(con, delete, values);
        } catch (SQLException e) {
            throw AppException.sqlException(e);
        }

        databaseService.backWritable(con);
    }

    @Override
    public List<ReleaseStatus> status(String applicationId) throws OXException {
        DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        Connection con = databaseService.getReadOnly();

        SELECT select = new SELECT(ASTERISK).FROM("applicationReleases").WHERE(new EQUALS("application", PLACEHOLDER));
        List<Object> values = new ArrayList<Object>();
        values.add(applicationId);

        StatementBuilder sb = new StatementBuilder();
        List<ReleaseStatus> retval = new ArrayList<ReleaseStatus>();
        try {
            ResultSet resultSet = sb.executeQuery(con, select, values);
            while (resultSet.next()) {
                String target = null;
                Integer contextId = resultSet.getInt("cid");
                Integer userId = resultSet.getInt("userId");

                if (contextId == 0 && userId == 0) {
                    target = "all";
                } else if (userId == 0) {
                    target = "context";
                } else {
                    target = "user";
                }

                ReleaseStatus status = new ReleaseStatus();
                status.setTarget(target);
                status.setContextId(contextId);
                status.setUserId(userId);

                retval.add(status);
            }
        } catch (SQLException e) {
            throw AppException.sqlException(e);
        }

        databaseService.backReadOnly(con);
        return retval;
    }

}
