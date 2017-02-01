package ch.gpitteloud.tree;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Tests for {@link Tree}.
 *
 * @author Gaëtan Pitteloud
 */
public class TreeTestCase {

    private static final Logger logger = Logger.getLogger(TreeTestCase.class);

    @Test
    public void getNodeFromPath() throws Exception {
        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(new SampleNode("c1"));

        root.getChildAt(0).addChild(new SampleNode("c00"));
        root.getChildAt(0).addChild(new SampleNode("c01"));

        Tree<SampleNode> tree = new Tree<>(root);
        assertNull(tree.getNodeFromPath(null));

        assertEquals(root, tree.getNodeFromPath(new int[] {}));
        assertEquals("c0", tree.getNodeFromPath(new int[] { 0 }).getValue());
        assertEquals("c1", tree.getNodeFromPath(new int[] { 1 }).getValue());

        assertEquals("c00", tree.getNodeFromPath(new int[] { 0, 0 }).getValue());
        assertEquals("c01", tree.getNodeFromPath(new int[] { 0, 1 }).getValue());

        if (logger.isInfoEnabled()) {
            logger.info(tree);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getNodeFromPathFails() throws Exception {
        SampleNode root = new SampleNode("root");
        root.addChild(new SampleNode("c0"));
        root.addChild(new SampleNode("c1"));

        root.getChildAt(0).addChild(new SampleNode("c00"));
        root.getChildAt(0).addChild(new SampleNode("c01"));

        Tree<SampleNode> tree = new Tree<>(root);

        int[] path = new int[] { 2, 6, 5 };
        try {
            tree.getNodeFromPath(path);
            fail("invalid path should fail");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains(Arrays.toString(path))
                    .contains(Arrays.toString(new int[]{2}));
        }
        if (logger.isInfoEnabled()) {
            logger.info(tree);
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
        if (logger.isInfoEnabled()) {
            logger.info(tree);
        }
    }

    @Test
    public void setCurrentNode() throws Exception {
        Tree<SampleNode> tree = new Tree<>(new SampleNode("root"));
        SampleNode c0 = new SampleNode("c0");
        SampleNode c1 = new SampleNode("c1");
        assertTrue(tree.getRoot().addChild(c0));
        assertTrue(tree.getRoot().addChild(c1));

        tree.setCurrentNode(c0);
        assertTrue(c0.isCurrent());
        assertFalse(c1.isCurrent());

        // new becomes current, old is not current anymore
        tree.setCurrentNode(c1);
        assertTrue(c1.isCurrent());
        assertFalse(c0.isCurrent());

        if (logger.isInfoEnabled()) {
            logger.info(tree);
        }
    }

    @Test
    public void disconnectCurrentNodeFails() throws Exception {
        Tree<SampleNode> tree = new Tree<>(new SampleNode("root"));
        SampleNode c0 = new SampleNode("c0");
        SampleNode c1 = new SampleNode("c1");
        assertTrue(tree.getRoot().addChild(c0));
        assertTrue(tree.getRoot().addChild(c1));

        tree.setCurrentNode(c0);
        try {
            tree.getRoot().getChildren().remove(c0);
            fail("Cannot remove current node");
        } catch (IllegalStateException e) {
            // ok
        }
    }

    @Test
    public void disconnectParentOfCurrentNodeFails() throws Exception {
        Tree<SampleNode> tree = new Tree<>(new SampleNode("root"));
        tree.getRoot().addChild(new SampleNode("c0"));
        tree.getRoot().getChildAt(0).addChild(new SampleNode("c00"));
        SampleNode current = new SampleNode("c000");
        tree.getRoot().getChildAt(0).getChildAt(0).addChild(current);

        tree.setCurrentNode(current);

        try {
            tree.getRoot().getChildren().remove(0);
            fail("Cannot remove a parent of the current node");
        } catch (IllegalStateException e) {
            // ok
        }
    }

    @Test
    public void serializeTree() throws Exception {
        SampleNode root = new SampleNode("root");
        SampleNode c0 = new SampleNode("c0");
        SampleNode c1 = new SampleNode("c1");
        SampleNode c2 = new SampleNode("c2");
        root.addChild(c0);
        root.addChild(c1);
        root.addChild(c2);
        c1.addChild(new SampleNode("c10"));
        c1.addChild(new SampleNode("c11"));
        c1.addChild(new SampleNode("c12"));
        c1.getChildAt(0).addChild(new SampleNode("c100"));
        c1.getChildAt(1).addChild(new SampleNode("c110"));

        c0.addChild(new SampleNode("c00"));
        c2.addChild(new SampleNode("c20"));

        Tree<SampleNode> tree = new Tree<>(root);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(tree);
        out.flush();

        byte[] bytes = baos.toByteArray();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object serializedForm = in.readObject();
        assertNotNull(serializedForm);
        assertTrue(serializedForm instanceof Tree<?>);
        @SuppressWarnings("unchecked")
        Tree<SampleNode> stree = (Tree<SampleNode>) serializedForm;

        TreeIterator<SampleNode> it = tree.iterator();
        TreeIterator<SampleNode> sit = stree.iterator();
        while (it.hasNext()) {
            assertTrue(sit.hasNext());
            SampleNode node = it.next();
            SampleNode snode = sit.next();
            assertEquals(node.getValue(), snode.getValue());
        }

        if (logger.isInfoEnabled()) {
            logger.info("Original Tree: " + tree);
            logger.info("Serialized tree: " + stree);
        }
    }
}
