// 
// Decompiled by Procyon v0.5.36
// 

package FileComparer;

import java.time.temporal.TemporalAccessor;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.io.ByteOrderMark;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import java.util.Iterator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import java.io.InputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.File;

public class Utilities
{
    private static final String[] emailColomnNameVariants;
    private static final String[] urlColomnNameVariants;
    private static int urlIndexStartFromZero;
    private static int emailIndexStartFromZero;
    private static String[] urlPortion;
    private static String[] urlExceptions;
    private static boolean isHasTitle;
    
    public static boolean isValidFileFormatTxtOrCSV(final String name) {
        final boolean result = name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith(".txt");
        return result;
    }
    
    public static boolean isValidFileFormatXLS(final String name) {
        final boolean result = name.toLowerCase().endsWith(".xls");
        return result;
    }
    
    public static boolean isValidFileFormatXLSX(final String name) {
        final boolean result = name.toLowerCase().endsWith(".xlsx");
        return result;
    }
    
    public static int getUrlIndexStartFromZero() {
        return Utilities.urlIndexStartFromZero;
    }
    
    public static String getUrlIndexStringStartFromOne() {
        return Integer.toString(Utilities.urlIndexStartFromZero + 1);
    }
    
    private static void searchIndexesInTitle(final String[] firstLineColomnsInOriginalCase) {
        resetIndexes();
        for (int i = 0; i < firstLineColomnsInOriginalCase.length; ++i) {
            final String colomnUrl = firstLineColomnsInOriginalCase[i];
            if (isEqualURLTitle(colomnUrl)) {
                Utilities.urlIndexStartFromZero = i;
                break;
            }
        }
        for (int i = 0; i < firstLineColomnsInOriginalCase.length; ++i) {
            final String colomnEmail = firstLineColomnsInOriginalCase[i];
            if (isEqualEmailTitle(colomnEmail)) {
                Utilities.emailIndexStartFromZero = i;
                break;
            }
        }
    }
    
    private static boolean isEqualURLTitle(String colomnUrl) {
        for (final String urlTitlesVariant : Utilities.urlColomnNameVariants) {
            colomnUrl = colomnUrl.trim();
            if (colomnUrl.equals(urlTitlesVariant)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isEqualEmailTitle(String colomnEmail) {
        for (final String emailTitlesVariant : Utilities.emailColomnNameVariants) {
            colomnEmail = colomnEmail.trim();
            if (colomnEmail.equals(emailTitlesVariant)) {
                return true;
            }
        }
        return false;
    }
    
    public static int getEmailIndexStartFromZero() {
        return Utilities.emailIndexStartFromZero;
    }
    
    public static String getEmailIndexStringStartFromOne() {
        return Integer.toString(Utilities.emailIndexStartFromZero + 1);
    }
    
    public static String fileEnding(final String filePath) {
        final int fileEndingIndex = filePath.lastIndexOf(46);
        String fileEnding = filePath.substring(fileEndingIndex, filePath.length());
        fileEnding = fileEnding.toLowerCase();
        return fileEnding;
    }
    
    private static String readFirstLine(final String filePath) {
        return readLine(filePath, false);
    }
    
    private static String readSecondLine(final String filePath) {
        return readLine(filePath, true);
    }
    
    private static String readLine(final String filePath, final boolean readSecondLine) {
        final String fileEnding = fileEnding(filePath);
        String firstLine = null;
        final String s = fileEnding;
        switch (s) {
            case ".txt": {
                firstLine = readLineInTxtOrCsvFile(filePath, readSecondLine);
                break;
            }
            case ".csv": {
                firstLine = readLineInTxtOrCsvFile(filePath, readSecondLine);
                break;
            }
            case ".xls": {
                firstLine = readLineInXlsOrXlsx(filePath, false, readSecondLine);
                break;
            }
            case ".xlsx": {
                firstLine = readLineInXlsOrXlsx(filePath, true, readSecondLine);
                break;
            }
        }
        return firstLine;
    }
    
    private static String readLineInXlsOrXlsx(final String filePath, final Boolean isXLSX, final boolean readSecondLine) {
        try {
            final File inputFile = new File(filePath);
            XSSFWorkbook workbookXSSF = null;
            HSSFWorkbook workbookHSSF = null;
            if (isXLSX) {
                workbookXSSF = new XSSFWorkbook(new FileInputStream(inputFile));
                workbookXSSF.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            }
            else {
                workbookHSSF = new HSSFWorkbook(new FileInputStream(inputFile));
                workbookHSSF.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            }
            Iterator<Row> rowIterator;
            if (isXLSX) {
                final XSSFSheet sheetXSSF = workbookXSSF.getSheetAt(0);
                rowIterator = sheetXSSF.iterator();
            }
            else {
                final HSSFSheet sheetHSSF = workbookHSSF.getSheetAt(0);
                rowIterator = sheetHSSF.iterator();
            }
            if (readSecondLine) {
                final Row row = rowIterator.next();
                System.err.println(row.toString());
            }
            if (rowIterator.hasNext()) {
                final Row row = rowIterator.next();
                final ArrayList<String> cells = new ArrayList<String>();
                for (int i = 0; i < row.getLastCellNum(); ++i) {
                    final Cell cell = row.getCell(i);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case BOOLEAN: {
                                cells.add(Boolean.toString(cell.getBooleanCellValue()));
                                break;
                            }
                            case NUMERIC: {
                                cells.add(Double.toString(cell.getNumericCellValue()));
                                break;
                            }
                            case STRING: {
                                cells.add(cell.getStringCellValue());
                                break;
                            }
                            case BLANK: {
                                cells.add("");
                                break;
                            }
                            default: {
                                cells.add(cell.toString());
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
                final String firstLine = Parcer.csvString(parts);
                if (firstLine.isEmpty()) {
                    System.err.println("Empty line in method readFirstLineInXlsOrXlsx()");
                    return null;
                }
                return firstLine;
            }
        }
        catch (FileNotFoundException ex) {
            System.err.println("Error in process XLS or XLSX file: " + ex.getMessage());
        }
        catch (IOException ex2) {
            System.err.println("Error in process XLS or XLSX file: " + ex2.getMessage());
        }
        return null;
    }
    
    private static String readLineInTxtOrCsvFile(final String filePath, final boolean readSecondLine) {
        try {
            final File file = new File(filePath);
            final InputStream inputStream = new FileInputStream(file);
            final BOMInputStream bOMInputStream = new BOMInputStream(inputStream, new ByteOrderMark[] { ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE });
            final ByteOrderMark bom = bOMInputStream.getBOM();
            final String charsetName = (bom == null) ? "UTF-8" : bom.getCharsetName();
            final InputStreamReader inputStreamReader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName);
            final BufferedReader reader = new BufferedReader(inputStreamReader);
            if (readSecondLine) {
                final String line = reader.readLine();
                System.out.print(line);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                return line;
            }
            reader.close();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
    
    private static void resetIndexes() {
        Utilities.urlIndexStartFromZero = -1;
        Utilities.emailIndexStartFromZero = -1;
    }
    
    private static void searchIndexesInString(final String string) {
        resetIndexes();
        final Parcer parcer = new Parcer();
        final String[] parts = parcer.parce(string);
        Utilities.emailIndexStartFromZero = searchEmailIndex(parts);
        Utilities.urlIndexStartFromZero = searchPreferableUrlIndex(parts);
    }
    
    private static int searchEmailIndex(final String[] parts) {
        for (int i = 0; i < parts.length; ++i) {
            final String part = parts[i];
            if (part.contains("@")) {
                return i;
            }
        }
        return -1;
    }
    
    private static int searchPreferableUrlIndex(final String[] parts) {
        int anyUrlIndex = -1;
        for (int i = 0; i < parts.length; ++i) {
            String part = parts[i];
            part = part.trim().toLowerCase();
            if (isContainUrlPortion(part)) {
                anyUrlIndex = i;
                if (!isContainException(part)) {
                    return i;
                }
            }
        }
        return anyUrlIndex;
    }
    
    private static boolean isContainUrlPortion(final String part) {
        for (final String urlException : Utilities.urlPortion) {
            final boolean isContainExceptin = part.contains(urlException);
            if (isContainExceptin) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isContainException(final String part) {
        for (final String urlException : Utilities.urlExceptions) {
            final boolean isContainExceptin = part.contains(urlException);
            if (isContainExceptin) {
                return true;
            }
        }
        return false;
    }
    
    public static int indexOfPartContainingEmail(final String[] parts) {
        final int index = -1;
        for (final String part : parts) {
            if (part.contains("@")) {
                return index;
            }
        }
        return index;
    }
    
    public static int indexOfPartContainingURL(final String[] parts) {
        final int index = -1;
        for (final String part : parts) {
            if (isContainUrlPortion(part) && !isContainException(part)) {
                return index;
            }
        }
        return index;
    }
    
    static void searchIndexesInSourceFile(final String pathToFile) {
        final String firstLine = readFirstLine(pathToFile);
        searchIndexesInStringSource(firstLine);
        if (Utilities.urlIndexStartFromZero == -1 && Utilities.urlIndexStartFromZero == -1) {
            final String secondLine = readSecondLine(pathToFile);
            searchIndexesInStringSource(secondLine);
        }
    }
    
    private static void searchIndexesInStringSource(final String string) {
        Utilities.isHasTitle = isStringSourceTableTitle(string);
        if (Utilities.isHasTitle) {
            final Parcer parcer = new Parcer();
            final String[] firstLineColomnsInOriginalCase = parcer.parce(string);
            searchIndexesInTitle(firstLineColomnsInOriginalCase);
        }
        else {
            searchIndexesInString(string);
        }
    }
    
    private static boolean isStringSourceTableTitle(final String string) {
        boolean isHasEmailTitle = false;
        for (final String emailColomnNameVariant : Utilities.emailColomnNameVariants) {
            if (string.contains(emailColomnNameVariant)) {
                isHasEmailTitle = true;
                break;
            }
        }
        boolean isHasURLTitle = false;
        for (final String urlColomnNameVariant : Utilities.urlColomnNameVariants) {
            if (string.contains(urlColomnNameVariant)) {
                isHasURLTitle = true;
                break;
            }
        }
        return isHasEmailTitle || isHasURLTitle;
    }
    
    public static boolean isTableTitle(final String string) {
        return isStringSourceTableTitle(string);
    }
    
    static void searchIndexesInDatabaseFile(final String pathToFile) {
        final String firstLine = readFirstLine(pathToFile);
        searchIndexesInStringDatabase(firstLine);
    }
    
    private static void searchIndexesInStringDatabase(final String string) {
        if (isStringDatabaseTableTitle(string)) {
            final Parcer parcer = new Parcer();
            final String[] firstLineColomnsInOriginalCase = parcer.parce(string);
            searchIndexesInTitle(firstLineColomnsInOriginalCase);
        }
        else {
            searchIndexesInString(string);
        }
    }
    
    private static boolean isStringDatabaseTableTitle(final String testString) {
        boolean isHasEmailColomnTitle = false;
        for (final String emailColomnNameVariant : Utilities.emailColomnNameVariants) {
            if (testString.contains(emailColomnNameVariant)) {
                isHasEmailColomnTitle = true;
                break;
            }
        }
        boolean isHasURLColomnTitle = false;
        for (final String urlColomnNameVariant : Utilities.urlColomnNameVariants) {
            if (testString.contains(urlColomnNameVariant)) {
                isHasURLColomnTitle = true;
                break;
            }
        }
        return isHasEmailColomnTitle && isHasURLColomnTitle;
    }
    
    public static String dateAndTimeISO() {
        final LocalDateTime now = LocalDateTime.now();
        final String dateAndTimeISO = DateTimeFormatter.ISO_INSTANT.format(now.toInstant(ZoneOffset.UTC));
        return dateAndTimeISO;
    }
    
    static {
        emailColomnNameVariants = new String[] { "e-mail", "E-mail", "email", "Email" };
        urlColomnNameVariants = new String[] { "website", "Website", "WEBSITE", "url", "URL", "Url", "web", "Web", "WEB" };
        Utilities.urlIndexStartFromZero = -1;
        Utilities.emailIndexStartFromZero = -1;
        Utilities.urlPortion = new String[] { "www.", "https://", "http://" };
        Utilities.urlExceptions = new String[] { ".instagram.", ".twitter.", ".facebook.", ".soundcloud.", ".thearmoryshow." };
        Utilities.isHasTitle = false;
    }
}
