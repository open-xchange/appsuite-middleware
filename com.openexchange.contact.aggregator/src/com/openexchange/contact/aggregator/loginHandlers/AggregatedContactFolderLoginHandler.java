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

package com.openexchange.contact.aggregator.loginHandlers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.aggregator.AggregatingSubscribeService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * The {@link AggregatedContactFolderLoginHandler} creates the standard aggregated contact folder. That is a contact folder that has a
 * com.openexchange.contact.aggregator subscription that is executed periodically.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AggregatedContactFolderLoginHandler implements LoginHandlerService {

    /**
     * 
     */
    private static final String LAST_SLOW_RUN_PROPERTY = "com.openexchange.contact.aggregator.lastSlowRun";

    /**
     * 
     */
    private static final String SLOW_RUN_INTERVAL_PROPERTY = "com.openexchange.contact.aggregator.slowRunInterval";

    /**
     * 
     */
    private static final String LAST_FAST_RUN_PROPERTY = "com.openexchange.contact.aggregator.lastFastRun";

    /**
     * 
     */
    private static final String FAST_RUN_INTERVAL_PROPERTY = "com.openexchange.contact.aggregator.fastRunInterval";

    /**
     * 
     */
    private static final String ENABLED_PROPERTY = "com.openexchange.contact.aggregator.enabled";

    /**
     * 
     */
    private static final String DEFAULT_FOLDER_PROPERTY = "com.openexchange.contact.aggregator.defaultFolder";

    private final ConfigViewFactory configs;

    private final DatabaseService databaseService;

    private final AggregatingSubscribeService subscribeService;

    private final SubscriptionExecutionService executor;

    public AggregatedContactFolderLoginHandler(final ConfigViewFactory configs, final DatabaseService databaseService, final AggregatingSubscribeService subscribeService, final SubscriptionExecutionService executor) {
        super();
        this.configs = configs;
        this.databaseService = databaseService;
        this.subscribeService = subscribeService;
        this.executor = executor;
    }

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AggregatedContactFolderLoginHandler.class);

    public void handleLogin(final LoginResult login) throws OXException {
        final int cid = login.getSession().getContextId();
        final int uid = login.getSession().getUserId();

        Connection con = null;
        try {
            final ConfigView view = configs.getView(uid, cid);

            if (!isEnabled(view)) {
                return;
            }
            con = databaseService.getWritable(cid);
            final String folderName = StringHelper.valueOf(login.getUser().getLocale()).getString(FolderStrings.DEFAULT_AGGREGATION_CONTACT_FOLDER_NAME);

            final boolean[] force = new boolean[]{false};
            final int folderId = create(login.getSession(), login.getContext(), folderName, con, view, force);
            runSubscription(login.getSession(), view, folderId, force[0]);
        } catch (final Throwable t) {
            final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(AggregatedContactFolderLoginHandler.class));
            logger.error("Unexpected error: " + t.getMessage(), t);
        } finally {
            if (con != null) {
                databaseService.backWritable(cid, con);
            }
        }
    }

    private boolean isEnabled(final ConfigView view) throws OXException {
        final ComposedConfigProperty<Boolean> property = view.property(ENABLED_PROPERTY, boolean.class);
        return property.isDefined() && property.get();
    }

    private boolean shouldRunFast(final ConfigView view) throws OXException {
        final ComposedConfigProperty<Long> intervalProp = view.property(FAST_RUN_INTERVAL_PROPERTY, long.class);

        final ComposedConfigProperty<Long> lastRunProp = view.property(LAST_FAST_RUN_PROPERTY, long.class);

        if (!intervalProp.isDefined()) {
            return false;
        }

        if (!lastRunProp.isDefined()) {
            view.set("user", LAST_FAST_RUN_PROPERTY, System.currentTimeMillis());
            return true;
        }

        if (lastRunProp.get() + intervalProp.get() < System.currentTimeMillis()) {
            view.set("user", LAST_FAST_RUN_PROPERTY, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    private boolean shouldRunSlow(final ConfigView view) throws OXException {
        final ComposedConfigProperty<Long> intervalProp = view.property(SLOW_RUN_INTERVAL_PROPERTY, long.class);

        final ComposedConfigProperty<Long> lastRunProp = view.property(LAST_SLOW_RUN_PROPERTY, long.class);

        if (!intervalProp.isDefined()) {
            return false;
        }

        if (!lastRunProp.isDefined()) {
            view.set("user", LAST_SLOW_RUN_PROPERTY, System.currentTimeMillis());
            view.set("user", LAST_FAST_RUN_PROPERTY, System.currentTimeMillis());
            return false; // First run should always be fast
        }

        if (lastRunProp.get() + intervalProp.get() < System.currentTimeMillis()) {
            view.set("user", LAST_SLOW_RUN_PROPERTY, System.currentTimeMillis());
            view.set("user", LAST_FAST_RUN_PROPERTY, System.currentTimeMillis());
            return true;

        }
        return false;
    }

    private void runSubscription(final Session session, final ConfigView view, final int folderId, final boolean force) throws OXException {
        final boolean runFast = shouldRunFast(view);
        final boolean runSlow = shouldRunSlow(view);

        if (!runFast && !runSlow && !force) {
            return;
        }

        final Subscription temporarySubscription = new Subscription();
        temporarySubscription.setEnabled(true);
        temporarySubscription.setFolderId(folderId);
        temporarySubscription.setSource(subscribeService.getSubscriptionSource());
        final Map<String, Object> configuration = new HashMap<String, Object>();
        if (!runSlow) {
            configuration.put("fast", true);
        }
        temporarySubscription.setConfiguration(configuration);

        final ServerSessionAdapter serverSession = new ServerSessionAdapter(session);

        temporarySubscription.setSession(serverSession);
        temporarySubscription.setContext(serverSession.getContext());
        temporarySubscription.setUserId(session.getUserId());

        executor.executeSubscriptions(Arrays.asList(temporarySubscription), serverSession);

    }

    public int create(final Session session, final Context ctx, final String folderName, final Connection con, final ConfigView view, final boolean[] force) throws OXException, SQLException {
        final int cid = session.getContextId();
        final int userId = session.getUserId();

        final ComposedConfigProperty<Integer> property = view.property(DEFAULT_FOLDER_PROPERTY, int.class);

        final OXFolderAccess folderAccess = new OXFolderAccess(con, ctx);
        if (property.isDefined()) {
            final int folderId = property.get().intValue();
            if (folderAccess.exists(folderId)) {
                /*
                 * Folder already exists
                 */
                return folderId;
            }
        }

        /*
         * Create folder
         */
        int aggregationFolderID = 0;
        final int parent = folderAccess.getDefaultFolder(userId, FolderObject.CONTACT).getObjectID();
        try {
            aggregationFolderID = OXFolderManager.getInstance(session, folderAccess, con, con).createFolder(
                createNewContactFolder(userId, folderName, parent),
                true,
                System.currentTimeMillis()).getObjectID();
            force[0] = true;
        } catch (final OXException folderException) {
            if (OXFolderExceptionCode.NO_DUPLICATE_FOLDER.equals(folderException)) {
                LOG.info(new StringBuilder("Found Folder with name of aggregation folder. Guess this is the dedicated folder."));
                aggregationFolderID = OXFolderSQL.lookUpFolder(parent, folderName, FolderObject.CONTACT, con, ctx);
            }
        }
        /*
         * Remember folder ID
         */
        view.set("user", DEFAULT_FOLDER_PROPERTY, aggregationFolderID);
        if (LOG.isInfoEnabled()) {
            LOG.info(new StringBuilder("Aggregation folder (id=").append(aggregationFolderID).append(") successfully created for user ").append(
                userId).append(" in context ").append(cid));
        }
        return aggregationFolderID;
    }

    private FolderObject createNewContactFolder(final int userId, final String name, final int parent) {
        final FolderObject newFolder = new FolderObject();
        newFolder.setFolderName(name);
        newFolder.setParentFolderID(parent);
        newFolder.setType(FolderObject.PRIVATE);
        newFolder.setModule(FolderObject.CONTACT);

        final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
        // User is Admin and can read, write or delete everything
        final OCLPermission perm = new OCLPermission();
        perm.setEntity(userId);
        perm.setFolderAdmin(true);
        perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setReadObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setWriteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setDeleteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setGroupPermission(false);
        perms.add(perm);
        newFolder.setPermissions(perms);

        return newFolder;
    }

    public void handleLogout(final LoginResult logout) {
        // Nothing to do on logout
    }

}
