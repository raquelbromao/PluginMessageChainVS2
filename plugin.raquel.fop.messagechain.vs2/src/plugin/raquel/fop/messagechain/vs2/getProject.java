package plugin.raquel.fop.messagechain.vs2;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class getProject implements IWorkbenchWindowActionDelegate {
	IWorkbenchWindow activeWindow = null;
	public Shell shlMessageChain;
	IProject projectSelection;
	IPackageFragment[] packagesSelection;
	
	/**
	 * Lista os projetos da Workspace em utilização
	 */
	public IProject[] getAllProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		return projects;
	}
	
	/**
	 * Gera a janela 1 do Plug-in
	 * @wbp.parser.entryPoint
	 */
	public void run(IAction proxyAction) {
		// proxyAction has UI information from manifest file (ignored)
		// constrói a janela principal
		shlMessageChain = new Shell();
		shlMessageChain.setBackground(SWTResourceManager.getColor(240, 240, 240));
		shlMessageChain.setToolTipText("");
		shlMessageChain.setSize(670, 114);
		shlMessageChain.setText("Message Chain Plugin");
		shlMessageChain.setLayout(null);
		
		// adiciona texto na janela principal
		Label lbl = new Label(shlMessageChain, SWT.NONE);
		lbl.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lbl.setBounds(25, 10, 394, 17);
		lbl.setText("Message Chain: all methods in workspace!");

		// adiciona tipo combo com todos os projetos da workspace
		Combo comboProjects = new Combo(shlMessageChain, SWT.NONE);
		comboProjects.setBounds(25, 31, 425, 23);

		// Gets all projects from workspace e insere no combo
		IProject[] projects = getAllProjects();
		for (int i = 0; i < projects.length; i++) {
			comboProjects.add(projects[i].getName());
		}

		// deixa como pré-selecionado o projeto no combo[0]
		comboProjects.select(0);
		
		// adiciona botões de APPLY e CANCEL
		// APPLY
		Button btnApplyProjects = new Button(shlMessageChain, SWT.NONE);
		btnApplyProjects.setSelection(true);
		btnApplyProjects.setBounds(456, 25, 75, 25);
		btnApplyProjects.setText("Apply");
		// CANCEL
		Button btnCancel = new Button(shlMessageChain, SWT.NONE);
		btnCancel.setBounds(456, 50, 75, 25);
		btnCancel.setText("Cancel");
		
		// adiciona evento ao clicar nos botões de APPLY e CANCEL
		// APPLY
		btnApplyProjects.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					// Acha a raiz da workspace para criar/carregar o IProject selecionado pelo usuário
					String nameProject = comboProjects.getItem(comboProjects.getSelectionIndex());
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();

					// Pega a raiz do projeto selecionado pelo usuário
					projectSelection = root.getProject(nameProject);
					projectSelection.open(null);

					// Gera a lista de todas as classes do projeto selecionado
					// com o tipo IPackageFragment que obtenho todas as classes de um projeto
					// IProject -> IPackageFragment -> ICompilationUnit -> arq.java
					packagesSelection = JavaCore.create(projectSelection).getPackageFragments();

					// envia o projeto selecionado para a nova janela (getMC)
					getMC.main(null,packagesSelection);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		//CANCEL
		btnCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shlMessageChain.close();
			}
		});
		
		// comando para abertura da janela corretamente
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
		//  nothing to do
	}
}
