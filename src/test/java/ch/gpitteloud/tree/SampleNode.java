package ch.gpitteloud.tree;

/**
 * A SampleNode for tests.
 *
 * @author Gaëtan Pitteloud
 */
class SampleNode extends TreeNode<SampleNode> {

    private static final long serialVersionUID = 1L;

    private String value;

    SampleNode(String value) {
        super(SampleNode.class);
        this.value = value;
    }

    SampleNode() {
        super(SampleNode.class);
    }

    /**
     * @return return value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @param value
     *            value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "SampleNode(" + value + ")";
    }

    void addAll(SampleNode... children) {
        for (SampleNode child : children) {
            addChild(child);
        }
    }

    static SampleNode createTree(String rootName, String... childrenNames) {
        SampleNode root = new SampleNode(rootName);
        root.createChildren(childrenNames);
        return root;
    }

    void createChildren(String... childrenNames) {
        for (String childName : childrenNames) {
            addChild(new SampleNode(childName));
        }
    }

}
