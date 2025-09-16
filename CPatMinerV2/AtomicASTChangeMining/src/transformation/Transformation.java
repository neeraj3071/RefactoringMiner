package transformation;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.srcml.SrcmlCsTreeGenerator;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;


public class Transformation {

    public static CompilationUnit transform_csharp_to_java(String content) {
        try {
            SrcmlCsTreeGenerator l = new SrcmlCsTreeGenerator();
            TreeContext tc = l.generateFrom().string(content);
            Tree tree_csharp = tc.getRoot();
            Tree transformedTree = TransformationUtils.transformTree(tree_csharp);
            String tree_string = tree_csharp.toTreeString();
            //System.out.println(tree_string);

            SrcMLTreeVisitor visitor = new SrcMLTreeVisitor();
            if (transformedTree instanceof UnitNode) {
                CompilationUnit m = visitor.visit((UnitNode) transformedTree);
                //System.out.println(m.toString());
                return m;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public static void transform() {

        Run.initGenerators(); // registers the available parsers
        String thisfile = "test.cs";
        String yaml_file = "test.yaml";
        String java_file = "test.java";
        try {
            SrcmlCsTreeGenerator l = new SrcmlCsTreeGenerator();
            TreeContext tc = l.generateFrom().file(thisfile);
            // TreeContext tc = l.generateFrom().file("TestClass.xml");
            Tree tree_csharp = tc.getRoot();
            //Tree tree_yaml = new YamlTreeGenerator().generateFrom().file(yaml_file).getRoot();

            //System.out.println(tree_csharp.toTreeString());
            Tree transformedTree = TransformationUtils.transformTree(tree_csharp);
            transformedTree.toString();

            SrcMLTreeVisitor visitor = new SrcMLTreeVisitor();
            iterate_children(transformedTree, visitor);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void iterate_children(Tree root, SrcMLTreeVisitor visitor) {
        if (root == null) {
            return;
        } else if (root instanceof UnitNode) {
            CompilationUnit m = visitor.visit((UnitNode) root);
            System.out.println(m.toString());
        } else {
            for (Tree child : root.getChildren()) {
                iterate_children(child, visitor);
            }
        }
    }

}
