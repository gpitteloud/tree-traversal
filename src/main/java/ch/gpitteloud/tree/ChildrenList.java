package ch.gpitteloud.tree;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * The class representing the children's list. This implementation manages {@link TreeNode#getIndex() index},
 * {@link TreeNode#getParent() parent} and {@link TreeNode#getPath() path} when nodes are added/removed from the list.
 * The ChildrenList is linked to the parent and cannot be used for another node.
 * <p>
 * The list is backed by an ArrayList and supports all its List's operations.
 * <p>
 * If some method fails and throw an IllegalStateException because the parent of a node cannot be set (see
 * {@link TreeNode#setParent(TreeNode)}), the list remains unchanged, and all parent-children relations are like they
 * where before the failing method invocation, except for {@link #addAll(Collection)}, {@link #addAll(int, Collection)},
 * {@link #removeAll(Collection)}, {@link #retainAll(Collection)} and {@link #clear()}, that are not "atomic" (however,
 * the parent-child relations are still ensured for all elements).
 *
 * @author Gaëtan Pitteloud
 * @param <N> The node type
 */
class ChildrenList<N extends TreeNode<N>> implements List<N>, Serializable {

    private static final long serialVersionUID = -8412006456869009027L;

    private static final Logger logger = Logger.getLogger(ChildrenList.class);

    private final List<N> delegate = new ArrayList<>();
    private final N owner;

    ChildrenList(N owner) {
        assert owner != null : "Invalid null owner";
        this.owner = owner;
    }

    /**
     * Add a node if it is not already present
     *
     * @param node the node to add, not null
     * @return true if node was inserted, false otherwise
     */
    public boolean add(N node) {
        checkNotNull(node);
        if (delegate.contains(node)) {
            if (logger.isDebugEnabled()) {
                logger.debug("The node is already in the children's list");
            }
            return false;
        }
        checkCanAddChild(node);
        node.setParent(owner); // may fail if node is already the child of some other parent node
        delegate.add(node);
        return true;
    }

    /**
     * Check that the given node can be added to this children's list, by invoking owner's
     * {@link TreeNode#canAddChild(TreeNode)} method.
     *
     * @param node a node
     * @throws IllegalArgumentException if the node cannot be added
     */
    void checkCanAddChild(N node) throws IllegalArgumentException {
        if (!owner.canAddChild(node)) {
            throw new IllegalArgumentException("Node " + node + " cannot be added as a child of " + owner);
        }
    }

    /**
     * Add a node in the children's list, shifting the next nodes. If the node is already in the list, an exception is
     * thrown.
     *
     * @param index the index
     * @param node the node
     */
    public void add(int index, N node) {
        checkNotNull(node);
        checkNotAlreadyInList(node);
        checkCanAddChild(node);
        checkIndexForAdd(index);

        node.setParent(owner); // may fail if node is already the child of some other parent node
        delegate.add(index, node);
    }

    /**
     * ensure delegate.add() will not fail and leave the node connected to the parent but not in the parent's children
     *
     * @param index an index
     */
    private void checkIndexForAdd(int index) {
        int size = size();
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    /**
     * Check that the given node is not already in the children list.
     *
     * @param node a node
     * @throws IllegalArgumentException if the node is already in the children list
     */
    void checkNotAlreadyInList(N node) throws IllegalArgumentException {
        if (delegate.contains(node)) {
            throw new IllegalArgumentException("The node is already in the children list");
        }
    }

    public boolean addAll(Collection<? extends N> col) {

        // add one by one in order to connect the child and its new parent
        for (N node : col) {
            add(node);
        }
        return true;
    }

    public boolean addAll(int index, Collection<? extends N> col) {
        int i = index;
        // added one by one
        for (N node : col) {
            add(i++, node);
        }
        return true;
    }

    public void clear() {
        int size = delegate.size();
        // removed one by one in order to disconnect the parent of each child
        for (int i = 0; i < size; i++) {
            remove(0);
        }
    }

    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    public N get(int index) {
        return delegate.get(index);
    }

    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * Return an iterator over the children of the owning node. The iterator does not traverse the children of each node
     * contained in this children's list.
     *
     * @return an iterator over these children
     */
    public Iterator<N> iterator() {
        return new ParentChildAwareListIterator(delegate.listIterator());
    }

    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    /**
     * Return a list iterator over the children of the owning node. The iterator does not traverse the children of each
     * node contained in this children's list.
     *
     * @return a list iterator over these children
     */
    public ListIterator<N> listIterator() {
        return new ParentChildAwareListIterator(delegate.listIterator());
    }

    /**
     * Return a list iterator over the children of the owning node, starting at the specified index. The iterator does
     * not traverse the children of each node contained in this children's list.
     *
     * @param index starting index
     * @return a list iterator over these children
     */
    public ListIterator<N> listIterator(int index) {
        return new ParentChildAwareListIterator(delegate.listIterator(index));
    }

    /**
     * Remove a node from this list, and reset its parent. The children of the specified node are still connected to
     * that node.
     *
     * @param o a node
     * @return true if the node was in the list, false otherwise
     */
    public boolean remove(Object o) {

        // keep the index to be able to reconnect the node if removal fails later
        int index = delegate.indexOf(o);
        if (index > -1) {
            delegate.remove(index); // faster than delegate.remove(o) since we know the index
            // we know o was in the list -> o is of type N
            @SuppressWarnings("unchecked")
            N node = (N) o;
            try {
                node.setParent(null);
            } catch (IllegalStateException e) {
                // push back o into the delegate list to keep it in synch with its (unchanged) parent
                delegate.add(index, node);
                throw e;
            }
            return true;
        }
        return false;
    }

    /**
     * Remove a node from this list, and reset its parent. The children of the specified node are still connected to
     * that node.
     *
     * @param index the node index
     * @return the previous node at that index.
     */
    public N remove(int index) {
        N node = delegate.remove(index);
        try {
            node.setParent(null);
        } catch (IllegalStateException e) {
            // push back o into the delegate list to keep it in synch with its (unchanged) parent
            delegate.add(index, node);
            throw e;
        }
        return node;
    }

    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object object : c) {
            changed |= remove(object);
        }
        return changed;
    }

    public boolean retainAll(Collection<?> c) {
        Iterator<N> it = iterator();
        boolean changed = false;
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Replace the node at the specified index. Connect the new node to the children's owner and disconnect the previous
     * one.
     *
     * @param index the index
     * @param node the new node
     * @return the previous node
     */
    public N set(int index, N node) {
        checkNotNull(node);
        checkNotAlreadyInList(node);
        checkCanAddChild(node);
        checkIndexForSet(index);

        node.setParent(owner); // may fail if node is already a child of some other node
        // could also do delegate.get(index) to get previous and invoke set when we're sure it's OK
        // -> prevents set in catch block below
        // I assume the method fails less often than it succeeds
        N previous = delegate.set(index, node);
        try {
            previous.setParent(null);
        } catch (IllegalStateException e) {
            // cannot disconnect previous because it has a current node in its subtree
            // previous parent was null, otherwise node.setParent(owner) above would have failed
            node.resetParent(null);
            // restore internal list previous state
            delegate.set(index, previous);
            throw e;
        }
        return previous;
    }

    /**
     * ensure delegate.set() will not fail and leave the node connected to the parent but not in the parent's children
     * list
     *
     * @param index an index
     */
    private void checkIndexForSet(int index) {
        int size = size();
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    public int size() {
        return delegate.size();
    }

    public List<N> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return delegate.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public String toString() {
        return "parent=" + owner + ", children=" + delegate;
    }

    /**
     * check the node is not null, throwing an exception if null
     *
     * @param node a node
     */
    void checkNotNull(N node) {
        if (node == null) {
            throw new IllegalArgumentException("Invalid null node");
        }
    }

    /**
     * The ListIterator (and Iterator) for this list. Aware of connection from parent to child, and updates connections
     * when the relationships change.
     *
     * @author Gaëtan Pitteloud
     */
    private class ParentChildAwareListIterator implements ListIterator<N> {

        private final ListIterator<N> listItr;

        private N current;

        public ParentChildAwareListIterator(ListIterator<N> listItr) {
            this.listItr = listItr;
        }

        public boolean hasNext() {
            return listItr.hasNext();
        }

        public boolean hasPrevious() {
            return listItr.hasPrevious();
        }

        public N next() {
            this.current = listItr.next();
            return current;
        }

        public N previous() {
            current = listItr.previous();
            return current;
        }

        public int nextIndex() {
            return listItr.nextIndex();
        }

        public int previousIndex() {
            return listItr.previousIndex();
        }

        public void add(N node) {
            checkNotNull(node);
            checkNotAlreadyInList(node);
            checkCanAddChild(node);
            node.setParent(owner);
            listItr.add(node);
            current = null;
        }

        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            current.setParent(null);
            listItr.remove();
            current = null;
        }

        public void set(N node) {
            checkNotNull(node);
            if (current == null) {
                throw new IllegalStateException();
            }
            checkNotAlreadyInList(node);
            checkCanAddChild(node);
            node.setParent(owner); // may fail if node is the child of some other node
            try {
                current.setParent(null);
            } catch (IllegalStateException e) {
                // cannot disconnect previous because it has a current node in its subtree
                // previous parent was null, otherwise node.setParent(owner) above would have failed
                node.resetParent(null);
                throw e;
            }
            listItr.set(node);
            this.current = node;
        }

    }

}
