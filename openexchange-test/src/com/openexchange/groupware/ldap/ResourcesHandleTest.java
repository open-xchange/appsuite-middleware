package com.openexchange.groupware.ldap;

import java.util.Iterator;
import java.util.Set;

import com.openexchange.groupware.contexts.TestContextImpl;
import com.openexchange.groupware.ldap.Factory;
import com.openexchange.groupware.ldap.ResourcesHandle;

import junit.framework.TestCase;

public class ResourcesHandleTest extends TestCase {

    private ResourcesHandle rh;
    
    protected void setUp() throws Exception {
        super.setUp();
        if (null == LdapTests.p) {
            LdapTests.init();
        }
        rh = Factory.newResourcesHandle(new TestContextImpl(), null);
    }

    protected void tearDown() throws Exception {
        rh.close();
        rh = null;
        super.tearDown();
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.existsGroup(String)'
     */
    public void testExistsGroup() throws Throwable {
        String resourceGroup1 = LdapTests.p.getProperty("resourceGroup1");
        assertTrue(rh.existsGroup(resourceGroup1));
        String nonExistentGroup = LdapTests.p.getProperty("nonExistentResourceGroup");
        assertFalse(rh.existsGroup(nonExistentGroup));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.existsResource(String)'
     */
    public void testExistsResource() {
        String resource1 = LdapTests.p.getProperty("resource1");
        assertTrue(rh.existsResource(resource1));
        String nonExistentResource = LdapTests.p.getProperty("nonExistentResource");
        assertFalse(rh.existsResource(nonExistentResource));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.getGroups()'
     */
    public void testGetResourceGroups() {
    	String resGrp01Id = LdapTests.p.getProperty("resourceGroup1");
    	String resGrp02ID = LdapTests.p.getProperty("resourceGroup2");
    	Set grpsSet = rh.getGroups();
    	if (grpsSet.size() != 2) {
    		fail("RdbResourcesHandle.getGroups() returned wrong amount of resource groups");
    	}
    	String[] arr = { resGrp01Id, resGrp02ID}; 
    	for (Iterator iter = grpsSet.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			for (int i = 0; i < arr.length; i++) {
				if (element.equals(arr[i])) {
					iter.remove();
					arr = removeElementAt(i, arr);
				}
			}
		}
    	assertTrue(grpsSet.isEmpty() && arr.length == 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.getResourcesInGroup(String)'
     */
    public void testGetResourcesInGroup() {
    	String resGrp01Id = LdapTests.p.getProperty("resourceGroup1");
    	String resInGrp = LdapTests.p.getProperty("resource1");
    	Set members = rh.getResourcesInGroup(resGrp01Id);
    	if (members.size() != 1) {
    		fail("RdbResourcesHandle.getGroups() returned wrong amount of resource groups");
    	}
    	for (Iterator iter = members.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			assertTrue("Checking " + element, element.equals(resInGrp));
		}
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.isResGroupNotAvailable(String)'
     */
    public void testIsResGroupNotAvailable() {
    	String resGrp01Id = LdapTests.p.getProperty("resourceGroup1");
    	boolean notAvailable = rh.isResGroupNotAvailable(resGrp01Id);
    	assertFalse(notAvailable);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.searchGroups(String)'
     */
    public void testSearchGroups() {
    	String pattern01 = "Aut*";
    	String pattern02 = "*otebo*";
    	String pattern03 = "*o*";
    	String resGrp01Id = LdapTests.p.getProperty("resourceGroup1"); // Autos
    	String resGrp02Id = LdapTests.p.getProperty("resourceGroup2"); // Notebooks
    	Set searchResult = rh.searchGroups(pattern01);
    	if (searchResult.size() != 1) {
    		fail("Unexpected result size! Resource Group \"Autos\" not found?");
    	}
    	for (Iterator iter = searchResult.iterator(); iter.hasNext();) {
			String result = (String) iter.next();
			assertTrue(result.equals(resGrp01Id));
		}
    	searchResult = rh.searchGroups(pattern02);
    	if (searchResult.size() != 1) {
    		fail("Unexpected result size! Resource Group \"Notebooks\" not found?");
    	}
    	for (Iterator iter = searchResult.iterator(); iter.hasNext();) {
			String result = (String) iter.next();
			assertTrue(result.equals(resGrp02Id));
		}
    	searchResult = rh.searchGroups(pattern03);
    	if (searchResult.size() != 2) {
    		fail("Unexpected result size! Resource Groups which contain character 'o' not found?");
    	}
    	String[] resArr = { resGrp01Id, resGrp02Id };
    	for (Iterator iter = searchResult.iterator(); iter.hasNext();) {
			String result = (String) iter.next();
			for (int i = 0; i < resArr.length; i++) {
				if (resArr[i].equals(result)) {
					iter.remove();
					resArr = removeElementAt(i, resArr);
				}
			}
		}
    	assertTrue(searchResult.isEmpty() && resArr.length == 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.searchResources(String)'
     */
    public void testSearchResources() {
		String pattern01 = "Twin*";
		String pattern02 = "*ord F*";
		String pattern03 = "*o*"; // 4
		String resourceTwingo = LdapTests.p.getProperty("resource1"); // Twingo
		String resourceFordFocus = LdapTests.p.getProperty("resourceWithSpace");
		String[] resourcesWithO = { resourceTwingo, resourceFordFocus, LdapTests.p.getProperty("resource4"),
				LdapTests.p.getProperty("resource5") };
		Set searchResult = rh.searchResources(pattern01);
		if (searchResult.size() != 1) {
			fail("Unexpected result size! Twingo not found?");
		}
		for (Iterator iter = searchResult.iterator(); iter.hasNext();) {
			String result = (String) iter.next();
			assertTrue(result.equals(resourceTwingo)); // Id of resource "Twingo"
		}
		searchResult = rh.searchResources(pattern02);
		if (searchResult.size() != 1) {
			fail("Unexpected result size! Ford Focus not found?");
		}
		for (Iterator iter = searchResult.iterator(); iter.hasNext();) {
			String result = (String) iter.next();
			assertTrue(result.equals(resourceFordFocus));
		}
		searchResult = rh.searchResources(pattern03);
		if (searchResult.size() != 4) {
			fail("Unexpected result size! Resources with an 'o' not found?");
		}
		for (Iterator iter = searchResult.iterator(); iter.hasNext();) {
			String result = (String) iter.next();
			for (int i = 0; i < resourcesWithO.length; i++) {
				if (result.equals(resourcesWithO[i])) {
					iter.remove();
					resourcesWithO = removeElementAt(i, resourcesWithO);
				}
			}
		}
		assertTrue(searchResult.isEmpty() && resourcesWithO.length == 0);
	}
    
    private String[] removeElementAt(int index, String[] src) {
    	String[] res = new String[src.length - 1];
    	System.arraycopy(src, 0, res, 0, index);
    	System.arraycopy(src, (index + 1), res, index, (src.length - index - 1));
    	return res;
    }
}
