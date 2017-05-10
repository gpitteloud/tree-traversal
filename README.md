# tree-traversal
This project provides a simple API to traverse the nodes of a tree in an iterator- or jdk8 stream-like way.

Any data structure that defines a parent-children relation can be adapted to this API.
This includes the following structures:
 * File systems with folder and files
 * XML nodes
 * Classes hierarchies within reflection modules
 
Each time such a new data structure needs traversal, a new method that resembles this needs to be rewritten:

    private static void traverse(File root, Callback callback) {
        callback.doWithFile(root);
        // invoke callback on children
        File[] children = root.listFiles();
        if (children != null) {
            for (File child : children) {
                traverse(child, callback);
            }
        }
    }
    
The aim of this API is to get rid of such methods and instead provides an iterator or stream API with the same
functionality. 


The API provides the following features:
 * Iteration over the nodes (in BFS or DFS mode) with a `java.lang.Iterator`.
 * The Iterator supports an additional skipChildren() method
 * Iteration over the nodes with a `java.util.stream.Stream`. DFS mode supports parallel processing.
 * Utility methods invoked with callbacks:
    * Copy a tree structure and transform the nodes
    * Execute action during node traversal (on node, before/after children)

The API can be adapted to any tree-like structure. In order to take advantage of this,
you only have to implement this simple interface:

    interface ParentChildResolver<N> {
        /**
         * Return the children of the node
         * 
         * @param node a node, not null
         * @return the children of that node, never null
         */
        List<N> getChildren(N node);
 
        /**
         * Return the parent of the node
         * 
         * @param node a node, not null
         * @return the parent of that node, or null if the node is a the root of a tree
         */
        N getParent(N node);
    }

For example, to navigate through the File system, the implementation is as simple as this:

    public class FileSystemResolver implements ParentChildResolver<File> {
        @Override
        public List<File> getChildren(File node) {
            if (node.isDirectory()) {
                return Arrays.asList(node.listFiles());
            } else {
                return Collections.emptyList();
            }
        }
    
        @Override
        public File getParent(File node) {
            return node.getParentFile();
        }
    }

With this simple implementation, one can write code such as this:

    public static void getFileNames() {
        File root = new File("/some/root/path");
        List<String> fileNames = createStream(root, false)
                .filter((file -> !file.getName().startsWith(".")))
                .map((File::getName)).collect(Collectors.toList());
    }

    private static Stream<File> createStream(File root, boolean parallel) {
        FileSystemResolver resolver = new FileSystemResolver();
        return StreamSupport.stream(new DfsTreeSpliterator<>(root, resolver), parallel);
    }

Of course, such API already exists with `Files.walk()` or `Files.walkFileTree()`. The main advantage of
this API is to provide a unique API for traversing any kind of tree.
