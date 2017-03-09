import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Sinogram {
    private final ImageManager imageManager;
    public float[][] sinogramMatrix;

    public Sinogram() {
        this.imageManager = new ImageManager("test_image.png");
        // TODO private sinogramMatrix
    }

    public void initializeSinogramMatrix(int steps, int detectorsSensorsCount) {
        this.sinogramMatrix = new float[steps][detectorsSensorsCount];
    }

    private class ImageManager {
        private final BufferedImage inputImage;
        // TODO wywalić imageOutput, wywalić macierz?
        //private BufferedImage outputImage;
        private float[][] forOutputImageMatrix;
        private boolean[][] forOutputImageMatrixFlag;

        private ImageManager(String imagePath) {
            this.inputImage = readInputImage(imagePath);
            //this.outputImage = new BufferedImage(getInputImageSize(), getInputImageSize(), BufferedImage.TYPE_INT_RGB);
            this.forOutputImageMatrix = new float[getInputImageSize()][getInputImageSize()];
            this.forOutputImageMatrixFlag = new boolean[getInputImageSize()][getInputImageSize()];
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

    public void insertRowToMatrix(float[] row, int index) {
        this.sinogramMatrix[index] = row;
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
                += sinogramMatrix[emmiterIndex][sensorIndex];
        imageManager.forOutputImageMatrixFlag[x + getInputImageSize()/2 - 1][y + getInputImageSize()/2 - 1] = true;

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
                        += sinogramMatrix[emmiterIndex][sensorIndex];
                imageManager.forOutputImageMatrixFlag[x + getInputImageSize()/2 - 1][y + getInputImageSize()/2 - 1] = true;
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
                        += sinogramMatrix[emmiterIndex][sensorIndex];
                imageManager.forOutputImageMatrixFlag[x + getInputImageSize()/2 - 1][y + getInputImageSize()/2 - 1] = true;
            }
        }
        return;
    }

    public void SinogramToImage() {
        saveArrayAsImage(sinogramMatrix, "GrayScale.jpg");
        filterSinogram();
        saveArrayAsImage(sinogramMatrix,"GrayScaleWithFilter.jpg");
    }

    public void saveOutputImage(String fileName) {
        saveArrayAsImage(imageManager.forOutputImageMatrix,fileName);
    }

    public void saveArrayAsImage(float[][] arrayToSave, String fileName) {
        try {
            float max = 0.0f;
            float min = Float.POSITIVE_INFINITY;
            for(int i = 0; i< arrayToSave.length; i++) {
                for(int j = 0; j< arrayToSave[i].length; j++) {
                    max = Math.max(max, arrayToSave[i][j]);
                    min = Math.min(min, arrayToSave[i][j]);
                }
            }
            BufferedImage image = new BufferedImage(arrayToSave.length, arrayToSave[0].length, BufferedImage.TYPE_INT_RGB );
            for(int i = 0; i< arrayToSave.length; i++) {
                for(int j = 0; j< arrayToSave[i].length; j++) {
                    int a = (int)(normalize(arrayToSave[i][j], max, min) * 255);
                    Color newColor = new Color(a,a,a);
                    image.setRGB(i,j,newColor.getRGB());
                }
            }
            File output = new File(fileName);
            ImageIO.write(image, "jpg", output);
        }

        catch(Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Problem saving inputImage to graphic file");
        }
    }

    public void filterSinogram() {
        // find min & max
        float max = 0.0f;
        float min = Float.POSITIVE_INFINITY;
        for(int i = 0; i< sinogramMatrix.length; i++) {
            for(int j = 0; j< sinogramMatrix[i].length; j++) {
                max = Math.max(max, sinogramMatrix[i][j]);
                min = Math.min(min, sinogramMatrix[i][j]);
            }
        }

        // normalize sinogram
        for(int i = 0; i< sinogramMatrix.length; i++) {
            for(int j = 0; j< sinogramMatrix[i].length; j++) {
                int a = (int)(normalize(sinogramMatrix[i][j], max, min) * 255);
            }
        }
        // filter
        for (int i = 0; i < sinogramMatrix.length; i++) {
            float filtered_row[] = new float[sinogramMatrix[0].length];
            for (int j = 0; j < sinogramMatrix[0].length; j++) {
                filtered_row[j] = filterKernel(i,j);
            }
            sinogramMatrix[i] = filtered_row;
        }
    }

    private float filterKernel(int emitterIndex, int detectorIndex) {
        float filter_value = (float) (-1/Math.pow(Math.PI, 2));
        float[] kernel = {0, filter_value, 1,  filter_value, 0};
        float result = 0.0f;

        for (int i = detectorIndex - 2; i <=detectorIndex +2; i++) {
            if (i >= 0 && i < sinogramMatrix[emitterIndex].length) {
                result += sinogramMatrix[emitterIndex][i] * kernel[i - (detectorIndex - 2)];
            }
        }
        return result;
    }
}