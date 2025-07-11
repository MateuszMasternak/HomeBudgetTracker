package com.rainy.homebudgettracker.images;

import com.rainy.homebudgettracker.handler.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
public class ImageCompressor {
    private static final float COMPRESSION_QUALITY = 0.5f;

    public static File compressImage(File inputFile) {
        ImageWriter writer = null;
        try {
            BufferedImage image = ImageIO.read(inputFile);
            // JPEG does not support alpha channel
            image = removeAlphaChannel(image);

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(COMPRESSION_QUALITY);

            File outputFile = File.createTempFile("compressed", ".jpg");
            try (FileOutputStream fos = new FileOutputStream(outputFile);
                 ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
                return outputFile;
            }
        } catch (IOException e) {
            log.error("Error compressing image: {}", e.getMessage(), e);
            throw new FileProcessingException("Failed to compress image", e);
        } finally {
            if (writer != null) {
                writer.dispose();
            }
        }
    }

    private static BufferedImage removeAlphaChannel(BufferedImage img) {
        if (!img.getColorModel().hasAlpha()) {
            return img;
        }
        BufferedImage target = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return target;
    }
}
