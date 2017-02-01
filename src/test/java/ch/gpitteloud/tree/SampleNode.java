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
}
