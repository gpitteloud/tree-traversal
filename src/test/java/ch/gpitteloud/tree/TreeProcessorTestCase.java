package ch.gpitteloud.tree;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link TreeProcessor}
 *
 * @author Gaëtan Pitteloud
 */
public class TreeProcessorTestCase {

    private static final Logger logger = Logger.getLogger(TreeProcessorTestCase.class);

    @Test
    public void nullRootNode() throws Exception {
        TreeProcessor<SampleNode> p = new TreeProcessor<>(new TreeNode.Resolver<>());
        TestCallback callback = new TestCallback();
        p.processNodes(null, callback);

        assertTrue(callback.actions.isEmpty());
    }

    @Test
    public void onlyRoot() throws Exception {
        final SampleNode rootSource = new SampleNode("1");

        TreeProcessor<SampleNode> p = new TreeProcessor<>(new TreeNode.Resolver<>());
        TestCallback callback = new TestCallback();
        p.processNodes(rootSource, callback);

        List<String> actions = callback.actions;
        assertEquals(1, actions.size());
        assertEquals("process 1", actions.get(0));
    }

    @Test
    public void aroundChildren() throws Exception {
        final SampleNode rootSource = new SampleNode("1");
        rootSource.addChild(new SampleNode("10"));
        rootSource.addChild(new SampleNode("11"));
        rootSource.addChild(new SampleNode("12"));
        rootSource.getChildAt(0).addChild(new SampleNode("100"));
        rootSource.getChildAt(0).addChild(new SampleNode("101"));

        rootSource.getChildAt(1).addChild(new SampleNode("110"));
        rootSource.getChildAt(1).addChild(new SampleNode("111"));
        rootSource.getChildAt(1).getChildAt(1).addChild(new SampleNode("1110"));

        if (logger.isInfoEnabled()) {
            logger.info(new Tree<>(rootSource));
        }

        TreeProcessor<SampleNode> p = new TreeProcessor<>(new TreeNode.Resolver<>());
        TestCallback callback = new TestCallback();
        p.processNodes(rootSource, callback);

        List<String> actions = callback.actions;
        // assertEquals(16, actions.size());
        assertEquals(17, actions.size());
        Iterator<String> i = actions.iterator();
        assertEquals("process 1", i.next());
        assertEquals("before children 1", i.next());
        assertEquals("process 10", i.next());
        assertEquals("before children 10", i.next());
        assertEquals("process 100", i.next());
        assertEquals("process 101", i.next());
        assertEquals("after children 10", i.next());
        assertEquals("process 11", i.next());
        assertEquals("before children 11", i.next());
        assertEquals("process 110", i.next());
        assertEquals("process 111", i.next());
        assertEquals("before children 111", i.next());
        assertEquals("process 1110", i.next());
        assertEquals("after children 111", i.next());
        assertEquals("after children 11", i.next());
        assertEquals("process 12", i.next());
        assertEquals("after children 1", i.next());

    }

    @Test
    public void skipChildren() throws Exception {
        final SampleNode rootSource = new SampleNode("1");
        rootSource.addChild(new SampleNode("10"));
        rootSource.addChild(new SampleNode("11"));
        rootSource.addChild(new SampleNode("12"));
        rootSource.getChildAt(0).addChild(new SampleNode("100"));
        rootSource.getChildAt(0).addChild(new SampleNode("101"));

        rootSource.getChildAt(1).addChild(new SampleNode("110"));
        rootSource.getChildAt(1).addChild(new SampleNode("111"));
        rootSource.getChildAt(1).getChildAt(1).addChild(new SampleNode("1110"));

        if (logger.isInfoEnabled()) {
            logger.info(new Tree<>(rootSource));
        }

        TreeProcessor<SampleNode> p = new TreeProcessor<>(new TreeNode.Resolver<>());
        TestCallback callback = new TestCallback() {

            @Override
            public boolean processNode(SampleNode node) {
                super.processNode(node);
                return !node.getValue().equals("11");
            }
        };
        p.processNodes(rootSource, callback);

        List<String> actions = callback.actions;
        assertEquals(10, actions.size());
        Iterator<String> i = actions.iterator();
        assertEquals("process 1", i.next());
        assertEquals("before children 1", i.next());
        assertEquals("process 10", i.next());
        assertEquals("before children 10", i.next());
        assertEquals("process 100", i.next());
        assertEquals("process 101", i.next());
        assertEquals("after children 10", i.next());
        assertEquals("process 11", i.next());
        assertEquals("process 12", i.next());
        assertEquals("after children 1", i.next());

    }

    @Test
    public void inputNotRootNode() throws Exception {
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

        TreeProcessor<SampleNode> processor = new TreeProcessor<>(new TreeNode.Resolver<>());
        TestCallback callback = new TestCallback();
        processor.processNodes(c1, callback);

        List<String> actions = callback.actions;
        assertEquals(12, actions.size());
        Iterator<String> i = actions.iterator();
        assertEquals("process c1", i.next());
        assertEquals("before children c1", i.next());
        assertEquals("process c10", i.next());
        assertEquals("before children c10", i.next());
        assertEquals("process c100", i.next());
        assertEquals("after children c10", i.next());
        assertEquals("process c11", i.next());
        assertEquals("before children c11", i.next());
        assertEquals("process c110", i.next());
        assertEquals("after children c11", i.next());
        assertEquals("process c12", i.next());
        assertEquals("after children c1", i.next());

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
