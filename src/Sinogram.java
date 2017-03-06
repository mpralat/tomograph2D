import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Sinogram {
    private final ImageManager imageManager;
    private ArrayList<ArrayList<Float>> matrix;

    public Sinogram() {
        this.imageManager = new ImageManager("res/test_image.png");
        this.matrix = new ArrayList<>();
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

    public void insertRowToMatrix(ArrayList<Float> row) {
        this.matrix.add(row);
    }

    static float normalize(float value, float max, float min) {
        return ((value - min)/(max -min));
    }

    public float BresenhamAlgorithm(int emitterPosX, int emitterPosY, int sensorPosX, int sensorPosY) {
        float colour_value = 0.0f;
        float RGBValue = 0.0f;

        //System.out.println("sensorPosX = " + sensorPosX + " sensorPosY = " + sensorPosY);

        int d, dx, dy, ai, bi, xi, yi;
        int x = emitterPosX, y = emitterPosY;
        // x way of drawing
        if (emitterPosX < sensorPosX) {
            xi = 1;
            dx = sensorPosX - emitterPosX;
        } else {
            xi = -1;
            dx = emitterPosX - sensorPosX;
        }
        // y way of drawing
        if (emitterPosY < sensorPosY) {
            yi = 1;
            dy = sensorPosY - emitterPosY;
        } else {
            yi = -1;
            dy = emitterPosY - sensorPosY;
        }

        RGBValue = imageManager.image.getRGB(x + imageManager.getImageSize()/2 ,y + imageManager.getImageSize()/2 )&0xFF;
        colour_value += normalize(RGBValue, 255.0f, 0.0f);

        // OX axis
        if (dx > dy) {
            ai = (dy - dx) * 2;
            bi = dy * 2;
            d = bi - dx;
            // iterate x
            while (x != sensorPosX) {
                if (d >= 0) {
                    x += xi;
                    y += yi;
                    d += ai;
                } else {
                    d += bi;
                    x += xi;
                }
                RGBValue = imageManager.image.getRGB(x + imageManager.getImageSize()/2 ,y + imageManager.getImageSize()/2 )&0xFF;
                colour_value += normalize(RGBValue, 255.0f, 0.0f);
            }
        }
        // OY axis
        else {
            ai = (dx - dy) * 2;
            bi = dx * 2;
            d = bi - dy;
            // iterate y
            while (y != sensorPosY) {
                if (d >= 0) {
                    x += xi;
                    y += yi;
                    d += ai;
                } else {
                    d += bi;
                    y += yi;
                }
                RGBValue = imageManager.image.getRGB(x + imageManager.getImageSize()/2 ,y + imageManager.getImageSize()/2 )&0xFF;
                colour_value += normalize(RGBValue, 255.0f, 0.0f);
            }
        }
        return colour_value;
    }

    public void saveSinogramAsImage() {
        // TODO ulepszyć normalizację w matrixie
        float max = 0.0f;
        float min = Float.POSITIVE_INFINITY;
        for(int i=0; i< matrix.size(); i++) {
            for(int j=0; j< matrix.get(i).size(); j++) {
                if (matrix.get(i).get(j) > max) max = matrix.get(i).get(j);
                if (matrix.get(i).get(j) < min) min = matrix.get(i).get(j);
            }
        }


        try {
            // TODO ulepszyć rozmiar obrazu
            BufferedImage image = new BufferedImage(matrix.size(), matrix.get(0).size(), BufferedImage.TYPE_INT_RGB );
            for(int i=0; i< matrix.size(); i++) {
                for(int j=0; j< matrix.get(i).size(); j++) {
                    int a = (int)(normalize(matrix.get(i).get(j), max, min) * 255);
                    Color newColor = new Color(a,a,a);
                    image.setRGB(i,j,newColor.getRGB());
                }
            }
            File output = new File("GrayScale.jpg");
            ImageIO.write(image, "jpg", output);
        }

        catch(Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Problem saving image to graphic file");
        }
    }

}
