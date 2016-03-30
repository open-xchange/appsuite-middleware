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

package com.openexchange.xing.json;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.documentation.annotations.Module;
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
import com.openexchange.xing.json.actions.GetActivityLikesAction;
import com.openexchange.xing.json.actions.GetCommentsActivityAction;
import com.openexchange.xing.json.actions.FindByMailsRequestAction;
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
@Module(name = "xing", description = "Provides access to XING module.")
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

    @Override
    public Collection<? extends AJAXActionService> getSupportedServices() {
        return java.util.Collections.unmodifiableCollection(actions.values());
    }

}
