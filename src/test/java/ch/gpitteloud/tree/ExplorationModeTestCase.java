package ch.gpitteloud.tree;

import org.junit.Test;

import java.util.Iterator;

import static ch.gpitteloud.tree.ExplorationMode.DFS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests the SearchMode algorithms.
 *
 * @author Gaëtan Pitteloud
 */
public class ExplorationModeTestCase {

    @Test
    public void bfsRootOnly() throws Exception {
        String name = "root";
        SampleNode root = new SampleNode(name);
        Tree<SampleNode> tree = new Tree<>(root);

        assertIterationOrder(tree.iterator(), name);
    }

    @Test
    public void bfsWithChildren() throws Exception {
        String name = "root";
        String child1 = "child1";
        String child2 = "child2";
        SampleNode root = SampleNode.createTree(name, child1, child2);
        Tree<SampleNode> tree = new Tree<>(root);

        assertIterationOrder(tree.iterator(), name, child1, child2);
    }

    @Test
    public void dfsRootOnly() throws Exception {
        SampleNode root = new SampleNode("root");
        Tree<SampleNode> tree = new Tree<>(root);

        assertIterationOrder(tree.iterator(DFS), "root");
    }

    @Test
    public void dfsWithChildren() throws Exception {
        String name = "root";
        String child1 = "child1";
        String child2 = "child2";
        SampleNode root = SampleNode.createTree(name, child1, child2);
        Tree<SampleNode> tree = new Tree<>(root);

        assertIterationOrder(tree.iterator(DFS), name, child1, child2);
    }

    @Test
    public void bfsComplexTree() throws Exception {
        Tree<SampleNode> tree = createLargeTree();

        assertIterationOrder(tree.iterator(), "root", "child1", "child2", "child11", "child12", "child13", "child21",
                "child22", "child111", "child112", "child121", "child131", "child211", "child1211", "child1311",
                "child2111", "child13111", "child13112", "child21111", "child21112", "child21113", "child21114",
                "child211141");
    }

    @Test
    public void dfsComplexTree() throws Exception {
        Tree<SampleNode> tree = createLargeTree();

        assertIterationOrder(tree.iterator(DFS), "root", "child1", "child11", "child111", "child112", "child12",
                "child121", "child1211", "child13", "child131", "child1311", "child13111", "child13112", "child2",
                "child21", "child211", "child2111", "child21111", "child21112", "child21113", "child21114",
                "child211141",
                "child22");
    }

    private Tree<SampleNode> createLargeTree() {
        SampleNode root = new SampleNode("root");

        SampleNode child1 = new SampleNode("child1");
        SampleNode child11 = new SampleNode("child11");
        SampleNode child12 = new SampleNode("child12");
        SampleNode child13 = new SampleNode("child13");
        SampleNode child111 = new SampleNode("child111");
        SampleNode child112 = new SampleNode("child112");
        SampleNode child121 = new SampleNode("child121");
        SampleNode child1211 = new SampleNode("child1211");
        SampleNode child131 = new SampleNode("child131");
        SampleNode child1311 = new SampleNode("child1311");
        SampleNode child13111 = new SampleNode("child13111");
        SampleNode child13112 = new SampleNode("child13112");

        SampleNode child2 = new SampleNode("child2");
        SampleNode child21 = new SampleNode("child21");
        SampleNode child22 = new SampleNode("child22");
        SampleNode child211 = new SampleNode("child211");
        SampleNode child2111 = new SampleNode("child2111");
        SampleNode child21111 = new SampleNode("child21111");
        SampleNode child21112 = new SampleNode("child21112");
        SampleNode child21113 = new SampleNode("child21113");
        SampleNode child21114 = new SampleNode("child21114");
        SampleNode child211141 = new SampleNode("child211141");

        root.addAll(child1, child2);
        child1.addAll(child11, child12, child13);
        child11.addAll(child111, child112);
        child12.addChild(child121);
        child121.addChild(child1211);
        child13.addChild(child131);
        child131.addChild(child1311);
        child1311.addAll(child13111, child13112);
        child2.addAll(child21, child22);
        child21.addChild(child211);
        child211.addChild(child2111);
        child2111.addAll(child21111, child21112, child21113, child21114);
        child21114.addChild(child211141);

        return new Tree<>(root);
    }

    private void assertIterationOrder(Iterator<SampleNode> it, String... nodes) {
        int index = 0;
        while (it.hasNext()) {
            assertEquals(nodes[index++], it.next().getValue());
        }
        assertFalse(it.hasNext());
    }
}
