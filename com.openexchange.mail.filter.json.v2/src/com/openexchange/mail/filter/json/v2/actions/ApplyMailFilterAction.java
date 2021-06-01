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

package com.openexchange.mail.filter.json.v2.actions;

import static com.openexchange.java.Autoboxing.I;
import java.rmi.server.UID;
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.EnqueuableAJAXActionService;
import com.openexchange.ajax.requesthandler.jobqueue.JobKey;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageMailFilterApplication;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFilterResult;
import com.openexchange.mail.filter.json.v2.Action;
import com.openexchange.mail.filter.json.v2.json.RuleParser;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.osgi.Tools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ApplyMailFilterAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class ApplyMailFilterAction extends AbstractMailFilterAction {

    public static final Action ACTION = Action.APPLY;
    private static final String SCRIPT_ID = "id";
    private static final String FOLDER = "folderId";

    private final RuleParser ruleParser;


    /**
     * Initializes a new {@link ApplyMailFilterAction}.
     */
    public ApplyMailFilterAction(RuleParser ruleParser, ServiceLookup services) {
        super(services);
        this.ruleParser = ruleParser;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        if (!(request.getData() != null || request.containsParameter(SCRIPT_ID))) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        final MailFilterService mailFilterService = Tools.requireService(MailFilterService.class, services);
        try {
            Rule rule = null;
            Credentials credentials = getCredentials(session, request);
            if (request.getData() != null) {
                rule = ruleParser.parse(getJSONBody(request.getData()), ServerSessionAdapter.valueOf(request.getSession()));
            } else {
                int ruleId = request.getIntParameter(SCRIPT_ID);
                rule = mailFilterService.getFilterRule(credentials, ruleId);
                if (rule == null) {
                    throw MailFilterExceptionCode.NO_SUCH_ID.create(I(ruleId), I(session.getUserId()), I(session.getContextId()));
                }
            }
            rule.setCommented(false); // ensures that the required command is properly added
            String folder = request.getParameter(FOLDER);
            String stringRule = mailFilterService.convertToString(credentials,rule);
            return applyRule(session, stringRule, Strings.isEmpty(folder) ? "INBOX" : folder);
        } catch (SieveException e) {
            throw MailFilterExceptionCode.handleSieveException(e);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, e.getMessage());
        }
    }

    private AJAXRequestResult applyRule(ServerSession session, String rule, String folder) throws OXException, JSONException {
        MailServletInterface mailInterface = MailServletInterface.getInstance(session);
        try {
            mailInterface.openFor(folder);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = mailInterface.getMailAccess();
            MailConfig mailConfig = mailAccess.getMailConfig();

            {
                MailCapabilities capabilities = mailConfig.getCapabilities();
                if (!capabilities.hasMailFilterApplication()) {
                    throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
                }
            }

            IMailMessageStorageMailFilterApplication mailFilterMessageStorage;
            {
                IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                mailFilterMessageStorage = messageStorage.supports(IMailMessageStorageMailFilterApplication.class);
                if (null == mailFilterMessageStorage) {
                    throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
                }
                if (!mailFilterMessageStorage.isMailFilterApplicationSupported()) {
                    throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
                }
            }

            FullnameArgument fullnameArgument = MailFolderUtility.prepareMailFolderParam(folder);
            List<MailFilterResult> results = mailFilterMessageStorage.applyMailFilterScript(fullnameArgument.getFullName(), rule, null, true);
            JSONArray jResults = new JSONArray(results.size());
            for (MailFilterResult result : results) {
                JSONObject jResult = new JSONObject(6);
                jResult.put(AJAXServlet.PARAMETER_ID, result.getId());
                jResult.put(AJAXServlet.PARAMETER_FOLDERID, folder);
                if (result.hasErrors()) {
                    jResult.put("result", "ERRORS");
                    jResult.put("errors", result.getErrors());
                } else if (result.hasWarnings()) {
                    jResult.put("result", "WARNINGS");
                    jResult.put("warnings", result.getWarnings());
                } else {
                    jResult.put("result", "OK");
                }
                jResults.put(jResult);
            }
            return new AJAXRequestResult(jResults, "json");
        } finally {
            mailInterface.close(true);
        }
    }

    @Override
    public Result isEnqueueable(AJAXRequestData request, ServerSession session) throws OXException {
        JSONObject jKeyDesc = new JSONObject(4);
        if (!(request.getData() != null || request.containsParameter(SCRIPT_ID))) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        Object data = request.getData();
        String scriptId;
        if (data != null) {
            scriptId = new UID().toString();
        } else {
            scriptId = request.requireParameter(SCRIPT_ID);
        }
        String folderId = request.getParameter(FOLDER);
        try {
            jKeyDesc.put("module", "mailfilter/v2");
            jKeyDesc.put("action", "apply");
            jKeyDesc.put("folder", Strings.isEmpty(folderId) ? "INBOX" : folderId);
            jKeyDesc.put("script", scriptId);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }

        return EnqueuableAJAXActionService.resultFor(true, new JobKey(session.getUserId(), session.getContextId(), jKeyDesc.toString()), this);
    }

}
