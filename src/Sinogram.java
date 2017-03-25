

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.SocketPermission;
import java.util.*;

import java.util.Arrays;


public class Sinogram {
    private final ImageManager imageManager;
    public float[][] sinogramMatrix;
    int[][] sumCount;
    private final Controller controller;

    public Sinogram(Controller controller) {
        this.controller = controller;
        this.imageManager = new ImageManager("src/horse.png");
        // TODO private sinogramMatrix
    }

    public void initializeSinogramMatrix(int steps, int detectorsSensorsCount) {
        this.sinogramMatrix = new float[steps][detectorsSensorsCount];
        //this.sumCount = new int[getInputImageSize()][getInputImageSize()];
        this.sumCount = new int[steps][detectorsSensorsCount];
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


    public float BresenhamAlgorithm(int emitterIndex, int sensorIndex, Tomograph tomograph, boolean sinogramCreation) throws IOException {
        float colour_value = 0.0f;
        float RGBValue = 0.0f;
        //System.out.println("sensorPosX = " + sensorPosX + " sensorPosY = " + sensorPosY);
        int emitterPosX = tomograph.getEmitterPosX(emitterIndex);
        int emitterPosY = tomograph.getEmitterPosY(emitterIndex);
        int sensorPosX = tomograph.getDetectorsSensorPosX(emitterIndex, sensorIndex);
        int sensorPosY = tomograph.getDetectorsSensorPosY(emitterIndex, sensorIndex);

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
//
//        RGBValue = imageManager.inputImage.getRGB(x + imageManager.getInputImageSize()/2 ,y + imageManager.getInputImageSize()/2 )&0xFF;
//        colour_value += normalize(RGBValue, 255.0f, 0.0f);
        if(sinogramCreation) {
            colour_value+=sinogramCreation(x,y);
            //sumCount[x + getInputImageSize() / 2][y + getInputImageSize() / 2] += 1;
            sumCount[emitterIndex][sensorIndex] += 1;
            imageManager.forOutputImageMatrixFlag[x + getInputImageSize() / 2][y + getInputImageSize() / 2] = true;
        }
        else sinogramReversion(x,y,emitterIndex,sensorIndex);

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
                if(sinogramCreation) {
                    colour_value+=sinogramCreation(x,y);
                    //sumCount[x + getInputImageSize() / 2][y + getInputImageSize() / 2] += 1;
                    sumCount[emitterIndex][sensorIndex] += 1;
                    imageManager.forOutputImageMatrixFlag[x + getInputImageSize() / 2][y + getInputImageSize() / 2] = true;
                }
                else sinogramReversion(x,y,emitterIndex,sensorIndex);
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
                if(sinogramCreation) {
                    colour_value+=sinogramCreation(x,y);
                    //sumCount[x + getInputImageSize() / 2][y + getInputImageSize() / 2] += 1;
                    sumCount[emitterIndex][sensorIndex] += 1;
                    imageManager.forOutputImageMatrixFlag[x + getInputImageSize() / 2][y + getInputImageSize() / 2] = true;
                }
                else sinogramReversion(x,y,emitterIndex,sensorIndex);
            }
        }
        return colour_value;
    }
    public float sinogramCreation(int x, int y){
        float RGBValue = imageManager.inputImage.getRGB(x + imageManager.getInputImageSize()/2 ,y + imageManager.getInputImageSize()/2 )&0xFF;
        //return normalize(RGBValue, 255.0f, 0.0f);
        return RGBValue;
    }

    public void sinogramReversion(int x, int y, int emitterIndex, int sensorIndex){
        imageManager.forOutputImageMatrix[x + getInputImageSize()/2 - 1][y + getInputImageSize()/2 - 1]
                += (sinogramMatrix[emitterIndex][sensorIndex]); // / Math.max(1, sumCount[x + getInputImageSize()/2 - 1][y + getInputImageSize()/2 - 1]));
    }

    public void ResultAsImage() {
        saveArrayAsImage(imageManager.forOutputImageMatrix,"output/output.jpg");
    }

    public void SinogramToImage() {
        saveArrayAsImage(sinogramMatrix, "output/GrayScale.jpg");

        normalizeSinogram();
        saveArrayAsImage(sinogramMatrix, "output/GrayScaleNormalized.jpg");

        filterSinogram();
        saveArrayAsImage(sinogramMatrix, "output/GrayScaleWithFilterd.jpg");

        WritableImage sinogramImage = saveArrayAsImage(sinogramMatrix,"output/GrayScaleZFinal.jpg");
        controller.getSinogramImage().setImage(sinogramImage);
    }

    public void saveOutputImage(String fileName) {
        WritableImage finalImage = saveArrayAsImage(imageManager.forOutputImageMatrix,fileName);
        controller.getFinalImage().setImage(finalImage);
    }

    private void normalizeSinogram() {
        float max = 0.0f;
        float min = Float.POSITIVE_INFINITY;
        for (int i = 0; i < sinogramMatrix.length; i++) {
            for (int j=0; j < sinogramMatrix[0].length; j++) {
                // sinogram values divided by InputImageSize:
                sinogramMatrix[i][j] /= (imageManager.getInputImageSize()-1);
                // sinogram values divided by SumCount:
                // sinogramMatrix[i][j] /= Math.max(1, sumCount[i][j]);
                max = Math.max(max, sinogramMatrix[i][j]);
                min = Math.min(min, sinogramMatrix[i][j]);
            }
        }

        System.out.println("min = " + min + " max = " + max + " when normalizing sinogram" );
        for(int i = 0; i< sinogramMatrix.length; i++) {
            for(int j = 0; j< sinogramMatrix[i].length; j++) {
                sinogramMatrix[i][j] = (int)(normalize(sinogramMatrix[i][j], max, min) * 255);
            }
        }
    }


    public WritableImage saveArrayAsImage(float[][] arrayToSave, String fileName) {
        try {
            float max = 0.0f;
            float min = Float.POSITIVE_INFINITY;
            for(int i = 0; i< arrayToSave.length; i++) {
                for(int j = 0; j< arrayToSave[i].length; j++) {
                    max = Math.max(max, arrayToSave[i][j]);
                    min = Math.min(min, arrayToSave[i][j]);
                }
            }
            System.out.println("min = " + min + " max = " + max + " in " + fileName);
            BufferedImage image = new BufferedImage(arrayToSave.length, arrayToSave[0].length, BufferedImage.TYPE_BYTE_GRAY );
            for(int i = 0; i< arrayToSave.length; i++) {
                for(int j = 0; j< arrayToSave[i].length; j++) {
                    int a = (int)(normalize(arrayToSave[i][j], max, min) * 255);
                    //int a = (int) arrayToSave[i][j];
                    Color newColor = new Color(a,a,a);
                    image.setRGB(i,j,newColor.getRGB());
                }
            }
            File output = new File(fileName);
            ImageIO.write(image, "jpg", output);
            WritableImage sinImage = SwingFXUtils.toFXImage(image, null);
            return sinImage;
        }

        catch(Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Problem saving inputImage to graphic file");
            return null;
        }
    }

    public void filterSinogram() {
        System.out.println("Sensors: " + sinogramMatrix.length + " detectors: " + sinogramMatrix[0].length);
        float[][] newSinogramMatrix = new float[sinogramMatrix.length][sinogramMatrix[0].length];
        for (int i = 0; i < sinogramMatrix.length; i++) {
            for (int j = 0; j < sinogramMatrix[0].length; j++) {
                //newSinogramMatrix[i][j] = filterKernelSum(i,j, 3);            // sum
                //newSinogramMatrix[i][j] = filterKernelMedian(i,j, 3);        // median
                newSinogramMatrix[i][j] = filterKernel(i ,j ,sinogramMatrix[0].length);        // median
            }
        }
        sinogramMatrix = newSinogramMatrix;

    }

    private float filterKernel(int realEmitterIndex, int realDetectorIndex, int kernel_width) {
        //System.out.println("Emitter = " + realEmitterIndex + " Detector = " + realDetectorIndex);
        float filter_multiplier = (float) (-4/Math.pow(Math.PI, 2));
        float[] kernel = new float[kernel_width];
        float result = 0.0f;

        for (int i = realDetectorIndex - kernel_width/2; i < realDetectorIndex + kernel_width/2; i++) { // real indexes
            //System.out.println("Prawdziwy Index i = " + i);
            if (i >= 0 && i < sinogramMatrix[0].length) {        // jeśli mieści się w rzędzie
                //int i_index = i - (sinogramMatrix[0].length / 2);
                int i_index = i - realDetectorIndex;
                //System.out.println("Sztuczny Index i_index = " + i_index);
                float filter_value;
                if (i_index == 0) filter_value = 1;
                else if (i_index % 2 == 0) filter_value = 0;
                else filter_value = (float) (filter_multiplier / Math.pow(i_index, 2));
                result += sinogramMatrix[realEmitterIndex][i] * filter_value;
            }
        }
        return result;
    }

    private float filterKernelSum(int realEmitterIndex, int realDetectorIndex, int kernel_width) {
        Float[] value_list = new Float[kernel_width*kernel_width];
        float[] kernel = {0, -1f, 0, -1f, 6, -1f, 0, -1f, 0};
        int index = 0;
        for (int i = realEmitterIndex - kernel_width/2; i <= realEmitterIndex + kernel_width/2; i++) {
            for (int j = realDetectorIndex - kernel_width/2; j <= realDetectorIndex + kernel_width/2; j++) {
                if (i < 0 || j < 0) value_list[index++] = 0.0f;
                else if (i >= sinogramMatrix.length || j >= sinogramMatrix[0].length) value_list[index++] = 0.0f;
                else {
                    value_list[index] = sinogramMatrix[i][j] * kernel[index];   // for sum
                    index++;
                }
            }
        }
        float sum = 0.0f;
        for (float i : value_list) sum += i;
        Math.max(sum, 0.0f);
        Math.min(sum, 255);

        return sum;
    }

    private float filterKernelMedian(int realEmitterIndex, int realDetectorIndex, int kernel_width) {
        Float[] value_list = new Float[kernel_width*kernel_width];
        float[] kernel = {0, -1f, 0, -1f, 6, -1f, 0, -1f, 0};
        int index = 0;
        for (int i = realEmitterIndex - kernel_width/2; i <= realEmitterIndex + kernel_width/2; i++) {
            for (int j = realDetectorIndex - kernel_width/2; j <= realDetectorIndex + kernel_width/2; j++) {
                if (i < 0 || j < 0) value_list[index++] = 0.0f;
                else if (i >= sinogramMatrix.length || j >= sinogramMatrix[0].length) value_list[index++] = 0.0f;
                else {
                    value_list[index] = sinogramMatrix[i][j];                 // for median
                    index++;
                }
            }
        }
        // median
        Arrays.sort(value_list);
        float median;
        if (value_list.length % 2 == 0)
            median = (value_list[value_list.length/2] + value_list[value_list.length/2 - 1])/2.0f;
        else
            median = value_list[value_list.length/2];
        return median;
    }
}