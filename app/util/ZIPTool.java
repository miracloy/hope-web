package util;

import models.dbmanager.GlobalTool;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

/**
 * Created by shanmao on 15-12-8.
 */
public class ZIPTool {
    /**
     * 把文件压缩成zip格式
     * @param files         需要压缩的文件
     * @param zipFilePath 压缩后的zip文件路径   ,如"D:/test/aa.zip";
     */
    public static void compressFiles2Zip(File[] files,String zipFilePath) {
        if(files != null && files.length >0) {
            if(isEndsWithZip(zipFilePath)) {
                ZipArchiveOutputStream zaos = null;
                try {
                    File zipFile = new File(zipFilePath);
                    zaos = new ZipArchiveOutputStream(zipFile);
                    //Use Zip64 extensions for all entries where they are required
                    zaos.setUseZip64(Zip64Mode.AsNeeded);

                    //将每个文件用ZipArchiveEntry封装
                    //再用ZipArchiveOutputStream写到压缩文件中
                    for(File file : files) {
                        if(file != null) {
                            ZipArchiveEntry zipArchiveEntry  = new ZipArchiveEntry(file,file.getName());
                            zaos.putArchiveEntry(zipArchiveEntry);
                            InputStream is = null;
                            try {
                                is = new BufferedInputStream(new FileInputStream(file));
                                byte[] buffer = new byte[1024 * 5];
                                int len = -1;
                                while((len = is.read(buffer)) != -1) {
                                    //把缓冲区的字节写入到ZipArchiveEntry
                                    zaos.write(buffer, 0, len);
                                }
                                //Writes all necessary data for this entry.
                                zaos.closeArchiveEntry();
                            }catch(Exception e) {
                                throw new RuntimeException(e);
                            }finally {
                                if(is != null)
                                    is.close();
                            }

                        }
                    }
                    zaos.finish();
                }catch(Exception e){
                    throw new RuntimeException(e);
                }finally {
                    try {
                        if(zaos != null) {
                            zaos.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

        }

    }


    public static void compressFiles2Zip(String[] paths,String zipFilePath) {
        File[] files = new File[paths.length];
        for(int i=0;i<paths.length;i++){
            File tmpf = new File(paths[i]);
            files[i] = tmpf;
        }
        compressFiles2Zip(files,zipFilePath);
    }

    /**
     * 把zip文件解压到指定的文件夹
     * @param zipFilePath   zip文件路径, 如 "D:/test/aa.zip"
     * @param saveFileDir   解压后的文件存放路径, 如"D:/test/"
     */
    public static void decompressZip(String zipFilePath,String saveFileDir) {
        if(isEndsWithZip(zipFilePath)) {
            File file = new File(zipFilePath);
            if(file.exists()) {
                InputStream is = null;
                //can read Zip archives
                ZipArchiveInputStream zais = null;
                try {
                    is = new FileInputStream(file);
                    zais = new ZipArchiveInputStream(is);
                    ArchiveEntry  archiveEntry = null;
                    //把zip包中的每个文件读取出来
                    //然后把文件写到指定的文件夹
                    while((archiveEntry = zais.getNextEntry()) != null) {
                        //获取文件名
                        String entryFileName = archiveEntry.getName();
                        //构造解压出来的文件存放路径
                        String entryFilePath = saveFileDir + entryFileName;
                        byte[] content = new byte[(int) archiveEntry.getSize()];
                        zais.read(content);
                        OutputStream os = null;
                        try {
                            //把解压出来的文件写到指定路径
                            File entryFile = new File(entryFilePath);
                            os = new BufferedOutputStream(new FileOutputStream(entryFile));
                            os.write(content);
                        }catch(IOException e) {
                            throw new IOException(e);
                        }finally {
                            if(os != null) {
                                os.flush();
                                os.close();
                            }
                        }

                    }
                }catch(Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    try {
                        if(zais != null) {
                            zais.close();
                        }
                        if(is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * 把一个zip文件解压到一个指定的目录中
     * @param zipFileName   zip文件抽象地址
     * @param outputDir     目录绝对地址
     */
    public static void unZipToFolder(String zipFileName, String outputDir) throws IOException {
        File zipFile = new File(zipFileName);
        if (zipFile.exists()) {
            outputDir = outputDir + File.separator;
            FileTool.createDestDirectoryIfNotExists(outputDir);
            //FileUtils.forceMkdir(new File(outputDir));
            GlobalTool.logger.error("系统默认编码格式:" + System.getProperty("file.encoding"));
            ZipFile zf = new ZipFile(zipFile, System.getProperty("file.encoding"));
            Enumeration zipArchiveEntries = zf.getEntries();
            while (zipArchiveEntries.hasMoreElements()) {
                ZipArchiveEntry zipArchiveEntry = (ZipArchiveEntry) zipArchiveEntries.nextElement();
                if (zipArchiveEntry.isDirectory()) {
                    FileTool.createDestDirectoryIfNotExists(outputDir + zipArchiveEntry.getName() + File.separator);
                    //FileUtils.forceMkdir(new File(outputDir + zipArchiveEntry.getName() + File.separator));
                } else {
                    try {
                        IOUtils.copy(zf.getInputStream(zipArchiveEntry), Files.newOutputStream(Paths.get(outputDir + zipArchiveEntry.getName())));
                    } catch (Exception e) {
                        throw new IOException("解压这个文件时发生错误:" + zipArchiveEntry.getName());
                    }
                }
            }
        } else {
            throw new IOException("解压zip包失败:\t" + zipFileName);
        }
    }

    /**
     * 判断文件名是否以.zip为后缀
     * @param fileName        需要判断的文件名
     * @return 是zip文件返回true,否则返回false
     */
    public static boolean isEndsWithZip(String fileName) {
        boolean flag = false;
        if(fileName != null && !"".equals(fileName.trim())) {
            if(fileName.endsWith(".ZIP")||fileName.endsWith(".zip")){
                flag = true;
            }
        }
        return flag;
    }
}
