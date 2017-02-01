package ch.gpitteloud.tree;

/**
 * This exception is thrown during iteration over the nodes of a tree, when the same node is encountered a second time
 * by the <code>next()</code> method, meaning the tree contains a cycle.
 * <p>
 * It is essential to detect the cycles and block iteration, as a cycle otherwise means non-ending iteration.
 *
 * @author Gaëtan Pitteloud
 */
public class CycleInTreeException extends RuntimeException {

    private static final long serialVersionUID = -1648801610924269167L;
    private final transient Object node;

    /**
     *
     * @param node
     */
    public CycleInTreeException(Object node) {
        super(node.toString());
        this.node = node;
    }

    /**
     * @return return node
     */
    public Object getNode() {
        return this.node;
    }

}
