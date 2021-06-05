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

package com.openexchange.xing.json;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.xing.json.actions.AbstractXingAction;
import com.openexchange.xing.json.actions.ChangeStatusAction;
import com.openexchange.xing.json.actions.CommentActivityAction;
import com.openexchange.xing.json.actions.ContactRequestAction;
import com.openexchange.xing.json.actions.CreateProfileAction;
import com.openexchange.xing.json.actions.DeleteActivityAction;
import com.openexchange.xing.json.actions.DeleteCommentActivityAction;
import com.openexchange.xing.json.actions.FindByMailRequestAction;
import com.openexchange.xing.json.actions.FindByMailsRequestAction;
import com.openexchange.xing.json.actions.GetActivityLikesAction;
import com.openexchange.xing.json.actions.GetCommentsActivityAction;
import com.openexchange.xing.json.actions.InviteRequestAction;
import com.openexchange.xing.json.actions.LikeActivityAction;
import com.openexchange.xing.json.actions.NewsFeedAction;
import com.openexchange.xing.json.actions.RevokeContactRequestAction;
import com.openexchange.xing.json.actions.ShareActivityAction;
import com.openexchange.xing.json.actions.ShareLinkAction;
import com.openexchange.xing.json.actions.ShowActivityAction;
import com.openexchange.xing.json.actions.UnlikeActivityAction;
import com.openexchange.xing.json.actions.UserFeedAction;


/**
 * {@link XingActionFactory} - The XING action factory.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class XingActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AbstractXingAction> actions;

    /**
     * Initializes a new {@link XingActionFactory}.
     */
    public XingActionFactory(final ServiceLookup serviceLookup) {
        super();
        actions = new ConcurrentHashMap<String, AbstractXingAction>(24, 0.9f, 1);
        actions.put("invite", new InviteRequestAction(serviceLookup));
        actions.put("contact_request", new ContactRequestAction(serviceLookup));
        actions.put("revoke_contact_request", new RevokeContactRequestAction(serviceLookup));
        actions.put("newsfeed", new NewsFeedAction(serviceLookup));
        actions.put("userfeed", new UserFeedAction(serviceLookup));
        actions.put("create", new CreateProfileAction(serviceLookup));
        actions.put("comment", new CommentActivityAction(serviceLookup));
        actions.put("get_comments", new GetCommentsActivityAction(serviceLookup));
        actions.put("delete_comment", new DeleteCommentActivityAction(serviceLookup));
        actions.put("like", new LikeActivityAction(serviceLookup));
        actions.put("unlike", new UnlikeActivityAction(serviceLookup));
        actions.put("get_likes", new GetActivityLikesAction(serviceLookup));
        actions.put("change_status", new ChangeStatusAction(serviceLookup));
        actions.put("share_link", new ShareLinkAction(serviceLookup));
        actions.put("show_activity", new ShowActivityAction(serviceLookup));
        actions.put("share_activity", new ShareActivityAction(serviceLookup));
        actions.put("delete_activity", new DeleteActivityAction(serviceLookup));
        actions.put("find_by_mail", new FindByMailRequestAction(serviceLookup));
        actions.put("find_by_mails", new FindByMailsRequestAction(serviceLookup));
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }
}
