package startup.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.lang3.StringUtils;

public class SplitFile {
    private static int BYTE = 1;
    private static int KB = 1024 * BYTE;
    private static int MB = 1024 * KB;

    enum UNIT {
        KB, MB;
    }

    public static void main(String[] args) {
        String SOURCE_FILE = "F:\\CMMI4\\课件\\基线建立-1.wmv"; // 文件的路径
        String OUT_FILE = "F:\\out-1.wmv"; // 文件的路径
        Long count = getSplitFile(SOURCE_FILE, UNIT.KB);
        if (count <= -1)
            return;
        merge(OUT_FILE, SOURCE_FILE, count.intValue());
    }

    /**
     * 文件分割方法
     */
    public static Long getSplitFile(String sourceFile, UNIT unit) {
        // 文件的路径
        String file = sourceFile;
        // 文件分割的份数
        long count = -1;
        // 获取目标文件 预分配文件所占的空间 在磁盘中创建一个指定大小的文件 r 是只读
        try (RandomAccessFile raf = new RandomAccessFile(new File(file), "r")) {
            long length = raf.length();// 文件的总长度
            switch (unit) {
                case KB:
                    count = length / KB;
                    break;
                case MB:
                    count = length / MB;
                    break;
            }
            long maxSize = length / count;// 文件切片后的长度
            long offSet = 0L;// 初始化偏移量
            for (int i = 0; i < count; i++) {
                long end = (i + 1) * maxSize;
                // 最后一片单独处理
                if (i == count - 1) {
                    getWrite(file, i, offSet, length);
                } else {
                    offSet = getWrite(file, i, offSet, end);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 指定文件每一份的边界，写入不同文件中
     * 
     * @param file
     *            源文件
     * @param index
     *            源文件的顺序标识
     * @param begin
     *            开始指针的位置
     * @param end
     *            结束指针的位置
     * @return long
     */
    public static long getWrite(String file, int index, long begin, long end) {
        String fileName = StringUtils.substringBeforeLast(file, ".");
        long endPointer = 0L;
        try (
            // 申明文件切割后的文件磁盘
            RandomAccessFile in = new RandomAccessFile(new File(file), "r");
            // 定义一个可读，可写的文件并且后缀名为.tmp的二进制文件
            RandomAccessFile out = new RandomAccessFile(new File(fileName + "_" + index + ".tmp"), "rw")) {
            // 申明具体每一文件的字节数组
            byte[] b = new byte[1024];
            int n = 0;
            // 从指定位置读取文件字节流
            in.seek(begin);
            // 判断文件流读取的边界
            while (in.getFilePointer() <= end && (n = in.read(b)) != -1) {
                // 从指定每一份文件的范围，写入不同的文件
                out.write(b, 0, n);
            }
            // 定义当前读取文件的指针
            endPointer = in.getFilePointer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return endPointer;
    }

    /**
     * 文件合并
     * 
     * @param file
     *            指定合并文件
     * @param tempFile
     *            分割前的文件名
     * @param tempCount
     *            文件个数
     */
    public static void merge(String file, String tempFile, int tempCount) {
        String fileName = StringUtils.substringBeforeLast(tempFile, ".");
        try (// 申明随机读取文件RandomAccessFile
            RandomAccessFile raf = new RandomAccessFile(new File(file), "rw")) {
            // 开始合并文件，对应切片的二进制文件
            for (int i = 0; i < tempCount; i++) {
                // 读取切片文件
                RandomAccessFile reader = new RandomAccessFile(new File(fileName + "_" + i + ".tmp"), "r");
                byte[] b = new byte[1024];
                int n = 0;
                // 先读后写
                while ((n = reader.read(b)) != -1) {// 读
                    raf.write(b, 0, n);// 写
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}