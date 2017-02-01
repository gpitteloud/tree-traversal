package ch.gpitteloud.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Spliterator that explores a tree in DFS mode. The split operation divides the siblings of the current node (if
 * there are many unhandled such nodes) between a forked spliterator and this spliterator, or explore the only
 * remaining branch (if there is a single unhandled node == current in this level) until many children are found, in
 * order to split them.</p>
 * The spliterator's characteristics are ORDERED, NONNULL, DISTINCT.
 *
 * @author Gaetan Pitteloud
 */
public class DfsTreeSpliterator<T> implements Spliterator<T> {

    private TreeIterator<T> nodes;
    private final List<T> roots;
    private final ParentChildResolver<T> resolver;
    private T current;

    public DfsTreeSpliterator(T root, final ParentChildResolver<T> resolver) {
        this.roots = Collections.singletonList(root);
        this.resolver = resolver;
        nodes = new DefaultTreeIterator<>(ExplorationMode.DFS, root, resolver);
        advance();
    }

    private DfsTreeSpliterator(List<T> roots, ParentChildResolver<T> resolver) {
        this.resolver = resolver;
        this.roots = roots;
        nodes = new DefaultTreeIterator<T>(ExplorationMode.DFS, roots, resolver);
        advance();
    }

    @Override
    public void forEachRemaining(final Consumer<? super T> action) {
        while (current != null) {
            action.accept(current);
            advance();
        }
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        if (current != null) {
            action.accept(current);
            advance();
            return true;
        }
        return false;
    }

    private void advance() {
        current = nodes.hasNext() ? nodes.next() : null;
    }

    /**
     * Current nodes and its children are handled by forked spliterator, remaining nodes (siblings of current + their
     * children) are handled by this spliterator.
     *
     * @return forked iterator, or null if tree exploration is exhausted
     */
    @Override
    public Spliterator<T> trySplit() {
        if (current == null) {
            // nodes are exhausted
            return null;
        }

        final List<T> siblings = getSiblings(current);

        // how many siblings are left unhandled ?
        int unhandled = siblings.size() - siblings.indexOf(current);
        if (unhandled > 1) {
            // first half of children handled by forked spliterator
            // second half handled by this spliterator
            int forkedSize = unhandled >>> 1;
            final List<T> forkedRoots = new ArrayList<>(forkedSize);
            for (int i = 0; i < forkedSize; i++) {
                forkedRoots.add(current);
                nodes.skipChildren();
                advance();
            }
            assert current != null; // this spliterator contains unhandled nodes
            return new DfsTreeSpliterator<>(forkedRoots, resolver);
        } else { // unhandled == 1, the subtree rooted by current is the only unhandled branch
            T node = current;
            while (true) {
                final List<T> children = resolver.getChildren(node);
                int size = children.size();
                switch (size) {
                    case 0:
                        return null; // do not split : the only remaining node is current
                    case 1:
                        // 1 child : do not split here, explore further
                        node = children.get(0);
                        break;
                    default:
                        // node has many children : split them
                        int mid = size >>> 1;
                        final List<T> forkedChildren = new ArrayList<>(children.subList(0, mid));
                        PartialResolver<T> forkedResolver = new PartialResolver<>(current, node, forkedChildren, resolver);
                        final DfsTreeSpliterator<T> forkedSpliterator = new DfsTreeSpliterator<>(current, forkedResolver);

                        // replace the iterator, restarting with newRoots
                        final List<T> newRoots = new ArrayList<>(children.subList(mid, size));
                        nodes = new DefaultTreeIterator<>(ExplorationMode.DFS, newRoots, resolver);
                        advance();

                        return forkedSpliterator;
                }
            }
        }
    }

    private List<T> getSiblings(T node) {
        if (roots.contains(node)) {
            return roots;
        } else {
            final T parent = resolver.getParent(node);
            return resolver.getChildren(parent);
        }
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return DISTINCT | NONNULL | ORDERED;
    }

    private static class PartialResolver<N> implements ParentChildResolver<N> {
        private final N root;
        private final N partialParent;
        private final List<N> partialChildren;
        private final ParentChildResolver<N> delegate;

        public PartialResolver(N root, N partialParent, List<N> partialChildren, ParentChildResolver<N> delegate) {
            this.root = root;
            this.partialParent = partialParent;
            this.partialChildren = partialChildren;
            this.delegate = delegate;
        }

        @Override
        public N getParent(final N node) {
            return node == root ? null : delegate.getParent(node);
        }

        @Override
        public List<N> getChildren(final N node) {
            return node == partialParent ? partialChildren : delegate.getChildren(node);
        }
    }

}
