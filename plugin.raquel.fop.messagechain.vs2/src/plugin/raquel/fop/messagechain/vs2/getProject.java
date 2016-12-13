package plugin.raquel.fop.messagechain.vs2;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import java.io.File;
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
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class getProject implements IWorkbenchWindowActionDelegate {
	IWorkbenchWindow activeWindow = null;
	public Shell shell;
	IProject projectSelection;
	IPackageFragment[] packageSelection;
	private static Text results;
	static Map<String, ArrayList<String>> array1 = new HashMap<String, ArrayList<String>>();
	static Map<String, ArrayList<String>> array2 = new HashMap<String, ArrayList<String>>();
	static Map<String, ArrayList<String>> array3 = new HashMap<String, ArrayList<String>>();
	static Map<ICompilationUnit, ArrayList<MethodDeclaration>> CLAeMD = new HashMap<ICompilationUnit, ArrayList<MethodDeclaration>>();
	static Map<MethodDeclaration, ArrayList<MethodInvocation>> MDeMI = new HashMap<MethodDeclaration, ArrayList<MethodInvocation>>();
	static Map<File, ArrayList<File>> FEAeCLA = new HashMap<File, ArrayList<File>>();
	static Map<MethodDeclaration,ICompilationUnit> MDeCLA = new HashMap<MethodDeclaration,ICompilationUnit>();
	static Map<MethodInvocation,MethodDeclaration> MIeMD = new HashMap<MethodInvocation,MethodDeclaration>();
	static ArrayList<ICompilationUnit> CLA = new ArrayList<ICompilationUnit>();
	static ArrayList<MethodDeclaration> MD = new ArrayList<MethodDeclaration>();
	static ArrayList<MethodDeclaration> MD2 = new ArrayList<MethodDeclaration>();
	static ArrayList<MethodInvocation> MI = new ArrayList<MethodInvocation>();
	static ArrayList<String> FEA = new ArrayList<String>();
	
	/**
	 * Clean all static Maps and ArrayLists
	 */
	public void clearAll() {
		CLA.clear();MD.clear();MI.clear();FEA.clear();MD2.clear();
		array1.clear();array2.clear();array3.clear();
		CLAeMD.clear();MDeMI.clear();FEAeCLA.clear();MDeCLA.clear();MIeMD.clear();
	}

	/**
	 * Lista os projetos da Workspace em utiliza��o
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

		// Aumenta o contador se o n� filho for MethodInvocation ou
		// SuperMethodInvocation e lista seus m�todos componentes, assim
		// como par�metros (se houver) de cada m�todo encadeado
		if (node.getNodeType() == 32) {
			cont++;
			MethodInvocation nodev = (MethodInvocation) node;
			results.append("\tMethodInvocation: " + nodev.getName() + "\n\t\tNMCS -> " + cont + "\n");
			// Lista par�metros do MethodInvocation
			if (nodev.arguments().toString().equals(compara) != true) {
				for (int k = 0; k < nodev.arguments().size(); k++) {
					results.append("\t\tArgument[" + k + "]: " + nodev.arguments().get(k).toString() + "\n");
					// Verifica se par�metro � m�todo p/ poder incrementar cont
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
			// Lista par�metros do SuperMethodInvocation
			if (nodesv.arguments().toString().equals(compara) != true) {
				for (int k = 0; k < nodesv.arguments().size(); k++) {
					results.append("\t\tArgument[" + k + "]: " + nodesv.arguments().get(k).toString() + "\n");
					// Verifica se par�metro � m�todo p/ poder incrementar cont
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

		// Recurs�o para encontrar pr�ximo n� (filho do filho)
		return getChildren(children.get(0), cont);
	}
	
	protected void analyseCLA(ICompilationUnit classe) {
		// now create the AST for the ICompilationUnit
		CompilationUnit parse = parse(classe);

		// Calls the method for visit node in AST e return your information*/
		MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
		parse.accept(visitor);
				
		results.append("\n\nCLASSE: "+classe.getElementName()+"\nM�TODOS DECLARADOS: ");
		for (MethodDeclaration node : visitor.getExpression()) {
			String md = node.getName().toString();
			results.append("[" + md + "]\t");
			MDeCLA.put(node, classe);
			MD2.add(node);
		}
	}

	private void analyseMD(ArrayList<MethodDeclaration> arrayMD) {
		int contMI = 0;

		for (MethodDeclaration mdS : arrayMD) {	
			results.append("\n\nM�TODO DECLARADO: "+mdS.getName().toString()+"\nM�TODOS INVOCADOS: ");
			if (mdS.getBody() == null) {
				results.append("M�todo com corpo nulo");
			} else {
				String bd = mdS.getBody().toString();
				char body[] = bd.toCharArray();
				Block parse2 = parseBlock(body);
					
				// Calls the method for visit node in AST e return your information
				MethodInvocationVisitor visitor2 = new MethodInvocationVisitor();
				parse2.accept(visitor2);

				for (MethodInvocation node : visitor2.getExpression()) {
					contMI++;
					//aux.add(node.getParent().toString());
					results.append("#"+contMI+"["+node.toString()+"]\t");
					MIeMD.put(node, mdS);
					MI.add(node);
				}	
			}		
		}
		contMI = 0;
	}
	
	/**
	 * 
	 * @param packageSelection
	 * @throws JavaModelException
	 */
	private void geraMaps(IPackageFragment[] packageSelection) throws JavaModelException {			
		for (IPackageFragment mypackage : packageSelection) {
			//results.append("PATH PACKAGE: "+mypackage.getPath().toString()+"\n");
			for (final ICompilationUnit classe : mypackage.getCompilationUnits()) {
				results.append("PATH CLASS: "+classe.getPath().toString()+"\n");
				CLA.add(classe);
			}
		}
		
		int contCLA = 0;
		int contMD = 0;
		int contMI = 0;
		
		results.append("\nSIZE CLA: "+CLA.size()+"\nCLASSES: ");
		for (ICompilationUnit cla : CLA) {
			contCLA++;
			results.append("#"+contCLA+"["+cla.getElementName()+"]\t");
		}		
		
		for (ICompilationUnit node : CLA) {
			analyseCLA(node);
		}
		
		results.append("\n\nTODOS OS M�TODOS DECLARADOS EM MD2\n");
		for (MethodDeclaration node : MD2) {
			contMD++;
			results.append("CLASSE ORIGINAL: "+MDeCLA.get(node).getElementName()+"\n#"+contMD+"["+node.getName().toString()+"]\n\n");
		}
		
		analyseMD(MD2);
		
		results.append("\n\n");
		results.append("SIZE MI: "+MI.size()+"\nM�TODOS INVOCADOS: ");
		for(MethodInvocation node : MI) {
			contMI++;
			results.append("#"+contMI+"["+node.getParent().toString().trim()+"]\t");
		}
		
		contMI = 0;	
		results.append("\n\n\nAN�LISE DA �RVORE\n\n");
		for (Map.Entry<MethodInvocation,MethodDeclaration> aux3 : MIeMD.entrySet()) {
			contMI++;
			results.append("#"+contMI+" MI: "+aux3.getKey().getParent().toString().trim()+"\n\tSOBE(MD): "+aux3.getValue().getName().toString());
			results.append("\n\t\tSOBE(CLA): "+MDeCLA.get(aux3.getValue()).getElementName()+"\n\n");
		}
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
     * List all the files under a directory
     * @param directoryName to be listed
     * @return 
     */
    public ArrayList<String> listFiles(String directoryName){
    	ArrayList<String> aux = new ArrayList<String>();
        File directory = new File(directoryName);
        //get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList){
            if (file.isFile()){
                //results.append(file.getName()+"\t");
                aux.add(file.getName());
            }
        }
        return aux;
    }

	/**
	 * Gera a janela 1 do Plug-in
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void run(IAction proxyAction) {
		// proxyAction has UI information from manifest file (ignored)
		// constr�i a janela principal
		shell = new Shell();
		shell.setSize(678, 474);
		shell.setText("Message Chain Plug-in");
		
		Combo comboProjects = new Combo(shell, SWT.NONE);
		comboProjects.setBounds(10, 26, 414, 23);
		
		// Gets all projects from workspace
		IProject[] projects = getAllProjects();
		for (int i = 0; i < projects.length; i++) {
			comboProjects.add(projects[i].getName());
		}

		comboProjects.select(0);
		
		Button btnApply = new Button(shell, SWT.NONE);
		btnApply.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					clearAll();
					results.setText("");

					// Acha a raiz da workspace para criar/carregar o IProject
					// selecionado pelo usu�rio
					String nameProject = comboProjects.getItem(comboProjects.getSelectionIndex());
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();

					// Pega a raiz do projeto selecionado pelo usu�rio
					projectSelection = root.getProject(nameProject);
					projectSelection.open(null);
					
					/*results.append("PATH PROJECT FULL: "+projectSelection.getFullPath().toString()
							+"\nIs empty? "+projectSelection.getFullPath().isEmpty()
							//+"\nFeatures Path: "+projectSelection.getFile("/features").getFullPath().toString()
							+"\nProject Path: "+root.getProject(nameProject).getFullPath().toString()
							+"\n\n");*/
					
					//int contFEA = 0;
					String aux = projectSelection.getFile("/features").getFullPath().toString();
					
					String directoryName = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
					directoryName = directoryName.concat(aux);

					// List all the files under a directory
					//File directory = new File(directoryName);
					
					// VERIFICA A EXIST�NCIA DA PASTA FEATURES NO PROJETO
					//if (directory.exists()) {
						// LISTA TODAS AS FEATURES (OU SEJA TODAS AS SUBPASTAS DA PASTA FEATURES)
						// get all the files from a directory
						//File[] fList = directory.listFiles();
						//results.append("FEATURES PATH: "+directoryName+"\n\nSIZE FEA: "+fList.length);
						//for (File file : fList){
							//contFEA++;
							//results.append("\n#"+contFEA+"["+file.getName()+"] and CLA:\t");
							//array3.put(file.getName(), listFiles(file.getAbsolutePath()));
							//FEA.add(file.getName());
						//}
						//results.append("\n\n");
				    
					//	for (Map.Entry<String, ArrayList<String>> aux3 : array3.entrySet()) {
							//results.append("FEA: "+aux3.getKey()+"\nCLASSES: "+aux3.getValue()+"\n\n");
						//}	
				    
						// VERIFICA SE EXISTEM MAIS DE 2 FEATURES NO PROJETO
						//if (FEA.size() <= 2) {
							//results.append("N�mero de Features � menor ou igual a 2. Imposs�vel ter Message Chains!\n");
						//} else {
							// Gera a lista de todas as classes do projeto selecionado
							// com o tipo IPackageFragment onde obtenho todas as classes
							// do projeto: IProject -> IPackageFragment -> ICompilationUnit -> arq.java
							results.append("Gerando dados... Aguarde!\n\n");
							packageSelection = JavaCore.create(projectSelection).getPackageFragments();					
							geraMaps(packageSelection);
					
							results.append("\n\n");
							
							for (IPackageFragment mypackage : packageSelection) {
								for (final ICompilationUnit classe : mypackage.getCompilationUnits()) {
									CompilationUnit parse = parse(classe);
									int contMIP = 0;
									
									// Calls the method for visit node in AST e return your information*/
									MethodInvocationVisitor visitor = new MethodInvocationVisitor();
									parse.accept(visitor);
									
									for (MethodInvocation node : visitor.getExpression()) {
										// Take expression and converts to String, write in the screen
										contMIP++;
										String mi = node.getParent().toString();
										results.append("#"+contMIP+"MI: "+mi+"\t");	
									}
								}
							}
						//} 
				    //} else {
				    	//results.append("PASTA 'features' N�O EXISTE!\nImposs�vel prosseguir!\n");
				   // }
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}});
		btnApply.setBounds(437, 24, 46, 25);
		btnApply.setText("Apply");
		
		Button btnClear = new Button(shell, SWT.NONE);
		btnClear.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				clearAll();
				results.setText("");
			}
		});
		btnClear.setBounds(489, 24, 46, 25);
		btnClear.setText("Clear");
		
		Button btnClose = new Button(shell, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				clearAll();
				shell.close();
			}
		});
		btnClose.setText("Close");
		btnClose.setBounds(541, 24, 46, 25);
		
		results = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		results.setBounds(10, 55, 642, 370);

		shell.pack();
		shell.open();
	}
	
	// IActionDelegate method
	public void selectionChanged(IAction proxyAction, ISelection selection) {
		// do nothing, action is not dependent on the selection
	}

	// IWorkbenchWindowActionDelegate method
	public void init(IWorkbenchWindow window) {
		activeWindow = window;
	}

	public void dispose() {
		// nothing to do
	}
}