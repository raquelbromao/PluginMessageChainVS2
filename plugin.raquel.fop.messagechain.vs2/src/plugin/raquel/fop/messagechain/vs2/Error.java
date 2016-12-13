package plugin.raquel.fop.messagechain.vs2;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

public class Error {

private Shell shell;
    
    public Error(Display display) {

        initUI(display);
    }

    private void initUI(Display display) {
        
        shell = new Shell(display, SWT.SHELL_TRIM | SWT.CENTER);
        
        RowLayout layout = new RowLayout();
        layout.marginTop = 50;
        layout.marginBottom = 150;
        layout.marginLeft = 50;
        layout.marginRight = 150;
        shell.setLayout(layout);
        
        Button msgBtn = new Button(shell, SWT.PUSH);
        msgBtn.setText("Show message");
        msgBtn.addListener(SWT.Selection, event -> doShowMessageBox());
        
        shell.setText("Message box");
        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
          if (!display.readAndDispatch()) {
            display.sleep();
          }
        }      
    }
    
    private void doShowMessageBox() {
        
        int style = SWT.ICON_INFORMATION | SWT.OK;

        MessageBox dia = new MessageBox(shell, style);
        dia.setText("Information");
        dia.setMessage("Download completed."); 
        dia.open();
    }    
    
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Display display = new Display();
        Error err = new Error(display);
        display.dispose();
    }    
}
