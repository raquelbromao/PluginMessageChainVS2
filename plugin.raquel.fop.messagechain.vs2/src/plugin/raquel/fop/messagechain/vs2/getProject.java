package plugin.raquel.fop.messagechain.vs2;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
	//static Map<File, ArrayList<File>> FEAeCLA = new HashMap<File, ArrayList<File>>();
	static Map<ICompilationUnit,String> CLAeFEA = new HashMap<ICompilationUnit,String>();
	static Map<TypeDeclaration,String> TDeFEA = new HashMap<TypeDeclaration,String>();
	static Map<MethodDeclaration,ICompilationUnit> MDeCLA = new HashMap<MethodDeclaration,ICompilationUnit>();
	static Map<MethodInvocation,MethodDeclaration> MIeMD = new HashMap<MethodInvocation,MethodDeclaration>();
	static ArrayList<String> FEA = new ArrayList<String>();
	static ArrayList<ICompilationUnit> CLA = new ArrayList<ICompilationUnit>();
	static ArrayList<TypeDeclaration> TD = new ArrayList<TypeDeclaration>();
	static ArrayList<MethodDeclaration> MD = new ArrayList<MethodDeclaration>();
	static ArrayList<MethodInvocation> MI = new ArrayList<MethodInvocation>();
	static ArrayList<MethodInvocation> allMI = new ArrayList<MethodInvocation>();
	
	/**
	 * Clean all static Maps and ArrayLists
	 */
	public void clearAll() {
		FEA.clear();CLA.clear();MD.clear();MI.clear();allMI.clear();
		CLAeFEA.clear();TDeFEA.clear();MDeCLA.clear();MIeMD.clear();
	}

	/**
	 * Lista os projetos da Workspace em utilização
	 */
	public IProject[] getAllProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		return projects;
	}
	
	protected void analyseCLA(ICompilationUnit classe) {
		// now create the AST for the ICompilationUnit
		CompilationUnit parse = parse(classe);

		// Calls the method for visit node in AST e return your information*/
		MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
		parse.accept(visitor);
				
		TypeDeclarationVisitor visitor2 = new TypeDeclarationVisitor();
		parse.accept(visitor2);
		
		results.append("\n\n.JAK: "+classe.getElementName()+"\n\tTIPOS DECLARADOS: ");
		for (TypeDeclaration node2 : visitor2.getExpression()) {
			results.append("[" + node2.getName().toString().trim() + "]\t");
			TDeFEA.put(node2, CLAeFEA.get(classe));
			TD.add(node2);
		}
		
		results.append("\n\n.JAK: "+classe.getElementName()+"\n\tMÉTODOS DECLARADOS: ");
		for (MethodDeclaration node : visitor.getExpression()) {
			results.append("[" + node.getName().toString().trim() + "]\t");
			MDeCLA.put(node, classe);
			MD.add(node);
		}
	}

	private void analyseMD(ArrayList<MethodDeclaration> arrayMD) {
		//int contMI = 0;
		int contNullMI = 0;

		for (MethodDeclaration mdS : arrayMD) {	
			//results.append("\n\nMÉTODO DECLARADO: "+mdS.getName().toString()+"\nMÉTODOS INVOCADOS: ");
			if (mdS.getBody() == null) {
				contNullMI++;
				//results.append("Método com corpo nulo");
			} else {
				String bd = mdS.getBody().toString();
				char body[] = bd.toCharArray();
				Block parse2 = parseBlock(body);
					
				// Calls the method for visit node in AST e return your information
				MethodInvocationVisitor visitor2 = new MethodInvocationVisitor();
				parse2.accept(visitor2);

				for (MethodInvocation node : visitor2.getExpression()) {
					//contMI++;
					//results.append("#"+contMI+"["+node.toString()+"]\t");
					MIeMD.put(node, mdS);
					MI.add(node);
				}	
			}		
		}
		//contMI = 0;
		results.append("\n\nMétodos com corpo nulo: "+contNullMI);
		contNullMI = 0;
	}
	
	protected void analyseMI(ArrayList<MethodInvocation> allMI2) {
		ArrayList<String> aux_md = new ArrayList<String>();
		//breakMI(allMI2);
		
		for (MethodDeclaration n : MD) {
			aux_md.add(n.getName().toString().trim());
		}
		
		//results.append("\n\n### MI QUE MD CONTEM\n");			
		for (MethodInvocation m : allMI2) {				
				ArrayList<MethodInvocation> aux = breakMI2(m);
				for (MethodInvocation teste : aux) {
					results.append("\n\t\tO membro do MC ["+teste.getName().toString().trim()
								+"] pertence ao array de MD do projeto? "+aux_md.contains(teste.getName().toString().trim())
								+"\n");
					if (aux_md.contains(teste.getName().toString().trim())) {
						results.append("Vai pra próxima etapa: ANÁLISE DO SEU CORPO!");
					}
				}
		}
		results.append("\n\n### FIM ANALYSE MI ###");
	}
	
	private ArrayList<MethodInvocation> breakMI2(MethodInvocation node) {
		int NMCS = 0;		
		ArrayList<MethodInvocation> subMI = new ArrayList<MethodInvocation>();
		int contMembers = 0;
		
		results.append("\n\n"+node.toString().trim()+"\n");
		String exp = node.toString().trim();
		char expression[] = exp.toCharArray();
		Expression parse2 = parseMI(expression);
			
		// Calls the method for visit node in AST e return your information
		MethodInvocationVisitor2 visitor = new MethodInvocationVisitor2();
		parse2.accept(visitor);
		
		for (MethodInvocation min : visitor.getExpression()) {
			contMembers++;
			subMI.add(min);
			results.append("\t# "+min.getName().toString().trim()+"\n");
		}
		
		results.append("\t\tNMCS = "+contMembers);
		NMCS = NMCS + contMembers;
		results.append("\n\t\tARRAY =");
		for (MethodInvocation teste2 : subMI) {
			results.append("\t["+teste2.getName().toString().trim()+"]");
		}
		
		return subMI;
	}
	
	private void breakMI(ArrayList<MethodInvocation> allMI2) {
		int NMCS = 0;
		
		for (MethodInvocation teste : allMI2) {
			ArrayList<MethodInvocation> subMI = new ArrayList<MethodInvocation>();
			int contMembers = 0;
			results.append("\n\n"+teste.toString().trim()+"\n");
			String exp = teste.toString().trim();
			char expression[] = exp.toCharArray();
			Expression parse2 = parseMI(expression);
				
			// Calls the method for visit node in AST e return your information
			MethodInvocationVisitor2 visitor = new MethodInvocationVisitor2();
			parse2.accept(visitor);

			for (MethodInvocation min : visitor.getExpression()) {
				contMembers++;
				subMI.add(min);
				results.append("\t# "+min.getName().toString().trim()+"\n");
			}
			
			results.append("\t\tNMCS = "+contMembers);
			NMCS = NMCS + contMembers;
			results.append("\n\t\tARRAY =");
			for (MethodInvocation teste2 : subMI) {
				results.append("\t["+teste2.getName().toString().trim()+"]");
			}
		}
		results.append("\n\n\nNMCS total = "+NMCS+"\n\n\n");
		//return subMI;
	}
	
	/**
	 * 
	 * @param packageSelection
	 * @throws JavaModelException
	 */
	private void geraMaps(IPackageFragment[] packageSelection) throws JavaModelException {			
		for (IPackageFragment mypackage : packageSelection) {
			for (final ICompilationUnit classe : mypackage.getCompilationUnits()) {
				results.append("PATH CLASS: "+classe.getPath().toString().trim()+"\n");
				CLA.add(classe);
			}
		}
		
		//int contCLA = 0;
		//int contMD = 0;
		
		/*results.append("\nSIZE CLA: "+CLA.size()+"\nCLASSES: ");
		for (ICompilationUnit cla : CLA) {
			contCLA++;
			results.append("#"+contCLA+"["+cla.getElementName()+"]\t");
		}*/		
		
		for (ICompilationUnit node : CLA) {
			analyseCLA(node);
		}
		
		/*results.append("\n\nTODOS OS MÉTODOS DECLARADOS EM MD2\n");
		for (MethodDeclaration node : MD) {
			contMD++;
			results.append("CLASSE ORIGINAL: "+MDeCLA.get(node).getElementName()+"\n#"+contMD+"["+node.getName().toString()+"]\n\n");
		}*/
		
		analyseMD(MD);
		
		/*results.append("\n\n");
		results.append("SIZE MI: "+MI.size()+"\nMÉTODOS INVOCADOS: ");
		for(MethodInvocation node : MI) {
			contMI++;
			results.append("#"+contMI+"["+node.getParent().toString().trim()+"]\t");
		}*/
		
		/*int contMI = 0;	
		results.append("\n\n\nANÁLISE DA ÁRVORE\n\n");
		for (Map.Entry<MethodInvocation,MethodDeclaration> aux3 : MIeMD.entrySet()) {
			contMI++;
			results.append("#"+contMI+" MI: "+aux3.getKey().toString().trim()
					+"\n\tSOBE(MD): "+aux3.getValue().getName().toString().trim()
					+"\n\t\tSOBE(CLA): "+MDeCLA.get(aux3.getValue()).getElementName()
					+"\n\n");
		}*/
	} 
	
	/**
	 * Reads a char[] of Expression of MethodInvocation node and creates the AST DOM for
	 * manipulating the Java source
	 * 
	 * @param unit
	 * @return
	 */
	private static Expression parseMI(char[] unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (Expression) parser.createAST(null);
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
		// constrói a janela principal
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
					clearAll();results.setText("");
					int contMIP = 0;

					// Acha a raiz da workspace para criar/carregar o IProject
					// selecionado pelo usuário
					String nameProject = comboProjects.getItem(comboProjects.getSelectionIndex());
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();

					// Pega a raiz do projeto selecionado pelo usuário
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
					
					// VERIFICA A EXISTÊNCIA DA PASTA FEATURES NO PROJETO
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
							//results.append("Número de Features é menor ou igual a 2. Impossível ter Message Chains!\n");
						//} else {
							// Gera a lista de todas as classes do projeto selecionado
							// com o tipo IPackageFragment onde obtenho todas as classes
							// do projeto: IProject -> IPackageFragment -> ICompilationUnit -> arq.java
							results.append("Gerando dados... Aguarde!\n\n");
							packageSelection = JavaCore.create(projectSelection).getPackageFragments();					
							geraMaps(packageSelection);
					
							results.append("\n\n### METHOD INVOCATIONS ###\n");
							
							for (IPackageFragment mypackage : packageSelection) {
								for (final ICompilationUnit classe : mypackage.getCompilationUnits()) {
									CompilationUnit parse = parse(classe);
									
									// Calls the method for visit node in AST e return your information*/
									MethodInvocationVisitor visitor = new MethodInvocationVisitor();
									parse.accept(visitor);
									
									for (MethodInvocation node : visitor.getExpression()) {
										// Take expression and converts to String, write in the screen
										contMIP++;
										String mi = node.getParent().toString().trim();
										if (contMIP < 10) {
											results.append("\n#0"+contMIP+"\t"+node.toString().trim());
											allMI.add(node);
										} else {
											results.append("\n#"+contMIP+"\t"+node.toString().trim());	
											allMI.add(node);
										}
									}
								}
							}
							
							contMIP = 0;
							
							/*for (MethodInvocation auxi : allMI) {
								// Take expression and converts to String, write in the screen
								contMIP++;
								//String mi = node.getParent().toString().trim();
								if (contMIP < 10) {
									results.append("\n#0"+contMIP+"\t "
											+auxi.getExpression().toString().trim()
											+"\t "+auxi.toString().trim());
								} else {
									results.append("\n#"+contMIP+"\t "
											+auxi.getExpression().toString().trim()
											+"\t "+auxi.toString().trim());
								}
							}*/
							
							if (MI.size() != contMIP) {
								results.append("\n\nMÉTODOS INVOCADOS DEU DIFERENTE!\nMI = "
								+MI.size()+" ||| contMIP = "+contMIP);
							}
							
							results.append("\n\n### METHOD INVOCATION PARSE AND VISIT TEST ###");							
							analyseMI(allMI);							
						//} 
				    //} else {
				    	//results.append("PASTA 'features' NÃO EXISTE!\nImpossível prosseguir!\n");
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
				clearAll();results.setText("");
			}
		});
		btnClear.setBounds(489, 24, 46, 25);
		btnClear.setText("Clear");
		
		Button btnClose = new Button(shell, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				clearAll();shell.close();
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