package ch.gpitteloud.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a node in a {@link Tree}. When inserted into a Tree, a node is assigned a parent, an index
 * (representing the order in the parent's children list) and a path (representing the navigation indices from the Tree
 * root).
 * <p>
 * When the node is removed from the Tree, the index is -1, the parent is null and the path is empty.
 * <p>
 * When the node is set as the root node of a tree, it remains always attached to that tree and cannot be moved to some
 * other tree. The root node of a tree has no parent, its index is -1 and the path is empty.
 * <p>
 * The class is abstract, thus it has to be subclassed to be used; this also freezes N as "itself", as shown in the
 * following sample: <code><pre>
 * public class SampleNode extends TreeNode&lt;SampleNode&gt; {
 *
 *     private String value;
 *
 *     public SampleNode() {
 *         super(SampleNode.class);
 *     }
 *
 *     public SampleNode(String value) {
 *         super(SampleNode.class);
 *         this.value = value;
 *     }
 * }
 * </pre></code> When defining a subclass that freezes N, it is mandatory to define the parameter type N as the defined
 * class. If a TreeNode class is defined with another TreeNode class for the N parameter type, an exception is thrown at
 * construction.
 *
 * @author Gaëtan Pitteloud
 * @param <N> The node type
 */
public abstract class TreeNode<N extends TreeNode<N>> implements Iterable<N>, Serializable {

    private static final long serialVersionUID = 5463031337433768912L;

    /**
     * The resolver for TreeNode.
     *
     * @author Gaëtan Pitteloud
     * @param <N> The node type
     */
    public static class Resolver<N extends TreeNode<N>> implements ParentChildResolver<N> {

        public List<N> getChildren(N node) {
            return node.getChildren();
        }

        public N getParent(N node) {
            return node.getParent();
        }

    }

    private final N self; // == this, but cast to the actual node type
    private final ChildrenList<N> children;

    private boolean root;
    private boolean current; // true when this node is set as the current node of its owning tree
    private N parent;

    /**
     * Only constructor of a TreeNode, to be used by subclasses, but usually not exposed to nodes clients. The current
     * node class must be passed in, which ensures correct declaration at compile-time.
     *
     * @param treeNodeClass the current class
     */
    protected TreeNode(Class<N> treeNodeClass) {
        try {
            self = treeNodeClass.cast(this);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid tree class declaration");
        }
        children = new ChildrenList<>(self);
    }

    /**
     * All the children of this node, or empty list
     *
     * @return children list, never null
     */
    public final List<N> getChildren() {
        return children;
    }

    /**
     * Answer if the specified node can be added as a child to this node. Returns true by default. The method is meant
     * to be overridden in subclasses that only allow some sort of nodes to be added.
     * <p>
     * The method is invoked as a callback from the children list implementation.
     *
     * @param node the node
     * @return true if the node can be added, false otherwise.
     */
    protected boolean canAddChild(N node) {
        return true;
    }

    /**
     * The parent of this node, if the node is in a tree, null otherwise.
     *
     * @return the parent
     */
    public final N getParent() {
        return parent;
    }

    /**
     * The index of this node in the children of this node's parent if the node has a parent, -1 otherwise.
     *
     * @return node's index
     */
    public final int getIndex() {
        if (parent == null) {
            return -1;
        }
        //noinspection SuspiciousMethodCalls
        return parent.getChildren().indexOf(this);
    }

    /**
     * Path from the root to this node. If the node has no parent, return an empty array. A new array is returned for
     * each call. Modifying the returned array has no influence on the node.
     * <p>
     * The path is computed for each call, as the parent hierarchy of this node can be modified without notice to this
     * node.
     *
     * @return the path
     */
    public final int[] getPath() {
        N node = self;
        ArrayList<Integer> indices = new ArrayList<>();
        while (node.getParent() != null) {
            indices.add(node.getParent().getChildren().indexOf(node));
            node = node.getParent();
        }
        int size = indices.size();
        int[] path = new int[size];
        int i = size;
        for (Integer index : indices) {
            path[--i] = index;
        }

        return path;
    }

    /**
     * Add a child at the end of the children list. Shortcut for <code>getChildren().add(child)</code>
     *
     * @param child child node
     * @return true if node was added, false if it was already there
     */
    public final boolean addChild(N child) {
        return children.add(child);
    }

    /**
     * Return the child at the specified position. Shortcut for <code>getChildren().get(pos)</code>
     *
     * @param pos the position
     * @return the node at that position, or throw an exception if index is out of range (as per {@link List#get(int)}
     *         contract)
     */
    public final N getChildAt(int pos) {
        return children.get(pos);
    }

    /**
     * Set the parent. Invoked by callback from ChildrenList. If this node is the root node or if it already has a
     * parent, the parent cannot be set and the method throws an exception.
     *
     * @param parent the parent, or null if this node is to be detached from its parent.
     * @throws IllegalStateException The parent is null and this node or one of its children is tagged as current in the
     *             owning tree, or the parent is not null and this node is a root node, or it already has a non-null
     *             parent.
     */
    void setParent(N parent) {
        if (parent == null) {
            if (hasCurrentNodeInChildren()) {
                throw new IllegalStateException("The node '" + this
                        + "' cannot be detached from its parent because it is the current node of its owning tree");
            }
        } else {
            if (root) {
                throw new IllegalStateException("The root node of a tree cannot be added to another tree");
            }
            if (this.parent != null) {
                throw new IllegalStateException("The node '" + this + "' is already attached to a parent node ('"
                        + this.parent + "')");
            }
        }
        this.parent = parent;
    }

    /**
     * A method that assigns the parent without tests. Used only to reset the previous parent value.
     *
     * @param p the parent.
     */
    void resetParent(N p) {
        this.parent = p;
    }

    /**
     * Answer whether this node has a child node tagged as current in all its children.
     *
     * @return true if this has a current node, false otherwise
     */
    private boolean hasCurrentNodeInChildren() {
        for (N n : self) {
            if (n.isCurrent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return return root
     */
    public final boolean isRoot() {
        return this.root;
    }

    /**
     * Flag this node as the root node. Its parent, index and path are reset. Invoked when a tree is constructed.
     */
    void setRoot() {
        setParent(null);
        this.root = true;
    }

    /**
     * Is this node set as the current node of its owning tree ?
     *
     * @return true if node is the current node of its tree, false otherwise
     */
    boolean isCurrent() {
        return this.current;
    }

    /**
     * Set wether this node was set as the current node of its owning tree. If true, the node cannot be disconnected
     * (removed from children list or added as a child of a node of anther tree) from its tree.
     *
     * @param current current to set
     */
    void setCurrent(boolean current) {
        this.current = current;
    }

    /**
     * Iterate over this node and its children, using BFS algorithm.
     *
     * @see Iterable#iterator()
     */
    public final TreeIterator<N> iterator() {
        return new DefaultTreeIterator<>(ExplorationMode.BFS, self, new Resolver<N>());
    }

    /**
     * Iterate over this node and its children, using the specified algorithm.
     *
     * @param mode search algorithm
     * @return iterator
     */
    public final TreeIterator<N> iterator(ExplorationMode mode) {
        assert mode != null : "Invalid null exploration mode";
        return new DefaultTreeIterator<>(mode, self, new Resolver<N>());
    }

    /**
     * Iterate over this node and its children, visiting the children of a node before the node itself.
     *
     * @return iterator
     */
    public final Iterator<N> getReverseIterator() {
        return new ParentAfterChildrenIterator<>(new Resolver<N>(), self);
    }

    /**
     * Return the root node of this node, or null if this node is not connected to a root node (not in a tree). If this
     * node is the root node, return this.
     *
     * @return the root node
     */
    public final N getRootNode() {
        if (root) {
            return self;
        }
        N p = parent;
        while (p != null) {
            if (p.isRoot()) {
                return p;
            }
            p = p.getParent();
        }
        return null;
    }

    /**
     * Return the depth of this node, i.e. the distance from the root node to this node. The depth of the root node is
     * 0.
     *
     * @return depth of this node
     */
    public final int getDepth() {
        int depth = 0;
        N node = self;
        while (node.getParent() != null) {
            depth++;
            node = node.getParent();
        }
        return depth;
    }

    /*
     * Protect Identity equals
     */
    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    /*
     * Protect Identity hashcode
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

}
