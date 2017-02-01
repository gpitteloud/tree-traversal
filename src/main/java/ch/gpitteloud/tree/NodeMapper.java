package ch.gpitteloud.tree;

/**
 * Mapper from a source node to a target node.
 *
 * @author Gaëtan Pitteloud
 * @param <S>
 *            The source node type
 * @param <T>
 *            The target node type
 */
@FunctionalInterface
public interface NodeMapper<S, T> {

    /**
     *
     * @param sourceNode source node
     * @return the target node, or null if this node and its children must not be copied into the target tree.
     */
    T mapNode(S sourceNode);
}