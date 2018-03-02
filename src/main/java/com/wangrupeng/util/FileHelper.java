package com.wangrupeng.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import sun.misc.BASE64Decoder;

/**
 * Created by taozhiheng on 16-7-12.
 * helper to handle local file
 */
public class FileHelper {


  /**
   * download image from url.
   */
  public static boolean download(OutputStream os, String urlString) {
    if (urlString == null) {
      return false;
    }
    try {
      URL url = new URL(urlString);
      URLConnection conn = url.openConnection();
      InputStream is = conn.getInputStream();
      byte[] buf = new byte[1024];
      int size;
      while ((size = is.read(buf)) > -1) {
        //循环读取
        os.write(buf, 0, size);
      }
      os.flush();
      is.close();
      return true;
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 通过文件路径读取文件内容.
   * @param filePath 文件路径
   * @return 文件内容
   */
  public static String readString(String filePath) {
    String text = "";
    File file = new File(filePath);
    try {
      FileInputStream fis = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
      char[] buffer = new char[512];
      int count;
      StringBuilder builder = new StringBuilder();
      while ((count = reader.read(buffer)) != -1) {
        builder.append(buffer, 0, count);
      }
      text = builder.toString();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return text;
  }

  /**
   * 将图片的BASE64编码转为二进制.
   * @param src 图片的BASE64编码
   * @return
   */
  public static byte[] base64Decode(String src) {
    if (src == null) {
      return null;
    }
    byte[] data;
    BASE64Decoder decoder = new BASE64Decoder();
    try {
      data = decoder.decodeBuffer(src);
      return data;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 通过文件名读取文件并存储到传入的OutputStream对象中.
   * @param filename 文件名
   * @param os 文件的OutputStream对象
   * @throws IOException IO异常
   */
  public static void readByteFile(String filename, OutputStream os) throws IOException {
    readByteFile(new File(filename), os);
  }

  /**
   * 通过文件名读取文件并存储到传入的OutputStream对象中.
   * @param file 文件
   * @param os 文件的OutputStream对象
   * @throws IOException IO异常
   */
  public static void readByteFile(File file, OutputStream os) throws IOException {
    InputStream is = new FileInputStream(file);
    byte[] buf = new byte[1024];
    int size;
    while ((size = is.read(buf)) > -1) {
      os.write(buf, 0, size);
    }
    os.flush();
    is.close();
  }

  public static void writeByteFile(byte[] data, String filename) throws IOException {
    writeByteFile(data, new File(filename));
  }

  public static void writeByteFile(byte[] data, File file) throws IOException {
    writeByteFile(data, new FileOutputStream(file), true);
  }

  /**
   * 将文件的二进制数据写进OutputStream对象中.
   * @param data 文件二进制
   * @param os OutputStream对象
   * @param close 是否将OutputStream对象关闭
   * @throws IOException IO异常
   */
  public static void writeByteFile(byte[] data, OutputStream os, boolean close) throws IOException {
    os.write(data);
    if (close) {
      os.close();
    }
  }
}
