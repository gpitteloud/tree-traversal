package ch.gpitteloud.tree;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import static org.junit.Assert.*;

/**
 * Tests for {@link ChildrenList}
 *
 * @author Gaëtan Pitteloud
 */
public class ChildrenListTestCase {

    @Test
    public void addNullFails() throws Exception {
        SampleNode root = new SampleNode("root");
        try {
            root.getChildren().add(null);
            fail("add null should fail");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    @Test
    public void add() throws Exception {
        SampleNode parent = new SampleNode("parent");

        SampleNode child0 = new SampleNode("child0");
        assertNull(child0.getParent());
        assertEquals(0, child0.getPath().length);
        assertEquals(-1, child0.getIndex());

        SampleNode child1 = new SampleNode("child1");
        assertNull(child1.getParent());
        assertEquals(0, child1.getPath().length);
        assertEquals(-1, child1.getIndex());

        parent.getChildren().add(child0);
        parent.getChildren().add(child1);

        verifyChildrenNodes(parent, child0,  child1);

        SampleNode newChild = new SampleNode("newChild");
        assertNull(newChild.getParent());
        assertEquals(0, newChild.getPath().length);
        assertEquals(-1, newChild.getIndex());
        parent.getChildren().add(1, newChild);

        verifyChildrenNodes(parent, child0, newChild, child1);
    }

    private void verifyChildrenNodes(SampleNode parent, SampleNode... children) {
        int i = 0;
        for (SampleNode node : children) {
            assertSame(parent, node.getParent());
            assertEquals(1, node.getPath().length);
            assertEquals(i, node.getPath()[0]);
            assertEquals(i++, node.getIndex());
        }
    }

    @Test
    public void addAll() throws Exception {
        SampleNode ch1 = new SampleNode("ch1");
        SampleNode ch2 = new SampleNode("ch2");
        SampleNode ch3 = new SampleNode("ch3");
        SampleNode ch4 = new SampleNode("ch4");
        Collection<SampleNode> nodes = Arrays.asList(ch1, ch2, ch3, ch4);

        SampleNode p = new SampleNode("p");
        p.getChildren().add(new SampleNode("o1"));
        p.getChildren().add(new SampleNode("o2"));

        p.getChildren().addAll(nodes);
        assertEquals("o1", p.getChildren().get(0).getValue());
        assertEquals("o2", p.getChildren().get(1).getValue());

        ListIterator<SampleNode> it = p.getChildren().listIterator(2);
        for (int i = 1; i < 5; i++) {
            assertTrue(it.hasNext());
            assertEquals("ch" + i, it.next().getValue());
        }
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals("ch4", it.previous().getValue());
    }

    @Test
    public void remove() throws Exception {
        SampleNode parent = new SampleNode("parent");
        SampleNode child0 = new SampleNode("child0");
        SampleNode child1 = new SampleNode("child1");
        SampleNode child2 = new SampleNode("child2");
        SampleNode child3 = new SampleNode("child3");

        parent.getChildren().add(child0);
        parent.getChildren().add(child1);
        parent.getChildren().add(child2);
        parent.getChildren().add(child3);

        assertEquals(4, parent.getChildren().size());
        assertFalse(parent.getChildren().remove(new SampleNode()));
        assertTrue(parent.getChildren().remove(child1));
        assertNull(child1.getParent());

        assertEquals(3, parent.getChildren().size());
        assertTrue(parent.getChildren().contains(child0));
        assertTrue(parent.getChildren().contains(child2));
        assertFalse(parent.getChildren().contains(child1));

        assertEquals(child2, parent.getChildren().remove(1));
        assertEquals(2, parent.getChildren().size());
        assertNull(child2.getParent());

        parent.getChildren().clear();
        assertEquals(0, parent.getChildren().size());
        assertTrue(parent.getChildren().isEmpty());
    }

    @Test
    public void set() throws Exception {
        SampleNode parent = new SampleNode("parent");
        SampleNode child0 = new SampleNode("child0");
        SampleNode child1 = new SampleNode("child1");
        SampleNode child2 = new SampleNode("child2");
        SampleNode child3 = new SampleNode("child3");

        parent.getChildren().add(child0);
        parent.getChildren().add(child1);
        parent.getChildren().add(child2);
        parent.getChildren().add(child3);

        SampleNode c = new SampleNode("c");
        assertEquals(child2, parent.getChildren().set(2, c));
        assertNull(child2.getParent());
        assertEquals(parent, c.getParent());
    }

    @Test
    public void listIterator() throws Exception {
        SampleNode parent = new SampleNode("parent");
        String c0 = "child0";
        String c1 = "child1";
        String c2 = "child2";
        String c3 = "child3";
        SampleNode child0 = new SampleNode(c0);
        SampleNode child1 = new SampleNode(c1);
        SampleNode child2 = new SampleNode(c2);
        SampleNode child3 = new SampleNode(c3);

        List<SampleNode> children = parent.getChildren();
        children.add(child0);
        children.add(child1);
        children.add(child2);
        children.add(child3);

        StringBuilder buf = new StringBuilder();
        StringBuilder prevIndices = new StringBuilder();
        StringBuilder nextIndices = new StringBuilder();
        for (ListIterator<SampleNode> i = children.listIterator(children.size()); i.hasPrevious();) {
            prevIndices.append(i.previousIndex());
            buf.append(i.previous().getValue());
            nextIndices.append(i.nextIndex());
        }
        assertEquals("3210", prevIndices.toString());
        assertEquals(c3 + c2 + c1 + c0, buf.toString());
        assertEquals("3210", nextIndices.toString());
    }

    @Test
    public void listIteratorAdd() throws Exception {
        SampleNode parent = new SampleNode("parent");
        String c0 = "child0";
        String c1 = "child1";
        String c2 = "child2";
        String c3 = "child3";
        SampleNode child0 = new SampleNode(c0);
        SampleNode child1 = new SampleNode(c1);
        SampleNode child2 = new SampleNode(c2);
        SampleNode child3 = new SampleNode(c3);

        List<SampleNode> children = parent.getChildren();
        children.add(child0);
        children.add(child1);
        children.add(child2);
        children.add(child3);

        for (ListIterator<SampleNode> i = children.listIterator(0); i.hasNext();) {
            i.add(new SampleNode("c0" + i.nextIndex()));
            i.next();
            i.add(new SampleNode("c1" + i.nextIndex()));
        }

        assertEquals(12, children.size());
        for (int i = 0; i < 12; i += 3) {
            assertEquals("c0" + i, children.get(i).getValue());
            assertEquals("child" + (i / 3), children.get(i + 1).getValue());
            assertEquals("c1" + (i + 2), children.get(i + 2).getValue());
        }
    }

    @Test
    public void listIteratorSet() throws Exception {
        SampleNode parent = new SampleNode("parent");
        String c0 = "child0";
        String c1 = "child1";
        String c2 = "child2";
        String c3 = "child3";
        SampleNode child0 = new SampleNode(c0);
        SampleNode child1 = new SampleNode(c1);
        SampleNode child2 = new SampleNode(c2);
        SampleNode child3 = new SampleNode(c3);

        List<SampleNode> children = parent.getChildren();
        children.add(child0);
        children.add(child1);
        children.add(child2);
        children.add(child3);

        for (ListIterator<SampleNode> i = children.listIterator(0); i.hasNext();) {
            if (i.next().getValue().equals(c1)) {
                i.set(new SampleNode("child1bis"));
            }
        }

        assertEquals(4, children.size());
        assertEquals(c0, children.get(0).getValue());
        assertEquals("child1bis", children.get(1).getValue());
        assertEquals(c2, children.get(2).getValue());
        assertEquals(c3, children.get(3).getValue());
    }

    @Test
    public void addFailingSetParentKeepsListInSynch() throws Exception {
        SampleNode p = new SampleNode("parent");
        SampleNode node = new SampleNode("node");
        p.addChild(node);

        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(new SampleNode("c1"));
        try {
            root.getChildren().add(node);
            fail("did not fail");
        } catch (IllegalStateException e) {
            verifyStateAfterAddException(node, root, p);
        }
    }

    @Test
    public void addIntFailingSetParentKeepsListInSynch() throws Exception {
        SampleNode p = new SampleNode("parent");
        SampleNode node = new SampleNode("node");
        p.addChild(node);

        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(new SampleNode("c1"));
        try {
            root.getChildren().add(1, node);
            fail("did not fail");
        } catch (IllegalStateException e) {
            verifyStateAfterAddException(node, root, p);
        }
    }

    private void verifyStateAfterAddException(SampleNode node, SampleNode newRoot, SampleNode oldRoot) {
        assertEquals(2, newRoot.getChildren().size());
        assertEquals("c0", newRoot.getChildAt(0).getValue());
        assertEquals("c1", newRoot.getChildAt(1).getValue());
        assertEachChildIsConnectedToItsParent(newRoot);
        assertSame(oldRoot, node.getParent());
        assertSame(node, oldRoot.getChildAt(0));
        assertEquals(1, oldRoot.getChildren().size());
    }

    @Test
    public void removeFailingSetParentKeepsListInSynch() throws Exception {
        SampleNode node = new SampleNode("node");
        node.setCurrent(true);

        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(node);
        root.addChild(new SampleNode("c1"));

        List<SampleNode> children = root.getChildren();
        try {
            children.remove(node);
            fail("remove should fail");
        } catch (IllegalStateException e) {
            assertEquals(3, children.size());
            assertEquals("c0", root.getChildAt(0).getValue());
            assertEquals("node", root.getChildAt(1).getValue());
            assertEquals("c1", root.getChildAt(2).getValue());
            assertEachChildIsConnectedToItsParent(root);
        }
    }

    @Test
    public void removeIntFaililngSetParentKeepsListInSynch() throws Exception {
        SampleNode node = new SampleNode("node");
        node.setCurrent(true);

        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(node);
        root.addChild(new SampleNode("c1"));

        List<SampleNode> children = root.getChildren();
        try {
            children.remove(1);
            fail("remove should fail");
        } catch (IllegalStateException e) {
            assertEquals(3, children.size());
            assertEquals("c0", root.getChildAt(0).getValue());
            assertSame(node, root.getChildAt(1));
            assertEquals("c1", root.getChildAt(2).getValue());
            assertEachChildIsConnectedToItsParent(root);
        }
    }

    @Test
    public void setFailingSetParentKeepsListInSynch1() throws Exception {
        // cannot set new node: connected to another tree
        SampleNode p = new SampleNode("parent");
        SampleNode node = new SampleNode("node");
        p.addChild(node);

        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(new SampleNode("c1"));

        try {
            root.getChildren().set(1, node);
            fail("set should fail");
        } catch (IllegalStateException e) {
            verifyStateAfterAddException(node, root, p);
        }
    }

    @Test
    public void setFailingSetParentKeepsListInSynch2() throws Exception {
        // cannot remove previous node: current
        SampleNode node = new SampleNode("node");
        node.setCurrent(true);

        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(node);
        root.addChild(new SampleNode("c1"));

        SampleNode other = new SampleNode("other");
        try {
            root.getChildren().set(1, other);
            fail("set should fail");
        } catch (IllegalStateException e) {
            verifyStateAfterRemoveException(node, root, other);
        }
    }

    private void verifyStateAfterRemoveException(SampleNode node, SampleNode newRoot, SampleNode other) {
        assertEquals(3, newRoot.getChildren().size());
        assertEquals("c0", newRoot.getChildAt(0).getValue());
        assertSame(node, newRoot.getChildAt(1));
        assertEquals("c1", newRoot.getChildAt(2).getValue());
        assertEachChildIsConnectedToItsParent(newRoot);
        assertNull(other.getParent());
    }

    @Test
    public void listIteratorFailingAdd() throws Exception {
        SampleNode p = new SampleNode("parent");
        SampleNode node = new SampleNode("node");
        p.addChild(node);

        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(new SampleNode("c1"));

        try {
            for (ListIterator<SampleNode> it = root.getChildren().listIterator(); it.hasNext();) {
                if (it.next().getValue().equals("c0")) {
                    it.add(node);
                }
            }
            fail("itr.add should fail");
        } catch (IllegalStateException e) {
            verifyStateAfterAddException(node, root, p);
        }
    }

    @Test
    public void listIteratorFailingRemove() throws Exception {
        // cannot remove previous node: current
        SampleNode node = new SampleNode("node");
        node.setCurrent(true);

        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(node);
        root.addChild(new SampleNode("c1"));

        SampleNode other = new SampleNode("other");
        try {
            for (ListIterator<SampleNode> it = root.getChildren().listIterator(); it.hasNext();) {
                if (it.next() == node) {
                    it.remove();
                }
            }
            fail("itr.remove should fail");
        } catch (IllegalStateException e) {
            verifyStateAfterRemoveException(node, root, other);
        }
    }

    @Test
    public void listIteratorFailingSet1() throws Exception {
        // cannot set new node: connected to another tree
        SampleNode p = new SampleNode("parent");
        SampleNode node = new SampleNode("node");
        p.addChild(node);

        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(new SampleNode("c1"));

        try {
            for (ListIterator<SampleNode> it = root.getChildren().listIterator(); it.hasNext();) {
                if (it.next().getValue().equals("c0")) {
                    it.set(node);
                }
            }
            fail("list itr.set should fail");
        } catch (IllegalStateException e) {
            verifyStateAfterAddException(node, root, p);
        }
    }

    @Test
    public void listIteratorFailingSet2() throws Exception {
        // cannot remove previous node: current
        SampleNode node = new SampleNode("node");
        node.setCurrent(true);

        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(node);
        root.addChild(new SampleNode("c1"));

        SampleNode other = new SampleNode("other");
        try {
            for (ListIterator<SampleNode> it = root.getChildren().listIterator(); it.hasNext();) {
                if (it.next() == node) {
                    it.set(other);
                }
            }
            fail("list itr.set should fail");
        } catch (IllegalStateException e) {
            verifyStateAfterRemoveException(node, root, other);
        }
    }

    private void assertEachChildIsConnectedToItsParent(SampleNode parent) {
        for (SampleNode node : parent.getChildren()) {
            assertSame(parent, node.getParent());
        }
    }
}
