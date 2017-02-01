package ch.gpitteloud.tree;

/**
 * A callback to be used with the {@link TreeProcessor}, allowing to perform actions on a node and around its direct
 * children. For each traversed node, {@link #processNode(Object)} is invoked.
 * <ul>
 * <li>If it returns true, {@link #beforeChildren(Object)} is invoked for that node, followed by
 * {@link #processNode(Object)} on each of its children, and finally {@link #afterChildren(Object)} is invoked for that
 * node.</li>
 * <li>If {@link #processNode(Object)} returns false, the children are not traversed, and
 * {@link #beforeChildren(Object)} and {@link #afterChildren(Object)} methods are not invoked.</li>
 * </ul>
 *
 * @author Gaëtan Pitteloud
 * @param <N> The node type
 */
public interface AroundChildrenNodeCallback<N> {

    /**
     * Process the specified node
     *
     * @param node a node
     * @return true to process the children of this node, false to skip them
     */
    boolean processNode(N node);

    /**
     * Perform an action after having processed a node, but before processing its children.
     * <p>
     * The method is not invoked on a node if {@link #processNode(Object)} returned false for that node.
     *
     * @param node a node
     */
    void beforeChildren(N node);

    /**
     * Perform an action after having processed the children of the node, before processing the next sibling or parent
     * (if this node was the last child).
     * <p>
     * The method is not invoked on a node if {@link #processNode(Object)} returned false for that node.
     *
     * @param node a node
     */
    void afterChildren(N node);
}
