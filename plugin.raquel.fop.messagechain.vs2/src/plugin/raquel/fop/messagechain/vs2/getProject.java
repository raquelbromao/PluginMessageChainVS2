package plugin.raquel.fop.messagechain.vs2;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.wb.swt.SWTResourceManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class getProject implements IWorkbenchWindowActionDelegate {
	IWorkbenchWindow activeWindow = null;
	public Shell shlMessageChain;
	IProject projectSelection;
	IPackageFragment[] packageSelection;
	private static Text results;
	static Map<String, ArrayList<String>> m = new HashMap<String, ArrayList<String>>();
	static Map<String, ArrayList<String>> mdTOmi = new HashMap<String, ArrayList<String>>();
	static ArrayList<ICompilationUnit> CLA = new ArrayList<ICompilationUnit>();
	static ArrayList<MethodDeclaration> MD2 = new ArrayList<MethodDeclaration>();
	static ArrayList<MethodInvocation> MI2 = new ArrayList<MethodInvocation>();

	/**
	 * Lista os projetos da Workspace em utilização
	 */
	public IProject[] getAllProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		return projects;
	}
	
	public static int extractInfixExpression(ASTNode node, int cont) {
		int k = 0;

		InfixExpression aux = (InfixExpression) node;

		results.append("\tNode: "+node.toString() 
					+"\n\t\tLeft Side: "+aux.getLeftOperand().toString()
					+"\n\t\t\tType LeftSide: "+aux.getLeftOperand().getNodeType() 
					+"\n\t\tRight Side: "+aux.getRightOperand().toString() 
					+"\n\t\t\tType RightSide: "+aux.getRightOperand().getNodeType()
					+"\n");

		if (aux.getLeftOperand().getNodeType() == 32) {
			results.append("\t\t\tLeftSide its MethodInvocation!" 
						+"\n\t\t\t\tNMCS -> "+(cont + 1) 
						+"\n");
			k = k + getChildren(aux.getLeftOperand(), 0);
		}

		if (aux.getRightOperand().getNodeType() == 32) {
			results.append("\t\t\tRightSide its MethodInvocation!" 
						+"\n\t\t\t\tNMCS -> "+(cont + 1) 
						+"\n");
			k = k + getChildren(aux.getRightOperand(), 0);
		}

		return k;
	}

	public static int extractAssignment(Assignment node) {
		int k = 0;

		results.append("\tNode: "+node.toString() 
					+"\n\t\tLeft Side: "+node.getLeftHandSide().toString()
					+"\n\t\t\tType LeftSide: "+node.getLeftHandSide().getNodeType() 
					+"\n\t\tRight Side: "+node.getRightHandSide().toString() 
					+"\n\t\t\tType RightSide: "+node.getRightHandSide().getNodeType()
					+"\n\n");

		if (node.getLeftHandSide().getNodeType() == 32) {
			results.append("\t\t\tLeftSide its MethodInvocation!\n");
			k = getChildren(node.getLeftHandSide(), 0);
			//NMCS = NMCS + k;
		} 
		
		if (node.getRightHandSide().getNodeType() == 32) {
			results.append("\t\t\tRightSide its MethodInvocation!\n");
			k = getChildren(node.getRightHandSide(), 0);
			//NMCS = NMCS + k;
		} 

		if (node.getRightHandSide().getNodeType() == 27) {
			k = extractInfixExpression(node.getRightHandSide(), 0);
			//NMCS = NMCS + k;
		}

		return k;
	}

	public static int getChildren(ASTNode node, int n) {
		int cont = n;
		String compara = "[]";

		List<ASTNode> children = new ArrayList<ASTNode>();
		@SuppressWarnings("rawtypes")
		List list = node.structuralPropertiesForType();

		for (int i = 0; i < list.size(); i++) {
			Object child = node.getStructuralProperty((StructuralPropertyDescriptor) list.get(i));
			if (child instanceof ASTNode) {
				children.add((ASTNode) child);
			}
		}

		String teste = children.toString();
		// results.append("MethodInvocation Node:
		// "+children.get(0).toString()+"\nNMCS: "+cont+"\n");

		// Se a string do filho for igual a [] -> CHEGOU AO FIM
		// e retorna resultado do contador para analyseClass
		if (teste.equals(compara)) {
			results.append("\n---> NMCS = " + cont + "\n");
			return cont;
		}

		// Aumenta o contador se o nó filho for MethodInvocation ou
		// SuperMethodInvocation e lista seus métodos componentes, assim
		// como parâmetros (se houver) de cada método encadeado
		if (node.getNodeType() == 32) {
			cont++;
			MethodInvocation nodev = (MethodInvocation) node;
			results.append("\tMethodInvocation: " + nodev.getName() + "\n\t\tNMCS -> " + cont + "\n");
			// Lista parâmetros do MethodInvocation
			if (nodev.arguments().toString().equals(compara) != true) {
				for (int k = 0; k < nodev.arguments().size(); k++) {
					results.append("\t\tArgument[" + k + "]: " + nodev.arguments().get(k).toString() + "\n");
					// Verifica se parâmetro é método p/ poder incrementar cont
					// e,
					// consequentemente, NMCS
					ASTNode param = (ASTNode) nodev.arguments().get(k);
					if (param.getNodeType() == 32) {
						cont++;
						results.append(
								"\t\t\tArg[" + k + "] its MethodInvocation!" + "\n\t\t\t\tNMCS -> " + cont + "\n");
					} else if (param.getNodeType() == 27) {
						results.append("\t\t\tArg[" + k + "] its InfixExpression!\n");
						cont = cont + extractInfixExpression(param, cont);
					}
				}
			}
		} else if (node.getNodeType() == 48) {
			cont++;
			SuperMethodInvocation nodesv = (SuperMethodInvocation) node;
			results.append("\tSuperMethodInvocation: " + nodesv.getName() + "\n\t\tNMCS -> " + cont + "\n");
			// Lista parâmetros do SuperMethodInvocation
			if (nodesv.arguments().toString().equals(compara) != true) {
				for (int k = 0; k < nodesv.arguments().size(); k++) {
					results.append("\t\tArgument[" + k + "]: " + nodesv.arguments().get(k).toString() + "\n");
					// Verifica se parâmetro é método p/ poder incrementar cont
					// e,
					// consequentemente NMCS
					ASTNode param = (ASTNode) nodesv.arguments().get(k);
					if (param.getNodeType() == 32) {
						cont++;
						results.append(
								"\t\t\tArg[" + k + "] its MethodInvocation!" + "\n\t\t\t\tNMCS -> " + cont + "\n");
					} else if (param.getNodeType() == 27) {
						results.append("\t\t\tArg[" + k + "] its InfixExpression!\n");
						cont = cont + extractInfixExpression(param, cont);
					}
				}
			}
		}

		// Recursão para encontrar próximo nó (filho do filho)
		return getChildren(children.get(0), cont);
	}

	protected ArrayList<String> analyseClass(ICompilationUnit classe) {
		// ICompilationUnit unit == class
		// now create the AST for the ICompilationUnits
		CompilationUnit parse = parse(classe);

		// Calls the method for visit node in AST e return your information*/
		MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
		parse.accept(visitor);

		ArrayList<String> MD1 = new ArrayList<String>();
		ArrayList<String> MI1 = new ArrayList<String>();
		//results.append("\n\t#### METHODS DECLARATION\n");
		
		// Write in the screen: IfStatement and your type
		for (MethodDeclaration node : visitor.getExpression()) {
			// Take expression and converts to String, write in the screen
			//String md = node.getName().toString();
			//results.append("\t\tMD: [" + md + "]\n");
			//results.append("\t\t\tParameters: "+node.parameters().toString()+"\n");
			MD1.add(node.getName().toString());
			MD2.add(node);
		}
		
		//results.append("\n\t\t[MAP CLASSE WITH METHODSDECLARATION]\n\n"+m+"\n");
		for (int i = 0; i < MD2.size(); i++) {			
			String aux = MD2.get(i).getBody().toString();
			//results.append("\t\t\t"+aux+"\n");
			char body[] = aux.toCharArray();
			Block parse2 = parseBlock(body);
			
			// Calls the method for visit node in AST e return your information
			MethodInvocationVisitor visitor2 = new MethodInvocationVisitor();
			parse2.accept(visitor2);

				//results.append("\n\t\t#### METHODS INVOCATION\n");
				// Write in the screen: IfStatement and your type
				for (MethodInvocation node : visitor2.getExpression()) {
					// Take expression and converts to String, write in the screen
					//String md = node.getParent().toString();
					//results.append("\t\t\tMI: [" + md + "]\n");
					MI1.add(node.getParent().toString());
					MI2.add(node);
				}
		}
		
		return MD1;
	}

	/**
	 * Reads a char[] of Block of MethodDeclaration and creates the AST DOM for
	 * manipulating the Java source
	 * 
	 * @param unit
	 * @return
	 */
	private static Block parseBlock(char[] unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (Block) parser.createAST(null);
	}
	

	/**
	 * Reads a ICompilationUnit and creates the AST DOM for manipulating the
	 * Java source file
	 *
	 * @param unit
	 * @return
	 */
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

	/**
	 * Gera a janela 1 do Plug-in
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void run(IAction proxyAction) {
		// proxyAction has UI information from manifest file (ignored)
		// constrói a janela principal
		shlMessageChain = new Shell();
		shlMessageChain.setLayout(null);

		Label lblPrincipal = new Label(shlMessageChain, SWT.NONE);
		lblPrincipal.setFont(SWTResourceManager.getFont("@Microsoft JhengHei", 11, SWT.BOLD));
		lblPrincipal.setBounds(10, 0, 184, 28);
		lblPrincipal.setText("Message Chain Plug-in");

		Label lblSelectTheProject = new Label(shlMessageChain, SWT.NONE);
		lblSelectTheProject.setFont(SWTResourceManager.getFont("@Microsoft JhengHei", 10, SWT.NORMAL));
		lblSelectTheProject.setBounds(10, 34, 112, 15);
		lblSelectTheProject.setText("Select the project:");

		results = new Text(shlMessageChain, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		results.setBounds(10, 69, 559, 376);

		Combo comboProjects = new Combo(shlMessageChain, SWT.NONE);
		comboProjects.setBounds(128, 31, 441, 23);

		// Gets all projects from workspace
		IProject[] projects = getAllProjects();
		for (int i = 0; i < projects.length; i++) {
			comboProjects.add(projects[i].getName());
		}

		comboProjects.select(0);

		Button btnApply = new Button(shlMessageChain, SWT.NONE);
		btnApply.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					// LIMPA A JANELA DOS RESULTADOS QUANDO SELECIONADO UM NOVO
					// PROJETO
					CLA.clear();MI2.clear();m.clear();MD2.clear();
					results.setText("");

					// Acha a raiz da workspace para criar/carregar o IProject
					// selecionado pelo usuário
					String nameProject = comboProjects.getItem(comboProjects.getSelectionIndex());
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();

					// Pega a raiz do projeto selecionado pelo usuário
					projectSelection = root.getProject(nameProject);
					//results.append("## NAME OF PROJECT: " + projectSelection.getName() + "\n");
					//results.append("## PATH OF PROJECT: " + projectSelection.getFullPath() + "\n");
					projectSelection.open(null);

					// Gera a lista de todas as classes do projeto selecionado
					// com o tipo IPackageFragment que obtenho todas as classes
					// de um projeto
					// IProject -> IPackageFragment -> ICompilationUnit ->
					// arq.java
					packageSelection = JavaCore.create(projectSelection).getPackageFragments();

					for (IPackageFragment mypackage : packageSelection) {
						for (final ICompilationUnit classe : mypackage.getCompilationUnits()) {
							//results.append("\n### NAME OF CLASS: " + classe.getElementName() + "\n");
							//analyseClass(classe);
							CLA.add(classe);
							
							// M(CLASSE, LIST<MethodDeclaration>)
							m.put(classe.getElementName(),analyseClass(classe));
						}
					}
					
					results.append("\tTamanho: "+m.size()
								//+"\n\tConjunto de Keys: "+m.keySet()
								//+"\n\tConjunto de Values: "+m.values()
								+"\n");
					
					for (Map.Entry<String, ArrayList<String>> classe : m.entrySet()) {
						results.append("Classe:"+classe.getKey()+"\nMétodos:"+classe.getValue()+"\n\n");
					}				    
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		});
		btnApply.setBounds(584, 29, 75, 25);
		btnApply.setText("Apply");

		Button btnClear = new Button(shlMessageChain, SWT.NONE);
		btnClear.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				CLA.clear();MD2.clear();MI2.clear();m.clear();
				results.setText("");
			}
		});
		btnClear.setText("Clear");
		btnClear.setBounds(584, 63, 75, 25);

		Button btnClose = new Button(shlMessageChain, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				CLA.clear();MD2.clear();MI2.clear();m.clear();
				shlMessageChain.close();
			}
		});
		btnClose.setBounds(584, 96, 75, 25);
		btnClose.setText("Close");

		shlMessageChain.pack();
		shlMessageChain.open();
	}

	// IActionDelegate method
	public void selectionChanged(IAction proxyAction, ISelection selection) {
		// do nothing, action is not dependent on the selection
	}

	// IWorkbenchWindowActionDelegate method
	public void init(IWorkbenchWindow window) {
		activeWindow = window;
	}

	// IWorkbenchWindowActionDelegate method
	public void dispose() {
		// nothing to do
	}
}