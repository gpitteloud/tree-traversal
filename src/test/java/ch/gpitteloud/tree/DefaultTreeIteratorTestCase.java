package ch.gpitteloud.tree;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static ch.gpitteloud.tree.ExplorationMode.BFS;
import static ch.gpitteloud.tree.ExplorationMode.DFS;
import static org.junit.Assert.*;

/**
 * Tests for {@link DefaultTreeIterator}
 *
 * @author Gaëtan Pitteloud
 */
public class DefaultTreeIteratorTestCase {

    private SampleNode root = new SampleNode("root");
    private SampleNode c0 = new SampleNode("c0");
    private SampleNode c1 = new SampleNode("c1");
    private SampleNode c10 = new SampleNode("c10");
    private SampleNode c11 = new SampleNode("c11");
    private SampleNode c00 = new SampleNode("c10");
    private SampleNode c01 = new SampleNode("c11");

    @Before
    public void setUp() throws Exception {
        root.addAll(c0, c1);
        c0.addAll(c00, c01);
        c1.addAll(c10, c11);
    }

    @Test
    public void emptyIterator() throws Exception {
        DefaultTreeIterator<SampleNode> it = new DefaultTreeIterator<>(BFS, (SampleNode) null,
                new TreeNode.Resolver<>());
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("no elements");
        } catch (NoSuchElementException e) {
            // OK
        }
    }

    @Test
    public void bfs() throws Exception {
        DefaultTreeIterator<SampleNode> it = new DefaultTreeIterator<>(BFS, root, new TreeNode.Resolver<>());
        assertIterationOrder(it, root, c0, c1, c00, c01, c10, c11);
    }

    @Test
    public void dfs() throws Exception {
        DefaultTreeIterator<SampleNode> it = new DefaultTreeIterator<>(DFS, root, new TreeNode.Resolver<>());
        assertIterationOrder(it, root, c0, c00, c01, c1, c10, c11);
    }

    private void assertIterationOrder(Iterator<SampleNode> i, SampleNode... expectedNodes) {
        for (SampleNode node : expectedNodes) {
            assertTrue(i.hasNext());
            assertSame(node, i.next());
        }
        assertFalse(i.hasNext());
    }

    @Test(expected = IllegalStateException.class)
    public void remove_withoutNext() throws Exception {
        root.iterator().remove();
    }

    @Test
    public void remove() throws Exception {
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

    @Test(expected = IllegalStateException.class)
    public void skipChildren_withoutNext() throws Exception {
        root.iterator().skipChildren();
    }

    @Test
    public void skipChildren() throws Exception {
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
