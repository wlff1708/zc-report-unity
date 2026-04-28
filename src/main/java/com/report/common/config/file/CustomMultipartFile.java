package com.report.common.config.file;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.IdUtil;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;

/**
 * MultipartFile 实现类，用于构造文件上传请求
 */
public class CustomMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public CustomMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        Files.write(dest.toPath(), content);
    }

    /**
     * 根据文件路径构造 MultipartFile
     * 文件路径存在则直接引用，丢失则在固定目录创建随机文件占位
     *
     * @param baseDir  临时目录基础路径
     * @param filePath 文件路径
     * @return MultipartFile 对象
     */
    public static MultipartFile fromPath(String baseDir, String filePath) {
        boolean isTemp = filePath == null;
        File uploadFile = isTemp
                ? new File(baseDir + IdUtil.nanoId() + ".txt")
                : new File(filePath);

        try {
            byte[] content = Files.readAllBytes(uploadFile.toPath());
            return new CustomMultipartFile(
                    "file",
                    uploadFile.getName(),
                    MediaType.MULTIPART_FORM_DATA_VALUE,
                    content
            );
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }
}
