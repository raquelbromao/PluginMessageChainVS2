package plugin.raquel.fop.messagechain.vs2;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.wb.swt.SWTResourceManager;
import java.util.ArrayList;
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
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
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
	private Text results;
	static ArrayList<MethodDeclaration> MD = new ArrayList<MethodDeclaration>();

	/**
	 * Lista os projetos da Workspace em utilização
	 */
	public IProject[] getAllProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		return projects;
	}

	protected void analyseClass(ICompilationUnit classe) {
		// ICompilationUnit unit == class
		// now create the AST for the ICompilationUnits
		CompilationUnit parse = parse(classe);

		// Calls the method for visit node in AST e return your information*/
		MethodDeclarationVisitor visitor3 = new MethodDeclarationVisitor();
		parse.accept(visitor3);

		results.append("\t\t#### METHODS DECLARATION\n");
		// Write in the screen: IfStatement and your type
		for (MethodDeclaration node : visitor3.getExpression()) {
			// Take expression and converts to String, write in the screen
			String mi = node.getName().toString();
			results.append("\t\t\tMD: [" + mi + "]\n");
			results.append("\t\t\t\tParameters: "+node.parameters().toString()
					+"\n\t\t\t\tBody Type: "+node.getBody().getNodeType()
					+"\n\t\t\t\tBody StartPosition: "+node.getBody().getStartPosition()
					+"\n");
			MD.add(node);
		}
		
		//results.append("\n\t\t[ARRAY COM METHODSDECLARATION]\n");
		//for (int i = 0; i < MD.size(); i++) {
			//esults.append("\t\t\t"+MD.get(i).getName().toString()+"\n");			
		//}
		
		// Chama função para análise do corpo de cada MD da classe
		analyseBodyMD(MD);
	}

	private void analyseBodyMD(ArrayList<MethodDeclaration> node) {
		ArrayList<Block> bodyMD =  new ArrayList<Block>();
		ArrayList<ASTNode> statementsBody = new ArrayList<ASTNode>();
		
		// Adicionei todos os Bodys dos MD numa lista
		for (int i = 0; i < node.size(); i++) {
			bodyMD.add(node.get(i).getBody());
			results.append("\n\t\t\t\tBODY STATEMENT[0]: "+node.get(i).getBody().statements().get(0).toString()+"\n");
		}
		
		for(int j = 0; j < bodyMD.size(); j++) {
			statementsBody.add((ASTNode) bodyMD.get(j).statements().get(j));
		}
		
		analyseStatementsBody(statementsBody);		
	}

	private void analyseStatementsBody(ArrayList<ASTNode> node) {
		for(ASTNode c : node) {
			if (c.getNodeType()== 32)
			  results.append("\n\t\t\t\t\tMI: "+c.toString()+"\n");
		}		
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
					MD.clear();
					results.setText("");

					// Acha a raiz da workspace para criar/carregar o IProject
					// selecionado pelo usuário
					String nameProject = comboProjects.getItem(comboProjects.getSelectionIndex());
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();

					// Pega a raiz do projeto selecionado pelo usuário
					projectSelection = root.getProject(nameProject);
					results.append("## NAME OF PROJECT: " + projectSelection.getName() + "\n");
					results.append("## PATH OF PROJECT: " + projectSelection.getFullPath() + "\n");
					projectSelection.open(null);

					// Gera a lista de todas as classes do projeto selecionado
					// com o tipo IPackageFragment que obtenho todas as classes
					// de um projeto
					// IProject -> IPackageFragment -> ICompilationUnit ->
					// arq.java
					packageSelection = JavaCore.create(projectSelection).getPackageFragments();

					for (IPackageFragment mypackage : packageSelection) {
						for (final ICompilationUnit classe : mypackage.getCompilationUnits()) {
							results.append("\n\t### NAME OF CLASS: " + classe.getElementName() + "\n");
							analyseClass(classe);
						}
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
				MD.clear();
				results.setText("");
			}
		});
		btnClear.setText("Clear");
		btnClear.setBounds(584, 63, 75, 25);

		Button btnClose = new Button(shlMessageChain, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				MD.clear();
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
