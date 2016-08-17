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

package com.openexchange.admin.console.publication;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Pattern;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.user.UserAbstraction;
import com.openexchange.admin.rmi.OXPublicationInterface;
import com.openexchange.admin.rmi.dataobjects.Publication;

/**
 *
 * {@link PublicationAbstraction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class PublicationAbstraction extends UserAbstraction {

    protected static final String OPT_PUBLICATION_URL = "publication-url";

    protected static final String OPT_MODULE = "module";

    protected static final String OPT_ENTITY = "entity-id";

    protected final Map<String, CLIOption> options = new HashMap<String, CLIOption>();

    protected PublicationAbstraction() {
        super();
    }

    /**
     * <p>
     * The URL of a publication has always the following format:<br/>
     * <pre>
     * '/publications/[MODULE_NAME]/[CONTEXT_ID]/[SITE_NAME]?secret=[SECRET]'
     * </pre>
     * <br/>
     * If that's the case, then the URL is returned intact.</p>
     * <p>
     * If the provided URL contains an element identifier, e.g.
     * '/publications/infostore/12345/shared/31337/something-else?secret=foobar'
     * </p>
     * then, the rootURL, the module identifier, the context identifier and the site name are retained,
     * i.e. '/publications/infostore/12345/shared' plus the ?secret=foobar
     * and everything else is dispersed.
     *
     * @param parser
     * @return The publication URL
     * @throws URISyntaxException If the URL is malformed.
     */
    public String parseAndSetPublicationUrl(final AdminParser parser) throws URISyntaxException {
        String publicationUrl;
        final URI url;
        {
            String pubUrl = (String) parser.getOptionValue(options.get(OPT_PUBLICATION_URL));
            if (pubUrl == null) {
                return null;
            } else {
                url = new URI(pubUrl);
            }
        }

        publicationUrl = url.getPath() + "?" + url.getQuery();
        if (!publicationUrl.startsWith("/")) {
            publicationUrl = "/" + publicationUrl;
        }
        String[] split = Pattern.compile("/").split(publicationUrl);
        if (split.length > 4) {
            if (split[2].equals("files".toLowerCase())) {
                split[2] = "infostore";
            }
        }
        if (split.length > 5) {
            StringBuilder builder = new StringBuilder("/");
            // Pick up the first 4 relevant parts of the URL, i.e. the rootURL, the module, the contextId and the site name...
            for (int l = 1; l <= 4; l++) {
                builder.append(split[l]).append("/");
            }
            builder.setLength(builder.length() - 1);
            //... and the last part containing the secret
            String secret = split[split.length - 1];
            String[] secrets = secret.split("\\?secret=");
            secret = secrets[secrets.length - 1];
            builder.append("?secret=").append(secret);
            publicationUrl = builder.toString();
        }
        return publicationUrl;
    }

    /**
     * Prints the specified publications in a table
     *
     * @param publications The list with publications to print
     */
    protected void printList(List<Publication> publications) {
        if (null == publications || publications.isEmpty()) {
            System.out.println("No publications found.");
            return;
        }
        int widestURL = sortAndFetchWidestURL(publications);
        String headerFormatter = "| %-7s | %-10s | %-15s | %-9s | %-7s | %-" + widestURL + "s | %n";
        String bodyFormatter = "| %-7d | %-10d | %-15s | %-9s | %-7d | %-" + widestURL + "s | %n";

        StringBuilder dashBuilder = new StringBuilder();
        for (int i = 0; i < widestURL; i++) {
            dashBuilder.append("-");
        }
        dashBuilder.append("--+");

        System.out.format("+---------+------------+-----------------+-----------+---------+" + dashBuilder.toString() + "%n");
        System.out.format(headerFormatter, "User ID", "Context ID", "Module", "Entity ID", "ID", "URL");
        System.out.format("+---------+------------+-----------------+-----------+---------+" + dashBuilder.toString() + "%n");

        for (Publication publication : publications) {
            System.out.format(bodyFormatter, publication.getUserId(), publication.getContext().getId(), publication.getModule(), publication.getEntityId(), publication.getId(), publication.getUrl());
        }
        System.out.format("+---------+------------+-----------------+-----------+---------+" + dashBuilder.toString() + "%n");
    }

    /**
     * Sort the publications and fetch the length of the widest URL string
     *
     * @param publications The publications to sort
     * @return The length of the widest URL
     */
    private static int sortAndFetchWidestURL(List<Publication> publications) {
        int widestURL = 5;
        Object[] a = publications.toArray();
        Arrays.sort(a);
        ListIterator<Publication> i = publications.listIterator();
        for (int j = 0; j < a.length; j++) {
            Publication p = i.next();
            if (widestURL < p.getUrl().length()) {
                widestURL = p.getUrl().length();
            }
            i.set((Publication) a[j]);
        }
        return widestURL;
    }

    /**
     * Get the OXPublicationInterface
     *
     * @return The OXPublicationInterface
     * @throws NotBoundException
     * @throws MalformedURLException
     * @throws RemoteException
     */
    protected final OXPublicationInterface getPublicationInterface() throws NotBoundException, MalformedURLException, RemoteException {
        return (OXPublicationInterface) Naming.lookup(RMI_HOSTNAME + OXPublicationInterface.RMI_NAME);
    }

    /**
     * Set CLT's options
     *
     * @param parser
     */
    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        options.put(OPT_PUBLICATION_URL, setShortLongOpt(parser, 'u', OPT_PUBLICATION_URL, "The publication's URL", true, NeededQuadState.possibly));
        options.put(OPT_MODULE, setShortLongOpt(parser, 'm', OPT_MODULE, "The module", true, NeededQuadState.possibly));
        options.put(OPT_ENTITY, setShortLongOpt(parser, 'e', OPT_ENTITY, "The entity's identifier", true, NeededQuadState.possibly));
        options.put(OPT_ID_LONG, setShortLongOpt(parser, OPT_ID_SHORT, OPT_ID_LONG, "Id of the user", true, NeededQuadState.possibly));
        setFurtherOptions(parser);
    }

    @Override
    protected String getObjectName() {
        return "publication";
    }

    /**
     * Set further options to the command line tool
     *
     * @param parser The admin parser to use
     */
    protected abstract void setFurtherOptions(final AdminParser parser);
}
