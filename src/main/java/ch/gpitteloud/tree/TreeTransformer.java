package ch.gpitteloud.tree;

import java.util.Collections;
import java.util.List;

/**
 * A helper for working with tree, based on callback interfaces. Basically, methods work with an existing tree structure
 * (Source structure) and create or update another tree (Target) with (almost) the same structure as the Source tree.
 * <p>
 * The node types (Source and Target) are type parameter arguments without constraints, and thus associated parent-child
 * resolvers are to be specified.
 *
 * @author Gaëtan Pitteloud
 * @param <S> The source node type
 * @param <T> The target node type
 */
public class TreeTransformer<S, T> {

    private final ParentChildResolver<S> sourceResolver;
    final ParentChildResolver<T> targetResolver;

    /**
     * Construct a transformer from S to T
     *
     * @param sourceResolver source resolver
     * @param targetResolver target resolver
     */
    public TreeTransformer(ParentChildResolver<S> sourceResolver, ParentChildResolver<T> targetResolver) {
        assert sourceResolver != null : "Invalid null sourceResolver";
        this.sourceResolver = sourceResolver;
        this.targetResolver = targetResolver;
    }

    /**
     * Create a tree of T based on the structure of the tree of S, with each T node created from a S node using the
     * specified mapper.
     * <p>
     * The structure of the transformation result is the same as the source structure, except when a source node (and
     * its children) is programmatically skipped in the target tree.
     * <p>
     * The source tree is traversed in BFS.
     *
     * @param rootSource the root source node
     * @param mapper the mapper from S to T, not null
     * @return the root node of the target tree, null if the rootSource is null, or if the root source is skipped.
     */
    public final T create(S rootSource, NodeMapper<S, T> mapper) {
        assert targetResolver != null : "Invalid null targetResolver";
        assert mapper != null : "The mapper must not be null";
        NodeMapperCallbackHandler handler = new NodeMapperCallbackHandler(mapper);
        update(rootSource, null, handler);
        return handler.getRootNode();
    }

    /**
     * Update the Target tree represented by its root node T, based on the structure of the Source tree represented by
     * its root node S, and the callback. The source tree is navigated in BFS, which means all children of a level are
     * traversed before moving to the next level.
     * <p>
     * If the target root node is null, the callback is first invoked with the source root node as a parameter (and null
     * target parent node), in order to be able to create the root of the target tree (the callback must keep it in
     * order to retrieve it); if the target root node is not null, the callback is not invoked with the source root
     * node, in order to prevent the creation of 2 distinct trees. Thus, it is not possible to change the target root
     * node with this method.
     *
     * @param rootSource the root of the source tree
     * @param rootTarget the root of the target tree; if null, a new tree will be created to which nodes managed by the
     *            callback can be attached; if non-null, it remains the root of the target nodes that are managed in the
     *            callback.
     * @param callback action do be done on each source node to update the target tree
     */
    public final void update(S rootSource, T rootTarget, NodeCallbackHandler<S, T> callback) {
        assert callback != null : "Invalid null callback";
        update(rootSource, rootTarget, new SingleNodeToMultiNodesHandlerAdapter(callback));
    }

    /**
     * Update the Target tree represented by its root node T, based on the structure of the Source tree represented by
     * its root node S, and the callback. The source tree is navigated in BFS, which means all children of a level are
     * traversed before moving to the next level.
     * <p>
     * If the target root node is null, the callback is first invoked with the source root node as a parameter (and null
     * target parent node), in order to be able to create the root of the target tree (the callback must keep it in
     * order to be able to retrieve it later); if the target root node is not null, the callback is not invoked with the
     * source root node, in order to prevent the creation of 2 distinct trees. Thus, it is not possible to change the
     * target root node with this method.
     * <p>
     * Use this update method instead of {@link #update(Object, Object, NodeCallbackHandler)} when a single source node
     * may handle many target nodes.
     *
     * @param rootSource the root of the source tree
     * @param rootTarget the root of the target tree; if null, a new tree will be created to which nodes managed by the
     *            callback can be attached; if non-null, it remains the root of the target nodes that are managed in the
     *            callback.
     * @param callback action do be done on each source node to update the target tree
     */
    public final void update(S rootSource, T rootTarget, MultiNodesCallbackHandler<S, T> callback) {
        StatefulNodeCallbackTransformer<S, T> transformer = new StatefulNodeCallbackTransformer<>(sourceResolver,
                rootSource, rootTarget, callback);
        transformer.update();
    }

    /**
     * Adapter from single-node to multi-nodes handler.
     *
     * @author Gaëtan Pitteloud
     */
    private class SingleNodeToMultiNodesHandlerAdapter implements MultiNodesCallbackHandler<S, T> {

        private final NodeCallbackHandler<S, T> singleNodeHandler;

        SingleNodeToMultiNodesHandlerAdapter(NodeCallbackHandler<S, T> handler) {
            this.singleNodeHandler = handler;
        }

        public List<T> doWithNode(S sourceNode, T targetParentNode) {
            T node = singleNodeHandler.doWithNode(sourceNode, targetParentNode);
            if (node == null) {
                return null;
            }
            return Collections.singletonList(node);
        }

    }

    /**
     * Adapter from NodeMapper to NodeCallbackHandler.
     *
     * @author Gaëtan Pitteloud
     */
    private class NodeMapperCallbackHandler implements NodeCallbackHandler<S, T> {

        private final NodeMapper<S, T> mapper;
        private T rootNode;

        NodeMapperCallbackHandler(NodeMapper<S, T> mapper) {
            this.mapper = mapper;
        }

        public T doWithNode(S sourceNode, T targetParentNode) {

            T targetNode = mapper.mapNode(sourceNode);
            // targetParentNode is null <==> sourceNode is root -> keep root of target tree
            if (targetParentNode == null) {
                rootNode = targetNode;
            } else if (targetNode != null) {
                targetResolver.getChildren(targetParentNode).add(targetNode);
            }
            return targetNode;
        }

        /**
         * The target tree node
         *
         * @return The target tree node
         */
        public T getRootNode() {
            return rootNode;
        }

    }
}
