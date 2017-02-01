package ch.gpitteloud.tree;

/**
 * A callback that is to be implemented in order to do something with a source node, based on the corresponding target
 * node parent. This callback is to be used when a single target node is handled for one source node. If many target
 * nodes are handled, use {@link MultiNodesCallbackHandler}.
 *
 * @param <S>
 *            The source node type
 * @param <T>
 *            The target node type
 * @author Gaëtan Pitteloud
 */
@FunctionalInterface
public interface NodeCallbackHandler<S, T> {

    /**
     * Work with a source node and the equivalent target parent. Typical implementations may create a Target node and
     * attach it to the specified parent node.
     *
     * @param sourceNode
     *            the current source node
     * @param targetParentNode
     *            the parent of the target node corresponding to the parent of the source node; null if sourceNode is
     *            the root node of the source tree
     * @return the target node that is equivalent to the source node, or null if the children of the source node are to
     *         be ignored in the next loops
     */
    T doWithNode(S sourceNode, T targetParentNode);
}