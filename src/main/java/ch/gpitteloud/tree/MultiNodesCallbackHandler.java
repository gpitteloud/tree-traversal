package ch.gpitteloud.tree;

import java.util.List;

/**
 * A callback that is to be implemented in order to do something with a source node, based on the corresponding target
 * node parent. If many target nodes are handled for a single source node, this callback is to be used instead of
 * {@link NodeCallbackHandler}.
 *
 * @param <S>
 *            The source node type
 * @param <T>
 *            The target node type
 * @author Gaëtan Pitteloud
 */
@FunctionalInterface
public interface MultiNodesCallbackHandler<S, T> {

    /**
     * Work with a source node and the equivalent target parent. Typical implementations may create many Target nodes
     * and attach them to the specified parent node.
     *
     * @param sourceNode
     *            the current source node
     * @param targetParentNode
     *            the parent of the target node corresponding to the parent of the source node; null if sourceNode is
     *            the root node of the source tree
     * @return the target nodes that are equivalent to the source node. If all lists returned by the callback for a
     *         specific sourceNode are empty, the children of that source node are skipped.
     */
    List<T> doWithNode(S sourceNode, T targetParentNode);
}
