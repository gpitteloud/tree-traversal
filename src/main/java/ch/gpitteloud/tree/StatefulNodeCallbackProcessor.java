package ch.gpitteloud.tree;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal stateful class used to run {@link TreeProcessor#processNodes(Object, AroundChildrenNodeCallback)}.
 * <p>
 * Maintains a stack of nodes while iterating in DFS, on which beforeChildren and afterChildren are invoked
 *
 * @author Gaëtan Pitteloud
 * @param <N> the node type
 */
class StatefulNodeCallbackProcessor<N> {

    private static final Logger logger = Logger.getLogger(StatefulNodeCallbackProcessor.class);

    private final ParentChildResolver<N> resolver;
    private final AroundChildrenNodeCallback<N> callback;

    private final List<N> stackOfCallbackNode = new ArrayList<>();
    private final int rootDepth;
    private final TreeIterator<N> treeIterator;

    // the stack index of the node on which the before/afterChildren callback methods are invoked
    private int callbackNodeStackIndex = -1;

    public StatefulNodeCallbackProcessor(N root, ParentChildResolver<N> resolver, AroundChildrenNodeCallback<N> callback) {
        assert callback != null : "Invalid null callback";
        this.resolver = resolver;
        this.callback = callback;
        rootDepth = getDepth(root);
        treeIterator = new DefaultTreeIterator<>(ExplorationMode.DFS, root, resolver);
    }

    public void processNodes() {
        while (treeIterator.hasNext()) {
            final N currentNode = treeIterator.next();
            final int depthOfCurrentNode = getRelativeDepth(currentNode);

            // down at least 1 level
            popCallbackNodesAndRunAfterChildren(depthOfCurrentNode);

            if (depthOfCurrentNode == callbackNodeStackIndex) {
                // same level
                setCurrentNodeAsCallbackNode(currentNode);
            } else {
                // up 1 level
                pushCallbackNodeAndRunBeforeChildren(currentNode);
            }

            processCurrentNode(currentNode);
        }
        popCallbackNodesAndRunAfterChildren(0);
    }

    /**
     * @param downTo The lower limit index of popped nodes
     */
    private void popCallbackNodesAndRunAfterChildren(int downTo) {
        while (callbackNodeStackIndex > downTo) {
            N node = stackOfCallbackNode.remove(--callbackNodeStackIndex);
            callback.afterChildren(node);
            if (logger.isDebugEnabled()) {
                logger.debug("Invoked after children on " + node);
            }
        }
    }

    private void setCurrentNodeAsCallbackNode(final N currentlyProcessedNode) {
        stackOfCallbackNode.set(callbackNodeStackIndex, currentlyProcessedNode);
    }

    private void pushCallbackNodeAndRunBeforeChildren(final N currentlyProcessedNode) {
        stackOfCallbackNode.add(currentlyProcessedNode);
        if (callbackNodeStackIndex >= 0) {
            N node = stackOfCallbackNode.get(callbackNodeStackIndex);
            callback.beforeChildren(node);
            if (logger.isDebugEnabled()) {
                logger.debug("Invoked before children on " + node);
            }
        }
        callbackNodeStackIndex++;
    }

    private void processCurrentNode(final N currentNode) {
        boolean processChildren = callback.processNode(currentNode);
        if (logger.isDebugEnabled()) {
            logger.debug("Invoked process on " + currentNode + ", skip children ? " + !processChildren);
        }
        if (!processChildren) {
            treeIterator.skipChildren();
        }
    }

    /**
     * Return the depth of this node.
     *
     * @param node a node
     * @return -1 for a null node, 0 for the root node, > 0 for other nodes
     */
    private int getDepth(N node) {
        int depth = -1;
        N n = node;
        while (n != null) {
            n = resolver.getParent(n);
            depth++;
        }
        return depth;
    }

    private int getRelativeDepth(N node) {
        return getDepth(node) - rootDepth;
    }
}
