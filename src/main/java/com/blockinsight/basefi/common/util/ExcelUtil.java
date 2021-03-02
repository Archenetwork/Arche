package com.blockinsight.basefi.common.util;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by 97434 on 2019/7/9.
 */
@Slf4j
public class ExcelUtil
{
    private static String excelPath;

    public static void download(HttpServletRequest request, HttpServletResponse response, String title, Workbook workbook) throws IOException
    {
        String path = excelPath + "/ExcelData//" + title + ".xls";
        File file = new File(path);
        //判断目标文件所在的目录是否存在
        if (!file.getParentFile().exists())
        {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(new File(path));
        workbook.write(fos);
        workbook.close();
        fos.close();
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + title + ".xls");
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try
        {
            os = response.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(new File(path)));
            int i = bis.read(buff);
            while (i != -1)
            {
                os.write(buff, 0, buff.length);
                os.flush();
                i = bis.read(buff);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        log.debug("success");

    }

    public static void exportExcel(List<?> list, String title, String sheetName, Class<?> pojoClass, String fileName, boolean isCreateHeader, HttpServletResponse response)
    {
        ExportParams exportParams = new ExportParams(title, sheetName);
        exportParams.setCreateHeadRows(isCreateHeader);
        defaultExport(list, pojoClass, fileName, response, exportParams);

    }

    public static void exportExcel(List<?> list, String title, String sheetName, Class<?> pojoClass, String fileName, HttpServletResponse response)
    {
        defaultExport(list, pojoClass, fileName, response, new ExportParams(title, sheetName));
    }

    public static void exportExcel(List<Map<String, Object>> list, String fileName, HttpServletResponse response)
    {
        defaultExport(list, fileName, response);
    }

    private static void defaultExport(List<?> list, Class<?> pojoClass, String fileName, HttpServletResponse response, ExportParams exportParams)
    {
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, pojoClass, list);
        if (workbook != null)
        {
            downLoadExcel(fileName, response, workbook);
        }
    }

    public static void downLoadExcel(String fileName, HttpServletResponse response, Workbook workbook)
    {
        try
        {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            workbook.write(response.getOutputStream());
        }
        catch (Exception e)
        {
            log.error("", e);
        }
    }

    private static void defaultExport(List<Map<String, Object>> list, String fileName, HttpServletResponse response)
    {
        Workbook workbook = ExcelExportUtil.exportExcel(list, ExcelType.HSSF);
        if (workbook != null)
        {
            downLoadExcel(fileName, response, workbook);
        }
    }

    public static <T> List<T> importExcel(String filePath, Integer titleRows, Integer headerRows, Class<T> pojoClass)
    {
        if (StringUtils.isBlank(filePath))
        {
            return null;
        }
        ImportParams params = new ImportParams();
        params.setTitleRows(titleRows);
        params.setHeadRows(headerRows);
        List<T> list = null;
        try
        {
            list = ExcelImportUtil.importExcel(new File(filePath), pojoClass, params);
        }
        catch (NoSuchElementException e)
        {
            log.error("模板不能为空");
        }
        catch (Exception e)
        {
            log.error("", e);
        }
        return list;
    }

    public static <T> List<T> importExcel(MultipartFile file, Integer titleRows, Integer headerRows, Class<T> pojoClass)
    {
        if (file == null)
        {
            return null;
        }
        ImportParams params = new ImportParams();
        params.setTitleRows(titleRows);
        params.setHeadRows(headerRows);
        List<T> list = null;
        try
        {
            list = ExcelImportUtil.importExcel(file.getInputStream(), pojoClass, params);
        }
        catch (NoSuchElementException e)
        {
            log.error("excel文件不能为空");
        }
        catch (Exception e)
        {
            log.error("", e);
        }
        return list;
    }

/*

    */
/**
     * 处理单元格格式的简单方式
     *
     * @param hssfCell
     * @return
     *//*

    public static String formatCell(Cell hssfCell)
    {
        if (hssfCell == null)
        {
            return "";
        }
        else
        {
            if (hssfCell.getCellType() == Cell.CELL_TYPE_BOOLEAN)
            {
                return String.valueOf(hssfCell.getBooleanCellValue());
            }
            else if (hssfCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
            {
                return String.valueOf(hssfCell.getNumericCellValue());
            }
            else
            {
                return String.valueOf(hssfCell.getStringCellValue());
            }
        }
    }

    */
/**
     * 处理单元格格式的第二种方式: 包括如何对单元格内容是日期的处理
     *
     * @param cell
     * @return
     *//*

    public static String formatCell2(HSSFCell cell)
    {
        if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN)
        {
            return String.valueOf(cell.getBooleanCellValue());
        }
        else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
        {

            //针对单元格式为日期格式
            if (HSSFDateUtil.isCellDateFormatted(cell))
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
            }
            return String.valueOf(cell.getNumericCellValue());
        }
        else
        {
            return cell.getStringCellValue();
        }
    }

    */
/**
     * 处理单元格格式的第三种方法:比较全面
     *
     * @param cell
     * @return
     *//*

    public static String formatCell3(HSSFCell cell)
    {
        if (cell == null)
        {
            return "";
        }
        switch (cell.getCellType())
        {
            case HSSFCell.CELL_TYPE_NUMERIC:

                //日期格式的处理
                if (HSSFDateUtil.isCellDateFormatted(cell))
                {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
                }

                return String.valueOf(cell.getNumericCellValue());

            //字符串
            case HSSFCell.CELL_TYPE_STRING:
                return cell.getStringCellValue();

            // 公式
            case HSSFCell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();

            // 空白
            case HSSFCell.CELL_TYPE_BLANK:
                return "";

            // 布尔取值
            case HSSFCell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue() + "";

            //错误类型
            case HSSFCell.CELL_TYPE_ERROR:
                return cell.getErrorCellValue() + "";
        }

        return "";
    }
*/

}
