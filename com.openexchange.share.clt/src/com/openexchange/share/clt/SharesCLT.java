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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.share.clt;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.share.impl.rmi.ShareRMIService;

/**
 * {@link SharesCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class SharesCLT extends AbstractRmiCLI<Void> {

    private static final String SYNTAX = "shares [-c <contextId> -i <userId> -T <tokenId>] [-r [-f]] -A <masterAdmin> -P <masterAdminPassword> [-p <RMI-Port>] [-s <RMI-Server] | [-h]";
    private static final String FOOTER = "Command line tool to list and delete shares";

    /**
     * Entry point
     * 
     * @param args The command line argumnets
     */
    public static void main(String[] args) {
        new SharesCLT().execute(args);
    }

    private int contextId;
    private int guestId;
    private String token;
    private boolean iKnowWhatIamDoing = false;
    private boolean remove = false;

    /**
     * Initialises a new {@link SharesCLT}.
     */
    public SharesCLT() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#administrativeAuth(java.lang.String, java.lang.String, org.apache.commons.cli.CommandLine, com.openexchange.auth.rmi.RemoteAuthenticator)
     */
    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        if (contextId < 0) {
            authenticator.doAuthentication(login, password);
            return;
        }
        authenticator.doAuthentication(login, password, contextId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("c", "context", "contextId", "The context id.", false));
        options.addOption(createArgumentOption("i", "userid", "userId", "The guest user id.", false));
        options.addOption(createArgumentOption("T", "token", "token", "Token or URL.", false));
        options.addOption(createSwitch("r", "remove", "Remove the token.", false));
        options.addOption(createSwitch("f", "force", "Force removal of token.", false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractAdministrativeCLI#requiresAdministrativePermission()
     */
    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption("c")) {
            contextId = parseInt('c', -1, cmd, options);
        }
        if (cmd.hasOption("i")) {
            guestId = parseInt('i', -1, cmd, options);
        }
        if (cmd.hasOption("T")) {
            token = cmd.getOptionValue("T");
        }
        iKnowWhatIamDoing = cmd.hasOption("f");
        remove = cmd.hasOption("r");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        return remove ? removeShares(optRmiHostName) : listShares(optRmiHostName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getFooter()
     */
    @Override
    protected String getFooter() {
        return FOOTER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getName()
     */
    @Override
    protected String getName() {
        return SYNTAX;
    }

    //////////////////////////// HELPERS ///////////////////////////

    private Void removeShares(String optRmiHostName) throws MalformedURLException, RemoteException, NotBoundException, MissingOptionException, OXException {
        ShareRMIService rmiService = getRmiStub(optRmiHostName, ShareRMIService.RMI_NAME);
        int result = 0;
        if (Strings.isNotEmpty(token)) {
            if (isShareURL(token)) {
                token = extractTokenFromURL(token);
            }
            Pair<String, String> tokenAndPath = parseToken(token);
            String shareToken = tokenAndPath.getFirst();
            String targetPath = tokenAndPath.getSecond();
            if ((null == targetPath || "".equals(targetPath)) && !iKnowWhatIamDoing) {
                throw new MissingOptionException("Seems like you supplied a token without a share path. If you want to remove all shares identified by this token use option -f/--force");
            }
            if (contextId > 0) {
                result = rmiService.removeShare(shareToken, targetPath, contextId);
            } else {
                result = rmiService.removeShare(shareToken, targetPath);
            }
        } else if (contextId > 0) {
            if (guestId > 0) {
                result = rmiService.removeShares(contextId, guestId);
            } else {
                result = rmiService.removeShares(contextId);
            }
        }
        System.out.println(result + (result == 1 ? " share " : " shares ") + "removed.");
        return null;
    }

    private Void listShares(String optRmiHostName) throws MalformedURLException, RemoteException, NotBoundException {
        ShareRMIService rmiService = getRmiStub(optRmiHostName, ShareRMIService.RMI_NAME);
        String result;
        if (contextId < 0) {
            if (isShareURL(token)) {
                token = extractTokenFromURL(token);
            }
            result = rmiService.listShares(token);
        } else {
            if (guestId > 0) {
                result = rmiService.listShares(contextId, guestId);
            } else {
                result = rmiService.listShares(contextId);
            }
        }
        System.out.println(result);
        return null;
    }

    private Pair<String, String> parseToken(String token) {
        String shareToken = null;
        String targetPath = null;
        String[] split = token.split("/", 2);
        if (split.length == 1) {
            shareToken = token;
        } else if (split.length == 2) {
            shareToken = split[0];
            targetPath = split[1];
        }

        return new Pair<String, String>(shareToken, targetPath);
    }

    private final Pattern PATTERN = Pattern.compile("\\Ahttps?:\\/\\/.*?\\/ajax\\/share\\/(.*?)\\z");

    private boolean isShareURL(String url) {
        Matcher m = PATTERN.matcher(url);
        return m.matches();
    }

    private String extractTokenFromURL(String url) {
        Matcher m = PATTERN.matcher(url);
        if (m.matches()) {
            return m.group(1);
        }
        return url;
    }

}
