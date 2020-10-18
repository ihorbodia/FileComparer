// 
// Decompiled by Procyon v0.5.36
// 

package FileComparer;

import javax.swing.JOptionPane;
import java.util.prefs.BackingStoreException;
import javax.swing.LayoutStyle;
import java.awt.LayoutManager;
import javax.swing.GroupLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Font;
import java.awt.Component;
import java.io.FilenameFilter;
import java.awt.Frame;
import java.awt.FileDialog;
import java.io.File;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class Settings extends JFrame
{
    public static final String PREFS_FIRST_RUN2 = "PREFS_FIRST_RUN2";
    public static final String PREFS_FRAME_X2 = "PREFS_FRAME_X2";
    public static final String PREFS_FRAME_Y2 = "PREFS_FRAME_Y2";
    public static final String PREFS_DATABASE_FILE = "PREFS_DATABASE_FILE";
    public static final String URL_COLOMN_NUMBER = "URL_COLOMN_NUMBER";
    public static final String E_MAIL_COLOMN_NUMBER = "E_MAIL_COLOMN_NUMBER";
    private JFileChooser fileChooser;
    private Preferences preferences;
    private FileComparer fileComparer;
    private JLabel DatabaseJLabel;
    protected JButton chooseDataBaseButton;
    protected JTextField databaseJTextField;
    private JTextField emailColomnNumberJTextField;
    private JLabel jLabel1;
    private JLabel jLabel3;
    private JTextField urlColomnNumberJTextField;
    
    public Settings(final FileComparer fileComparer) {
        this.fileChooser = new JFileChooser();
        this.preferences = Preferences.userNodeForPackage(Settings.class);
        this.fileComparer = fileComparer;
        this.initComponents();
        if (this.preferences.getBoolean("PREFS_FIRST_RUN2", true)) {
            this.setLocationByPlatform(true);
            this.preferences.putBoolean("PREFS_FIRST_RUN2", false);
        }
        else {
            this.setLocation(this.preferences.getInt("PREFS_FRAME_X2", 0), this.preferences.getInt("PREFS_FRAME_Y2", 0));
        }
        final String pathToDatabase = this.preferences.get("PREFS_DATABASE_FILE", "");
        final File file = new File(pathToDatabase);
        if (file.exists()) {
            this.databaseJTextField.setText(pathToDatabase);
            final String urlColomnNumberStartFromOne = this.preferences.get("URL_COLOMN_NUMBER", "");
            this.urlColomnNumberJTextField.setText(urlColomnNumberStartFromOne);
            final String emailColomnNumberStartFromOne = this.preferences.get("E_MAIL_COLOMN_NUMBER", "");
            this.emailColomnNumberJTextField.setText(emailColomnNumberStartFromOne);
        }
    }
    
    private File chooseFile() {
        if (System.getProperty("os.name", "").contains("Mac")) {
            final FileDialog fileDialog = new FileDialog(this);
            fileDialog.setFilenameFilter(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return Utilities.isValidFileFormatTxtOrCSV(name);
                }
            });
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            fileDialog.setMultipleMode(false);
            fileDialog.setVisible(true);
            final String directory = fileDialog.getDirectory();
            final String file = fileDialog.getFile();
            return (file == null || directory == null) ? null : new File(directory, file);
        }
        if (this.fileChooser.showOpenDialog(this) == 0) {
            return this.fileChooser.getSelectedFile();
        }
        return null;
    }
    
    private void tryToSetStartButtonEnable() {
        if (this.isDatabaseFileConnected()) {
            this.fileComparer.enableStartButtonWithSourceCheck();
        }
    }
    
    public void openSettingsForSelectNewDatabaseFile(final boolean forDataBaseupdate) {
        this.setVisible(true);
        if (forDataBaseupdate) {
            this.databaseJTextField.setText("");
            this.openSelectFileWindow();
        }
    }
    
    private boolean isDatabaseFileConnected() {
        final boolean isDatabaseFileNotConnected = this.databaseJTextField.getText().isEmpty();
        final boolean isAllFilesConnected = !isDatabaseFileNotConnected;
        return isAllFilesConnected;
    }
    
    private void initComponents() {
        this.chooseDataBaseButton = new JButton();
        this.databaseJTextField = new JTextField();
        this.DatabaseJLabel = new JLabel();
        this.jLabel1 = new JLabel();
        this.emailColomnNumberJTextField = new JTextField();
        this.jLabel3 = new JLabel();
        this.urlColomnNumberJTextField = new JTextField();
        this.setDefaultCloseOperation(0);
        this.setTitle("Settings");
        this.setFont(new Font("Dialog", 0, 11));
        this.setLocationByPlatform(true);
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent evt) {
                Settings.this.formWindowClosing(evt);
            }
            
            @Override
            public void windowClosed(final WindowEvent evt) {
                Settings.this.formWindowClosed(evt);
            }
        });
        this.chooseDataBaseButton.setFont(new Font("Dialog", 0, 11));
        this.chooseDataBaseButton.setIcon(new ImageIcon(this.getClass().getResource("/folder.png")));
        this.chooseDataBaseButton.setText("Choose...");
        this.chooseDataBaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                Settings.this.chooseDataBaseButtonActionPerformed(evt);
            }
        });
        this.databaseJTextField.setEditable(false);
        this.databaseJTextField.setColumns(40);
        this.databaseJTextField.setFont(new Font("Dialog", 0, 11));
        this.DatabaseJLabel.setFont(new Font("Dialog", 0, 12));
        this.DatabaseJLabel.setHorizontalAlignment(11);
        this.DatabaseJLabel.setText("Database:");
        this.DatabaseJLabel.setPreferredSize(new Dimension(111, 14));
        this.jLabel1.setFont(new Font("Dialog", 0, 12));
        this.jLabel1.setText("e-mail colomn number:");
        this.emailColomnNumberJTextField.setColumns(2);
        this.emailColomnNumberJTextField.setHorizontalAlignment(4);
        this.jLabel3.setFont(new Font("Dialog", 0, 12));
        this.jLabel3.setText("url colomn number:");
        this.urlColomnNumberJTextField.setColumns(2);
        this.urlColomnNumberJTextField.setHorizontalAlignment(4);
        final GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup().addComponent(this.jLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.emailColomnNumberJTextField, -2, -1, -2).addGap(18, 18, 18).addComponent(this.jLabel3).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.urlColomnNumberJTextField, -2, -1, -2)).addGroup(layout.createSequentialGroup().addComponent(this.DatabaseJLabel, -2, 60, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.databaseJTextField, -2, 494, -2))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.chooseDataBaseButton).addContainerGap(-1, 32767)));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.DatabaseJLabel, -2, 26, -2).addComponent(this.databaseJTextField, -2, 26, -2).addComponent(this.chooseDataBaseButton)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.emailColomnNumberJTextField, -2, -1, -2).addComponent(this.jLabel1).addComponent(this.jLabel3).addComponent(this.urlColomnNumberJTextField, -2, -1, -2)).addContainerGap(-1, 32767)));
        this.pack();
        this.setLocationRelativeTo(null);
    }
    
    private void formWindowClosing(final WindowEvent evt) {
        this.dispose();
    }
    
    private void formWindowClosed(final WindowEvent evt) {
        this.savePreferences();
        this.tryToSetStartButtonEnable();
    }
    
    private void savePreferences() {
        this.preferences.putInt("PREFS_FRAME_X2", this.getX());
        this.preferences.putInt("PREFS_FRAME_Y2", this.getY());
        this.preferences.put("PREFS_DATABASE_FILE", this.databaseJTextField.getText());
        this.preferences.put("URL_COLOMN_NUMBER", this.urlColomnNumberJTextField.getText());
        this.preferences.put("E_MAIL_COLOMN_NUMBER", this.emailColomnNumberJTextField.getText());
        try {
            this.preferences.flush();
        }
        catch (BackingStoreException e) {
            System.err.println("Error in savePreferences from SettingsJFrame" + e.getMessage());
        }
    }
    
    private void mainWork() {
        Utilities.searchIndexesInDatabaseFile(this.databaseJTextField.getText());
        if (Utilities.getUrlIndexStartFromZero() == Utilities.getEmailIndexStartFromZero()) {
            JOptionPane.showMessageDialog(this, "Not able to detect the columns with emails and URLs. Set column ID manually if needed");
            this.urlColomnNumberJTextField.setText("");
            this.emailColomnNumberJTextField.setText("");
        }
        else if (Utilities.getUrlIndexStartFromZero() == -1) {
            JOptionPane.showMessageDialog(this, "Not able to detect the column with URLs. Set column ID manually if needed");
            this.emailColomnNumberJTextField.setText(Utilities.getEmailIndexStringStartFromOne());
            this.urlColomnNumberJTextField.setText("");
        }
        else if (Utilities.getEmailIndexStartFromZero() == -1) {
            JOptionPane.showMessageDialog(this, "Not able to detect the column with emails. Set column ID manually if needed");
            this.urlColomnNumberJTextField.setText(Utilities.getUrlIndexStringStartFromOne());
            this.emailColomnNumberJTextField.setText("");
        }
        else {
            this.urlColomnNumberJTextField.setText(Utilities.getUrlIndexStringStartFromOne());
            this.emailColomnNumberJTextField.setText(Utilities.getEmailIndexStringStartFromOne());
            this.savePreferences();
            this.tryToSetStartButtonEnable();
        }
    }
    
    private void chooseDataBaseButtonActionPerformed(final ActionEvent evt) {
        this.openSelectFileWindow();
    }
    
    private void openSelectFileWindow() {
        final File file = this.chooseFile();
        if (file != null) {
            this.databaseJTextField.setText(file.getAbsolutePath());
            this.mainWork();
        }
        this.tryToSetStartButtonEnable();
    }
}
