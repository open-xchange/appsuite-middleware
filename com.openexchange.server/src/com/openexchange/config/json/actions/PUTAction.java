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

package com.openexchange.config.json.actions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.json.ConfigAJAXRequest;
import com.openexchange.config.json.ConfigActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingExceptionCodes;
import com.openexchange.groupware.settings.impl.AbstractSetting;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.settings.impl.SettingStorage;
import com.openexchange.html.HtmlService;
import com.openexchange.java.HTMLDetector;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PUTAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OAuthAction(ConfigActionFactory.OAUTH_WRITE_SCOPE)
public final class PUTAction extends AbstractConfigAction {

    /** The paths to ignore */
    private final Set<String> ignorees;

    /**
     * Initializes a new {@link PUTAction}.
     */
    public PUTAction(final ServiceLookup services) {
        super(services);
        // Load paths to ignore
        this.ignorees = loadIgnorees();
    }

    private Set<String> loadIgnorees() {
        try {
            final ConfigurationService service = services.getService(ConfigurationService.class);
            final Set<String> ignorees = new HashSet<String>(16);
            String text = service.getText("appsuite.properties");
            if (!com.openexchange.java.Strings.isEmpty(text)) {
                for (final String line : SPLIT.split(text, 0)) {
                    if (!isComment(line)) {
                        final int pos = line.indexOf('=');
                        if (pos > 0) {
                            final String sPath = preparePath(line.substring(0, pos));
                            final int keyPos = sPath.lastIndexOf('/');
                            final String path = sPath.substring(0, keyPos);
                            if (null != path) {
                                ignorees.add('/' + path);
                                ignorees.add("/meta/" + path);
                            }
                        }
                    }
                }
            }
            text = service.getText("paths.perfMap");
            if (!com.openexchange.java.Strings.isEmpty(text)) {
                for (final String line : SPLIT.split(text, 0)) {
                    if (!isComment(line)) {
                        final int pos = line.indexOf('>');
                        if (pos > 0) {
                            final String sPath = preparePath(line.substring(pos + 1));
                            final int keyPos = sPath.lastIndexOf('/');
                            final String path = sPath.substring(0, keyPos);
                            if (null != path) {
                                ignorees.add('/' + path);
                                ignorees.add("/meta/" + path);
                            }
                        }
                    }
                }
            }
            return ignorees;
        } catch (final Exception e) {
            return Collections.emptySet();
        }
    }

    @Override
    protected AJAXRequestResult perform(final ConfigAJAXRequest req) throws OXException, JSONException {
        Object data = req.getData();
        if (null == data) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        String value = data.toString(); // Unparse
        if (value.length() > 0 && value.charAt(0) == '"') {
            value = value.substring(1);
        }
        if (value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }
        String path = req.getRequest().getSerlvetRequestURI();
        if (path.length() > 0 && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        ServerSession session = req.getSession();
        SettingStorage stor = SettingStorage.getInstance(session);
        {
            Setting setting = ConfigTree.getInstance().getSettingByPath(path);
            setting.setSingleValue(value);
            UserSettingMailStorage.getInstance().removeUserSettingMail(session.getUserId(), session.getContext());
            saveSettingWithSubs(stor, setting);
        }

        return getJSONNullResult();
    }

    /**
     * Splits a value for a not leaf setting into its subsettings and stores them.
     *
     * @param storage setting storage.
     * @param setting actual setting.
     * @throws OXException if an error occurs.
     * @throws JSONException if the json object can't be parsed.
     */
    private void saveSettingWithSubs(final SettingStorage storage, final Setting setting) throws OXException, JSONException {
        if (setting.isLeaf()) {
            final String path = setting.getPath();
            if (!ignorees.contains(path) && (path.indexOf("/io.ox/core") < 0)) {
                Object value = setting.getSingleValue();
                if (value != null) {
                    if (JSONArray.class.isInstance(value)) {
                        final JSONArray array = (JSONArray) value;
                        if (array.length() == 0) {
                            setting.setEmptyMultiValue();
                        } else {
                            for (int i = 0; i < array.length(); i++) {
                                setting.addMultiValue(array.getString(i));
                            }
                        }
                        setting.setSingleValue(null);
                    } else if (JSONObject.class.isInstance(value)) {
                        sanitizeJsonSetting(setting);
                    } else {
                        setting.setSingleValue(value.toString());
                    }
                }
                storage.save(setting);
            }
        } else {
            final JSONObject json = new JSONObject(setting.getSingleValue().toString());
            final Iterator<String> iter = json.keys();
            final StringBuilder sb = new StringBuilder(setting.getPath()).append(AbstractSetting.SEPARATOR);
            final int reset = sb.length();
            OXException exc = null;
            Next: while (iter.hasNext()) {
                final String key = iter.next();
                if (sb.length() > reset) {
                    sb.setLength(reset);
                }
                final String path = sb.append(key).toString();
                if (!ignorees.contains(path) && (path.indexOf("/io.ox/core") < 0)) {
                    Setting sub;
                    try {
                        sub = ConfigTree.getSettingByPath(setting, new String[] { key });
                    } catch (final OXException e) {
                        if (!SettingExceptionCodes.UNKNOWN_PATH.equals(e) && (path.indexOf("/io.ox/") < 0)) {
                            throw e;
                        }
                        // Swallow
                        continue Next;
                    }
                    sub.setSingleValue(json.get(key));
                    try {
                        // Catch single exceptions if GUI writes not writable fields.
                        saveSettingWithSubs(storage, sub);
                    } catch (final OXException e) {
                        exc = e;
                    }
                }
            }
            if (null != exc) {
                throw exc;
            }
        }
    }

    private static final Pattern P_TAG_BODY = Pattern.compile("(?:\r?\n)?</?body[^<]*>(?:\r?\n)?", Pattern.CASE_INSENSITIVE);

    /** Sanitizes possible JSON setting */
    public static void sanitizeJsonSetting(final Setting setting) {
        try {
            final JSONObject jConfig = (JSONObject) setting.getSingleValue();
            boolean saveBack = false;
            final JSONObject jMailConfig = jConfig.optJSONObject("mail");
            if (null != jMailConfig && jMailConfig.hasAndNotNull("signatures")) {
                final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                if (null != htmlService) {
                    final JSONArray jSignatures = jMailConfig.getJSONArray("signatures");
                    final int length = jSignatures.length();
                    for (int i = 0; i < length; i++) {
                        final JSONObject jSignature = jSignatures.getJSONObject(i);
                        String content = jSignature.optString("signature_text", null);
                        if (null != content && HTMLDetector.containsHTMLTags(content, true)) {
                            content = htmlService.sanitize(content, null, false, null, null);
                            content = P_TAG_BODY.matcher(content).replaceAll("");
                            jSignature.put("signature_text", content);
                            saveBack = true;
                        }
                    }
                }

            }
            if (saveBack) {
                setting.setSingleValue(jConfig.toString());
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    // ------------------------------------ HELPER ---------------------------------------

    private static final Pattern SPLIT = Pattern.compile("\r?\n");

    private static final Pattern SLASHES = Pattern.compile(Pattern.quote("//"));
    private static String preparePath(final String path) {
        if (null == path) {
            return path;
        }
        return SLASHES.matcher(path.trim()).replaceAll("/");
    }

    private static final Pattern COMMENT = Pattern.compile("^\\s*[!#]");
    private static boolean isComment(final String line) {
        if (com.openexchange.java.Strings.isEmpty(line)) {
            return true;
        }
        return COMMENT.matcher(line).find();
    }
}
