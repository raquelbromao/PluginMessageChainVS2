package plugin.raquel.fop.messagechain.vs2;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;

public class getProject implements IWorkbenchWindowActionDelegate {
	IWorkbenchWindow activeWindow = null;
	
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
		Shell shell = activeWindow.getShell();
		MessageDialog.openInformation(shell, "Hello World", "Hello World!");
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
