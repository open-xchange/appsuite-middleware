package org.quickcached;


import junit.framework.TestCase;

/**
 *  Template used to create other test
 */
public class SkeletonTest extends TestCase {

    public SkeletonTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public static void main(String args[]) {
         junit.textui.TestRunner.run(SkeletonTest.class);
    }

    public void testAssert() {
    	assertTrue(true);
    }
}
