package ch.gpitteloud.tree;

import org.junit.Test;

import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Tests for {@link TreeNode}
 *
 * @author Gaëtan Pitteloud
 */
public class TreeNodeTestCase {

    @Test(expected = IllegalArgumentException.class)
    public void invalidParametertypeFails() throws Exception {
        new WrongNode();
    }

    @Test
    public void subclass() throws Exception {
        SampleNodeSubclass subnode = new SampleNodeSubclass("value", 12);
        assertEquals("value", subnode.getValue());
        assertEquals(12, subnode.otherValue);
    }

    @Test
    public void subclasses() throws Exception {
        ConcreteNode root = new ConcreteNode();
        SubConcreteNode node = new SubConcreteNode();

        Tree<ConcreteNode> tree = new Tree<>(root);
        tree.getRoot().addChild(node);
    }

    @Test(expected = IllegalStateException.class)
    public void setParent_rootNodeFails() throws Exception {
        SampleNode root = new SampleNode();
        root.setRoot();

        SampleNode parent = new SampleNode();
        root.setParent(parent);
    }

    @Test
    public void getPath_1Level() throws Exception {
        SampleNode node = new SampleNode();
        SampleNode child1 = new SampleNode();
        SampleNode child2 = new SampleNode();
        node.addAll(child1, child2);

        int[] c1path = child1.getPath();
        int[] c2path = child2.getPath();
        assertEquals(1, c1path.length);
        assertEquals(0, c1path[0]);
        assertEquals(1, c2path.length);
        assertEquals(1, c2path[0]);
    }

    @Test
    public void getPath_manyLevels() throws Exception {
        SampleNode root = new SampleNode();
        SampleNode node = new SampleNode();
        root.addAll(new SampleNode(), new SampleNode(), node);

        SampleNode child1 = new SampleNode();
        SampleNode child2 = new SampleNode();
        node.addAll(child1, child2);

        assertEquals(2, node.getIndex());
        assertEquals(root, node.getParent());
        int[] nodePath = node.getPath();
        assertEquals(1, nodePath.length);
        assertEquals(2, nodePath[0]);

        int[] c1path = child1.getPath();
        int[] c2path = child2.getPath();
        assertEquals(2, c1path.length);
        assertEquals(2, c2path.length);
        assertEquals(2, c1path[0]);
        assertEquals(2, c2path[0]);
        assertEquals(0, c1path[1]);
        assertEquals(1, c2path[1]);
    }

    @Test
    public void removeThroughIteratorRemovesChildren() throws Exception {
        String remove = "remove";
        SampleNode node = new SampleNode(remove);
        SampleNode child1 = new SampleNode("c1");
        SampleNode child2 = new SampleNode("c2");
        node.addAll(child1, child2);

        SampleNode root = new SampleNode("root");
        root.addAll(new SampleNode("s1"), new SampleNode("s2"), node);

        int counter = 0;
        Collection<String> names = new ArrayList<>();
        for (Iterator<SampleNode> i = new Tree<>(root).iterator(); i.hasNext(); ) {
            SampleNode next = i.next();
            names.add(next.getValue());
            counter++;
            if (next.getValue().equals(remove)) {
                i.remove();
            }
        }
        assertEquals(4, counter);
        assertThat(names).containsOnly("root", remove, "s1", "s2");

        // node becomes root of its own tree
        assertNull(node.getParent());
        assertArrayEquals(new int[]{}, node.getPath());
        assertArrayEquals(new int[]{0}, child1.getPath());
        assertArrayEquals(new int[]{1}, child2.getPath());
    }

    @Test
    public void deepTreeGetPath() throws Exception {
        SampleNode root = SampleNode.createTree("root", "c0", "c1", "c2");

        SampleNode c1 = root.getChildAt(1);
        c1.createChildren("c10", "c11", "c12", "c13");

        SampleNode c12 = c1.getChildAt(2);
        c12.createChildren("c120", "c121");

        SampleNode c120 = c12.getChildAt(0);
        c120.createChildren("c1200", "c1201", "c1202", "c1203", "c1204", "c1205");

        SampleNode c1203 = c120.getChildAt(3);
        c1203.createChildren("c12030", "c12031", "c12032", "c12033");

        SampleNode c12033 = c1203.getChildAt(3);
        c12033.createChildren("c120330", "c120331");

        assertArrayEquals(new int[]{1, 2, 0, 3, 3, 0}, c12033.getChildAt(0).getPath());
        assertArrayEquals(new int[]{1, 2, 0, 3, 3, 1}, c12033.getChildAt(1).getPath());

        assertArrayEquals(new int[]{1, 2, 0, 3, 3}, c12033.getPath());
        assertArrayEquals(new int[]{1, 2, 0, 3}, c1203.getPath());
        assertArrayEquals(new int[]{1, 2, 0}, c120.getPath());
        assertArrayEquals(new int[]{1, 2}, c12.getPath());
        assertArrayEquals(new int[]{1}, c1.getPath());
        assertArrayEquals(new int[]{}, root.getPath());
    }

    @Test
    public void getRootNode_NotInATree() throws Exception {
        SampleNode c0 = new SampleNode("c01");
        SampleNode c1 = new SampleNode("c1");
        c0.addChild(c1);
        assertNull(c1.getRootNode());
    }

    @Test
    public void getRootNode_InATree() throws Exception {
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
        assertThat(c0.getChildren()).hasSize(1);
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
        assertThat(c0.getChildren()).isEmpty();
        assertThat(c1.getChildren()).hasSize(1).contains(child);
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
        root.createChildren("c0", "c1", "c2");

        SampleNode c1 = root.getChildAt(1);
        c1.createChildren("c10", "c11", "c12", "c13");

        SampleNode c12 = c1.getChildAt(2);
        c12.createChildren("c120", "c121");

        SampleNode c120 = c12.getChildAt(0);
        c120.createChildren("c1200", "c1201", "c1202", "c1203", "c1204", "c1205");

        SampleNode c1203 = c120.getChildAt(3);
        c1203.createChildren("c12030", "c12031", "c12032", "c12033");

        SampleNode c12033 = c1203.getChildAt(3);
        SampleNode c120330 = new SampleNode("c120330");
        SampleNode c120331 = new SampleNode("c120331");
        c12033.addAll(c12033, c120331);

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
                () -> parent.getChildren()
                        .addAll(0, Arrays.asList(new SampleNodeCannotAdd(), new SampleNodeCannotAdd())));
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