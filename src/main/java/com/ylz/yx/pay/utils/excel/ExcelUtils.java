package com.ylz.yx.pay.utils.excel;


import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;

public class ExcelUtils {
    /**
     * 使用浏览器选择路径下载
     * @param response
     * @param fileName
     * @param data
     * @throws Exception
     */
    public static void exportExcel(HttpServletResponse response, String fileName, ExcelData data) throws Exception {
        // 告诉浏览器用什么软件可以打开此文件
//        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Pragma", "public");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Content-Type", "application/force-download");
        // 下载文件的默认名称
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + ".xls", "utf-8"));
        
        response.setContentType("application/msexcel;charset=UTF-8");
        
        exportExcel(data, response.getOutputStream());
    }
 
    
    //生成Excel
    public static int generateExcel(ExcelData excelData, String path) throws Exception {
        File f = new File(path);
        FileOutputStream out = new FileOutputStream(f);
        return exportExcel(excelData, out);
    }
 
    private static int exportExcel(ExcelData data, OutputStream out) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        int rowIndex = 0;
        try {
            String sheetName = data.getName();
            if (null == sheetName) {
                sheetName = "Sheet1";
            }
            XSSFSheet sheet = wb.createSheet(sheetName);
            rowIndex = writeExcel(wb, sheet, data);
            wb.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //此处需要关闭 wb 变量
            out.close();
        }
        return rowIndex;
    }
 
    /**
     * 表不显示字段
     * @param wb
     * @param sheet
     * @param data
     * @return
     */
//    private static int writeExcel(XSSFWorkbook wb, Sheet sheet, ExcelData data) {
//        int rowIndex = 0;
//        writeTitlesToExcel(wb, sheet, data.getTitles());
//        rowIndex = writeRowsToExcel(wb, sheet, data.getRows(), rowIndex);
//        autoSizeColumns(sheet, data.getTitles().size() + 1);
//        return rowIndex;
//    }
 
    /**
     * 表显示字段
     * @param wb
     * @param sheet
     * @param data
     * @return
     */
    private static int writeExcel(XSSFWorkbook wb, Sheet sheet, ExcelData data) {
        int rowIndex = 0;
        rowIndex = writeTitlesToExcel(wb, sheet, data.getTitles());
        rowIndex = writeRowsToExcel(wb, sheet, data.getRows(), rowIndex);
        autoSizeColumns(sheet, data.getTitles().size() + 1);
        return rowIndex;
    }
    /**
     * 设置表头
     *
     * @param wb
     * @param sheet
     * @param titles
     * @return
     */
    private static int writeTitlesToExcel(XSSFWorkbook wb, Sheet sheet, List<String> titles) {
        int rowIndex = 0;
        int colIndex = 0;
        Font titleFont = wb.createFont();
        //设置字体
        titleFont.setFontName("simsun");
        //设置粗体
        // titleFont.setBoldweight(Short.MAX_VALUE);
        titleFont.setBold(true);
        //设置字号
        titleFont.setFontHeightInPoints((short) 14);
        //设置颜色
        titleFont.setColor(IndexedColors.BLACK.index);
        XSSFCellStyle titleStyle = wb.createCellStyle();
        //水平居中
        // titleStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        //垂直居中
        // titleStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置图案颜色
        titleStyle.setFillForegroundColor(new XSSFColor(new Color(182, 184, 192)));
        //设置图案样式
        // titleStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setFont(titleFont);
        setBorder(titleStyle, BorderStyle.THIN, new XSSFColor(new Color(0, 0, 0)));
        Row titleRow = sheet.createRow(rowIndex);
        titleRow.setHeightInPoints(25);
        colIndex = 0;
        for (String field : titles) {
            Cell cell = titleRow.createCell(colIndex);
            cell.setCellValue(field);
            cell.setCellStyle(titleStyle);
            colIndex++;
        }
        rowIndex++;
        return rowIndex;
    }
 
    /**
     * 设置内容
     *
     * @param wb
     * @param sheet
     * @param rows
     * @param rowIndex
     * @return
     */
    private static int writeRowsToExcel(XSSFWorkbook wb, Sheet sheet, List<List<Object>> rows, int rowIndex) {
        int colIndex;
        Font dataFont = wb.createFont();
        dataFont.setFontName("simsun");
        dataFont.setFontHeightInPoints((short) 14);
        dataFont.setColor(IndexedColors.BLACK.index);
 
        XSSFCellStyle dataStyle = wb.createCellStyle();
        // dataStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        // dataStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataStyle.setFont(dataFont);
        setBorder(dataStyle, BorderStyle.THIN, new XSSFColor(new Color(0, 0, 0)));
        for (List<Object> rowData : rows) {
            Row dataRow = sheet.createRow(rowIndex);
            dataRow.setHeightInPoints(25);
            colIndex = 0;
            for (Object cellData : rowData) {
                Cell cell = dataRow.createCell(colIndex);
                if (cellData != null) {
                    cell.setCellValue(cellData.toString());
                } else {
                    cell.setCellValue("");
                }
                cell.setCellStyle(dataStyle);
                colIndex++;
            }
            rowIndex++;
        }
        return rowIndex;
    }
 
    /**
     * 自动调整列宽
     *
     * @param sheet
     * @param columnNumber
     */
    private static void autoSizeColumns(Sheet sheet, int columnNumber) {
        for (int i = 0; i < columnNumber; i++) {
            int orgWidth = sheet.getColumnWidth(i);
            sheet.autoSizeColumn(i, true);
            int newWidth = sheet.getColumnWidth(i) + 50;
            if (newWidth > orgWidth) {
            	if(newWidth > 15000) {
            		newWidth = 15000;
            	}
                sheet.setColumnWidth(i, newWidth);
            } else {
                sheet.setColumnWidth(i, orgWidth);
            }
        }
    }
 
    /**
     * 设置边框
     *
     * @param style
     * @param border
     * @param color
     */
    private static void setBorder(XSSFCellStyle style, BorderStyle border, XSSFColor color) {
        style.setBorderTop(border);
        style.setBorderLeft(border);
        style.setBorderRight(border);
        style.setBorderBottom(border);
        style.setBorderColor(BorderSide.TOP, color);
        style.setBorderColor(BorderSide.LEFT, color);
        style.setBorderColor(BorderSide.RIGHT, color);
        style.setBorderColor(BorderSide.BOTTOM, color);
    }

    private String fileName = null;
    private Workbook wb = null;
    private Sheet sheet = null;
    private FileInputStream fileStream = null;
    private InputStream inputStream = null;
    
    public ExcelUtils(String filePath) throws Exception {
        this.fileName = filePath;
        this.fileStream = new FileInputStream(filePath);
        //使用后缀名判断文件类型不准确，可能存在后缀名被修改问题
    	/*String fileSuffix = filePath.split("\\.")[1];
    	if("xls".equals(fileSuffix)) {
    		wb = new HSSFWorkbook(fileStream);
    	}else if("xlsx".equals(fileSuffix)) {
    		wb = new XSSFWorkbook(fileStream);
    	}*/
        wb = WorkbookFactory.create(fileStream);
    }
    public ExcelUtils(String fileName, InputStream inputStream) throws Exception {
        this.fileName = fileName;
        this.inputStream = inputStream;
        //使用后缀名判断文件类型不准确，可能存在后缀名被修改问题
    	/*String fileSuffix = filePath.split("\\.")[1];
    	if("xls".equals(fileSuffix)) {
    		wb = new HSSFWorkbook(fileStream);
    	}else if("xlsx".equals(fileSuffix)) {
    		wb = new XSSFWorkbook(fileStream);
    	}*/
        wb = WorkbookFactory.create(inputStream);
    }
    public ExcelUtils(HSSFSheet sheet) throws Exception {
        this.sheet=sheet;
    }

    public String getFileName() {
        return fileName;
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public Workbook getWorkbook() {
        return wb;
    }


    public void setWb(Workbook wb) {
        this.wb = wb;
    }


    public Sheet getSheet() {
        return sheet;
    }


    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    /**
     * 获取行数
     * @return
     */
    public int getRowNum() {
        //获取第一个sheet
        sheet = wb.getSheetAt(0);
        //获取最大行数
        return sheet.getPhysicalNumberOfRows();
    }
    /**
     * 获取列数
     * @return
     */
    public int getColNum() {
        //获取最大列数
        return sheet.getRow(0).getPhysicalNumberOfCells();
    }

    /**
     * 获取指定行
     * @param index
     * @return
     */
    public Row getRow(int index) {
        return sheet.getRow(index);
    }
    /**
     * 获取指定列
     * @param index
     * @return
     */
    public String getCol(int index) {

        return null;
    }
    /**
     * 获取某个单元格
     * @param row
     * @param col
     * @return
     */
    public String getString(int row, int col) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Cell cell =sheet.getRow(row).getCell(col);
        if (null == cell) {
            return null;
        }
        String value = null;
        switch (cell.getCellType()) {
            case BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;

            case NUMERIC:

                // 日期类型，转换为日期
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = sdf.format(cell.getDateCellValue());
                }

                // 数值类型
                else {

                    // 默认返回double，创建BigDecimal返回准确值
                    value = new BigDecimal(cell.getNumericCellValue()).toString();
                }
                break;

            default:
                value = cell.toString();
                break;
        }

        return value;
    }

    public void ExcelfileStreamClose()throws Exception{
        this.fileStream.close();
    }

    public void ExcelInputStreamClose()throws Exception{
        this.inputStream.close();
    }
    /**
     *
     * @param row 行数
     * @param width 列宽
     */
    public void alterColumnWidth(int row, int width) {
        //修改列宽度
        for(int i=0; i<sheet.getRow(row).getPhysicalNumberOfCells();i++) {
            sheet.setColumnWidth(i, 256*width+184);
        }
    }
    
}
