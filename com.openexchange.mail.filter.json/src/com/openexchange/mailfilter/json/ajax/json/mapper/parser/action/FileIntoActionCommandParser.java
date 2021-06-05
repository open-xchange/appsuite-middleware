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

package com.openexchange.mailfilter.json.ajax.json.mapper.parser.action;

import java.util.ArrayList;
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.ActionCommand.Commands;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.fields.MoveActionField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ArgumentUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.mailfilter.properties.MailFilterProperty;
import com.openexchange.tools.session.ServerSession;
import com.sun.mail.imap.protocol.BASE64MailboxDecoder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;

/**
 * {@link FileIntoActionCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FileIntoActionCommandParser implements CommandParser<ActionCommand> {

    /**
     * Initialises a new {@link FileIntoActionCommandParser}.
     */
    public FileIntoActionCommandParser() {
        super();
    }

    @Override
    public ActionCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        final ArrayList<Object> argList = new ArrayList<Object>();

        boolean copy = jsonObject.optBoolean(MoveActionField.copy.name(), false);
        if (copy) {
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
        if (copy) {
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
                jsonObject.put(MoveActionField.copy.name(), true);
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
        // TODO: get for user?
        LeanConfigurationService config = Services.getService(LeanConfigurationService.class);
        return config.getBooleanProperty(MailFilterProperty.useUTF7FolderEncoding);
    }
}
