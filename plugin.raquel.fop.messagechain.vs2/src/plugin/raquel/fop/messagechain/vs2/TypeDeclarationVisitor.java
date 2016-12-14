package plugin.raquel.fop.messagechain.vs2;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeDeclarationVisitor extends ASTVisitor {
	List<TypeDeclaration> typed = new ArrayList<TypeDeclaration>();

	public boolean visit(TypeDeclaration node) {
		typed.add(node);
		return super.visit(node);
		//return false;
	}

	public List<TypeDeclaration> getExpression() {
		return typed;
	}
}
