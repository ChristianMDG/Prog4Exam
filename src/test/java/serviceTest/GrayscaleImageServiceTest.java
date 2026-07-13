package serviceTest;

import com.exam.app.submission.service.GrayscaleImageService;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class GrayscaleImageServiceTest {

    private final GrayscaleImageService grayscaleImageService = new GrayscaleImageService();

    @Test
    void convertToGrayscale_shouldProduceGrayscaleImage() throws Exception {
        // given : une image colorée 10x10 créée en mémoire
        BufferedImage coloredImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                coloredImage.setRGB(x, y, 0xFF0000); // rouge pur
            }
        }
        File inputFile = File.createTempFile("test-input-", ".png");
        ImageIO.write(coloredImage, "png", inputFile);

        // when
        File outputFile = grayscaleImageService.convertToGrayscale(inputFile, "png");

        // then
        assertThat(outputFile).exists();
        BufferedImage result = ImageIO.read(outputFile);
        assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_BYTE_GRAY);
        assertThat(result.getWidth()).isEqualTo(10);
        assertThat(result.getHeight()).isEqualTo(10);
    }

    @Test
    void convertToGrayscale_shouldSupportJpgFormat() throws Exception {
        BufferedImage coloredImage = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        File inputFile = File.createTempFile("test-input-", ".jpg");
        ImageIO.write(coloredImage, "jpg", inputFile);

        File outputFile = grayscaleImageService.convertToGrayscale(inputFile, "jpg");

        assertThat(outputFile).exists();
        assertThat(outputFile.getName()).endsWith(".jpg");
    }
}