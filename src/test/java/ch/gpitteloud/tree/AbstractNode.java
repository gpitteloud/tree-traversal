package ch.gpitteloud.tree;

/**
 * @author Gaëtan Pitteloud
 */
class AbstractNode<N extends AbstractNode<N>> extends TreeNode<N> {

    AbstractNode(Class<N> treeNodeClass) {
        super(treeNodeClass);
    }

}
