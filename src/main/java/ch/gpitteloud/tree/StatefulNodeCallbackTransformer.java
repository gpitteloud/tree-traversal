package ch.gpitteloud.tree;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Internal stateful class used to run {@link TreeTransformer} callback methods.
 *
 * @author Gaëtan Pitteloud
 * @param <S> Source node type
 * @param <T> Target node type
 */
class StatefulNodeCallbackTransformer<S, T> {

    private static final Logger logger = Logger.getLogger(StatefulNodeCallbackTransformer.class);

    private final ParentChildResolver<S> sourceResolver;
    private final S rootSource;
    private final MultiNodesCallbackHandler<S, T> callback;

    private final IdentityHashMap<S, List<T>> parentsMap;
    private final TreeIterator<S> sourceItr;
    private final boolean createTargetTreeRoot;

    StatefulNodeCallbackTransformer(ParentChildResolver<S> sourceResolver, S rootSource, T rootTarget,
            MultiNodesCallbackHandler<S, T> callback) {
        this.sourceResolver = sourceResolver;
        this.rootSource = rootSource;
        createTargetTreeRoot = rootTarget == null;
        assert callback != null : "The callback must not be null";
        this.callback = callback;
        parentsMap = new IdentityHashMap<>();
        parentsMap.put(rootSource, Collections.singletonList(rootTarget));
        // first iteration: the parent of the root source node is null; its (unique) equivalent target node is also null
        parentsMap.put(null, Collections.singletonList(null));

        sourceItr = new DefaultTreeIterator<>(ExplorationMode.BFS, rootSource, sourceResolver);
    }

    void update() {
        while (sourceItr.hasNext()) {
            S source = sourceItr.next();
            if (logger.isDebugEnabled()) {
                logger.debug("Handling source node " + source);
            }
            // maybe rootSource is not the root of the tree, but a standard node with a non-null parent
            // in this case, we must ignore its parent, as we are relative to rootSource
            S sourceParent = isRoot(source) ? null : sourceResolver.getParent(source);
            List<T> targetParents = parentsMap.get(sourceParent);

            // if we're on the source node, only invoke the callback when we must create the target root node
            if (!isRoot(source) || createTargetTreeRoot) {
                List<T> targets = invokeCallback(source, targetParents);
                handleCallbackResult(source, targets);
            }
        }
    }

    private boolean isRoot(S source) {
        return rootSource == source;
    }

    /**
     * Invoke the callback with the same source node, and every parent target node that was previously mapped to the
     * parent of the source node (i.e. targetParents).
     *
     * @param source the current source node
     * @param targetParents the parents of the target nodes
     * @return all target nodes resulting in the callback invocation(s)
     */
    private List<T> invokeCallback(S source, List<T> targetParents) {
        List<T> allTargetNodes = new LinkedList<>();
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking the callback on all corresponding target parent nodes: " + targetParents);
        }
        for (T targetParent : targetParents) {
            List<T> targets = callback.doWithNode(source, targetParent);
            if (targets != null) {
                allTargetNodes.addAll(targets);
            }
        }
        return allTargetNodes;
    }

    /**
     * Either skip the children of current node if target nodes are empty, or map all targets to the current node for a
     * later iteration.
     *
     * @param source the current source node
     * @param targets the result of callback invocations
     */
    private void handleCallbackResult(S source, List<T> targets) {
        if (targets.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping children of source node " + source);
            }
            // skip children, as specified by callback method
            sourceItr.skipChildren();
        } else {
            // map resulting nodes with current source node for a later iteration
            if (logger.isDebugEnabled()) {
                logger.debug("Mapping source node " + source + " with corresponding target nodes: " + targets);
            }
            parentsMap.put(source, targets);
        }
    }

}
