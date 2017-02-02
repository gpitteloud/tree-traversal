package ch.gpitteloud.tree;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {@link TreeProcessor}
 *
 * @author Gaëtan Pitteloud
 */
public class TreeProcessorTestCase {

    private TreeProcessor<SampleNode> processor = new TreeProcessor<SampleNode>(new TreeNode.Resolver<>());
    private SampleNode rootSource = new SampleNode("1");
    private TestCallback callback = new TestCallback();

    @Test
    public void nullRootNode() throws Exception {
        rootSource = null;

        processNodesFromRoot();

        assertThat(callback.actions).isEmpty();
    }

    @Test
    public void onlyRoot() throws Exception {

        processNodesFromRoot();

        assertThat(callback.actions).hasSize(1).contains("process 1");
    }

    @Test
    public void aroundChildren() throws Exception {
        rootSource.createChildren("10", "11", "12");
        rootSource.getChildAt(0).createChildren("100", "101");
        rootSource.getChildAt(1).createChildren("110", "111");
        rootSource.getChildAt(1).getChildAt(1).createChildren("1110");

        processNodesFromRoot();

        assertThat(callback.actions).hasSize(17).containsExactly(
                "process 1",
                "before children 1",
                "process 10",
                "before children 10",
                "process 100",
                "process 101",
                "after children 10",
                "process 11",
                "before children 11",
                "process 110",
                "process 111",
                "before children 111",
                "process 1110",
                "after children 111",
                "after children 11",
                "process 12",
                "after children 1");

    }

    @Test
    public void skipChildren() throws Exception {
        rootSource.createChildren("10", "11", "12");
        rootSource.getChildAt(0).createChildren("100", "101");
        rootSource.getChildAt(1).createChildren("110", "111");
        rootSource.getChildAt(1).getChildAt(1).createChildren("1110");

        callback = new TestCallback() {

            @Override
            public boolean processNode(SampleNode node) {
                super.processNode(node);
                return !node.getValue().equals("11");
            }
        };

        processNodesFromRoot();

        assertThat(callback.actions).hasSize(10).containsExactly(
                "process 1",
                "before children 1",
                "process 10",
                "before children 10",
                "process 100",
                "process 101",
                "after children 10",
                "process 11",
                "process 12",
                "after children 1"
        );
    }

    @Test
    public void inputNotRootNode() throws Exception {
        SampleNode root = new SampleNode("root");
        SampleNode c0 = new SampleNode("c0");
        SampleNode c1 = new SampleNode("c1");
        SampleNode c2 = new SampleNode("c2");
        root.addAll(c0, c1, c2);
        c0.createChildren("c00");
        c1.createChildren("c10", "c11", "c12");
        c1.getChildAt(0).createChildren("c100");
        c1.getChildAt(1).createChildren("c110");
        c2.createChildren("c20");

        processor.processNodes(c1, callback);

        assertThat(callback.actions).hasSize(12).containsExactly(
                "process c1",
                "before children c1",
                "process c10",
                "before children c10",
                "process c100",
                "after children c10",
                "process c11",
                "before children c11",
                "process c110",
                "after children c11",
                "process c12",
                "after children c1"
        );
    }

    private void processNodesFromRoot() {
        processor.processNodes(rootSource, callback);
    }

    static class TestCallback implements AroundChildrenNodeCallback<SampleNode> {

        List<String> actions = new ArrayList<>();

        public boolean processNode(SampleNode node) {
            actions.add("process " + node.getValue());
            return true;
        }

        public void beforeChildren(SampleNode node) {
            actions.add("before children " + node.getValue());
        }

        public void afterChildren(SampleNode node) {
            actions.add("after children " + node.getValue());
        }
    }

}
