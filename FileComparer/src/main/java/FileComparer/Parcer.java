// 
// Decompiled by Procyon v0.5.36
// 

package FileComparer;

import java.io.Writer;
import com.opencsv.CSVWriter;
import java.io.StringWriter;
import com.opencsv.CSVParserBuilder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.opencsv.CSVParser;
import org.apache.commons.lang3.StringUtils;

public class Parcer
{
    private boolean isDataFormatDetected;
    private boolean isTsvDataFormat;
    
    public Parcer() {
        this.isDataFormatDetected = false;
        this.isTsvDataFormat = false;
    }
    
    public String[] parce(final String string) {
        if (!this.isDataFormatDetected) {
            this.detectDataFormat(string);
        }
        String[] parts;
        if (this.isTsvDataFormat) {
            parts = this.parceTSV(string);
        }
        else {
            parts = this.parceCSV(string);
        }
        return parts;
    }
    
    private void detectDataFormat(final String string) {
        final int tabCountMatches = StringUtils.countMatches(string, "\t");
        final int commaCountMatches = StringUtils.countMatches(string, ",");
        if (tabCountMatches > 0 && tabCountMatches > commaCountMatches) {
            this.isTsvDataFormat = true;
        }
        else {
            this.isTsvDataFormat = false;
        }
        this.isDataFormatDetected = true;
    }
    
    private String[] parceCSV(final String string) {
        final CSVParser parser = new CSVParser();
        String[] colomnsOriginalCase = null;
        try {
            colomnsOriginalCase = parser.parseLineMulti(string);
        }
        catch (IOException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return colomnsOriginalCase;
    }
    
    private String[] parceTSV(final String string) {
        final CSVParser parser = new CSVParserBuilder().withSeparator('\t').withIgnoreQuotations(true).build();
        String[] colomnsOriginalCase = null;
        try {
            colomnsOriginalCase = parser.parseLineMulti(string);
        }
        catch (IOException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return colomnsOriginalCase;
    }
    
    public static String csvString(final String[] parts) {
        final StringWriter stringWriter = new StringWriter();
        final CSVWriter csvWriter = new CSVWriter(stringWriter, ',', '\"', '\"', "\n");
        csvWriter.writeNext(parts);
        final String csvString = stringWriter.toString().trim();
        return csvString;
    }
}
