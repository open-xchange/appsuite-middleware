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
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXPublicationInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Publication;

/**
 * {@link Delete}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Delete extends PublicationAbstraction {

    private static final String OPT_FORCE_DELETE = "force";

    private static final String OPT_VERBOSE = "verbose";

    /**
     * Entry point
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        new Delete(args);
    }

    /**
     * Initializes a new {@link Delete}.
     */
    public Delete(final String[] args) {
        super();
        final AdminParser parser = new AdminParser("deletepublication");
        parser.setUsage("[-f] [-A <adminuser> -P <adminpass>] [ -c <contextid> [-u <publication-url>] | [-e <entity-id>] | [-i <userid> [-m <module>]]] [-v]");
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

            final boolean verbose = parser.hasOption(options.get(OPT_VERBOSE));

            final List<Publication> publications;

            if (parser.hasOption(options.get(OPT_FORCE_DELETE))) {
                if (parser.hasOption(options.get(OPT_ID_LONG))) {
                    final int userId = Integer.parseInt((String) parser.getOptionValue(options.get(OPT_ID_LONG)));
                    if (parser.hasOption(options.get(OPT_MODULE))) {
                        final String module = (String) parser.getOptionValue(options.get(OPT_MODULE));
                        publications = oxpub.deletePublications(context, auth, userId, module);
                    } else {
                        publications = oxpub.deletePublications(context, auth, userId);
                    }
                } else if (parser.hasOption(options.get(OPT_ENTITY))) {
                    final String entityId = (String) parser.getOptionValue(options.get(OPT_ENTITY));
                    publications = oxpub.deletePublications(context, auth, entityId);
                } else if (parser.hasOption(options.get(OPT_PUBLICATION_URL))) {
                    final String publicationUrl = parseAndSetPublicationUrl(parser);
                    Publication publication = oxpub.deletePublication(context, auth, publicationUrl);
                    if (publication != null) {
                        System.out.println("Publication with URL \"" + publicationUrl + "\" successfully deleted from context " + context.getId());
                        if (verbose) {
                            createMessageForStdout(publication.toString(), null, null, parser);
                        }
                        error = false;
                    } else {
                        System.out.println("Failed to delete publication with URL \"" + publicationUrl + "\" from context " + context.getId());
                        error = true;
                    }
                    publications = null;
                    sysexit(0);
                } else {
                    publications = oxpub.deletePublications(context, auth);
                }

                printList(publications, verbose);
            } else {
                System.out.println("No publication deleted yet. Please make sure that you want to continue with this irreversible operation. To force deletion use the '-f' flag.");
            }

            error = false;
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
        } finally {
            if (error) {
                sysexit(SYSEXIT_UNKNOWN_OPTION);
            }
        }
    }

    /**
     * Print a list of publications
     * 
     * @param publications The list of publications to print
     * @param verbose if true then it prints verbose information for the publication; otherwise it prints just the ids
     */
    private void printList(List<Publication> publications, boolean verbose) {
        System.out.println("The following publications were deleted: ");
        if (verbose) {
            printList(publications);
        } else {
            for (Publication p : publications) {
                System.out.print(p.getId() + " ");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.console.publication.PublicationAbstraction#setFurtherOptions(com.openexchange.admin.console.AdminParser)
     */
    @Override
    protected void setFurtherOptions(AdminParser parser) {
        options.put(OPT_FORCE_DELETE, setShortLongOpt(parser, 'f', OPT_FORCE_DELETE, "Force deletion", false, NeededQuadState.possibly));
        options.put(OPT_VERBOSE, setShortLongOpt(parser, 'v', OPT_VERBOSE, "Verbose", false, NeededQuadState.notneeded));
    }
}
