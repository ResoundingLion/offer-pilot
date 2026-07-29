package com.offerpilot.user.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * PDF 文本提取服务
 * <p>
 * 使用 Apache PDFBox 从 PDF 文件中提取纯文本。
 * 只支持文本型 PDF，扫描件（图片型 PDF）会返回空文本。
 * 提取结果用于 AI 简历分析。
 */
@Slf4j
@Service
public class PdfService {

    /**
     * 从 PDF 输入流中提取纯文本
     *
     * @param inputStream PDF 文件输入流（不会关闭流，由调用方负责）
     * @return 提取的纯文本；若提取失败返回 null
     */
    public String extractText(InputStream inputStream) {
        if (inputStream == null) {
            log.warn("PDF 提取失败：输入流为 null");
            return null;
        }

        try {
            // InputStream → byte[]（PDFBox 3.x Loader.loadPDF 只接受 byte[] / File / RandomAccessRead）
            byte[] bytes = readAllBytes(inputStream);

            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                String text = stripper.getText(document);

                if (text == null || text.isBlank()) {
                    log.warn("PDF 提取结果为空——可能为扫描件或加密 PDF");
                    return null;
                }

                log.info("PDF 文本提取成功，长度={} 字符", text.length());
                return text.trim();
            }
        } catch (IOException e) {
            log.error("PDF 文本提取失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 InputStream 读取全部字节
     */
    private byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int len;
        while ((len = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, len);
        }
        return buffer.toByteArray();
    }
}
