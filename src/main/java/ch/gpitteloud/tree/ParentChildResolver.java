package ch.gpitteloud.tree;

import java.util.List;

/**
 * A parent-child resolver is an abstraction over parent and children resolution of a tree node.
 *
 * @author Gaëtan Pitteloud
 * @param <N>
 *            The node type
 */
public interface ParentChildResolver<N> {

    /**
     * Return the children of the node
     *
     * @param node
     *            a node, not null
     * @return the children of that node, never null
     */
    List<N> getChildren(N node);

    /**
     * Return the parent of the node
     *
     * @param node
     *            a node, not null
     * @return the parent of that node, or null if the node is a the root of a tree
     */
    N getParent(N node);
}
