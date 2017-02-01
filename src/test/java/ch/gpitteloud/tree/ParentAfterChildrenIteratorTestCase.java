package ch.gpitteloud.tree;

import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * @author Gaëtan Pitteloud
 */
public class ParentAfterChildrenIteratorTestCase {

    private ParentChildResolver<SampleNode> resolver = new TreeNode.Resolver<>();

    @Test
    public void firstNodeNull() throws Exception {
        Iterator<SampleNode> i = createIterator(null);
        assertFalse(i.hasNext());
    }

    @Test
    public void firstNodeNullNextFails() throws Exception {
        Iterator<SampleNode> i = createIterator(null);
        assertNextFails(i);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeNotSupported() throws Exception {
        SampleNode root = new SampleNode("root");
        Iterator<SampleNode> i = createIterator(root);
        assertTrue(i.hasNext());
        assertEquals(root, i.next());
        i.remove();
    }

    @Test
    public void singleNode() throws Exception {
        SampleNode root = new SampleNode("root");
        Iterator<SampleNode> i = createIterator(root);
        assertTrue(i.hasNext());
        assertEquals(root, i.next());
        assertFalse(i.hasNext());
        assertNextFails(i);
    }

    @Test
    public void nextWithExhaustedNavigation() throws Exception {
        SampleNode root = new SampleNode("root");
        SampleNode c0 = new SampleNode("0");
        SampleNode c1 = new SampleNode("1");
        SampleNode c2 = new SampleNode("2");
        root.addChild(c0);
        root.addChild(c1);
        root.addChild(c2);

        Iterator<SampleNode> i = createIterator(root);
        assertTrue(i.hasNext());
        assertEquals(c0, i.next());
        assertTrue(i.hasNext());
        assertEquals(c1, i.next());
        assertTrue(i.hasNext());
        assertEquals(c2, i.next());
        assertTrue(i.hasNext());
        assertEquals(root, i.next());
        assertFalse(i.hasNext());
        assertNextFails(i);
    }

    @Test
    public void completeTree() throws Exception {
        SampleNode root = createLargeTree();

        String[] expectedOrder = new String[] { "00", "01", "02", "0", "100", "1010", "1011", "101", "10", "1", "20",
                "210", "211", "212", "21", "2", "root" };
        assertIterationAsExpected(expectedOrder, root);
    }

    @Test
    public void relativeTree() throws Exception {
        SampleNode root = createLargeTree();
        SampleNode c1 = root.getChildAt(1);
        String[] expectedOrder = new String[] { "100", "1010", "1011", "101", "10", "1" };
        assertIterationAsExpected(expectedOrder, c1);
    }

    @Test
    public void oneChildAtEachLevel() throws Exception {
        SampleNode root = new SampleNode("root");
        SampleNode c0 = new SampleNode("0");
        root.addChild(c0);
        SampleNode c00 = new SampleNode("00");
        c0.addChild(c00);
        SampleNode c000 = new SampleNode("000");
        c00.addChild(c000);
        String[] expectedOrder = new String[] { "000", "00", "0", "root" };
        assertIterationAsExpected(expectedOrder, root);
    }

    private void assertIterationAsExpected(String[] expectedOrder, SampleNode root) {
        int index = 0;
        Iterator<SampleNode> i = createIterator(root);
        while (i.hasNext()) {
            String nextNodeValue = i.next().getValue();
            assertEquals(expectedOrder[index++], nextNodeValue);
        }
        assertEquals(expectedOrder.length, index);
    }

    private SampleNode createLargeTree() {
        SampleNode root = new SampleNode("root");
        SampleNode c0 = new SampleNode("0");
        SampleNode c1 = new SampleNode("1");
        SampleNode c2 = new SampleNode("2");
        root.addChild(c0);
        root.addChild(c1);
        root.addChild(c2);

        SampleNode c00 = new SampleNode("00");
        SampleNode c01 = new SampleNode("01");
        SampleNode c02 = new SampleNode("02");
        c0.addChild(c00);
        c0.addChild(c01);
        c0.addChild(c02);

        SampleNode c10 = new SampleNode("10");
        c1.addChild(c10);
        SampleNode c100 = new SampleNode("100");
        SampleNode c101 = new SampleNode("101");
        c10.addChild(c100);
        c10.addChild(c101);
        SampleNode c1010 = new SampleNode("1010");
        SampleNode c1011 = new SampleNode("1011");
        c101.addChild(c1010);
        c101.addChild(c1011);

        SampleNode c20 = new SampleNode("20");
        SampleNode c21 = new SampleNode("21");
        c2.addChild(c20);
        c2.addChild(c21);
        SampleNode c210 = new SampleNode("210");
        SampleNode c211 = new SampleNode("211");
        SampleNode c212 = new SampleNode("212");
        c21.addChild(c210);
        c21.addChild(c211);
        c21.addChild(c212);
        return root;
    }

    private void assertNextFails(Iterator<?> i) {
        try {
            i.next();
            fail();
        } catch (NoSuchElementException e) {
            // OK
        }
    }

    private Iterator<SampleNode> createIterator(SampleNode root) {
        return new ParentAfterChildrenIterator<>(resolver, root);
    }

}
