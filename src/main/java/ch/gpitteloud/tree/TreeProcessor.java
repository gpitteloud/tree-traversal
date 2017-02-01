package ch.gpitteloud.tree;

/**
 * An utility class that contains methods for performing actions while traversing a tree.
 *
 * @author Gaëtan Pitteloud
 * @param <N> The node type
 */
public class TreeProcessor<N> {

    private final ParentChildResolver<N> resolver;

    public TreeProcessor(ParentChildResolver<N> resolver) {
        this.resolver = resolver;
    }

    /**
     * Perform the callback actions on each node, then before processing its children and finally after having processed
     * its children (in that order).
     *
     * @param root the root node of the tree to traverse
     * @param callback the callback
     */
    public final void processNodes(N root, AroundChildrenNodeCallback<N> callback) {
        StatefulNodeCallbackProcessor<N> processor = new StatefulNodeCallbackProcessor<>(root, resolver, callback);
        processor.processNodes();
    }

}
