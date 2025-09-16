package transformation;

import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.Type;

public class UnitNode extends SrcMLNodeType {
   public static final String TYPE = "unit";

   public UnitNode(Type type) {
      super(type);
   }

   public UnitNode(Type type, String label) {
      super(type, label);
   }

   protected UnitNode(Tree other) {
      super(other);
   }

   public void accept(SrcMLTreeVisitor visitor) {
      visitor.visit(this);
   }
}
