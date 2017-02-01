package ch.gpitteloud.tree;

import ch.gpitteloud.tree.DefaultTreeIterator.Buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
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
        <N> Buffer<N> createEmptyBuffer() {
            return new Queue<>();
        }
    },

    /**
     * Depth-first search : explore the children of a node before its siblings.
     */
    DFS {

        @Override
        <N> Buffer<N> createEmptyBuffer() {
            return new Stack<>();
        }
    };

    abstract <N> Buffer<N> createEmptyBuffer();

    /**
     * List-based buffer.
     */
    private static abstract class OrderedBuffer<E, L extends List<E>> implements Buffer<E> {
        final L contents;

        OrderedBuffer(L contents) {
            this.contents = contents;
        }

        public void push(E element) {
            contents.add(element);
        }

        public void removeAll(Collection<? extends E> elements) {
            contents.removeAll(elements);
        }

        public int size() {
            return contents.size();
        }
    }

    /**
     * Buffer for BFS : Queue
     */
    private static class Queue<E> extends OrderedBuffer<E, LinkedList<E>> {

        public Queue() {
            super(new LinkedList<>());
        }

        public void pushAll(List<? extends E> elements) {
            contents.addAll(elements);
        }

        public E pop() {
            return contents.removeFirst();
        }

    }

    /**
     * Buffer for DFS : Stack
     */
    private static class Stack<E> extends OrderedBuffer<E, ArrayList<E>> {

        public Stack() {
            super(new ArrayList<>());
        }

        public void pushAll(List<? extends E> elements) {
            int nbNewElements = elements.size();
            contents.ensureCapacity(contents.size() + nbNewElements);
            // elements are inserted in reverse order, so that the first popped child is the first element, not the last
            for (int i = nbNewElements; i > 0; i--) {
                contents.add(elements.get(i - 1));
            }
        }

        public E pop() {
            return contents.remove(contents.size() - 1);
        }

    }

}
