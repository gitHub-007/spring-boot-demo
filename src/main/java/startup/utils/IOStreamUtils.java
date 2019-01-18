package startup.utils;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Noah
 * @description IOStreamUtils
 * @created at 2018-12-11 18:56:35
 **/
public class IOStreamUtils {

    private static byte[] ZIP_HEADER_1 = new byte[]{80, 75, 3, 4};
    private static byte[] ZIP_HEADER_2 = new byte[]{80, 75, 5, 6};

    private IOStreamUtils() {
    }

    public static ByteArrayOutputStream parse(InputStream in) throws Exception {
        Objects.requireNonNull(in);
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        int ch;
        while ((ch = in.read()) != -1) {
            swapStream.write(ch);
        }
        swapStream.flush();
        in.close();
        return swapStream;
    }

    public static ByteArrayInputStream parse(OutputStream out) {
        Objects.requireNonNull(out);
        ByteArrayOutputStream baos;
        ByteArrayInputStream swapStream = null;
        try {
            baos = (ByteArrayOutputStream) out;
            swapStream = new ByteArrayInputStream(baos.toByteArray());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return swapStream;
    }

    public static String parseString(InputStream in) throws Exception {
        Objects.requireNonNull(in);
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        int ch;
        while ((ch = in.read()) != -1) {
            swapStream.write(ch);
        }
        return swapStream.toString();
    }

    public static String parseString(OutputStream out) throws Exception {
        Objects.requireNonNull(out);
        ByteArrayOutputStream baos = (ByteArrayOutputStream) out;
        ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
        return swapStream.toString();
    }

    public static ByteArrayInputStream parseInputStream(String in) throws Exception {
        Objects.requireNonNull(in);
        ByteArrayInputStream input = new ByteArrayInputStream(in.getBytes());
        return input;
    }

    public static ByteArrayOutputStream parseOutputStream(String in) throws Exception {
        return parse(parseInputStream(in));
    }

    /**
     * 是否式zip文件
     *
     * @param file
     * @return
     */
    public static boolean isArchiveFile(File file) {

        if (file == null) {
            return false;
        }
        if (file.isDirectory()) {
            return false;
        }
        boolean isArchive = false;
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            byte[] buffer = new byte[4];
            int length = input.read(buffer, 0, 4);
            if (length == 4) {
                isArchive = (Arrays.equals(ZIP_HEADER_1, buffer)) || (Arrays.equals(ZIP_HEADER_2, buffer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(input);
        }

        return isArchive;
    }
}


