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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.xing;

import java.io.IOException;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.xing.actions.ChangeStatusRequest;
import com.openexchange.ajax.xing.actions.ChangeStatusResponse;
import com.openexchange.ajax.xing.actions.CommentActivityRequest;
import com.openexchange.ajax.xing.actions.CommentActivityResponse;
import com.openexchange.ajax.xing.actions.DeleteActivityRequest;
import com.openexchange.ajax.xing.actions.DeleteActivityResponse;
import com.openexchange.ajax.xing.actions.DeleteCommentRequest;
import com.openexchange.ajax.xing.actions.DeleteCommentResponse;
import com.openexchange.ajax.xing.actions.GetCommentsRequest;
import com.openexchange.ajax.xing.actions.GetCommentsResponse;
import com.openexchange.ajax.xing.actions.GetLikesRequest;
import com.openexchange.ajax.xing.actions.GetLikesResponse;
import com.openexchange.ajax.xing.actions.LikeActivityRequest;
import com.openexchange.ajax.xing.actions.LikeActivityResponse;
import com.openexchange.ajax.xing.actions.ShareActivityRequest;
import com.openexchange.ajax.xing.actions.ShareActivityResponse;
import com.openexchange.ajax.xing.actions.ShareLinkRequest;
import com.openexchange.ajax.xing.actions.ShareLinkResponse;
import com.openexchange.ajax.xing.actions.ShowActivityRequest;
import com.openexchange.ajax.xing.actions.ShowActivityResponse;
import com.openexchange.ajax.xing.actions.UnlikeActivityRequest;
import com.openexchange.ajax.xing.actions.UnlikeActivityResponse;
import com.openexchange.ajax.xing.actions.UserFeedRequest;
import com.openexchange.exception.OXException;
import com.openexchange.xing.UserField;

/**
 * {@link ActivityTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class ActivityTest extends AbstractAJAXSession {

	private final String XING_OWNER = "dimitribronkowitsch@googlemail.com";
	
    /**
     * Initializes a new {@link ActivityTest}.
     *
     * @param name
     */
    public ActivityTest(String name) {
        super(name);
    }

    /**
     * Test like an activity
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void testLikesActivity() throws OXException, IOException, JSONException {
        final String activityId = getActivityIdContainingPermission("LIKE");

        final LikeActivityRequest likeRequest = new LikeActivityRequest(activityId, true);
        final LikeActivityResponse likeResponse = client.execute(likeRequest);
        assertNotNull(likeResponse);

        final GetLikesRequest getLikesRequest = new GetLikesRequest(activityId, -1, -1, new int[0], true);
        final GetLikesResponse getLikesResponse = client.execute(getLikesRequest);
        assertNotNull(getLikesResponse);

        final UnlikeActivityRequest unlikeRequest = new UnlikeActivityRequest(activityId, true);
        final UnlikeActivityResponse unlikeResponse = client.execute(unlikeRequest);
        assertNotNull(unlikeResponse);
    }

    /**
     * Tests to create, show and delete an activity stream
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void testCreateDeleteStatusMessage() throws OXException, IOException, JSONException {
        final ChangeStatusRequest createRequest = new ChangeStatusRequest("My new status", true);
        final ChangeStatusResponse createResponse = client.execute(createRequest);
        assertNotNull(createResponse);
        assertTrue((Boolean) createResponse.getData());
        deleteActivity(false);
    }


    /**
     * Tests the show activity action with user fields
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void testShowActivityWithUserFields() throws OXException, IOException, JSONException {
        JSONObject json = (JSONObject) client.execute(new UserFeedRequest(XING_OWNER, -1, -1, new int[0], true)).getData();
        assertNotNull("No activity available", json.getJSONArray("network_activities").getJSONObject(0));
        String activityId = json.getJSONArray("network_activities").getJSONObject(0).getJSONArray("ids").getString(0);
        final int[] uf = { UserField.FIRST_NAME.ordinal(), UserField.LAST_NAME.ordinal(), UserField.DISPLAY_NAME.ordinal() };
        final ShowActivityRequest request = new ShowActivityRequest(activityId, uf, true);
        final ShowActivityResponse response = client.execute(request);
        assertNotNull(response);
        JSONObject jsonResponse = (JSONObject) response.getData();
        assertNotNull(jsonResponse.get("activities"));
    }

    /**
     * Tests the show activity action
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void testShowActivity() throws OXException, IOException, JSONException {
        JSONObject json = (JSONObject) client.execute(new UserFeedRequest(XING_OWNER, -1, -1, new int[0], true)).getData();
        assertNotNull("No activity available", json.getJSONArray("network_activities").getJSONObject(0));
        String activityId = json.getJSONArray("network_activities").getJSONObject(0).getJSONArray("ids").getString(0);
        final ShowActivityRequest request = new ShowActivityRequest(activityId, new int[0], true);
        final ShowActivityResponse response = client.execute(request);
        assertNotNull(response);
        JSONObject jsonResponse = (JSONObject) response.getData();
        assertNotNull(jsonResponse.get("activities"));
    }

    /**
     * Tests the delete activity action
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    private void deleteActivity() throws OXException, IOException, JSONException {
        deleteActivity(true);
    }

    /**
     * Tests the delete activity action
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    private void deleteActivity(boolean failOnError) throws OXException, IOException, JSONException {
        JSONObject json = (JSONObject) client.execute(new UserFeedRequest(XING_OWNER, -1, -1, new int[0], true)).getData();
        String activityId = json.getJSONArray("network_activities").getJSONObject(0).getJSONArray("ids").getString(0);
        final DeleteActivityRequest request = new DeleteActivityRequest(activityId, true);
        final DeleteActivityResponse response = client.execute(request);
        assertNotNull(response);
        assertTrue((Boolean) response.getData());
    }

    /**
     * Test to share a link on activity stream; also delete this activity
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void testShareLinkAndDelete() throws OXException, IOException, JSONException {
        final ShareLinkRequest request = new ShareLinkRequest("http://www.google.de", true);
        final ShareLinkResponse response = client.execute(request);
        assertNull("Got an exception: " + response.getException(), response.getException());
        assertNotNull(response.getData());
        assertTrue((Boolean) response.getData());
    }

    /**
     * Test to share an activity; also delete this activity
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void testShareActivityAndDelete() throws OXException, IOException, JSONException {
        final ChangeStatusRequest createRequest = new ChangeStatusRequest("My new status", true);
        client.execute(createRequest);
        final String activityId = getActivityIdContainingPermission("SHARE");
        final ShareActivityRequest request = new ShareActivityRequest(activityId, "My shared activity", false);
        final ShareActivityResponse response = client.execute(request);
        assertNotNull(response);

        if (response.hasError()) {
            final OXException exc = response.getException();
            if (!"XING".equals(exc.getPrefix()) || 2 != exc.getCode()) {
                fail(exc.getMessage());
            }
        }
        assertNull("Got an exception: " + response.getException(), response.getException());
        assertNotNull(response.getData());
        assertTrue((Boolean) response.getData());
        deleteActivity(false);
    }

    /**
     * Tests the action to create, delete an get comment on an activity
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void testCommentCRUD() throws OXException, IOException, JSONException {
        // Begin create comment
        final String activityId = getActivityIdContainingPermission("COMMENT");
        final CommentActivityRequest createRequest = new CommentActivityRequest(activityId, UUID.randomUUID().toString(), true);
        final CommentActivityResponse createResponse = client.execute(createRequest);
        assertNotNull(createResponse);
        // Begin get comment
        JSONObject json = (JSONObject) client.execute(new UserFeedRequest(XING_OWNER, -1, -1, new int[0], true)).getData();
        final GetCommentsRequest getRequest = new GetCommentsRequest(activityId, -1, -1, new int[0], true);
        final GetCommentsResponse getResponse = client.execute(getRequest);
        assertNotNull(getResponse);
        // Begin delete comment - only delete if comments are available
        int commentAmount =json.getJSONArray("network_activities").getJSONObject(0).getJSONObject("comments").getInt("amount");
        if(commentAmount > 0) {
            String commentId = json.getJSONArray("network_activities").getJSONObject(0).getJSONObject("comments").getJSONArray(
                "latest_comments").getJSONObject(0).getString("id");
            final DeleteCommentRequest deleteRequest = new DeleteCommentRequest(activityId, commentId, false);
            final DeleteCommentResponse deleteResponse = client.execute(deleteRequest);
            assertNotNull(deleteResponse);
        }
    }

    /**
     * Gets an activityId which contains the needed possible actions.
     *
     * @param permission the permission which should be present. Possible permissions are COMMENT, LIKE, SHARE or IGNORE
     * @return an activity id containing the permission
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    private String getActivityIdContainingPermission(final String permission) throws OXException, IOException, JSONException {
        JSONObject json = (JSONObject) client.execute(new UserFeedRequest(XING_OWNER, -1, -1, new int[0], true)).getData();
        JSONArray networkActivities = json.getJSONArray("network_activities");
        assertNotNull(networkActivities);

        String activityId = "-1";
        boolean found = false;
        for (int i = 0; i < networkActivities.length(); i++) {
            JSONObject activity = (JSONObject) networkActivities.get(i);
            assertTrue("Attribute \"ids\" not found", activity.hasAndNotNull("ids"));
            assertTrue("Attribute \"possible_actions\" not found", activity.hasAndNotNull("possible_actions"));
            JSONArray possibleActionsJSON = activity.getJSONArray("possible_actions");
            for (int j = 0; j < possibleActionsJSON.length(); j++) {
                String possibleActionObj = (String) possibleActionsJSON.get(j);
                if (possibleActionObj.equals(permission)) {
                    found = true;
                    break;
                }
            }
            // found an activity id with needed permissions
            if (found) {
                activityId = activity.getJSONArray("ids").getString(0);
                break;
            }
        }
        assertTrue("An activity with permission " + permission + " could not being found ", found);
        return activityId;
    }
}
