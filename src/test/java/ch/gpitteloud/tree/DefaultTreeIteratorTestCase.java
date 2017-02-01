package ch.gpitteloud.tree;

import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static ch.gpitteloud.tree.ExplorationMode.BFS;
import static org.junit.Assert.*;

/**
 * Tests for {@link DefaultTreeIterator}
 *
 * @author Gaëtan Pitteloud
 */
public class DefaultTreeIteratorTestCase {

    @Test
    public void emptyIterator() throws Exception {
        DefaultTreeIterator<SampleNode> it = new DefaultTreeIterator<>(BFS, (SampleNode) null, new TreeNode.Resolver<>());
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("no elements");
        } catch (NoSuchElementException e) {
            // OK
        }
    }

    @Test
    public void simpleIteration() throws Exception {
        SampleNode root = new SampleNode("root");
        SampleNode c0 = new SampleNode("c0");
        root.addChild(c0);
        SampleNode c1 = new SampleNode("c1");
        root.addChild(c1);

        DefaultTreeIterator<SampleNode> it = new DefaultTreeIterator<>(BFS, root, new TreeNode.Resolver<>());
        assertTrue(it.hasNext());
        assertSame(root, it.next());
        assertTrue(it.hasNext());
        assertSame(c0, it.next());
        assertTrue(it.hasNext());
        assertSame(c1, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void remove() throws Exception {
        SampleNode root = new SampleNode("root");
        SampleNode c0 = new SampleNode("c0");
        root.addChild(c0);
        SampleNode c1 = new SampleNode("c1");
        root.addChild(c1);

        SampleNode c00 = new SampleNode("c00");
        c0.addChild(c00);
        SampleNode c01 = new SampleNode("c01");
        c0.addChild(c01);

        SampleNode c10 = new SampleNode("c10");
        c1.addChild(c10);
        SampleNode c11 = new SampleNode("c11");
        c1.addChild(c11);

        try {
            root.iterator().remove();
            fail("remove without next");
        } catch (IllegalStateException e) {
            // OK
        }

        int count = 0;
        for (Iterator<SampleNode> it = root.iterator(); it.hasNext(); count++) {
            SampleNode node = it.next();
            if (node.getValue().equals("c1")) {
                it.remove();
            }
        }
        // c10 and c11 were not traversed
        assertEquals(5, count);
        // c1 was removed
        assertEquals(1, root.getChildren().size());
        assertFalse(root.getChildren().contains(c1));
        // the children of c1 are still connected to c1
        assertTrue(c1.getChildren().contains(c10));
        assertTrue(c1.getChildren().contains(c11));
    }

    @Test
    public void removeFromIteration() throws Exception {
        SampleNode root = new SampleNode("root");
        SampleNode c0 = new SampleNode("c0");
        root.addChild(c0);
        SampleNode c1 = new SampleNode("c1");
        root.addChild(c1);

        SampleNode c00 = new SampleNode("c00");
        c0.addChild(c00);
        SampleNode c01 = new SampleNode("c01");
        c0.addChild(c01);

        SampleNode c10 = new SampleNode("c10");
        c1.addChild(c10);
        SampleNode c11 = new SampleNode("c11");
        c1.addChild(c11);

        try {
            root.iterator().skipChildren();
            fail("skipChildren without next");
        } catch (IllegalStateException e) {
            // OK
        }

        int count = 0;
        for (TreeIterator<SampleNode> it = root.iterator(); it.hasNext(); count++) {
            SampleNode node = it.next();
            if (node.getValue().equals("c1")) {
                it.skipChildren();
            }
        }
        // c10 and c11 were not traversed
        assertEquals(5, count);
        // c1 was not removed
        assertEquals(2, root.getChildren().size());
        assertTrue(root.getChildren().contains(c1));
        // the children of c1 are still connected to c1
        assertTrue(c1.getChildren().contains(c10));
        assertTrue(c1.getChildren().contains(c11));
    }

}
