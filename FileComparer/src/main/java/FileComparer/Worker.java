// 
// Decompiled by Procyon v0.5.36
// 

package FileComparer;

import java.io.Writer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedInputStream;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.io.ByteOrderMark;
import java.io.FileInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;
import java.awt.Component;
import javax.swing.JOptionPane;
import java.util.HashSet;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Set;
import java.util.prefs.Preferences;

public class Worker extends Thread
{
    String charsetName;
    public static final String LINE_SEPARATOR;
    public static final String COMMENT_START_SYMBOL = "#";
    public static String colomnSeparatorInputFile;
    public static final String TAB_SYMBOL = "\t";
    public static final String COMMA_SYMBOL = ",";
    private String colomnSeparatorForOutputFiles;
    public static final String CSV_FILE_ENDING = ".csv";
    public static final String TXT_FILE_ENDING = ".txt";
    public static final String XLS_FILE_ENDING = ".xls";
    public static final String XLSX_FILE_ENDING = ".xlsx";
    private static final String outputFileEnding = ".csv";
    public static final String OUTPUT_MARK = "_OUTPUT";
    public static final String outputFileMarkWithEnding = ".csv_OUTPUT";
    public static final String LOG_MARK = "_LOG";
    public static final String logFileMarkWithEnding = "_LOG.txt";
    private Preferences preferences;
    private final FileComparer fileComparer;
    private int urlIndexInDatabaseStartFromZero;
    private int emailIndexInDatabaseStartFromZero;
    private int urlIndexInSource;
    private int emailIndexInSource;
    private String outPrefix;
    private String outputFileFullPath;
    private int numberOfNewURLs;
    private int numberOfAllURLs;
    private int numberOfNewEmails;
    private int numberOfAllEmails;
    private Set<String> allDatabaseEmails;
    private Set<String> allDatabaseURLs;
    private int numberOfLinesInAllSourceFiles;
    private int numberOfLinesWritenToOutputFile;
    private int numberOfSkippedErrorsLines;
    private final String errorMessagePullingDataFromLine = "Error pulling data from new source data line: ";
    int lineNumberInCurrentFile;
    private BufferedWriter bufferedWriterLog;
    private FileWriter fileWriterLog;
    private FileWriter fileWriterOutput;
    private BufferedWriter bufferedWriterOutput;
    private String titleForOutputFile;
    
    public Worker(final FileComparer mainJFrame, final Settings settingsJFrame) {
        this.colomnSeparatorForOutputFiles = ",";
        this.preferences = Preferences.userNodeForPackage(Worker.class);
        this.urlIndexInDatabaseStartFromZero = -1;
        this.emailIndexInDatabaseStartFromZero = -1;
        this.urlIndexInSource = -1;
        this.emailIndexInSource = -1;
        this.allDatabaseEmails = new HashSet<String>();
        this.allDatabaseURLs = new HashSet<String>();
        this.lineNumberInCurrentFile = 0;
        this.bufferedWriterLog = null;
        this.fileWriterLog = null;
        this.fileWriterOutput = null;
        this.bufferedWriterOutput = null;
        this.fileComparer = mainJFrame;
        this.urlIndexInDatabaseStartFromZero = this.fileComparer.getUrlColomnNumberInDataBaseStartFromZero();
        this.emailIndexInDatabaseStartFromZero = this.fileComparer.getEmailColomnNumberInDataBaseStartFromZero();
        this.urlIndexInSource = this.fileComparer.getURLColomnNumber();
        this.emailIndexInSource = this.fileComparer.getEmailColomnNumber();
    }
    
    @Override
    public void run() {
        this.fileComparer.disableUI();
        final ArrayList<String> listOfTheFiles = this.fileComparer.getListOfTheFiles();
        if (listOfTheFiles.isEmpty()) {
            final String message = "For some reason, the list of source data files was empty!";
            System.err.println(message);
        }
        else {
            this.outPrefix = listOfTheFiles.get(0);
            if (this.outPrefix.lastIndexOf(".") != -1) {
                this.outPrefix = this.outPrefix.substring(0, this.outPrefix.lastIndexOf("."));
            }
            else {
                final String message = "Not detected dot in end of the file name...";
                System.err.println(message);
                JOptionPane.showMessageDialog(this.fileComparer, message);
                System.exit(-1);
            }
            this.outputFileFullPath = this.outPrefix + "_OUTPUT" + ".csv";
            this.readDatabase();
            this.numberOfLinesInAllSourceFiles = 0;
            this.numberOfLinesWritenToOutputFile = 0;
            int nextFileIndex = 0;
            for (final String fileName : listOfTheFiles) {
                ++nextFileIndex;
                final String string = nextFileIndex + " from " + listOfTheFiles.size();
                this.fileComparer.updateFileCounter(string);
                this.processData(fileName);
            }
        }
        this.closeWriterOutput();
        this.printAndWriteToLog("Number of data lines writen to OUTPUT file: " + this.numberOfLinesWritenToOutputFile);
        this.printAndWriteToLog("Number of data lines skipped due to errors: " + this.numberOfSkippedErrorsLines);
        this.calculateAndUpdateStatistics();
        this.closeWriterLog();
        this.fileComparer.progressSetDone();
        this.fileComparer.enableUI();
    }
    
    private void calculateAndUpdateStatistics() {
        final int percentOfNewURLs = (int)(100.0f * this.numberOfNewURLs / this.numberOfAllURLs);
        final int percentOfNewEmails = (int)(100.0f * this.numberOfNewEmails / this.numberOfAllEmails);
        final String string = "Found " + this.numberOfNewURLs + " ( " + percentOfNewURLs + "% ) new URLs, " + this.numberOfNewEmails + " ( " + percentOfNewEmails + "% ) new e-mails";
        this.printAndWriteToLog(string);
        this.printAndWriteToLog(Worker.LINE_SEPARATOR + Utilities.dateAndTimeISO());
        this.fileComparer.updateStatistics(string);
    }
    
    private void readDatabase() {
        final Parcer parcer = new Parcer();
        final String databaseFilePath = this.fileComparer.getPathToDataBase();
        this.printAndWriteToLog(Worker.LINE_SEPARATOR + "Read database file: " + databaseFilePath);
        try {
            final File file = new File(databaseFilePath);
            final InputStream inputStream = new FileInputStream(file);
            final BOMInputStream bOMInputStream = new BOMInputStream(inputStream, new ByteOrderMark[] { ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE });
            final ByteOrderMark bom = bOMInputStream.getBOM();
            this.charsetName = ((bom == null) ? "UTF-8" : bom.getCharsetName());
            final InputStreamReader inputStreamReader = new InputStreamReader(new BufferedInputStream(bOMInputStream), this.charsetName);
            final BufferedReader reader = new BufferedReader(inputStreamReader);
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null && !this.isInterrupted()) {
                ++lineNumber;
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }
                try {
                    final String[] parts = parcer.parce(line);
                    if (this.emailIndexInDatabaseStartFromZero < parts.length) {
                        final String email = parts[this.emailIndexInDatabaseStartFromZero].trim().toLowerCase();
                        this.allDatabaseEmails.add(email);
                    }
                    else {
                        System.err.println("Line without E-MAIL in DATABASE: " + line);
                    }
                    if (this.urlIndexInDatabaseStartFromZero < parts.length) {
                        final String url = parts[this.urlIndexInDatabaseStartFromZero].trim().toLowerCase();
                        this.allDatabaseURLs.add(url);
                    }
                    else {
                        System.err.println("Line without URL in DATABASE: " + line);
                    }
                }
                catch (Exception e) {
                    final StringBuilder stringBuilder = new StringBuilder("Error reading the database in source line: ");
                    stringBuilder.append(line);
                    stringBuilder.append(Worker.LINE_SEPARATOR);
                    stringBuilder.append("Line number: ");
                    stringBuilder.append(Integer.toString(lineNumber));
                    stringBuilder.append(Worker.LINE_SEPARATOR);
                    stringBuilder.append("Error description: ");
                    stringBuilder.append(e.getMessage());
                    stringBuilder.append(Worker.LINE_SEPARATOR);
                    stringBuilder.append("File: ");
                    stringBuilder.append(databaseFilePath);
                    stringBuilder.append(Worker.LINE_SEPARATOR);
                    final String errorMessageString = stringBuilder.toString();
                    this.printAndWriteToLog(errorMessageString);
                }
            }
            this.printAndWriteToLog(Worker.LINE_SEPARATOR + "Database read to RAM, number of lines: " + lineNumber);
            reader.close();
        }
        catch (IOException e2) {
            System.err.println(e2.getMessage());
            JOptionPane.showMessageDialog(this.fileComparer, "There was a problem during reading the database" + Worker.LINE_SEPARATOR + Worker.LINE_SEPARATOR + "Details:" + Worker.LINE_SEPARATOR + e2.getMessage(), "Error", 0);
            this.stopIt();
        }
    }
    
    private void processData(final String filePath) {
        this.printAndWriteToLog(Worker.LINE_SEPARATOR + "Start process source file: " + filePath + Worker.LINE_SEPARATOR);
        final String fileEnding2;
        final String fileEnding = fileEnding2 = Utilities.fileEnding(filePath);
        switch (fileEnding2) {
            case ".txt": {
                this.processCsvFile(filePath);
                break;
            }
            case ".csv": {
                this.processCsvFile(filePath);
                break;
            }
            case ".xls": {
                this.processXlsOrXlsxFile(filePath, false);
                break;
            }
            case ".xlsx": {
                this.processXlsOrXlsxFile(filePath, true);
                break;
            }
        }
    }
    
    private void processCsvFile(final String filePath) {
        final Parcer parcer = new Parcer();
        final long numberOfLinesInFile = this.numberOfLinesInFile(filePath);
        this.fileComparer.calculateProgressRatio((float)numberOfLinesInFile);
        try {
            final File file = new File(filePath);
            final InputStream inputStream = new FileInputStream(file);
            final BOMInputStream bOMInputStream = new BOMInputStream(inputStream, new ByteOrderMark[] { ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE });
            final ByteOrderMark bom = bOMInputStream.getBOM();
            this.charsetName = ((bom == null) ? "UTF-8" : bom.getCharsetName());
            final InputStreamReader inputStreamReader = new InputStreamReader(new BufferedInputStream(bOMInputStream), this.charsetName);
            final BufferedReader reader = new BufferedReader(inputStreamReader);
            this.lineNumberInCurrentFile = 0;
            String line;
            while ((line = reader.readLine()) != null && !this.isInterrupted()) {
                ++this.lineNumberInCurrentFile;
                ++this.numberOfLinesInAllSourceFiles;
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }
                if (this.numberOfLinesInAllSourceFiles == 1 && Utilities.isTableTitle(line)) {
                    this.titleForOutputFile = line;
                    ++this.numberOfLinesWritenToOutputFile;
                }
                else {
                    final String[] parts = parcer.parce(line);
                    this.processLineFromFilePath(line, parts, filePath);
                }
            }
            reader.close();
            this.printAndWriteToLog(Worker.LINE_SEPARATOR + "Source data file processed, number of lines: " + this.lineNumberInCurrentFile);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            JOptionPane.showMessageDialog(this.fileComparer, "There was a problem during process source datafile" + Worker.LINE_SEPARATOR + Worker.LINE_SEPARATOR + "Details:" + Worker.LINE_SEPARATOR + e.getMessage(), "Error", 0);
            this.stopIt();
        }
    }
    
    private void processXlsOrXlsxFile(final String filePath, final Boolean isXLSX) {
        this.lineNumberInCurrentFile = 0;
        try {
            final File inputFile = new File(filePath);
            XSSFWorkbook workbookXSSF = null;
            HSSFWorkbook workbookHSSF = null;
            int numberOfSheets;
            if (isXLSX) {
                workbookXSSF = new XSSFWorkbook(new FileInputStream(inputFile));
                numberOfSheets = workbookXSSF.getNumberOfSheets();
                workbookXSSF.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            }
            else {
                workbookHSSF = new HSSFWorkbook(new FileInputStream(inputFile));
                numberOfSheets = workbookHSSF.getNumberOfSheets();
                workbookHSSF.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            }
            for (int index = numberOfSheets - 1; index >= 0; --index) {
                Iterator<Row> rowIterator;
                if (isXLSX) {
                    final XSSFSheet sheetXSSF = workbookXSSF.getSheetAt(index);
                    rowIterator = sheetXSSF.iterator();
                }
                else {
                    final HSSFSheet sheetHSSF = workbookHSSF.getSheetAt(index);
                    rowIterator = sheetHSSF.iterator();
                }
                while (rowIterator.hasNext()) {
                    final Row row = rowIterator.next();
                    final ArrayList<String> cells = new ArrayList<String>();
                    for (int i = 0; i < row.getLastCellNum(); ++i) {
                        final Cell cell = row.getCell(i);
                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case BOOLEAN: {
                                    String cellString = Boolean.toString(cell.getBooleanCellValue());
                                    cellString = cellString.trim();
                                    cells.add(cellString);
                                    break;
                                }
                                case NUMERIC: {
                                    String cellString = Double.toString(cell.getNumericCellValue());
                                    cellString = cellString.trim();
                                    cells.add(cellString);
                                    break;
                                }
                                case STRING: {
                                    String cellString = cell.getStringCellValue();
                                    cellString = cellString.trim();
                                    cells.add(cellString);
                                    break;
                                }
                                case BLANK: {
                                    cells.add("");
                                    break;
                                }
                                default: {
                                    String cellString = cell.toString();
                                    cellString = cellString.trim();
                                    cells.add(cellString);
                                    break;
                                }
                            }
                        }
                        else {
                            cells.add("");
                        }
                    }
                    String[] parts = new String[cells.size()];
                    parts = cells.toArray(parts);
                    cells.clear();
                    final String line = Parcer.csvString(parts);
                    ++this.lineNumberInCurrentFile;
                    ++this.numberOfLinesInAllSourceFiles;
                    if (line.isEmpty()) {
                        System.err.println("Empty line in method processXlsOrXlsxFile()");
                    }
                    else if (this.numberOfLinesInAllSourceFiles == 1 && Utilities.isTableTitle(line)) {
                        this.titleForOutputFile = line;
                    }
                    else {
                        this.processLineFromFilePath(line, parts, filePath);
                    }
                }
            }
            this.printAndWriteToLog(Worker.LINE_SEPARATOR + "Source data file processed, number of lines: " + this.lineNumberInCurrentFile);
        }
        catch (IOException ex) {
            System.err.println("Error in process XLS file: " + ex.getMessage());
            JOptionPane.showMessageDialog(this.fileComparer, "There was a problem during process source datafile" + Worker.LINE_SEPARATOR + Worker.LINE_SEPARATOR + "Details:" + Worker.LINE_SEPARATOR + ex.getMessage(), "Error", 0);
            this.stopIt();
        }
    }
    
    private void processLineFromFilePath(final String line, final String[] parts, final String filePath) {
        if (parts.length < 2 || !this.isMoreThanOneCellContainData(parts)) {
            ++this.numberOfSkippedErrorsLines;
            final StringBuilder stringBuilder = new StringBuilder("Error pulling data from new source data line: ");
            stringBuilder.append(line);
            stringBuilder.append(Worker.LINE_SEPARATOR);
            stringBuilder.append("Line number: ");
            stringBuilder.append(this.lineNumberInCurrentFile);
            stringBuilder.append(Worker.LINE_SEPARATOR);
            stringBuilder.append("Error description: perhaps the string doesn't contain useful data OR the CSV parser was unable to correctly split the string into parts!");
            stringBuilder.append(Worker.LINE_SEPARATOR);
            stringBuilder.append("File: ");
            stringBuilder.append(filePath);
            stringBuilder.append(Worker.LINE_SEPARATOR);
            final String errorMessageString = stringBuilder.toString();
            this.printAndWriteToLog(errorMessageString);
        }
        else {
            try {
                String url = null;
                int urlIndexInCurrentString = 0;
                boolean isContainUrl;
                if (this.urlIndexInSource < 0) {
                    isContainUrl = true;
                }
                else if (this.urlIndexInSource < parts.length) {
                    urlIndexInCurrentString = this.urlIndexInSource;
                    url = parts[this.urlIndexInSource];
                    url = url.trim().toLowerCase();
                    isContainUrl = this.allDatabaseURLs.contains(url);
                    ++this.numberOfAllURLs;
                }
                else {
                    urlIndexInCurrentString = Utilities.indexOfPartContainingURL(parts);
                    if (urlIndexInCurrentString != -1) {
                        url = parts[urlIndexInCurrentString];
                        url = url.trim().toLowerCase();
                        isContainUrl = this.allDatabaseURLs.contains(url);
                        ++this.numberOfAllURLs;
                    }
                    else {
                        isContainUrl = true;
                    }
                }
                String email = null;
                int emailIndexInCurrentString = 0;
                boolean isContainEmail;
                if (this.emailIndexInSource < 0) {
                    isContainEmail = true;
                }
                else if (this.emailIndexInSource < parts.length) {
                    emailIndexInCurrentString = this.emailIndexInSource;
                    email = parts[this.emailIndexInSource];
                    email = email.trim().toLowerCase();
                    isContainEmail = this.allDatabaseEmails.contains(email);
                    ++this.numberOfAllEmails;
                }
                else {
                    emailIndexInCurrentString = Utilities.indexOfPartContainingEmail(parts);
                    if (emailIndexInCurrentString != -1) {
                        email = parts[emailIndexInCurrentString];
                        email = email.trim().toLowerCase();
                        isContainEmail = this.allDatabaseEmails.contains(email);
                        ++this.numberOfAllEmails;
                    }
                    else {
                        isContainEmail = true;
                    }
                }
                if (urlIndexInCurrentString == -1 && emailIndexInCurrentString == -1) {
                    ++this.numberOfSkippedErrorsLines;
                    final StringBuilder stringBuilder2 = new StringBuilder("Error pulling data from new source data line: ");
                    stringBuilder2.append(line);
                    stringBuilder2.append(Worker.LINE_SEPARATOR);
                    stringBuilder2.append("Line number: ");
                    stringBuilder2.append(this.lineNumberInCurrentFile);
                    stringBuilder2.append(Worker.LINE_SEPARATOR);
                    stringBuilder2.append("Error description: URL and e-mail not detected in source line!");
                    stringBuilder2.append(Worker.LINE_SEPARATOR);
                    stringBuilder2.append("File: ");
                    stringBuilder2.append(filePath);
                    stringBuilder2.append(Worker.LINE_SEPARATOR);
                    final String errorMessageString2 = stringBuilder2.toString();
                    this.printAndWriteToLog(errorMessageString2);
                }
                else if (isContainEmail) {
                    if (!isContainUrl) {
                        this.allDatabaseURLs.add(url);
                        ++this.numberOfLinesWritenToOutputFile;
                        this.writeOutput(line);
                        ++this.numberOfNewURLs;
                    }
                }
                else {
                    if (isContainUrl) {
                        this.allDatabaseEmails.add(email);
                        ++this.numberOfNewEmails;
                    }
                    else {
                        this.allDatabaseURLs.add(url);
                        this.allDatabaseEmails.add(email);
                        ++this.numberOfNewURLs;
                        ++this.numberOfNewEmails;
                    }
                    ++this.numberOfLinesWritenToOutputFile;
                    this.writeOutput(line);
                }
            }
            catch (Exception e) {
                ++this.numberOfSkippedErrorsLines;
                final StringBuilder stringBuilder3 = new StringBuilder("Error pulling data from new source data line: ");
                stringBuilder3.append(line);
                stringBuilder3.append(Worker.LINE_SEPARATOR);
                stringBuilder3.append("Line number: ");
                stringBuilder3.append(Integer.toString(this.lineNumberInCurrentFile));
                stringBuilder3.append(Worker.LINE_SEPARATOR);
                stringBuilder3.append("Error description: ");
                stringBuilder3.append(e.getMessage());
                stringBuilder3.append(Worker.LINE_SEPARATOR);
                stringBuilder3.append("File: ");
                stringBuilder3.append(filePath);
                stringBuilder3.append(Worker.LINE_SEPARATOR);
                final String errorMessageString3 = stringBuilder3.toString();
                this.printAndWriteToLog(errorMessageString3);
            }
        }
        this.fileComparer.updateProgress(1L);
    }
    
    private boolean isMoreThanOneCellContainData(final String[] parts) {
        int number = 0;
        for (String part : parts) {
            part = part.replace("\"", "");
            if (!part.trim().isEmpty()) {
                if (number > 0) {
                    return true;
                }
                ++number;
            }
        }
        return number > 1;
    }
    
    private void printAndWriteToLog(final String errorMessageString) {
        System.err.println(errorMessageString);
        this.writeToLog(errorMessageString);
    }
    
    private void initWriterLog() {
        try {
            final String filePath = this.outPrefix + "_LOG.txt";
            this.fileWriterLog = new FileWriter(filePath, false);
            this.bufferedWriterLog = new BufferedWriter(this.fileWriterLog);
        }
        catch (IOException ex) {
            final String string = "Error init LOG file, reason: " + ex.getLocalizedMessage();
            System.err.println(string);
            JOptionPane.showMessageDialog(this.fileComparer, string);
        }
    }
    
    private void writeToLog(final String string) {
        if (this.fileWriterLog == null) {
            this.initWriterLog();
        }
        try {
            this.bufferedWriterLog.write(string + Worker.LINE_SEPARATOR);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            JOptionPane.showMessageDialog(this.fileComparer, "Cannot add new line to LOG file!" + Worker.LINE_SEPARATOR + Worker.LINE_SEPARATOR + "Details:" + Worker.LINE_SEPARATOR + e.getMessage(), "Error", 0);
        }
    }
    
    private void closeWriterLog() {
        try {
            if (this.bufferedWriterLog != null) {
                this.bufferedWriterLog.flush();
                this.bufferedWriterLog.close();
            }
        }
        catch (IOException ex) {
            System.err.println(ex.getMessage());
            JOptionPane.showMessageDialog(this.fileComparer, "The LOG file may not be closed correctly!" + Worker.LINE_SEPARATOR + Worker.LINE_SEPARATOR + "Details:" + Worker.LINE_SEPARATOR + ex.getMessage(), "Error", 0);
        }
    }
    
    private void initWriterOutput() {
        try {
            this.fileWriterOutput = new FileWriter(this.outputFileFullPath, false);
            this.bufferedWriterOutput = new BufferedWriter(this.fileWriterOutput);
            try {
                if (this.titleForOutputFile != null && !this.titleForOutputFile.isEmpty()) {
                    this.bufferedWriterOutput.write(this.titleForOutputFile + Worker.LINE_SEPARATOR);
                }
            }
            catch (IOException e) {
                final String string = "Error to write title to OUTPUT file, reason: " + e.getLocalizedMessage();
                System.err.println(string);
                JOptionPane.showMessageDialog(this.fileComparer, string);
            }
        }
        catch (IOException ex) {
            final String string = "Error init OUTPUT file, reason: " + ex.getLocalizedMessage();
            System.err.println(string);
            JOptionPane.showMessageDialog(this.fileComparer, string);
        }
    }
    
    private void closeWriterOutput() {
        try {
            if (this.bufferedWriterOutput != null) {
                this.bufferedWriterOutput.flush();
                this.bufferedWriterOutput.close();
            }
        }
        catch (IOException ex) {
            System.err.println(ex.getMessage());
            JOptionPane.showMessageDialog(this.fileComparer, "The OUTPUT CSV file may not be closed correctly!" + Worker.LINE_SEPARATOR + Worker.LINE_SEPARATOR + "Details:" + Worker.LINE_SEPARATOR + ex.getMessage(), "Error", 0);
        }
    }
    
    private void writeOutput(final String string) {
        if (this.fileWriterOutput == null) {
            this.initWriterOutput();
        }
        try {
            this.bufferedWriterOutput.write(string + Worker.LINE_SEPARATOR);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            JOptionPane.showMessageDialog(this.fileComparer, "Cannot add new line to source Data Dase CSV file" + Worker.LINE_SEPARATOR + Worker.LINE_SEPARATOR + "Details:" + Worker.LINE_SEPARATOR + e.getMessage(), "Error", 0);
        }
    }
    
    private void stopIt() {
        this.interrupt();
    }
    
    public long numberOfLinesInFile(final String filename) {
        try {
            final InputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
            try {
                final byte[] bytes = new byte[1024];
                int number = inputStream.read(bytes);
                if (number == -1) {
                    final long n = 0L;
                    inputStream.close();
                    return n;
                }
                int count = 0;
                while (number == 1024) {
                    int i = 0;
                    while (i < 1024) {
                        if (bytes[i++] == 10) {
                            ++count;
                        }
                    }
                    number = inputStream.read(bytes);
                }
                while (number != -1) {
                    for (int i = 0; i < number; ++i) {
                        if (bytes[i] == 10) {
                            ++count;
                        }
                    }
                    number = inputStream.read(bytes);
                }
                final long n2 = (count == 0) ? 1L : count;
                inputStream.close();
                return n2;
            }
            catch (Throwable t) {
                try {
                    inputStream.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            JOptionPane.showMessageDialog(this.fileComparer, "There was a problem during calculate string number in file: filename" + Worker.LINE_SEPARATOR + Worker.LINE_SEPARATOR + "Details:" + Worker.LINE_SEPARATOR + e.getMessage(), "Error", 0);
            this.stopIt();
            return -1L;
        }
    }
    
    static {
        LINE_SEPARATOR = System.getProperty("line.separator");
    }
}
