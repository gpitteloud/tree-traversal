package ch.gpitteloud.tree;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Tests for {@link TreeNode}
 *
 * @author Gaëtan Pitteloud
 */
public class TreeNodeTestCase {

    @Test
    public void invalidParametertypeFails() throws Exception {
        try {
            new WrongNode();
            fail("invalid param type argument");
        } catch (IllegalArgumentException e) {
            // OK
        }

    }

    @Test
    public void subclass() throws Exception {
        SampleNodeSubclass subnode = new SampleNodeSubclass("value", 12);
        assertEquals("value", subnode.getValue());
        assertEquals(12, subnode.otherValue);

        ConcreteNode n2 = new ConcreteNode();
        SubConcreteNode n3 = new SubConcreteNode();

        Tree<ConcreteNode> tree = new Tree<>(n2);
        tree.getRoot().addChild(n3);
    }

    @Test
    public void setParentRootNodeFails() throws Exception {
        SampleNode root = new SampleNode();
        root.setRoot();

        SampleNode parent = new SampleNode();
        try {
            root.setParent(parent);
        } catch (IllegalStateException e) {// ok
        }
    }

    @Test
    public void addNodesHierarchyToATree() throws Exception {
        SampleNode node = new SampleNode();
        SampleNode child1 = new SampleNode();
        SampleNode child2 = new SampleNode();
        node.addChild(child1);
        node.addChild(child2);
        int[] c1path = child1.getPath();
        int[] c2path = child2.getPath();
        assertEquals(1, c1path.length);
        assertEquals(0, c1path[0]);
        assertEquals(1, c2path.length);
        assertEquals(1, c2path[0]);

        SampleNode root = new SampleNode();
        int index = 2;
        Tree<SampleNode> tree = new Tree<>(root);
        root.addChild(new SampleNode());
        root.addChild(new SampleNode());
        tree.getRoot().addChild(node);

        assertEquals(index, node.getIndex());
        assertEquals(root, node.getParent());
        int[] nodePath = node.getPath();
        assertEquals(1, nodePath.length);
        assertEquals(index, nodePath[0]);

        c1path = child1.getPath();
        c2path = child2.getPath();
        assertEquals(2, c1path.length);
        assertEquals(2, c2path.length);
        assertEquals(index, c1path[0]);
        assertEquals(index, c2path[0]);
        assertEquals(0, c1path[1]);
        assertEquals(1, c2path[1]);
    }

    @Test
    public void removeThroughIteratorRemovesChildren() throws Exception {
        String remove = "remove";
        SampleNode node = new SampleNode(remove);
        SampleNode child1 = new SampleNode("c1");
        SampleNode child2 = new SampleNode("c2");
        node.addChild(child1);
        node.addChild(child2);
        assertArrayEquals(new int[] {}, node.getPath());
        assertArrayEquals(new int[] { 0 }, child1.getPath());
        assertArrayEquals(new int[] { 1 }, child2.getPath());

        SampleNode root = new SampleNode("root");
        Tree<SampleNode> tree = new Tree<>(root);
        root.addChild(new SampleNode("s1"));
        root.addChild(new SampleNode("s2"));
        tree.getRoot().addChild(node);
        assertArrayEquals(new int[] { 2 }, node.getPath());
        assertArrayEquals(new int[] { 2, 0 }, child1.getPath());
        assertArrayEquals(new int[] { 2, 1 }, child2.getPath());

        int counter = 0;
        HashSet<String> names = new HashSet<>();
        for (Iterator<SampleNode> i = tree.iterator(); i.hasNext();) {
            SampleNode next = i.next();
            names.add(next.getValue());
            counter++;
            if (next.getValue().equals(remove)) {
                i.remove();
            }
        }
        assertEquals(4, counter);
        assertTrue(names.contains("root"));
        assertTrue(names.contains(remove));
        assertTrue(names.contains("s1"));
        assertTrue(names.contains("s2"));

        assertArrayEquals(new int[] {}, node.getPath());
        assertArrayEquals(new int[] { 0 }, child1.getPath());
        assertArrayEquals(new int[] { 1 }, child2.getPath());
    }

    @Test
    public void deepTreeGetPath() throws Exception {
        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(new SampleNode("c1"));
        root.addChild(new SampleNode("c2"));

        SampleNode c1 = root.getChildAt(1);
        c1.addChild(new SampleNode("c10"));
        c1.addChild(new SampleNode("c11"));
        c1.addChild(new SampleNode("c12"));
        c1.addChild(new SampleNode("c13"));

        SampleNode c12 = c1.getChildAt(2);
        c12.addChild(new SampleNode("c120"));
        c12.addChild(new SampleNode("c121"));

        SampleNode c120 = c12.getChildAt(0);
        c120.addChild(new SampleNode("c1200"));
        c120.addChild(new SampleNode("c1201"));
        c120.addChild(new SampleNode("c1202"));
        c120.addChild(new SampleNode("c1203"));
        c120.addChild(new SampleNode("c1204"));
        c120.addChild(new SampleNode("c1205"));

        SampleNode c1203 = c120.getChildAt(3);
        c1203.addChild(new SampleNode("c12030"));
        c1203.addChild(new SampleNode("c12031"));
        c1203.addChild(new SampleNode("c12032"));
        c1203.addChild(new SampleNode("c12033"));

        SampleNode c12033 = c1203.getChildAt(3);
        c12033.addChild(new SampleNode("c120330"));
        c12033.addChild(new SampleNode("c120331"));

        assertArrayEquals(new int[] { 1, 2, 0, 3, 3, 0 }, c12033.getChildAt(0).getPath());
        assertArrayEquals(new int[] { 1, 2, 0, 3, 3, 1 }, c12033.getChildAt(1).getPath());

        assertArrayEquals(new int[] { 1, 2, 0, 3, 3 }, c12033.getPath());
        assertArrayEquals(new int[] { 1, 2, 0, 3 }, c1203.getPath());
        assertArrayEquals(new int[] { 1, 2, 0 }, c120.getPath());
        assertArrayEquals(new int[] { 1, 2 }, c12.getPath());
        assertArrayEquals(new int[] { 1 }, c1.getPath());
        assertArrayEquals(new int[] {}, root.getPath());
    }

    @Test
    public void getRootNodeNotInATree() throws Exception {
        SampleNode c0 = new SampleNode("c01");
        SampleNode c1 = new SampleNode("c1");
        c0.addChild(c1);
        assertNull(c1.getRootNode());
    }

    @Test
    public void getRootNodeInATree() throws Exception {
        SampleNode root = new SampleNode("root");
        new Tree<>(root);
        SampleNode c0 = new SampleNode("c0");
        root.addChild(c0);
        SampleNode c1 = new SampleNode("c1");
        c0.addChild(c1);
        assertSame(root, c1.getRootNode());
    }

    @Test
    public void getRootNodeOfRootNode() throws Exception {
        SampleNode root = new SampleNode("root");
        new Tree<>(root);
        assertSame(root, root.getRootNode());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void moveChildNodeToAnotherChildrenListWithoutRemovedFails() throws Exception {
        SampleNode c0 = new SampleNode("c0");
        SampleNode c1 = new SampleNode("c1");

        SampleNode child = new SampleNode("child");
        c0.addChild(child);
        try {
            c1.addChild(child);
            fail("cannot add a child node to 2 different nodes");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).contains(c0.toString()).contains(child.toString());
        }
    }

    @Test
    public void addTheSameNodeTwiceDoesNothing() throws Exception {
        SampleNode c0 = new SampleNode("c0");

        SampleNode child = new SampleNode("child");
        assertTrue(c0.getChildren().add(child));
        assertFalse(c0.getChildren().add(child));
    }

    @Test
    public void moveChildNodeToAnotherChildrenList() throws Exception {
        SampleNode c0 = new SampleNode("c0");
        SampleNode c1 = new SampleNode("c1");

        SampleNode child = new SampleNode("child");
        c0.addChild(child);
        c0.getChildren().remove(child);
        c1.addChild(child);

        assertEquals(c1, child.getParent());
        assertEquals(0, c0.getChildren().size());
        assertEquals(1, c1.getChildren().size());
        assertEquals(child, c1.getChildAt(0));
    }

    @Test
    public void removeCurrentNodeFails() throws Exception {
        SampleNode c0 = new SampleNode("c0");
        SampleNode c1 = new SampleNode("c1");
        c0.addChild(c1);

        c1.setCurrent(true);
        try {
            c0.getChildren().remove(c1);
            fail("cannot remove current node");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).contains(c1.toString());
        }
    }

    @Test
    public void getDepthRootNode() throws Exception {
        SampleNode root = new SampleNode("root");
        assertEquals(0, root.getDepth());
    }

    @Test
    public void getDepth() throws Exception {
        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(new SampleNode("c1"));
        root.addChild(new SampleNode("c2"));

        SampleNode c1 = root.getChildAt(1);
        c1.addChild(new SampleNode("c10"));
        c1.addChild(new SampleNode("c11"));
        c1.addChild(new SampleNode("c12"));
        c1.addChild(new SampleNode("c13"));

        SampleNode c12 = c1.getChildAt(2);
        c12.addChild(new SampleNode("c120"));
        c12.addChild(new SampleNode("c121"));

        SampleNode c120 = c12.getChildAt(0);
        c120.addChild(new SampleNode("c1200"));
        c120.addChild(new SampleNode("c1201"));
        c120.addChild(new SampleNode("c1202"));
        c120.addChild(new SampleNode("c1203"));
        c120.addChild(new SampleNode("c1204"));
        c120.addChild(new SampleNode("c1205"));

        SampleNode c1203 = c120.getChildAt(3);
        c1203.addChild(new SampleNode("c12030"));
        c1203.addChild(new SampleNode("c12031"));
        c1203.addChild(new SampleNode("c12032"));
        c1203.addChild(new SampleNode("c12033"));

        SampleNode c12033 = c1203.getChildAt(3);
        SampleNode c120330 = new SampleNode("c120330");
        SampleNode c120331 = new SampleNode("c120331");
        c12033.addChild(c120330);
        c12033.addChild(c120331);

        assertEquals(6, c120330.getDepth());
        assertEquals(6, c120331.getDepth());
        assertEquals(5, c12033.getDepth());
        assertEquals(4, c1203.getDepth());
        assertEquals(3, c120.getDepth());
        assertEquals(2, c12.getDepth());
        assertEquals(1, c1.getDepth());
        assertEquals(0, root.getDepth());
    }

    @Test
    public void canAddReturnsFalse() throws Exception {
        final SampleNodeCannotAdd parent = new SampleNodeCannotAdd();
        parent.addChild(new SampleNodeCannotAdd());
        parent.canAdd = false;

        assertThrowsIllegalArgumentException(() -> parent.addChild(new SampleNodeCannotAdd()));
        assertThrowsIllegalArgumentException(() -> parent.getChildren().add(0, new SampleNodeCannotAdd()));
        assertThrowsIllegalArgumentException(() -> parent.getChildren().set(0, new SampleNodeCannotAdd()));
        assertThrowsIllegalArgumentException(
                () -> parent.getChildren().addAll(Arrays.asList(new SampleNodeCannotAdd(), new SampleNodeCannotAdd())));
        assertThrowsIllegalArgumentException(
                () -> parent.getChildren().addAll(0, Arrays.asList(new SampleNodeCannotAdd(), new SampleNodeCannotAdd())));
        assertThrowsIllegalArgumentException(() -> {
            ListIterator<SampleNodeCannotAdd> it = parent.getChildren().listIterator();
            it.add(new SampleNodeCannotAdd());
        });
        assertThrowsIllegalArgumentException(() -> {
            ListIterator<SampleNodeCannotAdd> it = parent.getChildren().listIterator();
            it.next();
            it.set(new SampleNodeCannotAdd());
        });

    }

    private void assertThrowsIllegalArgumentException(Runnable runnable) {
        try {
            runnable.run();
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    static class SampleNodeCannotAdd extends TreeNode<SampleNodeCannotAdd> {

        private static final long serialVersionUID = 1L;

        boolean canAdd = true;

        SampleNodeCannotAdd() {
            super(SampleNodeCannotAdd.class);
        }

        @Override
        protected boolean canAddChild(SampleNodeCannotAdd node) {
            return canAdd;
        }
    }

    static class WrongNode extends TreeNode<SampleNode> {

        WrongNode() {
            super(SampleNode.class);
        }

        private static final long serialVersionUID = 1L;
    }

    static class SampleNodeSubclass extends SampleNode {

        private static final long serialVersionUID = 1L;

        int otherValue;

        SampleNodeSubclass(String value, int otherValue) {
            super(value);
            this.otherValue = otherValue;
        }
    }

}