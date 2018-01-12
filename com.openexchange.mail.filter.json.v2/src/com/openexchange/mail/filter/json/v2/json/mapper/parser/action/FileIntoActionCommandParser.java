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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.action;

import java.util.ArrayList;
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.ActionCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.fields.MoveActionField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.simplified.SimplifiedAction;
import com.openexchange.mail.filter.json.v2.mapper.ArgumentUtil;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailfilter.properties.MailFilterProperty;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.sun.mail.imap.protocol.BASE64MailboxDecoder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;

/**
 * {@link FileIntoActionCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FileIntoActionCommandParser extends AbstractActionCommandParser {

    /**
     * Initializes a new {@link FileIntoActionCommandParser}.
     */
    public FileIntoActionCommandParser(ServiceLookup services) {
        super(services, Commands.FILEINTO);
    }

    @Override
    public ActionCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        final ArrayList<Object> argList = new ArrayList<Object>();

        Boolean copy = jsonObject.optBoolean(MoveActionField.copy.name(), false);
        if (copy || jsonObject.getString("id").equals(SimplifiedAction.COPY.getCommandName())) {
            argList.add(ArgumentUtil.createTagArgument(MoveActionField.copy.name()));
        }

        String stringParam = CommandParserJSONUtil.getString(jsonObject, MoveActionField.into.name(), Commands.FILEINTO.getJsonName());

        final String folderName;
        if (useUTF7Encoding()) {
            folderName = BASE64MailboxEncoder.encode(MailFolderUtility.prepareMailFolderParam(stringParam).getFullname());
        } else {
            folderName = MailFolderUtility.prepareMailFolderParam(stringParam).getFullname();
        }

        argList.add(CommandParserJSONUtil.stringToList(folderName));
        ActionCommand result = new ActionCommand(Commands.FILEINTO, argList);
        if (copy || jsonObject.getString("id").equals(SimplifiedAction.COPY.getCommandName())) {
            result.addOptionalRequired(MoveActionField.copy.name());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(JSONObject jsonObject, ActionCommand actionCommand) throws JSONException, OXException {
        ArrayList<Object> arguments = actionCommand.getArguments();
        jsonObject.put(GeneralField.id.name(), actionCommand.getCommand().getJsonName());

        final String folderName;
        if (arguments.size() == 1) {
            if (useUTF7Encoding()) {
                folderName = BASE64MailboxDecoder.decode(((List<String>) arguments.get(0)).get(0));
            } else {
                folderName = ((List<String>) arguments.get(0)).get(0);
            }
            jsonObject.put(MoveActionField.into.name(), MailFolderUtility.prepareFullname(0, folderName));
        } else {
            String copyCommandString = ArgumentUtil.createTagArgument(MoveActionField.copy.name()).toString();
            if (actionCommand.getTagArguments().get(copyCommandString) != null) {
                jsonObject.put(GeneralField.id.name(), SimplifiedAction.COPY.getCommandName());
            }
            if (useUTF7Encoding()) {
                folderName = BASE64MailboxDecoder.decode(((List<String>) arguments.get(1)).get(0));
            } else {
                folderName = ((List<String>) arguments.get(1)).get(0);
            }
            jsonObject.put(MoveActionField.into.name(), MailFolderUtility.prepareFullname(0, folderName));
        }
    }

    /**
     * Helper method to fetch the value of the 'com.openexchange.mail.filter.useUTF7FolderEncoding' property
     *
     * @return The value of the 'com.openexchange.mail.filter.useUTF7FolderEncoding' property
     */
    private boolean useUTF7Encoding() {
        LeanConfigurationService config = services.getService(LeanConfigurationService.class);
        return config.getBooleanProperty(MailFilterProperty.useUTF7FolderEncoding);
    }
}
