package ch.gpitteloud.tree;

import org.junit.Test;

import java.io.*;
import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Tests for {@link Tree}.
 *
 * @author Gaëtan Pitteloud
 */
public class TreeTestCase {

    @Test
    public void getNodeFromPath() throws Exception {
        SampleNode root = SampleNode.createTree("root", "c0", "c1");

        root.getChildAt(0).createChildren("c00", "c01");

        Tree<SampleNode> tree = new Tree<>(root);
        assertNull(tree.getNodeFromPath(null));

        assertEquals(root, tree.getNodeFromPath(new int[]{}));
        assertEquals("c0", tree.getNodeFromPath(new int[]{0}).getValue());
        assertEquals("c1", tree.getNodeFromPath(new int[]{1}).getValue());

        assertEquals("c00", tree.getNodeFromPath(new int[]{0, 0}).getValue());
        assertEquals("c01", tree.getNodeFromPath(new int[]{0, 1}).getValue());
    }

    @Test
    public void getNodeFromPathFails() throws Exception {
        SampleNode root = SampleNode.createTree("root", "c0", "c1");

        root.getChildAt(0).createChildren("c00", "c01");

        Tree<SampleNode> tree = new Tree<>(root);

        int[] path = new int[]{2, 6, 5};
        try {
            tree.getNodeFromPath(path);
            fail("invalid path should fail");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains(Arrays.toString(path))
                    .contains(Arrays.toString(new int[]{2}));
        }
    }

    @Test
    public void setCurrentNodeNotInTheTreeFails() throws Exception {
        Tree<SampleNode> tree = new Tree<>(new SampleNode("root"));
        SampleNode node = new SampleNode("other");
        try {
            tree.setCurrentNode(node);
            fail("node not in the tree");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains(node.toString());
        }
    }

    @Test
    public void setCurrentNode() throws Exception {
        SampleNode root = SampleNode.createTree("root", "c0", "c1");
        Tree<SampleNode> tree = new Tree<>(root);
        SampleNode c0 = root.getChildAt(0);
        SampleNode c1 = root.getChildAt(1);

        tree.setCurrentNode(c0);
        assertTrue(c0.isCurrent());
        assertFalse(c1.isCurrent());
    }

    @Test
    public void changeCurrentNode() throws Exception {
        SampleNode root = SampleNode.createTree("root", "c0", "c1");
        Tree<SampleNode> tree = new Tree<>(root);
        SampleNode c0 = root.getChildAt(0);
        SampleNode c1 = root.getChildAt(1);

        tree.setCurrentNode(c0);
        tree.setCurrentNode(c1);

        assertTrue(c1.isCurrent());
        assertFalse(c0.isCurrent());
    }

    @Test(expected = IllegalStateException.class)
    public void disconnectCurrentNodeFails() throws Exception {
        SampleNode root = SampleNode.createTree("root", "c0", "c1");
        Tree<SampleNode> tree = new Tree<>(root);
        SampleNode c0 = root.getChildAt(0);

        tree.setCurrentNode(c0);
        tree.getRoot().getChildren().remove(c0);
    }

    @Test(expected = IllegalStateException.class)
    public void disconnectParentOfCurrentNodeFails() throws Exception {
        Tree<SampleNode> tree = new Tree<>(new SampleNode("root"));
        tree.getRoot().addChild(new SampleNode("c0"));
        tree.getRoot().getChildAt(0).addChild(new SampleNode("c00"));
        SampleNode current = new SampleNode("c000");
        tree.getRoot().getChildAt(0).getChildAt(0).addChild(current);

        tree.setCurrentNode(current);

        tree.getRoot().getChildren().remove(0);
    }

    @Test
    public void serializeTree() throws Exception {
        SampleNode root = new SampleNode("root");
        SampleNode c0 = new SampleNode("c0");
        SampleNode c1 = new SampleNode("c1");
        SampleNode c2 = new SampleNode("c2");
        root.addAll(c0, c1, c2);
        c0.createChildren("c00");
        c1.createChildren("c10", "c11", "c12");
        c1.getChildAt(0).createChildren("c100", "c110");
        c2.createChildren("c20");

        Tree<SampleNode> tree = new Tree<>(root);
        byte[] bytes = serializeTree(tree);
        Tree<SampleNode> stree = deserializeTree(bytes);

        TreeIterator<SampleNode> it = tree.iterator();
        TreeIterator<SampleNode> sit = stree.iterator();
        while (it.hasNext()) {
            assertTrue(sit.hasNext());
            SampleNode node = it.next();
            SampleNode snode = sit.next();
            assertEquals(node.getValue(), snode.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private Tree<SampleNode> deserializeTree(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object serializedForm = in.readObject();
        assertNotNull(serializedForm);
        assertTrue(serializedForm instanceof Tree<?>);
        return (Tree<SampleNode>) serializedForm;
    }

    private byte[] serializeTree(Tree<SampleNode> tree) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(tree);
        out.flush();

        return baos.toByteArray();
    }
}
