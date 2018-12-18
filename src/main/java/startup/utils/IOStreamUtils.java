package startup.utils;

import java.io.*;
import java.util.Objects;

/**
 * @author Noah
 * @description IOStreamUtils
 * @created at 2018-12-11 18:56:35
 **/
public class IOStreamUtils {
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
}


