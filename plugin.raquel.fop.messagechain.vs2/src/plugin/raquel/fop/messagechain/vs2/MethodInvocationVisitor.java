package plugin.raquel.fop.messagechain.vs2;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationVisitor extends ASTVisitor {
	List<MethodInvocation> methods = new ArrayList<MethodInvocation>();

	public boolean visit(MethodInvocation node) {
		methods.add(node);
		//return super.visit(node);
		return false;
	}

	public List<MethodInvocation> getExpression() {
		return methods;
	}
}
