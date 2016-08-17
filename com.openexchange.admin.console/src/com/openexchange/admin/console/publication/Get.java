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

import java.util.List;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXPublicationInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Publication;

/**
 * {@link Get}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Get extends PublicationAbstraction {

    /**
     * Entry point
     * 
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
        new Get(args);
    }

    /**
     * Initializes a new {@link Get}.
     */
    public Get(final String[] args) {
        super();
        final AdminParser parser = new AdminParser("getpublication");
        parser.setUsage("[-A <adminuser> -P <adminpass>] [ -c <contextid> [-u <publication-url>] | [-e <entity-id>] | [-i <userid> [-m <module>]]]");
        setOptions(parser);
        process(parser, args);
    }

    /**
     * Process the command
     * 
     * @param parser The admin parser
     * @param args The command line arguments
     */
    private void process(final AdminParser parser, final String[] args) {
        boolean error = true;
        String successtext = null;

        try {
            parser.ownparse(args);
            final Context context = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);
            final OXPublicationInterface oxpub = getPublicationInterface();

            final List<Publication> publications;

            if (parser.hasOption(options.get(OPT_ID_LONG))) {
                final int userId = Integer.parseInt((String) parser.getOptionValue(options.get(OPT_ID_LONG)));
                if (parser.hasOption(options.get(OPT_MODULE))) {
                    final String module = (String) parser.getOptionValue(options.get(OPT_MODULE));
                    publications = oxpub.listPublications(context, auth, userId, module);
                } else {
                    publications = oxpub.listPublications(context, auth, userId);
                }
            } else if (parser.hasOption(options.get(OPT_ENTITY))) {
                final String entityId = (String) parser.getOptionValue(options.get(OPT_ENTITY));
                publications = oxpub.listPublications(context, auth, entityId);
            } else if (parser.hasOption(options.get(OPT_PUBLICATION_URL))) {
                final String publicationUrl = parseAndSetPublicationUrl(parser);
                Publication publication = oxpub.getPublication(context, publicationUrl, auth);
                createMessageForStdout(publication.toString(), null, null, parser);
                publications = null;
                sysexit(0);
            } else {
                publications = oxpub.listPublications(context, auth);
            }
            printList(publications);

            error = false;
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
        } finally {
            if (error) {
                sysexit(SYSEXIT_UNKNOWN_OPTION);
            }
        }
    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {
        // nothing
    }
}
