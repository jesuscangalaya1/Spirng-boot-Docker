package com.idat.pe.reports.exports;

import com.idat.pe.exceptions.InternalServerErrorException;
import com.idat.pe.reports.helper.ExcelExportHelper;
import com.idat.pe.reports.helper.PdfExportHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.idat.pe.utils.constants.AppConstants.*;

@Component
public class ResourceExportImpl implements ResourceExport {

    @Override
    public File generateExcel(List<String> sheets, Map<String, List<String>> colsBySheet,
                              Map<String, List<Map<String, String>>> valuesBySheet, String fiLeName) throws Exception{
        try {
            Path temp = Files.createTempFile(fiLeName, FORMAT_EXCEL);
            return ExcelExportHelper.generateExcel(sheets, colsBySheet, valuesBySheet, temp.toString());
        } catch (RuntimeException e) {
            throw new InternalServerErrorException(ERROR_REPORTE);
        }
    }

    @Override
    public File generatePdf(List<String> tables, Map<String, List<String>> colsByTables,
                            Map<String, List<Map<String, String>>> valuesByTable, String fileName) throws Exception {
        try {
            Path temp = Files.createTempFile(fileName, FORMAT_PDF);
            return PdfExportHelper.generatePdf(tables, colsByTables, valuesByTable, temp.toString());
        } catch (RuntimeException e) {
            throw new InternalServerErrorException(ERROR_REPORTE);
        }
    }

}
