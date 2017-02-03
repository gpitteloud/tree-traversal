package ch.gpitteloud.tree;

import ch.gpitteloud.tree.DefaultTreeIterator.Buffer;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Search algorithms for exploration over a node and its children.
 *
 * @author Gaëtan Pitteloud
 */
public enum ExplorationMode {

    /**
     * Breadth-first search : explore the siblings of a node before its children.
     */
    BFS {

        @Override
        <N> Buffer<N> createInitialBuffer(N initialElement) {
            return new Queue<>(initialElement);
        }
    },

    /**
     * Depth-first search : explore the children of a node before its siblings.
     */
    DFS {

        @Override
        <N> Buffer<N> createInitialBuffer(N initialElement) {
            return new Stack<>(initialElement);
        }
    };

    abstract <N> Buffer<N> createInitialBuffer(N initialElement);

    private abstract static class ArrayDequeBuffer<E> implements Buffer<E> {
        final ArrayDeque<E> contents = new ArrayDeque<>();

        ArrayDequeBuffer(final E initialElement) {
            if (initialElement != null) {
                contents.add(initialElement);
            }
        }

        @Override
        public E removeOne() {
            return contents.pollFirst();
        }

        @Override
        public int size() {
            return contents.size();
        }

        @Override
        public String toString() {
            return contents.toString();
        }
    }

    /**
     * Buffer for BFS : Queue
     */
    private static class Queue<E> extends ArrayDequeBuffer<E> {

        Queue(final E initialElement) {
            super(initialElement);
        }

        @Override
        public void addAll(final List<? extends E> elements) {
            contents.addAll(elements);
        }

        @Override
        public void removeAll(final int count) {
            for (int i = 0; i < count; i++) {
                contents.pollLast();
            }
        }
    }

    /**
     * Buffer for DFS : Stack
     */
    private static class Stack<E> extends ArrayDequeBuffer<E> {

        Stack(final E initialElement) {
            super(initialElement);
        }

        @Override
        public void addAll(final List<? extends E> elements) {
            for (int i = elements.size(); i > 0; i--) {
                contents.push(elements.get(i - 1));
            }
        }

        @Override
        public void removeAll(final int count) {
            for (int i = 0; i < count; i++) {
                contents.pollFirst();
            }
        }
    }

}
