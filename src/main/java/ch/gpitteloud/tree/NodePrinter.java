package ch.gpitteloud.tree;

import java.util.List;
import java.util.function.Function;


/**
 * A helper that prints a tree, with each node on a new line, in the same layout as an explorer or the dependency tree
 * of Maven.
 *
 * @author Gaëtan Pitteloud
 * @param <N>
 *            The node type
 */
public class NodePrinter<N> {

    private final ParentChildResolver<N> resolver;
    private final Function<N, String> toStringFunction;

    private char beforeNodeChar = '-';
    private char sep = ' ';
    private char linkToNextChildChar = '|';
    private char linkToCurrentChildChar = '+';
    private char lastChildChar = '\\';

    /**
     * Create the printer.
     *
     * @param resolver
     *            parent-child resolver
     */
    public NodePrinter(ParentChildResolver<N> resolver) {
        this(resolver, String::valueOf);
    }

    public NodePrinter(ParentChildResolver<N> resolver, Function<N, String> toString) {
        assert resolver != null : "Invalid null resolver";
        assert toString != null : "Invalid null toString function";
        this.resolver = resolver;
        this.toStringFunction = toString;
    }

    /**
     * Return the string representation, without logging.
     *
     * @param rootNode root node
     * @return String representation
     */
    public String toString(N rootNode) {
        StringBuilder buf = new StringBuilder();
        DefaultTreeIterator<N> i = new DefaultTreeIterator<>(ExplorationMode.DFS, rootNode, resolver);
        while (i.hasNext()) {
            printNodeInTree(i.next(), buf);
        }

        return buf.toString();
    }

    /**
     * Print a single node at the end of the buffer, with correct indentation and final '\n'
     *
     * @param node a node
     * @param buf the current buffer
     */
    private void printNodeInTree(N node, StringBuilder buf) {
        N parent = resolver.getParent(node);
        if (parent == null) {
            appendNode(node, buf);
        } else {

            // position of insertion of leading chars for each parent
            int offset = buf.length();
            buf.append((isLastChild(node) ? lastChildChar : linkToCurrentChildChar)).append(beforeNodeChar).append(sep);
            appendNode(node, buf);

            while (parent != null) {
                N grandParent = resolver.getParent(parent);
                String prefix = grandParent == null ? "" : isLastChild(parent) ? ("" + sep + sep + sep) : (""
                        + linkToNextChildChar + sep + sep);
                buf.insert(offset, prefix);
                parent = grandParent;
            }
        }

    }

    private void appendNode(N node, StringBuilder buf) {
        buf.append(toStringFunction.apply(node)).append("\n");
    }

    /**
     * Answer if this node is the last child of its parent's children. Invalid if the node has no parents.
     *
     * @param node
     *            a node with a parent
     * @return true if last child, false otherwise
     */
    private boolean isLastChild(N node) {
        N parent = resolver.getParent(node);
        List<N> siblings = resolver.getChildren(parent);
        return siblings.get(siblings.size() - 1).equals(node);
    }

    /**
     * @return return beforeNodeChar
     */
    protected char getBeforeNodeChar() {
        return this.beforeNodeChar;
    }

    /**
     * The char that prints before a node. '-' by default
     *
     * @param beforeNodeChar
     *            beforeNodeChar to set
     */
    public void setBeforeNodeChar(char beforeNodeChar) {
        this.beforeNodeChar = beforeNodeChar;
    }

    /**
     * @return return sep
     */
    protected char getSep() {
        return this.sep;
    }

    /**
     * The whitespace char. ' ' by default.
     *
     * @param sep
     *            sep to set
     */
    public void setSep(char sep) {
        this.sep = sep;
    }

    /**
     * @return return linkToNextChildChar
     */
    protected char getLinkToNextChildChar() {
        return this.linkToNextChildChar;
    }

    /**
     * The char from one node to the next one, when there are children inbetween. '|' by default.
     *
     * @param linkToNextChildChar
     *            linkToNextChildChar to set
     */
    public void setLinkToNextChildChar(char linkToNextChildChar) {
        this.linkToNextChildChar = linkToNextChildChar;
    }

    /**
     * @return return linkToCurrentChildChar
     */
    protected char getLinkToCurrentChildChar() {
        return this.linkToCurrentChildChar;
    }

    /**
     * The char from one node to the next one, when there are no children inbetween. '+' by default.
     *
     * @param linkToCurrentChildChar
     *            linkToCurrentChildChar to set
     */
    public void setLinkToCurrentChildChar(char linkToCurrentChildChar) {
        this.linkToCurrentChildChar = linkToCurrentChildChar;
    }

    /**
     * @return return lastChildChar
     */
    protected char getLastChildChar() {
        return this.lastChildChar;
    }

    /**
     * The char from the penultimate node to the last one. '\' by default.
     *
     * @param lastChildChar
     *            lastChildChar to set
     */
    public void setLastChildChar(char lastChildChar) {
        this.lastChildChar = lastChildChar;
    }

    /**
     * @return return resolver
     */
    protected ParentChildResolver<N> getResolver() {
        return this.resolver;
    }

}
