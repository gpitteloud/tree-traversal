package ch.gpitteloud.tree;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for{@link TreeTransformer}
 *
 * @author Gaëtan Pitteloud
 */
public class TreeTransformerTestCase {

    private TreeTransformer<SampleNode, OtherNode> transformer = new TreeTransformer<>(
            new TreeNode.Resolver<SampleNode>(), new TreeNode.Resolver<OtherNode>());

    @Test
    public void createNullRootSource() throws Exception {

        @SuppressWarnings("unchecked")
        ParentChildResolver<Object> sourceResolver = mock(ParentChildResolver.class);
        @SuppressWarnings("unchecked")
        ParentChildResolver<Object> targetResolver = mock(ParentChildResolver.class);
        @SuppressWarnings("unchecked")
        NodeMapper<Object, Object> mapper = mock(NodeMapper.class);

        TreeTransformer<Object, Object> transformer = new TreeTransformer<>(sourceResolver,
                targetResolver);
        assertNull(transformer.create(null, mapper));

        verifyNoMoreInteractions(sourceResolver, targetResolver, mapper);
    }

    @Test
    public void createBasic() throws Exception {
        SampleNode rootSource = SampleNode.createTree("1", "10", "11");
        rootSource.getChildAt(0).createChildren("100", "101");
        rootSource.getChildAt(1).createChildren("110", "111", "112");
        rootSource.getChildAt(1).getChildAt(2).createChildren("1120");

        OtherNode result = transformer.create(rootSource,
                sourceNode -> new OtherNode(Integer.parseInt(sourceNode.getValue())));

        assertNotNull(result);
        assertEquals(1, result.value);
        assertEquals(2, result.getChildren().size());
        assertEquals(10, result.getChildAt(0).value);
        assertEquals(11, result.getChildAt(1).value);

        assertEquals(2, result.getChildAt(0).getChildren().size());

        OtherNode r00 = result.getChildAt(0).getChildAt(0);
        assertEquals(100, r00.value);
        assertEquals(0, r00.getChildren().size());

        OtherNode r01 = result.getChildAt(0).getChildAt(1);
        assertEquals(101, r01.value);
        assertEquals(0, r01.getChildren().size());

        assertEquals(3, result.getChildAt(1).getChildren().size());

        OtherNode r10 = result.getChildAt(1).getChildAt(0);
        assertEquals(110, r10.value);
        assertEquals(0, r10.getChildren().size());

        OtherNode r11 = result.getChildAt(1).getChildAt(1);
        assertEquals(111, r11.value);
        assertEquals(0, r11.getChildren().size());

        OtherNode r12 = result.getChildAt(1).getChildAt(2);
        assertEquals(112, r12.value);
        assertEquals(1, r12.getChildren().size());
        OtherNode r120 = r12.getChildAt(0);
        assertEquals(1120, r120.value);
        assertEquals(0, r120.getChildren().size());
    }

    @Test
    public void createSkipChildren() throws Exception {
        SampleNode rootSource = SampleNode.createTree("1", "10", "11");
        rootSource.getChildAt(0).createChildren("100", "101");
        rootSource.getChildAt(1).createChildren("110", "111", "112");
        rootSource.getChildAt(1).getChildAt(2).createChildren("1120");

        OtherNode result = transformer.create(rootSource, sourceNode -> {
            if (sourceNode.getValue().equals("11")) {
                return null;
            }
            return new OtherNode(Integer.parseInt(sourceNode.getValue()));
        });

        assertNotNull(result);
        assertEquals(1, result.value);
        assertEquals(1, result.getChildren().size());
        assertEquals(10, result.getChildAt(0).value);

        assertEquals(2, result.getChildAt(0).getChildren().size());

        OtherNode r00 = result.getChildAt(0).getChildAt(0);
        assertEquals(100, r00.value);
        assertEquals(0, r00.getChildren().size());

        OtherNode r01 = result.getChildAt(0).getChildAt(1);
        assertEquals(101, r01.value);
        assertEquals(0, r01.getChildren().size());
    }

    @Test
    public void updateNonNullTargetRootNode() throws Exception {
        SampleNode rootSource = SampleNode.createTree("1", "10", "11");
        rootSource.getChildAt(0).createChildren("100", "101");
        rootSource.getChildAt(1).createChildren("110", "111", "112");
        rootSource.getChildAt(1).getChildAt(2).createChildren("1120");

        OtherNode rootTarget = OtherNode.createTree(0, 0, 0);
        rootTarget.getChildAt(0).createChildren(0, 0);
        rootTarget.getChildAt(1).createChildren(0, 0, 0);
        rootTarget.getChildAt(1).getChildAt(2).createChildren(0);

        class TestHandler implements NodeCallbackHandler<SampleNode, OtherNode> {

            private int counter;

            public OtherNode doWithNode(SampleNode sourceNode, OtherNode targetParentNode) {
                counter++;
                assertNotNull(targetParentNode);
                assertNotSame(rootSource, sourceNode);
                // callback not invoked on root -> targetParentNode is never null
                OtherNode otherNode = targetParentNode.getChildAt(sourceNode.getIndex());
                otherNode.value = Integer.parseInt(sourceNode.getValue());
                return otherNode;
            }

        }

        TestHandler handler = new TestHandler();
        transformer.update(rootSource, rootTarget, handler);
        assertEquals(8, handler.counter); // callback not invoked on root

        assertNotNull(rootTarget);
        assertEquals(0, rootTarget.value); // root node value is not updated
        assertEquals(2, rootTarget.getChildren().size());
        assertEquals(10, rootTarget.getChildAt(0).value);
        assertEquals(11, rootTarget.getChildAt(1).value);

        assertEquals(2, rootTarget.getChildAt(0).getChildren().size());

        OtherNode r00 = rootTarget.getChildAt(0).getChildAt(0);
        assertEquals(100, r00.value);
        assertEquals(0, r00.getChildren().size());

        OtherNode r01 = rootTarget.getChildAt(0).getChildAt(1);
        assertEquals(101, r01.value);
        assertEquals(0, r01.getChildren().size());

        assertEquals(3, rootTarget.getChildAt(1).getChildren().size());

        OtherNode r10 = rootTarget.getChildAt(1).getChildAt(0);
        assertEquals(110, r10.value);
        assertEquals(0, r10.getChildren().size());

        OtherNode r11 = rootTarget.getChildAt(1).getChildAt(1);
        assertEquals(111, r11.value);
        assertEquals(0, r11.getChildren().size());

        OtherNode r12 = rootTarget.getChildAt(1).getChildAt(2);
        assertEquals(112, r12.value);
        assertEquals(1, r12.getChildren().size());
        OtherNode r120 = r12.getChildAt(0);
        assertEquals(1120, r120.value);
        assertEquals(0, r120.getChildren().size());
    }

    @Test
    public void updateOrCreate() throws Exception {
        SampleNode rootSource = SampleNode.createTree("1", "10", "11");
        rootSource.getChildAt(0).createChildren("100", "101");
        rootSource.getChildAt(1).createChildren("110", "111", "112");
        rootSource.getChildAt(1).getChildAt(2).createChildren("1120");

        OtherNode rootTarget = OtherNode.createTree(0, 0);
        rootTarget.getChildAt(0).createChildren(0, 0);
        // will create branch 11 in OtherNode

        class TestHandler implements NodeCallbackHandler<SampleNode, OtherNode> {

            private int counter;

            public OtherNode doWithNode(SampleNode sourceNode, OtherNode targetParentNode) {
                counter++;
                assertNotNull(targetParentNode);
                assertNotSame(rootSource, sourceNode);
                // callback not invoked on root -> targetParentNode is never null
                List<OtherNode> tchildren = targetParentNode.getChildren();
                int index = sourceNode.getIndex();
                OtherNode otherNode;
                if (index < tchildren.size()) {
                    otherNode = tchildren.get(index);
                } else {
                    otherNode = new OtherNode();
                    tchildren.add(otherNode);
                }

                otherNode.value = Integer.parseInt(sourceNode.getValue());
                return otherNode;
            }

        }

        TestHandler handler = new TestHandler();
        transformer.update(rootSource, rootTarget, handler);
        assertEquals(8, handler.counter);

        assertNotNull(rootTarget);
        assertEquals(0, rootTarget.value); // root node value is not updated
        assertEquals(2, rootTarget.getChildren().size());
        assertEquals(10, rootTarget.getChildAt(0).value);
        assertEquals(11, rootTarget.getChildAt(1).value);

        assertEquals(2, rootTarget.getChildAt(0).getChildren().size());

        OtherNode r00 = rootTarget.getChildAt(0).getChildAt(0);
        assertEquals(100, r00.value);
        assertEquals(0, r00.getChildren().size());

        OtherNode r01 = rootTarget.getChildAt(0).getChildAt(1);
        assertEquals(101, r01.value);
        assertEquals(0, r01.getChildren().size());

        assertEquals(3, rootTarget.getChildAt(1).getChildren().size());

        OtherNode r10 = rootTarget.getChildAt(1).getChildAt(0);
        assertEquals(110, r10.value);
        assertEquals(0, r10.getChildren().size());

        OtherNode r11 = rootTarget.getChildAt(1).getChildAt(1);
        assertEquals(111, r11.value);
        assertEquals(0, r11.getChildren().size());

        OtherNode r12 = rootTarget.getChildAt(1).getChildAt(2);
        assertEquals(112, r12.value);
        assertEquals(1, r12.getChildren().size());
        OtherNode r120 = r12.getChildAt(0);
        assertEquals(1120, r120.value);
        assertEquals(0, r120.getChildren().size());
    }

    @Test
    public void updateOrCreateMultiNodes() throws Exception {

        SampleNode rootSource = SampleNode.createTree("1", "10", "11");
        rootSource.getChildAt(0).createChildren("100", "101");
        rootSource.getChildAt(1).createChildren("110", "111", "112", "113", "114");
        rootSource.getChildAt(1).getChildAt(2).createChildren("1120");

        OtherNode rootTarget = OtherNode.createTree(0, 0, 0);
        rootTarget.getChildAt(0).createChildren(0, 0);
        rootTarget.getChildAt(1).createChildren(0, 0, 0, 0, 0);
        rootTarget.getChildAt(1).getChildAt(2).createChildren(0);

        class TestHandler implements MultiNodesCallbackHandler<SampleNode, OtherNode> {

            private int counter;

            public List<OtherNode> doWithNode(SampleNode sourceNode, OtherNode targetParentNode) {
                counter++;
                assertNotNull(targetParentNode);
                assertNotSame(rootSource, sourceNode);
                // callback not invoked on root -> targetParentNode is never null
                List<OtherNode> nodes;
                int value = Integer.parseInt(sourceNode.getValue());
                int nodeIndex = sourceNode.getIndex();
                // 3 nodes were added before 113 and 114 -> shift index for these nodes
                if (value == 113 || value == 114) {
                    nodeIndex += 3;
                }

                List<OtherNode> tchildren = targetParentNode.getChildren();
                OtherNode otherNode;
                // equivalent to snode=1120 and tparentNode has no children
                if (targetParentNode.value == 900112 || targetParentNode.value == 990112
                        || targetParentNode.value == 999112) {
                    // create node 1120 under 900112, 990112 and 999112
                    otherNode = new OtherNode();
                    tchildren.add(otherNode);
                } else {
                    // equivalent to snode=1120 and tparentNode = 112 (it has a single child, namely 1120)
                    otherNode = tchildren.get(nodeIndex);
                }
                otherNode.value = value;
                nodes = new ArrayList<>();
                nodes.add(otherNode);
                if (value == 112) {
                    OtherNode n = new OtherNode(900112);
                    tchildren.add(nodeIndex + 1, n);
                    nodes.add(n);

                    n = new OtherNode(990112);
                    tchildren.add(nodeIndex + 2, n);
                    nodes.add(n);

                    n = new OtherNode(999112);
                    tchildren.add(nodeIndex + 3, n);
                    nodes.add(n);
                }
                return nodes;
            }
        }

        TestHandler handler = new TestHandler();
        transformer.update(rootSource, rootTarget, handler);

        // target node has 16 nodes (without root) after update
        // but we did not visit the child (1120) of each added 112 node (900112, 990112, 999112)
        // this makes 13
        assertEquals(13, handler.counter);

        assertNotNull(rootTarget);
        assertEquals(0, rootTarget.value); // root node value is not updated
        assertEquals(2, rootTarget.getChildren().size());

        OtherNode r0 = rootTarget.getChildAt(0);
        OtherNode r1 = rootTarget.getChildAt(1);
        assertEquals(10, r0.value);
        assertEquals(11, r1.value);

        // branch 10
        assertEquals(2, r0.getChildren().size());

        OtherNode r00 = r0.getChildAt(0);
        assertEquals(100, r00.value);
        assertEquals(0, r00.getChildren().size());

        OtherNode r01 = r0.getChildAt(1);
        assertEquals(101, r01.value);
        assertEquals(0, r01.getChildren().size());

        // branch 11
        assertEquals(8, r1.getChildren().size());

        OtherNode r10 = r1.getChildAt(0);
        assertEquals(110, r10.value);
        assertEquals(0, r10.getChildren().size());

        OtherNode r11 = r1.getChildAt(1);
        assertEquals(111, r11.value);
        assertEquals(0, r11.getChildren().size());

        OtherNode r120;
        OtherNode r12 = r1.getChildAt(2);
        assertEquals(112, r12.value);
        assertEquals(1, r12.getChildren().size());
        r120 = r12.getChildAt(0);
        assertEquals(1120, r120.value);
        assertEquals(0, r120.getChildren().size());

        // 3 additional nodes under r1
        OtherNode r90012 = r1.getChildAt(3);
        assertEquals(900112, r90012.value);
        assertEquals(1, r90012.getChildren().size());
        r120 = r90012.getChildAt(0);
        assertEquals(1120, r120.value);
        assertEquals(0, r120.getChildren().size());

        OtherNode r99012 = r1.getChildAt(4);
        assertEquals(990112, r99012.value);
        assertEquals(1, r99012.getChildren().size());
        r120 = r99012.getChildAt(0);
        assertEquals(1120, r120.value);
        assertEquals(0, r120.getChildren().size());

        OtherNode r99912 = r1.getChildAt(5);
        assertEquals(999112, r99912.value);
        assertEquals(1, r99912.getChildren().size());
        r120 = r99912.getChildAt(0);
        assertEquals(1120, r120.value);
        assertEquals(0, r120.getChildren().size());

        OtherNode r13 = r1.getChildAt(6);
        assertEquals(113, r13.value);
        assertEquals(0, r13.getChildren().size());

        OtherNode r14 = r1.getChildAt(7);
        assertEquals(114, r14.value);
        assertEquals(0, r14.getChildren().size());
    }

    @Test
    public void multinodeCreateAdditionalNodeForEachSourceNode() throws Exception {
        SampleNode sourceRoot = new SampleNode("1");
        Tree<SampleNode> sourceTree = new Tree<>(sourceRoot);
        // create a tree where each node has a single child with value = 10*parent.value
        for (int i = 0; i < 6; i++) {
            int[] path = new int[i];
            StringBuilder val = new StringBuilder("10");
            for (int j = 0; j < i; j++) {
                path[j] = 0;
                val.append('0');
            }
            sourceTree.getNodeFromPath(path).addChild(new SampleNode(val.toString()));
        }

        class TestHandler implements MultiNodesCallbackHandler<SampleNode, OtherNode> {

            public List<OtherNode> doWithNode(SampleNode sourceNode, OtherNode targetParentNode) {
                int value = Integer.parseInt(sourceNode.getValue());
                List<OtherNode> children = new ArrayList<>();
                children.add(new OtherNode(value));
                children.add(new OtherNode(2 * value));
                targetParentNode.getChildren().addAll(children);
                return children;
            }

        }

        OtherNode targetRoot = new OtherNode(1);
        Tree<OtherNode> targetTree = new Tree<>(targetRoot);

        transformer.update(sourceRoot, targetRoot, new TestHandler());

        // every node is duplicated
        // the values in children of 2 branches are identical (they are based on the same unique SampleNode value)
        for (OtherNode node : targetTree) {
            // leaf nodes have no children
            if (node.getDepth() == 6) {
                assertEquals(0, node.getChildren().size());
            } else {
                // all other nodes have 2 children
                assertEquals(2, node.getChildren().size());
                int nodeVal = node.value;
                int factor;
                if (Integer.toString(nodeVal).charAt(0) == '1') { // 1000.. node
                    factor = 10;
                } else { // 20000.. node
                    factor = 5;
                }
                assertEquals(node.value * factor, node.getChildAt(0).value);
                assertEquals(node.value * 2 * factor, node.getChildAt(1).value);
            }
        }
    }

    @Test
    public void notRoot() throws Exception {
        SampleNode root = SampleNode.createTree("root", "10", "11", "12");
        root.getChildAt(0).createChildren("100");
        SampleNode c1 = root.getChildAt(1);
        c1.createChildren("110", "111", "112");
        c1.getChildAt(0).createChildren("1100");
        c1.getChildAt(1).createChildren("1110");
        root.getChildAt(2).createChildren("120");

        OtherNode result = transformer.create(c1, sourceNode -> new OtherNode(Integer.parseInt(sourceNode.getValue())));

        assertNotNull(result);
        assertEquals(3, result.getChildren().size());
        assertEquals(110, result.getChildAt(0).value);
        assertEquals(111, result.getChildAt(1).value);
        assertEquals(112, result.getChildAt(2).value);
        assertEquals(1, result.getChildAt(0).getChildren().size());
        assertEquals(1100, result.getChildAt(0).getChildAt(0).value);
        assertEquals(1, result.getChildAt(1).getChildren().size());
        assertEquals(1110, result.getChildAt(1).getChildAt(0).value);

    }

    static class OtherNode extends TreeNode<OtherNode> {

        private static final long serialVersionUID = 1L;
        int value;

        OtherNode() {
            super(OtherNode.class);
        }

        OtherNode(int v) {
            super(OtherNode.class);
            this.value = v;
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "OtherNode(val=" + Integer.toString(value) + ")";
        }

        static OtherNode createTree(int rootValue, int... childrenValues) {
            OtherNode root = new OtherNode(rootValue);
            root.createChildren(childrenValues);
            return root;
        }

        void createChildren(int... childrenValues) {
            for (int childValue : childrenValues) {
                addChild(new OtherNode(childValue));
            }
        }

    }
}