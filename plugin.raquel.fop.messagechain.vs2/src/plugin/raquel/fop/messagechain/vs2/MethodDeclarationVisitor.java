package plugin.raquel.fop.messagechain.vs2;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodDeclarationVisitor extends ASTVisitor {
	List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();

	public boolean visit(MethodDeclaration node) {
		methods.add(node);
		return super.visit(node);
		//return false;
	}

	public List<MethodDeclaration> getExpression() {
		return methods;
	}
}