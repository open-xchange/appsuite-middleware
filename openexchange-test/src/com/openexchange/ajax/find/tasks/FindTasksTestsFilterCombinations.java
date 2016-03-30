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

package com.openexchange.ajax.find.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.tasks.TasksFacetType;

/**
 * {@link FindTasksTests}
 * <p style="font-weight:bold; font-size:14px;">Matrix with Filter Combinations</p>
 * <table border="1">
 *  <tr style="font-weight: bold;"><td>&nbsp;</td><td>Type</td><td>Folder Type</td><td>Status</td><td>Participant</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">1</td><td>0</td><td>0</td><td>0</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">2</td><td>0</td><td>0</td><td>1</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">3</td><td>0</td><td>0</td><td>1</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">4</td><td>0</td><td>1</td><td>0</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">5</td><td>0</td><td>1</td><td>0</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">6</td><td>0</td><td>1</td><td>1</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">7</td><td>0</td><td>1</td><td>1</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">8</td><td>1</td><td>0</td><td>0</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">9</td><td>1</td><td>0</td><td>0</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">10</td><td>1</td><td>0</td><td>1</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">11</td><td>1</td><td>0</td><td>1</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">12</td><td>1</td><td>1</td><td>0</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">13</td><td>1</td><td>1</td><td>0</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">14</td><td>1</td><td>1</td><td>1</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">15</td><td>1</td><td>1</td><td>1</td><td>1</td></tr>
 * </table>
 *
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FindTasksTestsFilterCombinations extends AbstractFindTasksTest {

    /**
     * Initializes a new {@link FindTasksTestsFilterCombinations}.
     * @param name
     */
    public FindTasksTestsFilterCombinations(String name) {
        super(name);
    }

    /**
     * Get all relevant filters for the given combination
     * @param combination as char array
     * @return all relevant filters for that combination
     */
    private static final List<ActiveFacet> getRelevantActiveFacets(char[] combination) {
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        ArrayUtils.reverse(combination);
        for (int i = 0; i < combination.length; i++) {
            if (combination[i] == '1')
                facets.addAll(FindTasksTestEnvironment.getInstance().getLoActiveFacets().get(i));
        }
        return facets;
    }

    /**
     * Static test
     * Test with more external participants
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testMoreExternalParticipants() throws OXException, IOException, JSONException {
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>(2);
        List<String> queries = new ArrayList<String>(3);
        queries.add("olox20@premium");
        queries.add("thorben.betten@premium");
        queries.add("x_x_x_x_x_x_x@asdasdasda");
        Filter filter = new Filter(Collections.singletonList("participant"), queries);
        facets.add(new ActiveFacet(TasksFacetType.TASK_PARTICIPANTS, "contact/1/464373", filter));
        assertResults(0, facets);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////// TEST CASES BEGIN //////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test filter combination 1, i.e. with Participants
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithParticipant() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(1).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>(3);
        facets.add(f.get(0));   //participant a
        assertResults(5, facets);

        facets.clear();
        facets.add(f.get(1)); //participant b
        assertResults(4, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        assertResults(4, facets);

        facets.clear();
        facets.add(f.get(2)); //ext participant
        facets.add(FindTasksTestEnvironment.createGlobalFacet());
        assertResults(2, facets);

        assertResults(1, f); //all participants (a+b+ext)
    }

    /**
     * Test filter combination 2, i.e. with status
     * - NOT STARTED: 9
     * - IN PROGRESS: 6
     * - DONE:        6
     * - WAITING:     5
     * - DEFERRED:    5
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    @Test
    public void testWithStatus() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(2).toCharArray());
        assertResults(9, Collections.singletonList(f.get(0)));
        assertResults(6, Collections.singletonList(f.get(1)));
        assertResults(6, Collections.singletonList(f.get(2)));
        assertResults(5, Collections.singletonList(f.get(3)));
        assertResults(5, Collections.singletonList(f.get(4)));
    }

    /**
     * Test filter combination 3, i.e. with status and participants
     *
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    @Test
    public void testWithStatusAndParticipants() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(3).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();

        // Playing around with some multi-purpose code (maybe difficult to debug the test case :-/ )
        /*int[][] expectedResultsMatrix = { {3, 1, 1, 0, 0}, //participant a
                                          {2, 1, 1, 0, 0}, //participant b
                                          {2, 1, 1, 0, 0}, //participant a AND b
                                          {1, 1, 0, 0, 0}, //participant a and external
                                          {0, 1, 0, 0, 0}  //all participants (2int + 1ext)
                                        };

        int[][] participantCombinationMatrix = { {0},
                                                 {1},
                                                 {0, 1},
                                                 {0, 2},
                                                 {0, 1, 2}
                                                };

        for (int i = 0; i < expectedResultsMatrix.length; i++) {
            int k = 0;
            facets.clear();
            int r = participantCombinationMatrix[i].length;
            for (int py = 0; py < r; py++) {
                facets.add(f.get(participantCombinationMatrix[i][py]));
            }
            for(int j = 3; j < f.size(); j++) {
                if (j > 3)
                    facets.remove(r);
                facets.add(f.get(j));
                assertResults(expectedResultsMatrix[i][k++], facets);
            }
            r++;
        }*/

        facets.add(f.get(0));//participant a
        facets.add(f.get(3));//not started
        assertResults(3, facets);

        facets.remove(1);
        facets.add(f.get(4)); //in progress
        assertResults(1, facets);

        facets.remove(1);
        facets.add(f.get(6)); //waiting
        assertResults(0, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(3)); //not started
        assertResults(2, facets);

        facets.remove(2);
        facets.add(f.get(4));//in progress
        assertResults(1, facets);

        facets.remove(2);
        facets.add(f.get(7));//deferred
        assertResults(0, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(2)); //external participant
        facets.add(f.get(4));//in progress
        assertResults(1, facets);

        facets.remove(2);
        facets.add(f.get(5));//done
        assertResults(0, facets);


        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(2)); //external participant
        facets.add(f.get(4));//in progress
        assertResults(1, facets);

        facets.remove(3);
        facets.add(f.get(6));//waiting
        assertResults(0, facets);
    }

    /**
     * Test filter combination 4, i.e. with folder type
     * - in PRIVATE: 10
     * - in PUBLIC: 10
     * - in SHARED: 11
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    @Test
    public void testWithFolderType() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(4).toCharArray());
        assertResults(10, Collections.singletonList(f.get(0))); //private
        assertResults(10, Collections.singletonList(f.get(1))); //public
        assertResults(11, Collections.singletonList(f.get(2))); //shared
    }

    /**
     * Test filter combination 5, i.e. with folder type and participant
     *
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    @Test
    public void testWithFolderTypeAndParticipant() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(5).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(3)); //private
        assertResults(4, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(4)); //public
        assertResults(0, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(5)); //shared
        assertResults(1, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(3)); //private
        assertResults(3, facets);

        facets.remove(2);
        facets.add(f.get(4)); //public
        assertResults(0, facets);

        facets.remove(2);
        facets.add(f.get(5)); //shared
        assertResults(1, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(2)); //ext participant
        facets.add(f.get(3)); //private
        assertResults(1, facets);

        facets.remove(3);
        facets.add(f.get(4)); //public
        assertResults(0, facets);

        facets.remove(3);
        facets.add(f.get(5)); //shared
        assertResults(0, facets);
    }

    /**
     * Test filter combination 6, i.e. with folder type and status
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithFolderTypeAndStatus()  throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(6).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(f.get(0)); //not started
        facets.add(f.get(5)); //private
        assertResults(4, facets);

        facets.remove(1);
        facets.add(f.get(6)); //public
        assertResults(2, facets);

        facets.remove(1);
        facets.add(f.get(7)); //shared
        assertResults(3, facets);

        facets.clear();
        facets.add(f.get(1)); //in progress
        facets.add(f.get(5)); //private
        assertResults(2, facets);

        facets.remove(1);
        facets.add(f.get(6)); //public
        assertResults(2, facets);

        facets.remove(1);
        facets.add(f.get(7)); //shared
        assertResults(2, facets);

        facets.clear();
        facets.add(f.get(2)); //done
        facets.add(f.get(5)); //private
        assertResults(2, facets);

        facets.remove(1);
        facets.add(f.get(6)); //public
        assertResults(2, facets);

        facets.remove(1);
        facets.add(f.get(7)); //shared
        assertResults(2, facets);

        facets.clear();
        facets.add(f.get(3)); //waiting
        facets.add(f.get(5)); //private
        assertResults(1, facets);

        facets.remove(1);
        facets.add(f.get(6)); //public
        assertResults(2, facets);

        facets.remove(1);
        facets.add(f.get(7)); //shared
        assertResults(2, facets);

        facets.clear();
        facets.add(f.get(4)); //deferred
        facets.add(f.get(5)); //private
        assertResults(1, facets);

        facets.remove(1);
        facets.add(f.get(6)); //public
        assertResults(2, facets);

        facets.remove(1);
        facets.add(f.get(7)); //shared
        assertResults(2, facets);
    }

    /**
     * Test filter combination 7, i.e. with folder type and status and participant
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithFolderTypeAndStatusAndParticipant() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(7).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(3)); //status not started
        facets.add(f.get(8)); //private
        assertResults(2, facets);

        facets.remove(2);
        facets.add(f.get(9)); //public
        assertResults(0, facets);

        facets.remove(2);
        facets.add(f.get(10)); //shared
        assertResults(1, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(5)); //status done
        facets.add(f.get(8)); //private
        assertResults(1, facets);

        facets.remove(2);
        facets.add(f.get(9)); //public
        assertResults(0, facets);

        facets.remove(2);
        facets.add(f.get(10)); //shared
        assertResults(0, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(3)); //status not started
        facets.add(f.get(8)); //private
        assertResults(1, facets);

        facets.remove(3);
        facets.add(f.get(9)); //public
        assertResults(0, facets);

        facets.remove(3);
        facets.add(f.get(10)); //shared
        assertResults(1, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(5)); //status done
        facets.add(f.get(8)); //private
        assertResults(1, facets);

        facets.remove(3);
        facets.add(f.get(9)); //public
        assertResults(0, facets);

        facets.remove(3);
        facets.add(f.get(10)); //shared
        assertResults(0, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(2)); //ext participant
        facets.add(f.get(4)); //status in progress
        facets.add(f.get(8)); //private
        assertResults(1, facets);

        facets.remove(4);
        facets.add(f.get(9)); //public
        assertResults(0, facets);

        facets.remove(4);
        facets.add(f.get(10)); //shared
        assertResults(0, facets);
    }

    /**
     * Test filter combination 8, i.e. with task type
     * - SINGLE: 29
     * - SERIES:  2
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    @Test
    public void testWithType() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(8).toCharArray());
        assertResults(29, Collections.singletonList(f.get(0)), -1, 30);
        assertResults(2, Collections.singletonList(f.get(1)));
    }

    /**
     * Test filter combination 9, i.e. with type and participant
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithTypeAndParticipant() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(9).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(4)); //series
        assertResults(2, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(2)); //ext participant
        facets.add(f.get(3)); //single
        assertResults(1, facets);
    }

    /**
     * Test filter combination 10, i.e. with type and status
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithTypeAndStatus() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(10).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(f.get(1)); //status ion progress
        facets.add(f.get(5)); //single
        assertResults(6, facets);

        facets.clear();
        facets.add(f.get(0)); //status not started
        facets.add(f.get(6)); //series
        assertResults(2, facets);

        facets.clear();
        facets.add(f.get(4)); //status deferred
        facets.add(f.get(5)); //single
        assertResults(5, facets);
    }

    /**
     * Test filter combination 11, i.e. with type and status and participants
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithTypeAndStatusAndParticipant() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(11).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(3)); //not started
        facets.add(f.get(9)); //series
        assertResults(2, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(2)); //ext participant
        facets.add(f.get(3)); //not started
        facets.add(f.get(8)); //single
        assertResults(1, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(5)); //done
        facets.add(f.get(8)); //single
        assertResults(1, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(2)); //ext participant
        facets.add(f.get(7)); //deferred
        facets.add(f.get(9)); //series
        assertResults(0, facets);
    }

    /**
     * Test filter combination 12, i.e. with type and folder type
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithTypeAndFolderType() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(12).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(f.get(0)); //private
        facets.add(f.get(3)); //single
        assertResults(9, facets);

        facets.clear();
        facets.add(f.get(0)); //private
        facets.add(f.get(4)); //series
        assertResults(1, facets);

        facets.clear();
        facets.add(f.get(1)); //public
        facets.add(f.get(4)); //series
        assertResults(0, facets);

        facets.clear();
        facets.add(f.get(2)); //shared
        facets.add(f.get(4)); //single
        assertResults(1, facets);
    }

    /**
     * Test filter combination 13, i.e. with type and folder type and participant
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithTypeAndFolderTypeAndParticipant() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(13).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(3)); //private
        facets.add(f.get(6)); //single
        assertResults(3, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(5)); //shared
        facets.add(f.get(7)); //series
        assertResults(1, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(2)); //ext participant
        facets.add(f.get(4)); //public
        facets.add(f.get(6)); //single
        assertResults(0, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(2)); //ext participant
        facets.add(f.get(3)); //private
        facets.add(f.get(6)); //single
        assertResults(1, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(2)); //ext participant
        facets.add(f.get(3)); //private
        facets.add(f.get(6)); //single
        assertResults(2, facets);
    }

    /**
     * Test filter combination 14, i.e. with type and folder type and status
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithTypeAndFolderTypeAndStatus() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(14).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(f.get(0)); //not started
        facets.add(f.get(5)); //private
        facets.add(f.get(8)); //single
        assertResults(3, facets);

        facets.clear();
        facets.add(f.get(1)); //in progress
        facets.add(f.get(6)); //public
        facets.add(f.get(9)); //series
        assertResults(0, facets);

        facets.clear();
        facets.add(f.get(0)); //not started
        facets.add(f.get(7)); //shared
        facets.add(f.get(9)); //series
        assertResults(1, facets);
    }

    /**
     * Test filter combination 15, i.e. with all 4 filters
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithTypeAndFolderTypeAndStatusAndParticipant()  throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(15).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(3)); //not started
        facets.add(f.get(8)); //private
        facets.add(f.get(11)); //single
        assertResults(1, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(4)); //in progress
        facets.add(f.get(9)); //shared
        facets.add(f.get(11)); //single
        assertResults(0, facets);

        facets.clear();
        facets.add(f.get(0)); //participant a
        facets.add(f.get(1)); //participant b
        facets.add(f.get(3)); //not started
        facets.add(f.get(10)); //shared
        facets.add(f.get(12)); //series
        assertResults(1, facets);
    }
}
