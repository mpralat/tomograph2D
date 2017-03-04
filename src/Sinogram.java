import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Sinogram {
    private final ImageManager imageManager;
    public ArrayList<ArrayList<Float>> matrix;

    public Sinogram() {
        this.imageManager = new ImageManager("test_image.png");
        // TODO private matrix
        this.matrix = new ArrayList<>();
    }

    private class ImageManager {
        private final BufferedImage inputImage;
        // TODO wywalić imageOutput, wywalić macierz?
        private BufferedImage outputImage;
        private float forOutputImageMatrix[][];

        private ImageManager(String imagePath) {
            this.inputImage = readInputImage(imagePath);
            this.outputImage = new BufferedImage(getInputImageSize(), getInputImageSize(), BufferedImage.TYPE_INT_RGB);
            this.forOutputImageMatrix = new float[getInputImageSize()][getInputImageSize()];
        }

        private BufferedImage readInputImage(String imagePath){
            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(imagePath));
            } catch (IOException e) {
                System.out.println("Error reading file!");
            }
            return image;
        }
        public BufferedImage getInputImage() {
            return inputImage;
        }

        private int getInputImageSize(){
            return inputImage.getWidth();
        }
    }

    public int getInputImageSize(){
        return imageManager.getInputImageSize();
    }

    public void insertRowToMatrix(ArrayList<Float> row) {
        this.matrix.add(row);
    }

    static float normalize(float value, float max, float min) {
        return ((value - min)/(max -min));
    }

    public float BresenhamAlgorithm(int emitterPosX, int emitterPosY, int sensorPosX, int sensorPosY) throws IOException {
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

        RGBValue = imageManager.inputImage.getRGB(x + imageManager.getInputImageSize()/2 ,y + imageManager.getInputImageSize()/2 )&0xFF;
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
                RGBValue = imageManager.inputImage.getRGB(x + imageManager.getInputImageSize()/2 ,y + imageManager.getInputImageSize()/2 )&0xFF;
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
                RGBValue = imageManager.inputImage.getRGB(x + imageManager.getInputImageSize()/2 ,y + imageManager.getInputImageSize()/2 )&0xFF;
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
            System.out.println("Problem saving inputImage to graphic file");
        }
    }

    //-----------------------------------------------------------------------------------

    public void BresenhamAlgorithm2(int emitterPosX, int emitterPosY, int sensorPosX, int sensorPosY, int emmiterIndex, int sensorIndex) throws IOException {
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

        imageManager.forOutputImageMatrix[x + getInputImageSize()/2 - 1][y + getInputImageSize()/2 - 1]
                += matrix.get(emmiterIndex).get(sensorIndex);

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
                imageManager.forOutputImageMatrix[x + getInputImageSize()/2 - 1][y + getInputImageSize()/2 - 1]
                        += matrix.get(emmiterIndex).get(sensorIndex);
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
                imageManager.forOutputImageMatrix[x + getInputImageSize()/2 - 1][y + getInputImageSize()/2 - 1]
                        += matrix.get(emmiterIndex).get(sensorIndex);
            }
        }
        return;
    }


    public void saveOutputImage() {
        try {
            float min = Float.POSITIVE_INFINITY;
            float max = 0.0f;
            for (int i = 0; i < imageManager.outputImage.getHeight(); i++) {
                for (int j = 0; j < imageManager.outputImage.getHeight(); j++) {
                    min = Math.min(min, imageManager.forOutputImageMatrix[i][j]);
                    max = Math.max(max, imageManager.forOutputImageMatrix[i][j]);
                }
            }
            for (int i = 0; i < imageManager.outputImage.getHeight(); i++) {
                for (int j = 0; j < imageManager.outputImage.getHeight(); j++) {
                    int a = (int)(normalize(imageManager.forOutputImageMatrix[i][j], max, min) * 255);
                    Color newColor = new Color(a,a,a);
                    imageManager.outputImage.setRGB(i,j,newColor.getRGB());
                }
            }

            File output = new File("output.jpg");
            ImageIO.write(imageManager.outputImage, "jpg", output);
        }

        catch(Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Problem saving outputImage to graphic file");
        }
    }

}
