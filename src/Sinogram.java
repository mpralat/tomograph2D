import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Sinogram {
    private final ImageManager imageManager;

    public Sinogram() {
        this.imageManager = new ImageManager("test_image.png");
    }

    private class ImageManager {
        private final BufferedImage image;

        private ImageManager(String imagePath) {
            this.image = readImage(imagePath);
        }

        private BufferedImage readImage(String imagePath){
            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(imagePath));
            } catch (IOException e) {
                System.out.println("Error reading file!");
            }
            return image;
        }
        public BufferedImage getImage() {
            return image;
        }

        private int getImageSize(){
            return image.getWidth();
        }
    }


    public int getImageSize(){
        return imageManager.getImageSize();
    }
}
