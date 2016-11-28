package plugin.raquel.fop.messagechain.vs2;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class getProject implements IWorkbenchWindowActionDelegate {
	IWorkbenchWindow activeWindow = null;
	public Shell shlMessageChain;
	
	/**
	 * Lista os projetos da Workspace em utilização
	 */
	public IProject[] getAllProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		return projects;
	}
	
	/** Run the action. Display the Hello World message
	 */		
	public void run(IAction proxyAction) {
		// proxyAction has UI information from manifest file (ignored)
		shlMessageChain = new Shell();
		shlMessageChain.setSize(547, 300);
		shlMessageChain.setText("Message Chain Plugin");
		shlMessageChain.setLayout(null);
		
		Label lbl = new Label(shlMessageChain, SWT.NONE);
		lbl.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lbl.setBounds(25, 10, 394, 15);
		lbl.setText("Message Chain: all methods in workspace!");

		Combo comboProjects = new Combo(shlMessageChain, SWT.NONE);
		comboProjects.setBounds(25, 27, 425, 23);

		// Gets all projects from workspace
		IProject[] projects = getAllProjects();
		for (int i = 0; i < projects.length; i++) {
			comboProjects.add(projects[i].getName());
		}

		comboProjects.select(0);
		
		Button btnApplyProjects = new Button(shlMessageChain, SWT.NONE);
		btnApplyProjects.setSelection(true);
		btnApplyProjects.setBounds(456, 25, 75, 25);
		btnApplyProjects.setText("Apply");

		Button btnCancel = new Button(shlMessageChain, SWT.NONE);
		btnCancel.setBounds(456, 50, 75, 25);
		btnCancel.setText("Cancel");
		
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
