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

package com.openexchange.share.clt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.http.ParseException;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.Pair;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.impl.mbean.ShareMBean;

/**
 * {@link RemoveSharesCLT}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class RemoveSharesCLT extends AbstractMBeanCLI<Void> {

    private String contextId = "-1";

    private String guestId;

    private String token;

    private boolean iKnowWhatIamDoing = false;

    public RemoveSharesCLT() {
        super();
    }

    public static void main(String[] args) {
        new RemoveSharesCLT().execute(args);
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption("c")) {
            contextId = cmd.getOptionValue("c");
        }
        if (cmd.hasOption("i")) {
            guestId = cmd.getOptionValue("i");
        }
        if (cmd.hasOption("T")) {
            token = cmd.getOptionValue("T");
        }
        iKnowWhatIamDoing = cmd.hasOption("f");
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        if (null == contextId || contextId.isEmpty() || "-1".equals(contextId)) {
            //oxadminmaster required
            authenticator.doAuthentication(login, password);
        } else {
            //context admin required
            int cid;
            try {
                cid = Integer.parseInt(contextId);
                authenticator.doAuthentication(login, password, cid);
            } catch (NumberFormatException e) {
                throw new MBeanException(e);
            }
        }
    }

    @Override
    protected String getFooter() {
        return null;
    }

    @Override
    protected String getName() {
        return "removeshares";
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption("c", "context", true, "The context id.");
        options.addOption("i", "userid", true, "The id of the guest user.");
        options.addOption("T", "token", true, "Token or URL to remove.");
        options.addOption("f", "force", false, "Force removal of token.");
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        if ((null == contextId || contextId.isEmpty() || "-1".equals(contextId)) && (null == token || token.isEmpty())) {
            throw new MissingOptionException("ContextId and/or token is missing.");
        }
        int result = 0;
        ObjectName objectName = getObjectName(ShareMBean.class.getName(), ShareMBean.DOMAIN);
        ShareMBean mbean = MBeanServerInvocationHandler.newProxyInstance(mbsc, objectName, ShareMBean.class, false);
        try {
            if (null != token && !token.isEmpty()) {
                if (isShareURL(token)) {
                    token = extractTokenFromURL(token);
                }
                Pair<String, String> tokenAndPath = parseToken(token);
                String shareToken = tokenAndPath.getFirst();
                String targetPath = tokenAndPath.getSecond();
                if ((null == targetPath || "".equals(targetPath)) && !iKnowWhatIamDoing) {
                    throw new MissingOptionException("Seems like you supplied a token without a share path. If you want to remove"
                        + " all shares identified by this token use option -f/--force");
                }
                if (null != contextId && !contextId.isEmpty() && !"-1".equals(contextId)) {
                    result = mbean.removeShare(shareToken, targetPath, Integer.parseInt(contextId));
                } else {
                    result = mbean.removeShare(shareToken, targetPath);
                }
            } else if (null != contextId && !contextId.isEmpty() && !"-1".equals(contextId)) {
                if (null != guestId && !guestId.isEmpty()) {
                    result = mbean.removeShares(Integer.parseInt(contextId), Integer.parseInt(guestId));
                } else {
                    result = mbean.removeShares(Integer.parseInt(contextId));
                }
            }
        } catch (NumberFormatException e) {
            throw new ParseException("Cannot parse value: " + e.getMessage());
        }
        System.out.println(result + (result == 1 ? " share " : " shares ") + "removed.");
        return null;
    }

    private Pair<String, String> parseToken(String token) throws OXException {
        String shareToken = null;
        String targetPath = null;
        String[] split = token.split("/");
        if (split.length == 1) {
            shareToken = token;
        } else if (split.length == 2) {
            shareToken = split[0];
            targetPath = split[1];
        } else {
            throw ShareExceptionCodes.INVALID_TOKEN.create(token);
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
