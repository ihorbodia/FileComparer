// 
// Decompiled by Procyon v0.5.36
// 

package FileComparer;

import org.apache.commons.lang3.StringUtils;
import java.awt.EventQueue;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.util.prefs.BackingStoreException;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle;
import java.awt.LayoutManager;
import javax.swing.GroupLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Font;
import java.io.FilenameFilter;
import java.awt.Frame;
import java.awt.FileDialog;
import java.io.File;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.net.URL;
import javax.swing.JSeparator;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import java.util.prefs.Preferences;
import javax.swing.JFrame;

public class FileComparer extends JFrame
{
    private Preferences preferences;
    public static final String PREFS_FIRST_RUN = "PREFS_FIRST_RUN";
    public static final String PREFS_FRAME_X = "PREFS_FRAME_X";
    public static final String PREFS_FRAME_Y = "PREFS_FRAME_Y";
    public static final String PREFS_SOURCE_FILE_PATH = "PREFS_SOURCE_FILE_PATH";
    public static final String PREFS_OUTPUT_FILE_PATH = "PREFS_OUTPUT_FILE_PATH";
    public static final ImageIcon START_ICON;
    public static final ImageIcon STOP_ICON;
    public static final String DEFAULT_ENCODING = "UTF-8";
    private static String sourceFileEncoding;
    private JFileChooser jFileChooser;
    private Worker worker;
    private float progressRatio;
    private long allCurrentDoneWorkVolume;
    private Settings settingsJFrame;
    private static final String SETTINGS_ITEM_HEADER = "Settings";
    private static final String SETTINGS_ITEM_TOOLTIP = "Open settings window";
    private static final String QUIT_ITEM_HEADER = "Quit";
    private String pathToDataBase;
    private int urlColomnNumberInDataBaseStartFromZero;
    private int emailColomnNumberInDataBaseStartFromZero;
    private ArrayList<String> listOfTheFiles;
    protected JButton chooseSourceButton;
    private JTextField emailColomnNumberJTextField;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel numberOfFilesJLabel;
    private JProgressBar progressBar;
    protected JTextField sourceFile;
    protected JButton startStopButton;
    private JLabel statisticsJLabel;
    private JTextField urlColomnNumberJTextField;
    private int emailColomnIndex;
    private int urlSourceColomnIndex;
    
    public void calculateProgressRatio(final float allWorkVolume) {
        this.resetProgress();
        this.progressRatio = 100.0f / allWorkVolume;
    }
    
    public void updateProgress(final long doneNowPartOfWork) {
        this.allCurrentDoneWorkVolume += doneNowPartOfWork;
        final int prograss = (int)(this.progressRatio * this.allCurrentDoneWorkVolume);
        this.progressBar.setValue(prograss);
    }
    
    public void progressSetDone() {
        this.progressBar.setValue(100);
    }
    
    public void resetProgress() {
        this.progressRatio = 0.0f;
        this.progressBar.setValue(0);
        this.allCurrentDoneWorkVolume = 0L;
    }
    
    public FileComparer() {
        this.preferences = Preferences.userNodeForPackage(FileComparer.class);
        this.jFileChooser = new JFileChooser();
        this.progressRatio = 0.0f;
        this.allCurrentDoneWorkVolume = 0L;
        this.settingsJFrame = new Settings(this);
        this.pathToDataBase = null;
        this.urlColomnNumberInDataBaseStartFromZero = -1;
        this.emailColomnNumberInDataBaseStartFromZero = -1;
        this.listOfTheFiles = new ArrayList<String>();
        this.emailColomnIndex = -1;
        this.urlSourceColomnIndex = -1;
        this.initComponents();
        final JMenuBar jMenuBar = new JMenuBar();
        final JMenu mainMenu = new JMenu("Main");
        final JMenuItem aboutMenuItem = new JMenuItem("Settings");
        menuFiller(mainMenu, aboutMenuItem, "Open settings window", 83);
        aboutMenuItem.addActionListener(evt -> this.openSettingsWindow(false));
        mainMenu.add(new JSeparator());
        final JMenuItem quitMenuItem = new JMenuItem("Quit");
        menuFiller(mainMenu, quitMenuItem, "Quit", 81);
        quitMenuItem.addActionListener(evt -> this.exit());
        jMenuBar.add(mainMenu);
        this.setJMenuBar(jMenuBar);
        if (this.preferences.getBoolean("PREFS_FIRST_RUN", true)) {
            this.setLocationByPlatform(true);
            this.preferences.putBoolean("PREFS_FIRST_RUN", false);
        }
        else {
            this.setLocation(this.preferences.getInt("PREFS_FRAME_X", 0), this.preferences.getInt("PREFS_FRAME_Y", 0));
        }
        this.progressBar.setValue(0);
        this.pack();
        this.numberOfFilesJLabel.setVisible(false);
        this.setVisible(true);
        this.enableStartButtonWithCheck();
        this.updateFileCounter("0 from 0");
    }
    
    private static void menuFiller(final JMenu mainMenu, final JMenuItem menuItem, final String itemTooltip, final int keyEventVK) {
        menuFillerAlt(mainMenu, menuItem, itemTooltip, keyEventVK, 0);
    }
    
    private static void menuFillerAlt(final JMenu mainMenu, final JMenuItem menuItem, final String itemTooltip, final int keyEventVK, final int actionEvent) {
        mainMenu.add(menuItem);
        menuItem.setMnemonic(keyEventVK);
        final KeyStroke keyStroke = KeyStroke.getKeyStroke(keyEventVK, actionEvent);
        menuItem.setAccelerator(keyStroke);
        menuItem.setToolTipText(itemTooltip);
    }
    
    private void enableStartButtonWithCheck() {
        if (this.isDatabaseFileConnected()) {
            final boolean isSourceFileConnected = this.isSourceFileConnected();
            final boolean isDatabaseFileConnected = this.isDatabaseFileConnected();
            this.startStopButton.setEnabled(isSourceFileConnected && isDatabaseFileConnected);
        }
        else {
            this.startStopButton.setEnabled(false);
            this.openSettingsWindow(true);
        }
    }
    
    private void openSettingsWindow(final boolean forDataBaseupdate) {
        this.settingsJFrame.openSettingsForSelectNewDatabaseFile(forDataBaseupdate);
    }
    
    private boolean isSourceFileConnected() {
        final String pathToSource = this.sourceFile.getText();
        if (pathToSource.trim().isEmpty()) {
            return false;
        }
        final File testFile = new File(pathToSource);
        return testFile.exists();
    }
    
    private boolean isDatabaseFileConnected() {
        this.pathToDataBase = this.preferences.get("PREFS_DATABASE_FILE", "");
        if (this.pathToDataBase.isEmpty()) {
            return false;
        }
        final File testFile = new File(this.pathToDataBase);
        return testFile.exists() && this.isDatabaseColomnsDetected();
    }
    
    private boolean isDatabaseColomnsDetected() {
        final String tempUrlColomnStringStartFromOne = this.preferences.get("URL_COLOMN_NUMBER", "");
        final String tempEmailColomnStringStartFromOne = this.preferences.get("E_MAIL_COLOMN_NUMBER", "");
        if (tempUrlColomnStringStartFromOne.isEmpty() || tempEmailColomnStringStartFromOne.isEmpty()) {
            return false;
        }
        this.emailColomnNumberInDataBaseStartFromZero = Integer.parseInt(tempEmailColomnStringStartFromOne) - 1;
        this.urlColomnNumberInDataBaseStartFromZero = Integer.parseInt(tempUrlColomnStringStartFromOne) - 1;
        return true;
    }
    
    private File[] chooseFileOrFiles() {
        if (System.getProperty("os.name", "").contains("Mac")) {
            final FileDialog fileDialog = new FileDialog(this);
            fileDialog.setFilenameFilter(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return Utilities.isValidFileFormatTxtOrCSV(name) || Utilities.isValidFileFormatXLS(name) || Utilities.isValidFileFormatXLSX(name);
                }
            });
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            fileDialog.setMultipleMode(false);
            fileDialog.setVisible(true);
            final File[] files = fileDialog.getFiles();
            return files;
        }
        if (this.jFileChooser.showOpenDialog(this) == 0) {
            final File[] files2 = { this.jFileChooser.getSelectedFile() };
            return files2;
        }
        return null;
    }
    
    public String getPathToDataBase() {
        return this.pathToDataBase;
    }
    
    public int getUrlColomnNumberInDataBaseStartFromZero() {
        return this.urlColomnNumberInDataBaseStartFromZero;
    }
    
    public int getEmailColomnNumberInDataBaseStartFromZero() {
        return this.emailColomnNumberInDataBaseStartFromZero;
    }
    
    public void returnFromsettings() {
        this.enableStartButtonWithCheck();
    }
    
    public void enableStartButtonWithSourceCheck() {
        final boolean isSourceFileConnected = this.isSourceFileConnected();
        final boolean isDatabaseFileConnected = this.isDatabaseFileConnected();
        this.startStopButton.setEnabled(isSourceFileConnected && isDatabaseFileConnected);
    }
    
    private void initComponents() {
        this.chooseSourceButton = new JButton();
        this.startStopButton = new JButton();
        this.sourceFile = new JTextField();
        this.jLabel2 = new JLabel();
        this.progressBar = new JProgressBar();
        this.numberOfFilesJLabel = new JLabel();
        this.jLabel1 = new JLabel();
        this.emailColomnNumberJTextField = new JTextField();
        this.jLabel3 = new JLabel();
        this.urlColomnNumberJTextField = new JTextField();
        this.statisticsJLabel = new JLabel();
        this.setDefaultCloseOperation(0);
        this.setTitle("FileComparer");
        this.setFont(new Font("Dialog", 0, 11));
        this.setLocationByPlatform(true);
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent evt) {
                FileComparer.this.formWindowClosed(evt);
            }
            
            @Override
            public void windowClosing(final WindowEvent evt) {
                FileComparer.this.formWindowClosing(evt);
            }
        });
        this.chooseSourceButton.setFont(new Font("Dialog", 0, 11));
        this.chooseSourceButton.setIcon(new ImageIcon(this.getClass().getResource("/folder.png")));
        this.chooseSourceButton.setText("Choose...");
        this.chooseSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                FileComparer.this.chooseSourceButtonActionPerformed(evt);
            }
        });
        this.startStopButton.setFont(new Font("Dialog", 0, 11));
        this.startStopButton.setIcon(new ImageIcon(this.getClass().getResource("/start.png")));
        this.startStopButton.setText("Start");
        this.startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                FileComparer.this.startStopButtonActionPerformed(evt);
            }
        });
        this.sourceFile.setEditable(false);
        this.sourceFile.setColumns(40);
        this.sourceFile.setFont(new Font("Dialog", 0, 11));
        this.sourceFile.setToolTipText("");
        this.jLabel2.setFont(new Font("Dialog", 0, 12));
        this.jLabel2.setHorizontalAlignment(11);
        this.jLabel2.setText("Source file:");
        this.jLabel2.setToolTipText("");
        this.numberOfFilesJLabel.setFont(new Font("Dialog", 0, 12));
        this.numberOfFilesJLabel.setHorizontalAlignment(4);
        this.numberOfFilesJLabel.setText(" ");
        this.jLabel1.setFont(new Font("Dialog", 0, 12));
        this.jLabel1.setText("E-mail colomn number:");
        this.emailColomnNumberJTextField.setColumns(2);
        this.emailColomnNumberJTextField.setHorizontalAlignment(4);
        this.jLabel3.setFont(new Font("Dialog", 0, 12));
        this.jLabel3.setText("URLs colomn number:");
        this.urlColomnNumberJTextField.setColumns(2);
        this.urlColomnNumberJTextField.setHorizontalAlignment(4);
        this.statisticsJLabel.setFont(new Font("Dialog", 0, 12));
        this.statisticsJLabel.setText("Found ? ( ?% ) new URLs, ? ( ?% ) new e-mails");
        this.statisticsJLabel.setSize(new Dimension(0, 15));
        final GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(this.jLabel2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.sourceFile).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)).addGroup(layout.createSequentialGroup().addComponent(this.progressBar, -1, -1, 32767).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.numberOfFilesJLabel).addGap(9, 9, 9))).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(this.startStopButton, -1, -1, 32767).addComponent(this.chooseSourceButton, -1, -1, 32767))).addGroup(layout.createSequentialGroup().addComponent(this.jLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.emailColomnNumberJTextField, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(this.jLabel3).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.urlColomnNumberJTextField, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(this.statisticsJLabel, -1, 410, 32767))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.chooseSourceButton).addComponent(this.sourceFile, -2, -1, -2).addComponent(this.jLabel2)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.startStopButton).addComponent(this.numberOfFilesJLabel)).addComponent(this.progressBar, -1, -1, 32767)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.emailColomnNumberJTextField, -2, -1, -2).addComponent(this.jLabel1).addComponent(this.jLabel3).addComponent(this.urlColomnNumberJTextField, -2, -1, -2).addComponent(this.statisticsJLabel)).addContainerGap()));
        this.pack();
        this.setLocationRelativeTo(null);
    }
    
    private void formWindowClosing(final WindowEvent evt) {
        if (this.worker != null && this.worker.isAlive()) {
            if (JOptionPane.showConfirmDialog(this.getRootPane(), "The task is still active! Are you sure you want to quit?", "Warning", 0, 2) == 0) {
                this.worker.interrupt();
                this.exit();
            }
        }
        else {
            this.exit();
        }
    }
    
    private void formWindowClosed(final WindowEvent evt) {
        this.savePreferences();
    }
    
    private void savePreferences() {
        this.preferences.putInt("PREFS_FRAME_X", this.getX());
        this.preferences.putInt("PREFS_FRAME_Y", this.getY());
        this.preferences.put("PREFS_SOURCE_FILE_PATH", "");
        this.preferences.put("PREFS_OUTPUT_FILE_PATH", "");
        try {
            this.preferences.flush();
        }
        catch (BackingStoreException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public void updateStatistics(final String string) {
        this.statisticsJLabel.setText(string);
    }
    
    public void resetStatistics() {
        this.statisticsJLabel.setText("Found 0 ( 0% ) new URLs, 0 ( 0% ) new e-mails");
    }
    
    private void startStopButtonActionPerformed(final ActionEvent evt) {
        this.savePreferences();
        if (evt.getActionCommand().equals("Start")) {
            final boolean isEmailColomnOk = this.emailColomnOk(false);
            final boolean isUrlColomnOk = this.urlColomnOk(false);
            if (isEmailColomnOk || isUrlColomnOk) {
                this.resetStatistics();
                (this.worker = new Worker(this, this.settingsJFrame)).start();
            }
            else {
                JOptionPane.showMessageDialog(this, "Probably there is an error in the numbers of URL and E-mail colomns.");
            }
        }
        else {
            this.worker.interrupt();
        }
    }
    
    public ArrayList<String> getListOfTheFiles() {
        return this.listOfTheFiles;
    }
    
    private void chooseSourceButtonActionPerformed(final ActionEvent evt) {
        final File[] files = this.chooseFileOrFiles();
        if (files != null && files.length != 0) {
            if (files.length == 1) {
                this.setSourceFilePathText(files[0].getAbsolutePath());
            }
            else {
                this.setSourceFilePathText(files[0].getParent());
            }
            this.listOfTheFiles.clear();
            for (final File file : files) {
                final String fileName = file.getName();
                if ((fileName.endsWith(".csv") && !fileName.endsWith(".csv_OUTPUT")) || (fileName.endsWith(".txt") && !fileName.endsWith("_LOG.txt")) || fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                    this.listOfTheFiles.add(file.getAbsolutePath());
                }
            }
            if (!this.listOfTheFiles.isEmpty()) {
                this.prepareUI();
            }
        }
    }
    
    private void setSourceFilePathText(final String path) {
        this.sourceFile.setText(path);
    }
    
    protected void updateFileCounter(final String string) {
        this.numberOfFilesJLabel.setText(string);
    }
    
    private void prepareUI() {
        this.updateFileCounter("0 from " + this.listOfTheFiles.size());
        this.resetProgress();
        this.resetStatistics();
        this.colomnsConnect();
        this.enableStartButtonWithCheck();
    }
    
    private void colomnsConnect() {
        Utilities.searchIndexesInSourceFile(this.listOfTheFiles.get(0));
        if (Utilities.getUrlIndexStartFromZero() == Utilities.getEmailIndexStartFromZero()) {
            this.urlColomnNumberJTextField.setText("");
            this.emailColomnNumberJTextField.setText("");
            JOptionPane.showMessageDialog(this, "Not able to detect the columns with emails and URLs. Set column ID manually if needed");
        }
        else if (Utilities.getUrlIndexStartFromZero() == -1) {
            this.emailColomnNumberJTextField.setText(Utilities.getEmailIndexStringStartFromOne());
            this.urlColomnNumberJTextField.setText("");
            JOptionPane.showMessageDialog(this, "Not able to detect the column with URLs. Set column ID manually if needed");
        }
        else if (Utilities.getEmailIndexStartFromZero() == -1) {
            this.urlColomnNumberJTextField.setText(Utilities.getUrlIndexStringStartFromOne());
            this.emailColomnNumberJTextField.setText("");
            JOptionPane.showMessageDialog(this, "Not able to detect the column with emails. Set column ID manually if needed");
        }
        else {
            this.urlColomnNumberJTextField.setText(Utilities.getUrlIndexStringStartFromOne());
            this.emailColomnNumberJTextField.setText(Utilities.getEmailIndexStringStartFromOne());
        }
    }
    
    private void exit() {
        this.settingsJFrame.dispose();
        this.dispose();
    }
    
    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                final FontUIResource dialogFont = new FontUIResource(new Font("Dialog", 0, 11));
                UIManager.put("Button.font", dialogFont);
                UIManager.put("ColorChooser.font", dialogFont);
                UIManager.put("ComboBox.font", dialogFont);
                UIManager.put("Label.font", dialogFont);
                UIManager.put("List.font", dialogFont);
                UIManager.put("OptionPane.buttonFont", dialogFont);
                UIManager.put("OptionPane.messageFont", dialogFont);
                UIManager.put("Menu.font", dialogFont);
                UIManager.put("MenuItem.font", dialogFont);
                UIManager.put("RadioButtonMenuItem.font", dialogFont);
                UIManager.put("TableHeader.font", dialogFont);
                UIManager.put("TextField.font", dialogFont);
                UIManager.put("ToolTip.font", dialogFont);
                final FileComparer fileComparer = new FileComparer();
            }
        });
    }
    
    void enableUI() {
        this.startStopButton.setText("Start");
        this.startStopButton.setIcon(FileComparer.START_ICON);
        this.chooseSourceButton.setEnabled(true);
    }
    
    void disableUI() {
        this.startStopButton.setText("Stop");
        this.startStopButton.setIcon(FileComparer.STOP_ICON);
        this.chooseSourceButton.setEnabled(false);
    }
    
    public int getEmailColomnNumber() {
        return this.emailColomnIndex;
    }
    
    private boolean emailColomnOk(final boolean showMessage) {
        final String colomnNumberString = this.emailColomnNumberJTextField.getText().trim();
        if (colomnNumberString.length() > 0) {
            if (StringUtils.isNumeric(colomnNumberString)) {
                this.emailColomnIndex = Integer.parseInt(this.emailColomnNumberJTextField.getText().trim()) - 1;
                return true;
            }
            if (colomnNumberString.length() < 2) {
                final char colomnNumberChar = colomnNumberString.trim().toLowerCase().charAt(0);
                this.emailColomnIndex = colomnNumberChar - '`';
                if (this.emailColomnIndex > 1 && this.emailColomnIndex < 27) {
                    --this.emailColomnIndex;
                    return true;
                }
                if (showMessage) {
                    JOptionPane.showMessageDialog(this, "E-mail colomn number error: Invalid format, please enter a number or letter of the Latin alphabet.");
                }
            }
            else if (showMessage) {
                JOptionPane.showMessageDialog(this, "E-mail colomn number error: Only the length of one symbol is supported.");
            }
        }
        else if (showMessage) {
            JOptionPane.showMessageDialog(this, "E-mail colomn number warning: Empty column number field.");
        }
        return false;
    }
    
    public int getURLColomnNumber() {
        return this.urlSourceColomnIndex;
    }
    
    private boolean urlColomnOk(final boolean showMessage) {
        final String colomnNumberString = this.urlColomnNumberJTextField.getText().trim();
        if (colomnNumberString.length() > 0) {
            if (StringUtils.isNumeric(colomnNumberString)) {
                this.urlSourceColomnIndex = Integer.parseInt(this.urlColomnNumberJTextField.getText().trim()) - 1;
                return true;
            }
            if (colomnNumberString.length() < 2) {
                final char colomnNumberChar = colomnNumberString.trim().toLowerCase().charAt(0);
                this.urlSourceColomnIndex = colomnNumberChar - '`';
                if (this.urlSourceColomnIndex > 1 && this.urlSourceColomnIndex < 27) {
                    --this.urlSourceColomnIndex;
                    return true;
                }
                if (showMessage) {
                    JOptionPane.showMessageDialog(this, "URL colomn number error: Invalid format, please enter a number or letter of the Latin alphabet.");
                }
            }
            else if (showMessage) {
                JOptionPane.showMessageDialog(this, "URL colomn number error: Only the length of one symbol is supported.");
            }
        }
        else if (showMessage) {
            JOptionPane.showMessageDialog(this, "URL colomn number warning: Empty column number field.");
        }
        return false;
    }
    
    static {
        START_ICON = new ImageIcon(FileComparer.class.getResource("/start.png"));
        STOP_ICON = new ImageIcon(FileComparer.class.getResource("/stop.png"));
      
        FileComparer.sourceFileEncoding = "UTF-8";
    }
}
