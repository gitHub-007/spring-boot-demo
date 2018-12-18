package startup.utils;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Description TODO
 * @Author Noah
 * @Date 2018-12-18
 * @Version V1.0
 */
public class DownloadUtils {
    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);

    private static final String ZIP_SUFFIX = ".zip";

    public static InputStream zipFiles(Map<String, InputStream> inputStreams) {
        ZipOutputStream out;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            //将ZipOutputStream转换成ByteArrayOutputStream在转换成ByteArrayInputStream输出
            out = new ZipOutputStream(bos);
            byte[] buf = new byte[1024];
            try {
                int len;
                for (Map.Entry<String, InputStream> inputStreamMap : inputStreams.entrySet()) {
                    String fileName = inputStreamMap.getKey();
                    InputStream inputStream = inputStreamMap.getValue();
                    out.putNextEntry(new ZipEntry(fileName));
                    while ((len = inputStream.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.flush();
                    out.closeEntry();
                    inputStream.close();
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BufferedInputStream(new ByteArrayInputStream(bos.toByteArray()));
    }

    public static void downloadZipFiles(InputStream inputStream, String downloadFileName, HttpServletRequest request,
                                        HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            response.setContentType(String.format("%s;%s", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                                  StandardCharsets.UTF_8.name()));
            String fileName = String.format("%s%s", downloadFileName, ZIP_SUFFIX);
            fileName = encodeFilename(fileName, request);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            outputStream = response.getOutputStream();
            IOUtils.copyLarge(inputStream, outputStream);
            outputStream.flush();
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

    }

    public static String encodeFilename(String filename, HttpServletRequest request) {
        String agent = request.getHeader("USER-AGENT");
        try {
            if (StringUtils.indexOf(agent, "MSIE") >= 0) {
                String newFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8.name());
                newFileName = StringUtils.replace(newFileName, "+", "%20");
                if (newFileName.length() > 150) {
                    newFileName = new String(filename.getBytes("GB2312"), StandardCharsets.ISO_8859_1.name());
                    newFileName = StringUtils.replace(newFileName, " ", "%20");
                }
                return newFileName;
            }
            if (StringUtils.indexOf(agent, "Mozilla") >= 0)
                return MimeUtility.encodeText(filename, StandardCharsets.UTF_8.name(), "B");

            return filename;
        } catch (Exception ex) {
            return filename;
        }
    }

}
