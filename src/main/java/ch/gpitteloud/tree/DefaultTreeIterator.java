package ch.gpitteloud.tree;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Default implementation of {@link TreeIterator}. The implementation ensures that a node is always traversed before its
 * children. The traversal mode (depth-first or breadth-first) is represented by the {@link ExplorationMode}.
 * <p>
 * The iterator contains a cycle detector (disabled by default) that blocks the second time a node is traversed (throws
 * an exception).
 *
 * @author Gaëtan Pitteloud
 * @param <N> the node type
 */
public class DefaultTreeIterator<N> implements TreeIterator<N> {

    /**
     * Buffer containing elements as exploration advances. Depending on BFS or DFS mode, the elements are inserted at
     * and removed from the end or the beginning of the buffer.
     */
    interface Buffer<E> {
        /**
         * Add the children of the current element. Whatever BFS or DFS, the children must be removed in the same
         * order as was specified here.
         * @param elements children elements
         */
        void addAll(List<? extends E> elements);

        /**
         * Get and remove the next element to process
         * @return next element to process, or null if no more elements to process
         */
        E removeOne();

        /**
         * Remove all children that were last inserted. Depending on BFS or DFS, these are the last or the first N
         * elements.<p/>
         * This happens after the children of an element are skipped (its children were inserted after a call to
         * next(), and must be removed on a call to skipChildren() occurring later).
         * @param count the number of children to remove
         */
        void removeAll(int count);

        /**
         * buffer size
         * @return buffer size
         */
        int size();
    }

    private final Buffer<N> buffer;
    private N current;
    private final ParentChildResolver<N> resolver;

    /**
     * @param mode exploration mode
     * @param first the first element of the tree (root).
     * @param resolver The resolver of parent-child relationships for the node
     */
    public DefaultTreeIterator(ExplorationMode mode, N first, ParentChildResolver<N> resolver) {
        assert resolver != null : "Invalid null resolver";
        this.resolver = resolver;
        buffer = mode.createInitialBuffer(first);
    }

    /**
     * Used by DfsTreeSpliterator when splitting
     * @param mode exploration mode
     * @param roots the first elements of the tree.
     * @param resolver The resolver of parent-child relationships for the node
     */
    DefaultTreeIterator(ExplorationMode mode, List<N> roots, ParentChildResolver<N> resolver) {
        this(mode, (N) null, resolver);
        buffer.addAll(roots);
    }

    public boolean hasNext() {
        return buffer.size() > 0;
    }

    public N next() {
        if (hasNext()) {
            current = buffer.removeOne();
            buffer.addAll(resolver.getChildren(current));
            return current;
        }
        throw new NoSuchElementException();
    }

    /**
     * The current node is removed from the tree and its children will not be traversed by the iterator. This method can
     * be called only once per call to next.
     *
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        checkCurrentNotNull();
        N parent = resolver.getParent(current);
        if (parent != null) {
            resolver.getChildren(parent).remove(current);
        }
        removeChildrenAndSetCurrentToNull();
    }

    /**
     * The children of the current node are removed from traversal, but not from the underlying tree. This method can be
     * called only once per call to next.
     */
    public void skipChildren() {
        checkCurrentNotNull();
        removeChildrenAndSetCurrentToNull();
    }

    private void checkCurrentNotNull() {
        if (current == null) {
            throw new IllegalStateException();
        }
    }

    private void removeChildrenAndSetCurrentToNull() {
        buffer.removeAll(resolver.getChildren(current).size());
        current = null;
    }

}