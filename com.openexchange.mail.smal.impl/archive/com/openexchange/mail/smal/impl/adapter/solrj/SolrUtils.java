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

package com.openexchange.mail.smal.impl.adapter.solrj;

import java.io.IOException;
import java.net.SocketTimeoutException;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import com.openexchange.exception.OXException;
import com.openexchange.log.Log;

/**
 * {@link SolrUtils} - Utility methods for Solr.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SolrUtils {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(com.openexchange.log.LogFactory.getLog(SolrUtils.class));

    /**
     * Initializes a new {@link SolrUtils}.
     */
    private SolrUtils() {
        super();
    }

    /**
     * Performs a commit on specified Solr server.
     *
     * @param solrServer The Solr server
     * @throws SolrServerException If an index error occurs
     * @throws IOException If an I/O error occurs
     * @throws OXException If an OX error occurs
     */
    public static void commitSane(final CommonsHttpSolrServer solrServer) throws SolrServerException, IOException, OXException {
        try {
            commit(solrServer, false);
        } catch (final SolrServerException e) {
            if (!(e.getCause() instanceof SocketTimeoutException)) {
                throw e;
            }
            commit(solrServer, true);
        }
    }

    /**
     * Performs a commit on specified Solr server.
     *
     * @param solrServer The Solr server
     * @throws SolrServerException If an index error occurs
     * @throws IOException If an I/O error occurs
     * @throws OXException If an OX error occurs
     */
    public static void commitWithTimeout(final CommonsHttpSolrServer solrServer) throws SolrServerException, IOException, OXException {
        commit(solrServer, false);
    }

    /**
     * Performs a commit on specified Solr server.
     * <p>
     * Set <tt>noTimeout</tt> to temporarily disable possible default socket timeout (if any <tt>SO_TIMEOUT</tt> set) and to restore it
     * afterwards.
     *
     * @param solrServer The Solr server
     * @param noTimeout <code>true</code> for no timeout; otherwise <code>false</code>
     * @throws SolrServerException If an index error occurs
     * @throws IOException If an I/O error occurs
     * @throws OXException If an OX error occurs
     */
    public static void commit(final CommonsHttpSolrServer solrServer, final boolean noTimeout) throws SolrServerException, IOException, OXException {
        if (null != solrServer) {
            if (!noTimeout || solrServer.getHttpClient().getHttpConnectionManager().getParams().getSoTimeout() <= 0) {
                solrServer.commit();
                return;
            }
            noTimeoutServer(solrServer).commit();
        }
    }

    /**
     * Performs a safe roll-back for specified Solr server.
     *
     * @param solrServer The Solr server
     */
    public static void rollback(final CommonsHttpSolrServer solrServer) {
        if (null != solrServer) {
            try {
                if (solrServer.getHttpClient().getHttpConnectionManager().getParams().getSoTimeout() <= 0) {
                    solrServer.rollback();
                    return;
                }
                noTimeoutServer(solrServer).rollback();
            } catch (final Throwable t) {
                handleThrowable(t);
                LOG.warn("Rollback of Solr server failed.", t);
            }
        }
    }

    private static CommonsHttpSolrServer noTimeoutServer(final CommonsHttpSolrServer solrServer) throws OXException {
        final HttpClientParams params = solrServer.getHttpClient().getParams();
        return null; // ((CommonsHttpSolrServerManagement) params.getParameter("solr.server-management")).getNoTimeoutSolrServerFor(solrServer);
    }

    private static final String MARKER = " ---=== /!\\ ===--- ";

    /**
     * Checks whether the supplied <tt>Throwable</tt> is one that needs to be rethrown and swallows all others.
     *
     * @param t The <tt>Throwable</tt> to check
     */
    public static void handleThrowable(final Throwable t) {
        if (t instanceof ThreadDeath) {
            LOG.fatal(MARKER + "Thread death" + MARKER, t);
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            LOG.fatal(
                MARKER + "The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating." + MARKER,
                t);
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }

}
