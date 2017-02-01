package ch.gpitteloud.tree;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Tests the SearchMode algorithms.
 *
 * @author Gaëtan Pitteloud
 */
public class ExplorationModeTestCase {

    static class TestNode extends TreeNode<TestNode> {

        private static final long serialVersionUID = 1L;
        private final String value;

        TestNode(String name) {
            super(TestNode.class);
            this.value = name;
        }

        /**
         * @return return value
         */
        public String getValue() {
            return this.value;
        }

    }

    @Test
    public void bfsRootOnly() throws Exception {
        String name = "root";
        TestNode root = new TestNode(name);
        Tree<TestNode> tree = new Tree<>(root);

        int count = 0;
        for (TestNode node : tree) {
            assertSame(root, node);
            count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void bfsWithChildren() throws Exception {
        String name = "root";
        TestNode root = new TestNode(name);
        Tree<TestNode> tree = new Tree<>(root);
        String child1 = "child1";
        String child2 = "child2";
        root.addChild(new TestNode(child1));
        root.addChild(new TestNode(child2));

        int count = 0;
        String[] expected = new String[] { name, child1, child2 };
        for (TestNode node : tree) {
            assertEquals(expected[count], node.getValue());
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void bfsComplexTree() throws Exception {
        String name = "root";
        TestNode root = new TestNode(name);
        Tree<TestNode> tree = new Tree<>(root);
        String child1 = "child1";
        String child11 = "child11";
        String child12 = "child12";
        String child13 = "child13";
        String child111 = "child111";
        String child112 = "child112";
        String child121 = "child121";
        String child1211 = "child1211";
        String child131 = "child131";
        String child1311 = "child1311";
        String child13111 = "child13111";
        String child13112 = "child13112";

        String child2 = "child2";
        String child21 = "child21";
        String child22 = "child22";
        String child211 = "child211";
        String child2111 = "child2111";
        String child21111 = "child21111";
        String child21112 = "child21112";
        String child21113 = "child21113";
        String child21114 = "child21114";
        String child211141 = "child211141";

        TestNode nchild1 = new TestNode(child1);
        TestNode nchild11 = new TestNode(child11);
        TestNode nchild12 = new TestNode(child12);
        TestNode nchild13 = new TestNode(child13);
        TestNode nchild111 = new TestNode(child111);
        TestNode nchild112 = new TestNode(child112);
        TestNode nchild121 = new TestNode(child121);
        TestNode nchild1211 = new TestNode(child1211);
        TestNode nchild131 = new TestNode(child131);
        TestNode nchild1311 = new TestNode(child1311);
        TestNode nchild13111 = new TestNode(child13111);
        TestNode nchild13112 = new TestNode(child13112);

        TestNode nchild2 = new TestNode(child2);
        TestNode nchild21 = new TestNode(child21);
        TestNode nchild22 = new TestNode(child22);
        TestNode nchild211 = new TestNode(child211);
        TestNode nchild2111 = new TestNode(child2111);
        TestNode nchild21111 = new TestNode(child21111);
        TestNode nchild21112 = new TestNode(child21112);
        TestNode nchild21113 = new TestNode(child21113);
        TestNode nchild21114 = new TestNode(child21114);
        TestNode nchild211141 = new TestNode(child211141);

        root.addChild(nchild1);
        root.addChild(nchild2);
        nchild1.addChild(nchild11);
        nchild1.addChild(nchild12);
        nchild1.addChild(nchild13);

        nchild11.addChild(nchild111);
        nchild11.addChild(nchild112);
        nchild12.addChild(nchild121);
        nchild121.addChild(nchild1211);
        nchild13.addChild(nchild131);
        nchild131.addChild(nchild1311);
        nchild1311.addChild(nchild13111);
        nchild1311.addChild(nchild13112);

        nchild2.addChild(nchild21);
        nchild2.addChild(nchild22);
        nchild21.addChild(nchild211);
        nchild211.addChild(nchild2111);
        nchild2111.addChild(nchild21111);
        nchild2111.addChild(nchild21112);
        nchild2111.addChild(nchild21113);
        nchild2111.addChild(nchild21114);
        nchild21114.addChild(nchild211141);

        int count = 0;
        String[] expectedNames = new String[] { name, child1, child2, child11, child12, child13, child21, child22,
                child111, child112, child121, child131, child211, child1211, child1311, child2111, child13111,
                child13112, child21111, child21112, child21113, child21114, child211141 };
        for (TestNode node : tree) {
            assertEquals(expectedNames[count], node.getValue());
            count++;
        }
        assertEquals(23, count);

    }

    @Test
    public void dfsRootOnly() throws Exception {
        String name = "root";
        TestNode root = new TestNode(name);
        Tree<TestNode> tree = new Tree<>(root);

        int count = 0;
        for (Iterator<TestNode> it = tree.iterator(ExplorationMode.DFS); it.hasNext();) {
            TestNode node = it.next();
            assertSame(root, node);
            count++;
        }
        assertEquals(1, count);

    }

    @Test
    public void dfsWithChildren() throws Exception {
        String name = "root";
        TestNode root = new TestNode(name);
        Tree<TestNode> tree = new Tree<>(root);
        String child1 = "child1";
        String child2 = "child2";
        root.addChild(new TestNode(child1));
        root.addChild(new TestNode(child2));

        int count = 0;
        String[] expected = new String[] { name, child1, child2 };
        for (Iterator<TestNode> it = tree.iterator(ExplorationMode.DFS); it.hasNext();) {
            TestNode node = it.next();
            assertEquals(expected[count], node.getValue());
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void dfsComplexTree() throws Exception {
        String name = "root";
        TestNode root = new TestNode(name);
        Tree<TestNode> tree = new Tree<>(root);
        String child1 = "child1";
        String child11 = "child11";
        String child12 = "child12";
        String child13 = "child13";
        String child111 = "child111";
        String child112 = "child112";
        String child121 = "child121";
        String child1211 = "child1211";
        String child131 = "child131";
        String child1311 = "child1311";
        String child13111 = "child13111";
        String child13112 = "child13112";

        String child2 = "child2";
        String child21 = "child21";
        String child22 = "child22";
        String child211 = "child211";
        String child2111 = "child2111";
        String child21111 = "child21111";
        String child21112 = "child21112";
        String child21113 = "child21113";
        String child21114 = "child21114";
        String child211141 = "child211141";

        TestNode nchild1 = new TestNode(child1);
        TestNode nchild11 = new TestNode(child11);
        TestNode nchild12 = new TestNode(child12);
        TestNode nchild13 = new TestNode(child13);
        TestNode nchild111 = new TestNode(child111);
        TestNode nchild112 = new TestNode(child112);
        TestNode nchild121 = new TestNode(child121);
        TestNode nchild1211 = new TestNode(child1211);
        TestNode nchild131 = new TestNode(child131);
        TestNode nchild1311 = new TestNode(child1311);
        TestNode nchild13111 = new TestNode(child13111);
        TestNode nchild13112 = new TestNode(child13112);

        TestNode nchild2 = new TestNode(child2);
        TestNode nchild21 = new TestNode(child21);
        TestNode nchild22 = new TestNode(child22);
        TestNode nchild211 = new TestNode(child211);
        TestNode nchild2111 = new TestNode(child2111);
        TestNode nchild21111 = new TestNode(child21111);
        TestNode nchild21112 = new TestNode(child21112);
        TestNode nchild21113 = new TestNode(child21113);
        TestNode nchild21114 = new TestNode(child21114);
        TestNode nchild211141 = new TestNode(child211141);

        root.addChild(nchild1);
        nchild1.addChild(nchild11);
        nchild1.addChild(nchild12);
        nchild1.addChild(nchild13);

        nchild11.addChild(nchild111);
        nchild11.addChild(nchild112);
        nchild12.addChild(nchild121);
        nchild121.addChild(nchild1211);
        nchild13.addChild(nchild131);
        nchild131.addChild(nchild1311);
        nchild1311.addChild(nchild13111);
        nchild1311.addChild(nchild13112);

        root.addChild(nchild2);
        nchild2.addChild(nchild21);
        nchild2.addChild(nchild22);
        nchild21.addChild(nchild211);
        nchild211.addChild(nchild2111);
        nchild2111.addChild(nchild21111);
        nchild2111.addChild(nchild21112);
        nchild2111.addChild(nchild21113);
        nchild2111.addChild(nchild21114);
        nchild21114.addChild(nchild211141);

        int count = 0;
        String[] expectedNames = new String[] { name, child1, child11, child111, child112, child12, child121,
                child1211, child13, child131, child1311, child13111, child13112, child2, child21, child211, child2111,
                child21111, child21112, child21113, child21114, child211141, child22 };
        for (Iterator<TestNode> it = tree.iterator(ExplorationMode.DFS); it.hasNext();) {
            TestNode node = it.next();
            assertEquals(expectedNames[count], node.getValue());
            count++;
        }
        assertEquals(23, count);

    }
}
