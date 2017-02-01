/**
 * Created on May 11, 2009
 */
package ch.gpitteloud.tree;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static ch.gpitteloud.tree.ExplorationMode.DFS;
import static java.util.Spliterator.*;

/**
 * A collection of {@link TreeNode}s, linked with parent/children relationships. A tree is iterable over its nodes and
 * can remember one node tagged as the current node.
 * <p>
 * The root node of a tree cannot be removed from the tree, thus it cannot be added to another tree. A tree contains at
 * least one node, namely the root node.
 * @author Gaëtan Pitteloud
 * @see TreeNode
 * @param <N> The node type
 */
public class Tree<N extends TreeNode<N>> implements Iterable<N>, Serializable {

    private static final long serialVersionUID = -5964098969572007701L;

    private final N root;
    private N currentNode;

    /**
     * Construct a tree with a root. The root must not be part of another tree. The root cannot be removed from the tree
     * once set (thus cannot be part of another tree).
     *
     * @param root the root of this tree, not null
     */
    public Tree(N root) {
        assert root != null : "Invalid null root node";
        assert !root.isRoot() : "The node is already the root of another tree";
        assert root.getParent() == null : "The root is still part of another tree";
        root.setRoot();
        this.root = root;
    }

    /**
     * Iterate over the nodes of this tree using BFS algorithm.
     *
     * @see Iterable#iterator()
     */
    public final TreeIterator<N> iterator() {
        return iterator(ExplorationMode.BFS);
    }

    /**
     * Iterate over the nodes of this tree, traversing the children of a node before the node itself.
     *
     * @return iterator
     */
    public Iterator<N> getReverseIterator() {
        return root.getReverseIterator();
    }

    /**
     * Iterate over the nodes using the specified algorithm
     *
     * @param mode search algorithm
     * @return iterator
     */
    public final TreeIterator<N> iterator(ExplorationMode mode) {
        return root.iterator(mode);
    }

    /**
     * @return The root node
     */
    public final N getRoot() {
        return root;
    }

    /**
     * @return the current node
     */
    public final N getCurrentNode() {
        return this.currentNode;
    }

    /**
     * Get a node from its path. An empty path corresponds to the root node, the path [0] corresponds to the first
     * direct child of root, etc.
     *
     * @see TreeNode#getPath()
     * @param nodepath node path, null returns null
     * @return the node corresponding to this path
     * @throws IllegalArgumentException if a child element is invalid in this tree.
     */
    public final N getNodeFromPath(int[] nodepath) {
        if (nodepath == null) {
            return null;
        }

        N node = root;
        for (int i = 0; i < nodepath.length; i++) {
            try {
                node = node.getChildAt(nodepath[i]);
            } catch (IndexOutOfBoundsException e) {
                int[] failingPath = new int[i + 1];
                System.arraycopy(nodepath, 0, failingPath, 0, i + 1);
                String msg = "Invalid path in tree: " + Arrays.toString(nodepath) + ": node at path "
                        + Arrays.toString(failingPath) + " does not exit";
                throw new IllegalArgumentException(msg);
            }
        }
        return node;
    }

    /**
     * Answer whether this node is in this tree.
     *
     * @param node a node
     * @return true if the node is not null and is equal to a child in the tree, false otherwise
     */
    public final boolean contains(N node) {
        if (node == null) {
            return false;
        }
        for (N n : this) {
            if (n.equals(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the current node of this tree. The node must be present in the tree to be set as current
     *
     * @param node a node
     */
    public final void setCurrentNode(N node) {
        if (node != null) {
            // ensure the node is in this tree: compare node root with tree root
            if (node.getRootNode() == null || !node.getRootNode().equals(root)) {
                throw new IllegalArgumentException("The node '" + node
                        + "' cannot be set as current node of this tree because it is not part of this tree");
            }
        }
        if (this.currentNode != null) {
            this.currentNode.setCurrent(false);
        }
        this.currentNode = node;
        if (node != null) {
            node.setCurrent(true);
        }
    }

    /**
     * Process all nodes of this tree with the specified callback.
     *
     * @param callback a callback, not null
     */
    public final void processNodes(AroundChildrenNodeCallback<N> callback) {
        TreeProcessor<N> processor = new TreeProcessor<N>(new TreeNode.Resolver<>());
        processor.processNodes(root, callback);
    }

    /**
     * Return a sequential ordered stream over the nodes of this tree, in BFS.
     * @return stream over the nodes
     */
    public Stream<N> bfsStream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(DFS), ORDERED | DISTINCT | NONNULL),
                false);
    }

    /**
     * Return a ordered stream over the nodes of this tree, in DFS
     * @param parallel true to create a parallel stream, false to create a sequential stream
     * @return stream over the nodes
     */
    public Stream<N> dfsStream(boolean parallel) {
        return StreamSupport.stream(new DfsTreeSpliterator<>(root, new TreeNode.Resolver<>()), parallel);
    }

    /**
     * Return a String representation of this tree, with each node a line, in the same layout as an explorer or the
     * dependency tree of Maven.
     *
     * @see NodePrinter
     */
    @Override
    public String toString() {
        String result = getClass().getName() + "\n";
        NodePrinter<N> printer = new NodePrinter<N>(new TreeNode.Resolver<>());
        return result + printer.toString(root);
    }
}
