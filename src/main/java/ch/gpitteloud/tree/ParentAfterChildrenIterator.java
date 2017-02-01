package ch.gpitteloud.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Iterator for traversing a tree, that returns the children of each node before that node.
 * <p>
 * Does not implement {@link TreeIterator} : cannot skip the children of a node since the children are already
 * traversed. {@link #remove()} is not supported for the same reasons.
 *
 * @author Gaëtan Pitteloud
 * @param <N> The node type
 */
public class ParentAfterChildrenIterator<N> implements Iterator<N> {

    private final ParentChildResolver<N> resolver;
    private DefaultTreeIterator<N> iterator;
    private final int rootDepth;
    private final Stack<N> stack = new Stack<>();
    private N nextNode;

    public ParentAfterChildrenIterator(ParentChildResolver<N> resolver, N first) {
        this.resolver = resolver;
        iterator = new DefaultTreeIterator<>(ExplorationMode.DFS, first, resolver);
        rootDepth = first == null ? 0 : getDepth(first);
    }

    public boolean hasNext() {
        return iterator.hasNext() || !stack.isEmpty();
    }

    public N next() {
        initNextNodeFirstTime();

        while (getNextNodeRelativeDepth() > getStackIndex()) {
            stack.push(nextNode);
            moveNextNode();
        }

        if (getNextNodeRelativeDepth() == getStackIndex()) {
            if (stack.isEmpty()) {
                throw new NoSuchElementException();
            }
            N result = stack.pop();
            stack.push(nextNode);
            moveNextNode();
            return result;
        }

        if (getNextNodeRelativeDepth() < getStackIndex()) {
            return stack.pop();
        }
        // we should not get there
        throw new NoSuchElementException();
    }

    private void initNextNodeFirstTime() {
        if (nextNode == null && iterator.hasNext()) {
            nextNode = iterator.next();
        }
    }

    private void moveNextNode() {
        nextNode = iterator.hasNext() ? iterator.next() : null;
    }

    private int getStackIndex() {
        return stack.size() - 1;
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

    private int getNextNodeRelativeDepth() {
        return getDepth(nextNode) - rootDepth;
    }

}
