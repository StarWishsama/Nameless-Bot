package top.starwish.namelessbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * @author 夕橘子-O & Stiven.ding
 * @see https://www.cnblogs.com/XiOrang/p/5652875.html
 */

public class FileProcess {

    // main 函数仅供调试使用
    public static void main(String[] args) {
        UUID uuid = UUID.randomUUID();
        try {
            System.out.println(readFile(System.getProperty("user.dir") + "\\build\\myfile.txt"));
        } catch (Exception e) {
            createFile(System.getProperty("user.dir") + "\\build\\myfile.txt", uuid.toString());
        }
    }

    /**
     * 创建文件
     * 
     * @param path        文件路径，如 C:\a.txt
     * @param filecontent 文件内容
     * @return 是否创建成功，成功则返回true
     */

    public static boolean createFile(String path, String filecontent) {
        Boolean bool = false;
        File file = new File(path);
        try {
            // 如果文件不存在，则创建新的文件
            if (!file.exists()) {
                file.createNewFile();
                bool = true;
                System.out.println("success create file, the file is " + path);
                // 创建文件成功后，写入内容到文件里
                writeFileContent(path, filecontent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bool;
    }

    /**
     * 按行读取文件
     * 
     * @return 读取到的内容（仅支持单行）
     * @throws IOException
     */

    public static String readFile(String path) throws IOException {
        File file = new File(path);
        String tempString = "";
        // 判断文件是否存在
        if (!file.exists()) {
            System.out.println(path + "  文件不存在。");
            throw new IOException("File not exist");
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            tempString = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException localIOException1) {
                }
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException localIOException2) {
                }
        }
        return tempString;
    }

    /**
     * 向文件中写入内容 正常情况不应调用
     * 
     * @param filepath 文件路径与名称
     * @param newstr   写入的内容
     * @throws IOException
     */
    private static boolean writeFileContent(String filepath, String newstr) throws IOException {
        Boolean bool = false;
        String filein = newstr + "\r\n";// 新写入的行，换行
        String temp = "";

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileOutputStream fos = null;
        PrintWriter pw = null;
        try {
            File file = new File(filepath);// 文件路径(包括文件名称)
            // 将文件读入输入流
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buffer = new StringBuffer();

            // 文件原有内容
            for (; (temp = br.readLine()) != null;) {
                buffer.append(temp);
                // 行与行之间的分隔符 相当于“\n”
                buffer = buffer.append(System.getProperty("line.separator"));
            }
            buffer.append(filein);

            fos = new FileOutputStream(file);
            pw = new PrintWriter(fos);
            pw.write(buffer.toString().toCharArray());
            pw.flush();
            bool = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 不要忘记关闭
            if (pw != null) {
                pw.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        return bool;
    }

    /**
     * 删除文件
     * 
     * @param paht 文件路径
     * @return
     */
    public static boolean delFile(String path) {
        Boolean bool = false;
        File file = new File(path);
        try {
            if (file.exists()) {
                file.delete();
                bool = true;
            }
        } catch (Exception e) {
        }
        return bool;
    }

}