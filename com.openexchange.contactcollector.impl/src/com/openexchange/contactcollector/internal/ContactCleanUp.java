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

package com.openexchange.contactcollector.internal;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.contact.ContactService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Reference;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link ContactCleanUp} - Cleans unused collected contacts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class ContactCleanUp implements Callable<Void> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactCleanUp.class);

    /**
     * Determines the cleanup threshold for given session-associated user.
     *
     * @param session The session providing user information
     * @param services The service look-up
     * @return The cleanup threshold in milliseconds or a value less than/equal to <code>0</code> (zero) signaling no cleanup should take place
     */
    public static long getCleanUpThreshold(Session session, ServiceLookup services) {
        long defaultValue = 0;

        ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (viewFactory == null) {
            return defaultValue;
        }

        try {
            ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
            String sCleanUpThreshold = ConfigViews.getDefinedStringPropertyFrom("com.openexchange.contactcollector.cleanupUnusedAfter", Long.toString(defaultValue), view);
            return ConfigTools.parseTimespan(sCleanUpThreshold);
        } catch (Exception e) {
            LOG.error("Failed to obtain cleanup threshold configuration option for user {} in context {}. Cleanup disabled by default.", I(session.getUserId()), I(session.getContextId()), e);
            return defaultValue;
        }
    }

    /**
     * Gets an optional instance to cleanup unused collected contacts.
     *
     * @param session The session to use
     * @param services The service look-up
     * @return The optional cleanup instance
     */
    public static Optional<Callable<Void>> createContactCleanUp(Session session, ServiceLookup services) {
        return createContactCleanUp(getCleanUpThreshold(session, services), session, services);
    }

    /**
     * Gets an optional instance to cleanup unused collected contacts.
     *
     * @param cleanUpThresholdMillis The cleanup threshold in milliseconds; unchanged collected contacts having a last-modified time stamp older than given threshold will be cleaned
     * @param session The session to use
     * @param services The service look-up
     * @return The optional cleanup instance
     */
    public static Optional<Callable<Void>> createContactCleanUp(long cleanUpThresholdMillis, Session session, ServiceLookup services) {
        return cleanUpThresholdMillis <= 0 ? Optional.empty() : Optional.of(new ContactCleanUp(cleanUpThresholdMillis, session, services));
    }

    /**
     * Cleans unused collected contacts.
     *
     * @param session The session to use
     * @param services The service look-up
     */
    public static void performContactCleanUp(Session session, ServiceLookup services) {
        performContactCleanUp(getCleanUpThreshold(session, services), session, services);
    }

    /**
     * Cleans unused collected contacts.
     *
     * @param cleanUpThresholdMillis The cleanup threshold in milliseconds; unchanged collected contacts having a last-modified time stamp older than given threshold will be cleaned
     * @param session The session to use
     * @param services The service look-up
     */
    public static void performContactCleanUp(long cleanUpThresholdMillis, Session session, ServiceLookup services) {
        Optional<Callable<Void>> optionalCleanUp = createContactCleanUp(cleanUpThresholdMillis, session, services);
        if (optionalCleanUp.isPresent()) {
            try {
                optionalCleanUp.get().call();
            } catch (Exception e) {
                LOG.error("Failed contact collector cleanup run for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Session session;
    private final ServiceLookup services;
    private final long cleanUpThresholdMillis;

    /**
     * Initializes a new {@link ContactCleanUp}.
     *
     * @param cleanUpThresholdMillis The cleanup threshold in milliseconds; unchanged collected contacts having a last-modified time stamp older than given threshold will be cleaned
     * @param session The session
     * @param services The service look-up
     */
    private ContactCleanUp(long cleanUpThresholdMillis, Session session, ServiceLookup services) {
        super();
        this.cleanUpThresholdMillis = cleanUpThresholdMillis;
        this.session = session;
        this.services = services;
    }

    @Override
    public Void call() {
        ContactService contactService = services.getOptionalService(ContactService.class);
        if (null == contactService) {
            LOG.info("Contact collector cleanup run aborted for user {} in context {}: missing contact service", I(session.getUserId()), I(session.getContextId()));
            return null;
        }

        int folderId = MemorizerWorker.getFolderId(session);
        if (folderId <= 0) {
            LOG.info("Contact collector cleanup run aborted for user {} in context {}: cannot determine identifier of contact collector folder", I(session.getUserId()), I(session.getContextId()));
            return null;
        }

        try {
            Reference<Long> latestLastModified = new Reference<>();
            List<String> identifiers = determineContactsToCleanUp(folderId, latestLastModified);
            if (identifiers.isEmpty()) {
                LOG.debug("Contact collector cleanup run done for user {} in context {}: nothing dropped since no unused contacts were found", I(session.getUserId()), I(session.getContextId()));
                return null;
            }

            contactService.deleteContacts(session, Integer.toString(folderId), identifiers.toArray(new String[identifiers.size()]), new Date(latestLastModified.getValue().longValue()));
            LOG.info("Contact collector cleanup run done for user {} in context {}: dropped {} unused contacts", I(session.getUserId()), I(session.getContextId()), I(identifiers.size()));
        } catch (Exception e) {
            LOG.error("Failed contact collector cleanup run for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
        }
        return null;
    }

    private List<String> determineContactsToCleanUp(int folderId, Reference<Long> latestLastModified) throws OXException, SQLException {
        DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (databaseService == null) {
            LOG.info("Contact collector cleanup run aborted for user {} in context {}: missing database service", I(session.getUserId()), I(session.getContextId()));
            return Collections.emptyList();
        }

        Connection con = null;
        try {
            con = databaseService.getReadOnly(session.getContextId());
            return determineContactsToCleanUp(folderId, latestLastModified, con);
        } finally {
            if (con != null) {
                databaseService.backReadOnly(session.getContextId(), con);
            }
        }
    }

    private List<String> determineContactsToCleanUp(int folderId, Reference<Long> latestLastModified, Connection con) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder(
              "SELECT c.intfield01, c.changing_date FROM prg_contacts AS c LEFT JOIN object_use_count AS uc"
            + " ON c.cid=uc.cid AND c.created_from=uc.user AND c.fid=uc.folder AND c.intfield01=uc.object"
            + " WHERE c.cid=? AND c.created_from=? AND c.creating_date=c.changing_date AND c.fid=? AND ((c.useCount IS NULL OR c.useCount < 2) AND (uc.value IS NULL OR uc.value<2))"
            + " AND c.changing_date<?");

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(queryBuilder.toString());
            queryBuilder = null;
            stmt.setInt(1, session.getContextId());
            stmt.setInt(2, session.getUserId());
            stmt.setInt(3, folderId);
            stmt.setLong(4, System.currentTimeMillis() - cleanUpThresholdMillis);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            List<String> identifiers = new ArrayList<>(64);
            do {
                identifiers.add(Integer.toString(rs.getInt(1)));
                long lastModified = rs.getLong(2);
                if (latestLastModified.getValue() == null || latestLastModified.getValue().longValue() < lastModified) {
                    latestLastModified.setValue(Long.valueOf(lastModified));
                }
            } while (rs.next());
            return identifiers;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
