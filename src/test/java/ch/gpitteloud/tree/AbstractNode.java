package ch.gpitteloud.tree;

/**
 * @author Gaëtan Pitteloud
 */
class AbstractNode<N extends AbstractNode<N>> extends TreeNode<N> {

    private static final long serialVersionUID = 1L;

    AbstractNode(Class<N> treeNodeClass) {
        super(treeNodeClass);
    }

}
