package com.exam.app.submission.service;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class GrayscaleImageService {

  @SneakyThrows
  public File convertToGrayscale(File inputFile, String format) {
    BufferedImage original = ImageIO.read(inputFile);

    BufferedImage grayImage =
        new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

    Graphics g = grayImage.getGraphics();
    g.drawImage(original, 0, 0, null);
    g.dispose();

    File outputFile = File.createTempFile("processed-", "." + format);
    ImageIO.write(grayImage, format, outputFile);
    return outputFile;
  }
}
