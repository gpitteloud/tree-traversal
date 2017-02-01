package ch.gpitteloud.tree;

import java.util.Iterator;

/**
 * A tree iterator allows to iterate over the nodes of a tree.
 * <p>
 * This interface provides an additional method, {@link #skipChildren()}, which prevents the children of a traversed
 * node to be later traversed, without removing the node(s) from the underlying tree. This is useful to skip a
 * sub-branch of a node during iteration (typically based on a condition on the last traversed node), without altering
 * the tree.
 * <p>
 * The {@link #remove()} method removes the current node as well as its children from the traversed tree.
 *
 * @author Gaëtan Pitteloud
 * @param <N> The node type
 */
public interface TreeIterator<N> extends Iterator<N> {

    /**
     * Skip the children of the current node (the one returned by the previous call to <tt>next</tt>) from iteration.
     * This method can be called only once per call to <tt>next</tt>
     */
    public void skipChildren();
}
