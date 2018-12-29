package startup.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Noah
 **/
public class DocxTemplateUtils {

    private static final Pattern REQUIRED_PATTERN = Pattern.compile("\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);

    private static final Pattern OPTIONAL_PATTERN_START = Pattern.compile("\\{#(.+?)\\}", Pattern.CASE_INSENSITIVE);

    private static final Pattern OPTIONAL_PATTERN_END = Pattern.compile("\\{/#(.+?)\\}", Pattern.CASE_INSENSITIVE);

    public static final String ELECTRONIC_SIGNATURE = "电子签名";

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
                        if (matchKey.contains(ELECTRONIC_SIGNATURE) && key.equalsIgnoreCase(matchKey)) {
                            runText = runText.replace(matchKey, "");
                            insertPic(run, valueMap);
                        } else {
                            for (Map.Entry<String, Object> valueMapEntry : valueMap.entrySet()) {
                                key = valueMapEntry.getKey();
                                value = valueMapEntry.getValue();
                                if (key.equalsIgnoreCase(matchKey)) {
                                    runText = runText.replace(matchKey, String.valueOf(value == null ? "" : value));
                                }
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
        int optionalStart;
        int optionalEnd;
        String optionalKeyStart = "";
        List<XWPFRun> runs = para.getRuns();
        Matcher matcher;
        List<String> list = new ArrayList<>();
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String runText = run.toString();
            optionalStart = optionalEnd = -1;
            matcher = matcher(OPTIONAL_PATTERN_START, runText);
            if (matcher.find()) {
                optionalKeyStart = matcher.group();
                optionalStart = i;
            }
            matcher = matcher(OPTIONAL_PATTERN_END, runText);
            if (matcher.find()) {
                optionalEnd = i;
            }
            if (optionalStart != -1 && optionalEnd != -1) {
                list.add(String.format("%s_%d,%d", optionalKeyStart, optionalStart, optionalEnd));
            }
        }
        boolean noRunInParagraph = false;
        if (!CollectionUtils.isEmpty(list)) {
            String keys = params.keySet().stream().collect(Collectors.joining());
            list.forEach(string -> {
                String[] keyIndex = StringUtils.split(string, "_");
                if (!StringUtils.isBlank(keyIndex[0]) && keys.indexOf(keyIndex[0]) < 0) {
                    String[] indexs = StringUtils.split(keyIndex[1], ",");
                    for (int i = Integer.parseInt(indexs[1]); i >= Integer.parseInt(indexs[0]); i--) {
                        para.removeRun(i);
                    }
                }

            });
            runs = para.getRuns();
            if (CollectionUtils.isEmpty(runs)) {
                noRunInParagraph = true;
            } else {
                for (int i = 0; i < runs.size(); i++) {
                    XWPFRun run = runs.get(i);
                    String runText = run.toString();
                    matcher = matcher(OPTIONAL_PATTERN_START, runText);
                    while (matcher.find()) {
                        runText = runText.replace(matcher.group(), "");
                        run.setText(runText, 0);
                    }
                    matcher = matcher(OPTIONAL_PATTERN_END, runText);
                    while (matcher.find()) {
                        runText = runText.replace(matcher.group(), "");
                        run.setText(runText, 0);
                    }
                }
            }
        }
        return noRunInParagraph;
    }

    private static Matcher matcher(Pattern pattern, String str) {
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }

    /**
     * //插入图片
     *
     * @param run
     * @param params
     */
    private static void insertPic(XWPFRun run, Map<String, Object> params) {
        if (CollectionUtils.isEmpty(params)) return;
        int width = Integer.parseInt(params.get("width").toString());
        int height = Integer.parseInt(params.get("height").toString());
        int picType = getPictureType(params.get("type").toString());
        InputStream picStream = (InputStream) params.get("content");
        try {
            run.addPicture(picStream, picType, (String) params.get("fileName"), Units.toEMU(width),
                           Units.toEMU(height));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(picStream);
        }
    }

    private static int getPictureType(String picType) {
        int res = Document.PICTURE_TYPE_PICT;
        if (!StringUtils.isBlank(picType)) {
            switch (picType.toLowerCase()) {
                case "png":
                    res = Document.PICTURE_TYPE_PNG;
                    break;
                case "jpg":
                case "jpeg":
                    res = Document.PICTURE_TYPE_JPEG;
                    break;
                case "gif":
                    res = Document.PICTURE_TYPE_GIF;
                    break;
            }
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> header = new HashMap<>();
        header.put("width", 50);
        header.put("height", 20);
        header.put("type", "jpg");
        Path picPath = Paths.get("F:\\法官电子签名.jpg");
        header.put("fileName", picPath.getFileName().toString());
        byte[] pic = IOUtils.toByteArray(new FileInputStream(picPath.toFile()));
        header.put("content", pic);
        param.put("{承办法官电子签名}", header);
        InputStream inputStream = new FileInputStream("F:\\java_workspace\\finance_court\\WebContent\\WEB-INF" +
                                                              "\\docx_template\\要素式判决-保险保证合同纠纷.docx");
        inputStream = DocxTemplateUtils.createDocxByTemplate(inputStream, param, null);
        Path path = Paths.get("f:\\test.docx");
        OutputStream outputStream = new FileOutputStream(path.toFile());
        IOUtils.copyLarge(inputStream, outputStream);
        outputStream.flush();
        outputStream.close();
        System.out.println(path.getFileName().toString());
        System.out.println(StringUtils.substringAfter(path.getFileName().toString(), "."));
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

}




