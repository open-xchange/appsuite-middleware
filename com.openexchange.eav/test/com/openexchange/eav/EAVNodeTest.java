package com.openexchange.eav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVNodeVisitor;
import com.openexchange.eav.EAVPath;
import junit.framework.TestCase;


/**
 * {@link EAVNodeTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class EAVNodeTest extends TestCase {
    public void testIsRoot() {
        assertTrue("No parent should be root", new EAVNode().isRoot());
        assertFalse("With parent should not be root", new EAVNode(new EAVNode()).isRoot());
    }
    
    public void testChildren() {
        EAVNode root = new EAVNode();
        EAVNode node1 = new EAVNode(root);
        EAVNode node2 = new EAVNode(root);
        EAVNode node3 = new EAVNode(root);
        
        assertEquals("Should have 3 children", 3, root.getChildren().size());
        
        Set<EAVNode> allNodes = new HashSet<EAVNode>(root.getChildren());
        
        for(EAVNode node : Arrays.asList(node1, node2, node3)) {
            assertTrue("Did not find a node", allNodes.remove(node));
        }
    }
    
    public void testIsLeaf() {
        assertTrue("Node without children is a leaf", new EAVNode().isLeaf());
        EAVNode root = new EAVNode();
        new EAVNode(root);
        
        assertFalse("Node with children is inner node", root.isLeaf());
    }
    
    public void testPath() {
        EAVNode root = new EAVNode("com.openexchange.test");
        EAVNode exampleAttribute = new EAVNode(root, "exampleAttribute");
        
        assertEquals(new EAVPath("com.openexchange.test", "exampleAttribute"), exampleAttribute.getPath());
    }
    
    public void testVisitUpwards() {
        EAVNode root = new EAVNode("com.openexchange.test");
        EAVNode exampleAttribute = new EAVNode(root, "exampleAttribute");
        TestVisitor visitor = new TestVisitor();
        exampleAttribute.visitUpward(visitor);
        
        assertEquals(exampleAttribute, visitor.visited.get(0));
        assertEquals(root, visitor.visited.get(1));
        
        assertEquals((int)0, (int)visitor.level.get(0));
        assertEquals((int)-1, (int)visitor.level.get(1));
     
        assertEquals(visitor.visited.size(), 2);
    }
    
    public void testVisitDownwards() {
        EAVNode root = new EAVNode("com.openexchange.test");
        EAVNode exampleAttribute = new EAVNode(root, "exampleAttribute");
        EAVNode exampleAttributeChild = new EAVNode(exampleAttribute, "exampleAttributeChild");
        EAVNode exampleAttribute2 = new EAVNode(root, "exampleAttribute2");
        
        TestVisitor visitor = new TestVisitor();
        
        root.visit(visitor);
        
        assertEquals(root, visitor.visited.get(0));
        assertEquals(exampleAttribute, visitor.visited.get(1));
        assertEquals(exampleAttributeChild, visitor.visited.get(2));
        assertEquals(exampleAttribute2, visitor.visited.get(3));
         
        assertEquals((int)0, (int)visitor.level.get(0));
        assertEquals((int)1, (int)visitor.level.get(1));
        assertEquals((int)2, (int)visitor.level.get(2));
        assertEquals((int)1, (int)visitor.level.get(3));
    }
    
    public void testGetChildByName() {
        EAVNode root = new EAVNode("com.openexchange.test");
        EAVNode exampleAttribute = new EAVNode(root, "exampleAttribute");
        EAVNode exampleAttribute2 = new EAVNode(root, "exampleAttribute2");
        
        assertEquals(exampleAttribute, root.getChildByName("exampleAttribute"));
        assertEquals(exampleAttribute2, root.getChildByName("exampleAttribute2"));
        
    }
    
    public void testResolvePath() {
        
        EAVNode root = new EAVNode("com.openexchange.test");
        EAVNode exampleAttribute = new EAVNode(root, "exampleAttribute");
        EAVNode exampleAttributeChild = new EAVNode(exampleAttribute, "exampleAttributeChild");
        
        EAVNode found = root.resolve(new EAVPath("exampleAttribute", "exampleAttributeChild"));
        
        assertEquals(exampleAttributeChild, found);
        
    }
    
    private static final class TestVisitor implements EAVNodeVisitor {

        public List<EAVNode> visited = new ArrayList<EAVNode>();
        public List<Integer> level = new ArrayList<Integer>();
        
        
        public void visit(int index, EAVNode node) {
            visited.add(node);
            level.add(index);
        }
        
    }
}
