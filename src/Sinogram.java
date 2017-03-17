

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Sinogram {
    private final ImageManager imageManager;
    public float[][] sinogramMatrix;
    int[][] sumCount;
    private final Controller controller;

    public Sinogram(Controller controller) {
        this.controller = controller;
        this.imageManager = new ImageManager("test_image.png");
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
        saveArrayAsImage(imageManager.forOutputImageMatrix,"output.jpg");
    }

    public void SinogramToImage() {
        normalizeSinogram();
        WritableImage sinogramImage = saveArrayAsImage(sinogramMatrix, "src/GrayScale.jpg");
        controller.getSinogramImage().setImage(sinogramImage);
        // filterSinogram();
        // saveArrayAsImage(sinogramMatrix,"GrayScaleWithFilter.jpg");
    }

    public void saveOutputImage(String fileName) {
        WritableImage finalImage = saveArrayAsImage(imageManager.forOutputImageMatrix,fileName);
        controller.getFinalImage().setImage(finalImage);
    }

    private void normalizeSinogram() {
        for (int i = 0; i < sinogramMatrix.length; i++) {
            for (int j=0; j < sinogramMatrix[0].length; j++) {
                // wartości w sinogramie dzielone przez max wartość jaką jest średnica koła
                //sinogramMatrix[i][j] /= (imageManager.getInputImageSize()-1);
                // wartości w sinoramie dzielone przez ilość dodawań
                sinogramMatrix[i][j] /= Math.max(1, sumCount[i][j]);
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
            System.out.println("min = " + min + " max = " + max);
            BufferedImage image = new BufferedImage(arrayToSave.length, arrayToSave[0].length, BufferedImage.TYPE_BYTE_GRAY );
            for(int i = 0; i< arrayToSave.length; i++) {
                for(int j = 0; j< arrayToSave[i].length; j++) {
                    int a = (int)(normalize(arrayToSave[i][j], max, min) * 255);
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
                sinogramMatrix[i][j] = (int)(normalize(sinogramMatrix[i][j], max, min));
            }
        }
        // filter
        for (int i = 0; i < sinogramMatrix.length; i++) {
            float filtered_row[] = new float[sinogramMatrix[0].length];
            for (int j = 0; j < sinogramMatrix[0].length; j++) {
                filtered_row[j] = filterKernel(i,j, 20);
            }
            sinogramMatrix[i] = filtered_row;
        }
    }

    private float filterKernel(int emitterIndex, int realDetectorIndex, int kernel_width) {
        int detectorIndex = realDetectorIndex - (sinogramMatrix[0].length / 2);
        float filter_multiplier = (float) (-4/Math.pow(Math.PI, 2));
        float result = 0.0f;

//        for (int i = realDetectorIndex - kernel_width/2; i <=realDetectorIndex + kernel_width/2; i++) {
//            if (i >= 0 && i < sinogramMatrix[emitterIndex].length) {        // jeśli mieści się w rzędzie
//                int i_index = i - (sinogramMatrix[0].length / 2);
//                float filter_value;
//                if (i_index == 0) filter_value = 1;
//                else if (i_index % 2 == 0) filter_value = 0;
//                else filter_value = (float) (filter_multiplier / Math.pow(i_index, 2));
//                result += sinogramMatrix[emitterIndex][i] * filter_value;
//            }
//        }
//        return result;

        int i_index = detectorIndex;
        float filter_value;
        if (i_index == 0) filter_value = 1;
        else if (i_index % 2 == 0) filter_value = 0;
        else filter_value = (float) (filter_multiplier / Math.pow(i_index, 2));
        result += sinogramMatrix[emitterIndex][realDetectorIndex] * filter_value;

        return result;
    }
}