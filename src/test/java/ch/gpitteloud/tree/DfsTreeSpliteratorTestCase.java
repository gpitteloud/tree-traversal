package ch.gpitteloud.tree;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

public class DfsTreeSpliteratorTestCase {

    private boolean log = false;
    private long time;
    @Rule
    public TestName nameRule = new TestName();

    private SampleNode root;
    private int depth = 5;
    private int width = 3;
    private AtomicInteger counter = new AtomicInteger();
    private int expectedCount = 1;
    private Consumer<SampleNode> c = n -> {
        counter.incrementAndGet();
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
        }
        log("Consuming node " + n);
    };

    @Before
    public void setUp() throws Exception {
        for (int i = 0; i <= depth; i++) {
            expectedCount *= width;
        }
        expectedCount--;
        expectedCount /= width - 1;
        log("w=" + width + ", d=" + depth + " -> expectedCount=" + expectedCount);
        createTree();
        if (log) {
            log(new NodePrinter<SampleNode>(new TreeNode.Resolver<>()).toString(root));
        }

        time = System.currentTimeMillis();
    }

    @After
    public void tearDown() throws Exception {
        time = System.currentTimeMillis() - time;
        System.out.println("Test '" + nameRule.getMethodName() + "' duration : " + time);
    }

    private String toString(Object o) {
        return Integer.toString(System.identityHashCode(o) % 1000);
    }

    private void logFork(Object from, Object to) {
        log("Forked spliterator from " + toString(from) + ": " + toString(to));
    }

    @Test
    public void count() throws Exception {
        consumeAll(createSpliterator());
        verifyCount();
    }

    @Test
    public void singleSplit() throws Exception {
        final DfsTreeSpliterator<SampleNode> s0 = createSpliterator();
        final Spliterator<SampleNode> s1 = s0.trySplit();
        logFork(s0, s1);

        assertNotNull(s1);

        consumeAll(s0);
        assertNull(s0.trySplit());
        consumeAll(s1);
        verifyCount();
    }

    @Test
    public void advanceThenSplit() throws Exception {
        final DfsTreeSpliterator<SampleNode> s0 = createSpliterator();
        consumeOne(s0);

        final Spliterator<SampleNode> s1 = s0.trySplit();
        logFork(s0, s1);
        assertNotNull(s1);

        consumeAll(s0);
        consumeAll(s1);

        verifyCount();
    }

    @Test
    public void multipleSplit() throws Exception {
        final Spliterator<SampleNode> s0 = createSpliterator();
        log("Initial spliterator: " + toString(s0));
        final Spliterator<SampleNode> s1 = s0.trySplit();
        logFork(s0, s1);

        consumeOne(s0);
        consumeOne(s1);
        consumeOne(s1);
        final Spliterator<SampleNode> s2 = s0.trySplit();
        logFork(s0, s2);
        consumeOne(s0);
        consumeOne(s1);
        consumeOne(s0);
        consumeOne(s2);
        consumeOne(s2);
        consumeOne(s0);

        final Spliterator<SampleNode> s3 = s1.trySplit();
        logFork(s1, s3);
        consumeOne(s1);
        consumeOne(s0);
        consumeOne(s2);
        consumeOne(s3);
        consumeOne(s3);

        final Spliterator<SampleNode> s4 = s0.trySplit();
        logFork(s0, s4);
        consumeOne(s4);
        consumeOne(s4);

        consumeAll(s0);
        assertFalse(s0.tryAdvance(n -> {
        }));
        assertNull(s0.trySplit());

        consumeAll(s1);
        consumeAll(s2);
        consumeAll(s3);
        consumeAll(s4);

        verifyCount();
    }

    @Test
    public void concurrent() throws Exception {
        final DfsTreeSpliterator<SampleNode> s = createSpliterator();
        final Stream<SampleNode> stream = StreamSupport.stream(s, true);
        stream.forEach(c);
    }

    private DfsTreeSpliterator<SampleNode> createSpliterator() {
        final DfsTreeSpliterator<SampleNode> s = new DfsTreeSpliterator<>(root, new TreeNode.Resolver<>());
        log("Creating spliterator starting on " + root + " : " + toString(s));
        return s;
    }

    private void consumeOne(Spliterator<SampleNode> s) {
        if (s == null || !s.tryAdvance(c)) {
            log("Spliterator " + toString(s) + " is exhausted");
        } else {
            log("Successfully advanced spliterator " + toString(s));
        }
    }

    private void consumeAll(Spliterator<SampleNode> s) {
        if (s == null) {
            log("Spliterator is exhaused");
        } else {
            log("About to consume spliterator " + toString(s));
            s.forEachRemaining(c);
            log("Consumed spliterator " + toString(s));
        }
    }


    private void createTree() {
        root = new SampleNode("n");

        List<SampleNode> nodes = new ArrayList<>();
        nodes.add(root);
        while (!nodes.isEmpty()) {
            SampleNode node = nodes.remove(nodes.size() - 1);
            if (canAddChildren(node)) {
                createChildren(node, nodes);
            }
        }
    }

    private boolean canAddChildren(SampleNode node) {
        return node.getDepth() < depth;
    }

    private void createChildren(SampleNode parent, List<SampleNode> buffer) {
        String parentName = parent.getValue();
        for (int index = 0; index < width; index++) {
            SampleNode child = new SampleNode(parentName + ':' + index);
            parent.addChild(child);
            buffer.add(child);
        }
    }

    private void log(String s) {
        if (log) {
            System.out.println(s);
        }
    }

    private void verifyCount() {
        assertEquals(expectedCount, counter.get());
    }

}