package com.irwin.utils;

import com.irwin.transfer.IProgressListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ZipUtil {
    static final int BUFFER = 20 * 1024;

    /**
     * This method will take too much memory.
     *
     * @param archive
     * @param decompressDir
     * @param listener
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ZipException
     */
    public static void unZipFile(String archive, String decompressDir, IProgressListener listener) throws IOException, FileNotFoundException, ZipException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive);
            Enumeration e = zf.entries();
            int size = zf.size();
            int progress = 0;
            byte[] buffer = new byte[BUFFER];
            while (e.hasMoreElements()) {
                ZipEntry ze2 = (ZipEntry) e.nextElement();
                String entryName = ze2.getName();
                String path = decompressDir + "/" + entryName;
                if (ze2.isDirectory()) {
                    File decompressDirFile = new File(path);
                    if (!decompressDirFile.exists()) {
                        decompressDirFile.mkdirs();
                    }
                } else {
                    String fileDir = path.substring(0, path.lastIndexOf("/"));
                    File fileDirFile = new File(fileDir);
                    if (!fileDirFile.exists()) {
                        fileDirFile.mkdirs();
                    }
                    bos = new BufferedOutputStream(new FileOutputStream(decompressDir + "/" + entryName));
                    bis = new BufferedInputStream(zf.getInputStream(ze2));
                    int readCount = bis.read(buffer);
                    while (readCount != -1) {
                        bos.write(buffer, 0, readCount);
                        readCount = bis.read(buffer);
                    }
                    bos.close();
                    bos = null;
                    bis.close();
                    bis = null;
                }
                progress++;
                if (listener != null) {
                    listener.onProgress(progress, size);
                }
            }
        } finally {
            if (bos != null) {
                bos.close();
            }
            if (bis != null) {
                bis.close();
            }
            if (zf != null) {
                zf.close();
            }
        }

    }

    /**
     * A better method to replace <code>unZipFile<code/>
     * @param archive
     * @param decompressDir
     * @param listener
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ZipException
     */
    public static void unZipFileLite(String archive, String decompressDir, IProgressListener listener) throws IOException, FileNotFoundException, ZipException {
        BufferedOutputStream bos = null;
        ZipInputStream zins = null;
        try {
            File target = new File(archive);
            long totalSize = target.length();
            zins = new ZipInputStream(new FileInputStream(target));
            int progress = 0;
            byte[] buffer = new byte[BUFFER];
            ZipEntry entry = null;
            long startTime = System.currentTimeMillis();
            long now = 0;
            while ((entry = zins.getNextEntry()) != null) {
                String entryName = entry.getName();
                String path = decompressDir + "/" + entryName;
                if (entry.isDirectory()) {
                    File decompressDirFile = new File(path);
                    if (!decompressDirFile.exists()) {
                        decompressDirFile.mkdirs();
                    }
                } else {
                    String fileDir = path.substring(0, path.lastIndexOf("/"));
                    File fileDirFile = new File(fileDir);
                    if (!fileDirFile.exists()) {
                        fileDirFile.mkdirs();
                    }
                    bos = new BufferedOutputStream(new FileOutputStream(decompressDir + "/" + entryName));
                    int length = 0;
                    while ((length = zins.read(buffer)) != -1) {
                        if (length > 0) {
                            bos.write(buffer, 0, length);
                            progress += length;
                        }
                    }
                    bos.close();
                    bos = null;
                }
                now = System.currentTimeMillis();
                if (now - startTime >= 2000) {
                    if (listener != null) {
                        listener.onProgress(progress, totalSize);
                    }
                    startTime = now;
                }
            }
        } finally {
            if (bos != null) {
                bos.close();
            }
            if (zins != null) {
                zins.close();
            }
        }

    }


    /**
     * 执行压缩操作
     *
     * @param srcPath 被压缩的文件/文件夹
     */
    public static void compress(String srcPath, File zipFile) {
        File file = new File(srcPath);
        if (!file.exists()) {
            throw new RuntimeException(srcPath + "不存在！");
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new CRC32());
            ZipOutputStream out = new ZipOutputStream(cos);
            String basedir = "";
            compressByType(file, out, basedir);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断是目录还是文件，根据类型（文件/文件夹）执行不同的压缩方法
     *
     * @param file
     * @param out
     * @param basedir
     */
    private static void compressByType(File file, ZipOutputStream out, String basedir) {
        /* 判断是目录还是文件 */
        if (file.isDirectory()) {
            compressDirectory(file, out, basedir);
        } else {
            compressFile(file, out, basedir);
        }
    }

    /**
     * 压缩一个目录
     *
     * @param dir
     * @param out
     * @param basedir
     */
    private static void compressDirectory(File dir, ZipOutputStream out, String basedir) {
        if (!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {    
            /* 递归 */
            compressByType(files[i], out, basedir + dir.getName() + "/");
        }
    }

    /**
     * 压缩一个文件
     *
     * @param file
     * @param out
     * @param basedir
     */
    private static void compressFile(File file, ZipOutputStream out, String basedir) {
        if (!file.exists()) {
            return;
        }
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            ZipEntry entry = new ZipEntry(basedir + file.getName());
            out.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            bis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}  