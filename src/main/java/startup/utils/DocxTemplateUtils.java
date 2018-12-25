package startup.utils;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.util.CollectionUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Noah
 * @description DocxTemplateUtils
 * @created at 2018-12-12 09:44:48
 **/
public class DocxTemplateUtils {

    private static final Pattern REQUIRED_PATTERN = Pattern.compile("\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);

    private static final Pattern OPTIONAL_PATTERN_START = Pattern.compile("\\{#(.+?)\\}", Pattern.CASE_INSENSITIVE);

    private static final Pattern OPTIONAL_PATTERN_END = Pattern.compile("\\{/#(.+?)\\}", Pattern.CASE_INSENSITIVE);

    private DocxTemplateUtils() {
    }

    /**
     * 根据模板生成word(.docx格式)
     *
     * @param docTemplate 模板流
     * @param params      需要替换的参数
     * @param tableList   需要插入的参数
     */
    public static InputStream createDocxByTemplate(InputStream docTemplate, Map<String, Object> params,
                                                   List<String[]> tableList) {
        Objects.requireNonNull(docTemplate);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        try {
            XWPFDocument doc = new XWPFDocument(OPCPackage.open(docTemplate));
            replaceInPara(doc, params);    //替换文本里面的变量
            replaceInTable(doc, params, tableList); //替换表格里面的变量
            doc.write(outputStream);
            inputStream = new BufferedInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    private static void replaceInPara(XWPFDocument doc, Map<String, Object> params) {
        Iterator<XWPFParagraph> iterator = doc.getParagraphsIterator();
        XWPFParagraph para;
        int paragraphIndex = 0;
        List<Integer> tobeRemoveGraphs = new ArrayList<>();
        //处理可选段落
        while (iterator.hasNext()) {
            para = iterator.next();
            boolean isParagraphRemove = removeOptional(para, params);
            if (isParagraphRemove) {
                tobeRemoveGraphs.add(paragraphIndex);
            }
            paragraphIndex++;
        }
        tobeRemoveGraphs.stream().sorted(Comparator.reverseOrder()).forEach(index -> doc.removeBodyElement(index));
        //填充数据
        iterator = doc.getParagraphsIterator();
        while (iterator.hasNext()) {
            para = iterator.next();
            replaceInPara(para, params);
        }
    }

    private static void replaceInPara(XWPFParagraph para, Map<String, Object> params) {
        if (CollectionUtils.isEmpty(params)) return;
        String paragraphText = para.getParagraphText();
        if (!matcher(REQUIRED_PATTERN, paragraphText).find()) return;
        List<XWPFRun> runs = para.getRuns();
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String runText = run.toString();
            Matcher matcher = matcher(REQUIRED_PATTERN, runText);
            String matchKey;
            boolean isReplace = false;
            while (matcher.find()) {
                isReplace = true;
                matchKey = matcher.group();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value != null && Map.class.isAssignableFrom(value.getClass())) {
                        Map<String, Object> valueMap = (Map) value;
                        for (Map.Entry<String, Object> valueMapEntry : valueMap.entrySet()) {
                            key = valueMapEntry.getKey();
                            value = valueMapEntry.getValue();
                            if (key.equalsIgnoreCase(matchKey)) {
                                runText = runText.replace(matchKey, String.valueOf(value == null ? "" : value));
                            }
                        }
                    } else {
                        if (key.equalsIgnoreCase(matchKey)) {
                            runText = runText.replace(matchKey, String.valueOf(value == null ? "" : value));
                        }
                    }
                }
            }
            if (isReplace) {
                run.setText(runText, 0);
            }
        }
    }

    private static boolean removeOptional(XWPFParagraph para, Map<String, Object> params) {
        int optionalStart = -1;
        int optionalEnd = -1;
        List<XWPFRun> runs = para.getRuns();
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String runText = run.toString();
            if (matcher(OPTIONAL_PATTERN_START, runText).find()) {
                optionalStart = i;
            }
            if (matcher(OPTIONAL_PATTERN_END, runText).find()) {
                optionalEnd = i;
            }
        }
        boolean noRunInParagraph = false;
        if (optionalStart > -1) {
            String string = params.keySet().stream().collect(Collectors.joining());
            if (!matcher(OPTIONAL_PATTERN_START, string).find()) {
                for (int i = optionalEnd; i >= optionalStart; i--) {
                    para.removeRun(i);
                }
            }
            runs = para.getRuns();
            //如果当前的Paragraph没有任何元素，就删除该Paragraph
            if (CollectionUtils.isEmpty(runs)) {
                noRunInParagraph = true;
            } else {
                for (int i = 0; i < runs.size(); i++) {
                    XWPFRun run = runs.get(i);
                    String runText = run.toString();
                    Matcher matcherStart = matcher(OPTIONAL_PATTERN_START, runText);
                    while (matcherStart.find()) {
                        runText = runText.replace(matcherStart.group(), "");
                        run.setText(runText, 0);
                    }
                    Matcher matcherEnd = matcher(OPTIONAL_PATTERN_END, runText);
                    while (matcherEnd.find()) {
                        runText = runText.replace(matcherEnd.group(), "");
                        run.setText(runText, 0);
                    }
                }
            }
        }
        return noRunInParagraph;
    }

//    private static void replaceInPara(XWPFParagraph para, Map<String, Object> params, XWPFDocument doc) {
//        if (CollectionUtils.isEmpty(params)) return;
//        List<XWPFRun> runs;
//        if (matcher(para.getParagraphText()).find()) {
//            runs = para.getRuns();
//            int start = -1;
//            int end = -1;
//            String str = "";
//            for (int i = 0; i < runs.size(); i++) {
//                XWPFRun run = runs.get(i);
//                String runText = run.toString();
//                if ('$' == runText.charAt(0) && '{' == runText.charAt(1)) {
//                    start = i;
//                }
//                if ((start != -1)) {
//                    str += runText;
//                }
//                if ('}' == runText.charAt(runText.length() - 1)) {
//                    if (start != -1) {
//                        end = i;
//                        break;
//                    }
//                }
//            }
//
//            for (int i = start; i <= end; i++) {
//                para.removeRun(i);
//                i--;
//                end--;
//            }
//            for (Map.Entry<String, Object> entry : params.entrySet()) {
//                String key = entry.getKey();
//                if (str.indexOf(key) != -1) {
//                    Object value = entry.getValue();
//                    if (value instanceof String) {
//                        str = str.replace(key, value.toString());
//                        para.createRun().setText(str, 0);
//                        break;
//                    } else if (value instanceof Map) {
//                        str = str.replace(key, "");
//                        Map pic = (Map) value;
//                        int width = Integer.parseInt(pic.get("width").toString());
//                        int height = Integer.parseInt(pic.get("height").toString());
//                        int picType = getPictureType(pic.get("type").toString());
//                        byte[] byteArray = (byte[]) pic.get("content");
//                        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteArray);
//                        try {
////int ind = doc.addPicture(byteInputStream,picType);
////doc.createPicture(ind, width , height,para);
//                            doc.addPictureData(byteInputStream, picType);
//// doc.createPicture(doc.getAllPictures().size() - 1, width, height, para);
//                            para.createRun().setText(str, 0);
//                            break;
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//    }

    /**
     * 为表格插入数据，行数不够添加新行
     *
     * @param table     需要插入数据的表格
     * @param tableList 插入数据集合
     */
    private static void insertTable(XWPFTable table, List<String[]> tableList) {
        if (CollectionUtils.isEmpty(tableList)) return;
//创建行,根据需要插入的数据添加新行，不处理表头
        for (int i = 0; i < tableList.size(); i++) {
            XWPFTableRow row = table.createRow();
        }
//遍历表格插入数据
        List<XWPFTableRow> rows = table.getRows();
        int length = table.getRows().size();
        for (int i = 1; i < length - 1; i++) {
            XWPFTableRow newRow = table.getRow(i);
            List<XWPFTableCell> cells = newRow.getTableCells();
            for (int j = 0; j < cells.size(); j++) {
                XWPFTableCell cell = cells.get(j);
                String s = tableList.get(i - 1)[j];
                cell.setText(s);
            }
        }
    }

    /**
     * 替换表格里面的变量
     *
     * @param doc    要替换的文档
     * @param params 参数
     */
    private static void replaceInTable(XWPFDocument doc, Map<String, Object> params, List<String[]> tableList) {
        if (CollectionUtils.isEmpty(tableList)) return;
        Iterator<XWPFTable> iterator = doc.getTablesIterator();
        XWPFTable table;
        List<XWPFTableRow> rows;
        List<XWPFTableCell> cells;
        List<XWPFParagraph> paras;
        while (iterator.hasNext()) {
            table = iterator.next();
            if (table.getRows().size() > 1) {
//判断表格是需要替换还是需要插入，判断逻辑有$为替换，表格无$为插入
                if (matcher(REQUIRED_PATTERN, table.getText()).find()) {
                    rows = table.getRows();
                    for (XWPFTableRow row : rows) {
                        cells = row.getTableCells();
                        for (XWPFTableCell cell : cells) {
                            paras = cell.getParagraphs();
                            for (XWPFParagraph para : paras) {
                                replaceInPara(para, params);
                            }
                        }
                    }
                } else {
                    insertTable(table, tableList); //插入数据
                }
            }
        }
    }

    /**
     * 正则匹配字符串
     *
     * @param str
     * @return
     */
    private static Matcher matcher(Pattern pattern, String str) {
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }

    /**
     * 根据图片类型，取得对应的图片类型代码
     *
     * @param picType
     * @return int
     */
    private static int getPictureType(String picType) {
        int res = XWPFDocument.PICTURE_TYPE_PICT;
        if (picType != null) {
            if (picType.equalsIgnoreCase("png")) {
                res = XWPFDocument.PICTURE_TYPE_PNG;
            } else if (picType.equalsIgnoreCase("dib")) {
                res = XWPFDocument.PICTURE_TYPE_DIB;
            } else if (picType.equalsIgnoreCase("emf")) {
                res = XWPFDocument.PICTURE_TYPE_EMF;
            } else if (picType.equalsIgnoreCase("jpg") || picType.equalsIgnoreCase("jpeg")) {
                res = XWPFDocument.PICTURE_TYPE_JPEG;
            } else if (picType.equalsIgnoreCase("wmf")) {
                res = XWPFDocument.PICTURE_TYPE_WMF;
            }
        }
        return res;
    }

}




