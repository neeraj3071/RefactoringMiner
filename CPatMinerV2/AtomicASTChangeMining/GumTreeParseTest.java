import com.github.gumtreediff.gen.srcml.SrcmlCsTreeGenerator;
import com.github.gumtreediff.tree.TreeContext;

public class GumTreeParseTest {
    public static void main(String[] args) throws Exception {
        SrcmlCsTreeGenerator gen = new SrcmlCsTreeGenerator();
        TreeContext tc = gen.generateFrom().file("TestClass.xml");
        System.out.println(tc.getRoot().toTreeString());
    }
}
