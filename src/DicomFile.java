import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.dcm4che2.data.*;
import org.dcm4che2.io.*;
import org.dcm4che2.media.*;
import org.dcm4che2.util.UIDUtils;
import org.dcm4che3.imageio.codec.CompressionRules;
import org.dcm4che2.data.DicomObject;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.*;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Attributes;

// tutorial:
// http://samucs.blogspot.com/2008/12/converting-jpeg-to-dicom-using-dcm4che.html
// dokumentacja:
// http://www.dcm4che.org/docs/dcm4che-2.0.14-apidocs/org/dcm4che2/data/BasicDicomObject.html#putInt(int[], org.dcm4che2.data.VR, int)
// online dicom viewer
// http://www.ofoct.com/viewer/dicom-viewer-online.html
// dicom tags and codes
// ftp://dicom.nema.org/medical/DICOM/2013/output/chtml/part05/sect_6.2.html

public class DicomFile {
    private Controller controller;
    public DicomFile(String filename, Controller controller) throws IOException {
        //DicomObject obj;


        File jpgSource = new File(filename + ".jpg");
        File dcmDestination = new File(filename + ".dcm");

        BufferedImage jpegImage = null;
        try {
            jpegImage = ImageIO.read(jpgSource);
            if (jpegImage == null) throw new Exception("Invalid file.");
        } catch (Exception e) {
            System.out.println("ERROR: "+ e.getMessage());
            return;
        }

        int colorComponents = jpegImage.getColorModel().getNumColorComponents();
        int bitsPerPixel = jpegImage.getColorModel().getPixelSize();
        int bitsAllocated = (bitsPerPixel / colorComponents);
        int samplesPerPixel = colorComponents;

        // start building DICOM file:
        DicomObject dicom = new BasicDicomObject();
        dicom.putString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        dicom.putString(Tag.PhotometricInterpretation, VR.CS, samplesPerPixel == 3 ? "YBR_FULL_422" : "MONOCHROME2");

        // mandatory information:
        dicom.putInt(Tag.SamplesPerPixel, VR.US, samplesPerPixel);
        dicom.putInt(Tag.Rows, VR.US, jpegImage.getHeight());
        dicom.putInt(Tag.Columns, VR.US, jpegImage.getWidth());
        dicom.putInt(Tag.BitsAllocated, VR.US, bitsAllocated);
        dicom.putInt(Tag.BitsStored, VR.US, bitsAllocated);
        dicom.putInt(Tag.HighBit, VR.US, bitsAllocated-1);
        dicom.putInt(Tag.PixelRepresentation, VR.US, 0);

        // date:
        dicom.putDate(Tag.InstanceCreationDate, VR.DA, new Date());
        dicom.putDate(Tag.InstanceCreationTime, VR.TM, new Date());

        // unique identifiers:
        dicom.putString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID());
        dicom.putString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        dicom.putString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());

        // patient's info
        dicom.putString(Tag.PatientName, VR.PN, controller.getNameTextEdit().getText());
        dicom.putString(Tag.PatientAge, VR.AS, controller.getAgeTextEdit().getText());
        dicom.putString(Tag.PatientSex, VR.SH, controller.getSexChoiceBox().getSelectionModel().getSelectedItem().toString());
        dicom.putString(Tag.ImageComments, VR.ST, controller.getCommentsTextEdit().getText());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        dicom.putString(Tag.Date, VR.DT, dateFormat.format(date));

        //  initiates Dicom metafile information considering JPEGBaseline1 as transfer syntax:
        dicom.initFileMetaInformation(UID.JPEGBaseline1);

        // open an output stream for saving our Dicom dataset
        FileOutputStream fos = new FileOutputStream(dcmDestination);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DicomOutputStream dos = new DicomOutputStream(bos);
        dos.writeDicomFile(dicom);

        dos.writeHeader(Tag.PixelData, VR.OB, -1);
        dos.writeHeader(Tag.Item, null, 0);


        /*
        According to Gunter from dcm4che team we have to take care that
        the pixel data fragment length containing the JPEG stream has
        an even length.
        */
        int jpgLen = (int) jpgSource.length();
        dos.writeHeader(Tag.Item, null, (jpgLen+1)&~1);

        FileInputStream fis = new FileInputStream(jpgSource);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        byte[] buffer = new byte[65536];
        int b;
        while ((b = dis.read(buffer)) > 0) {
            dos.write(buffer, 0, b);
        }

        /*
        According to Gunter from dcm4che team we have to take care that
        the pixel data fragment length containing the JPEG stream has
        an even length. So if needed the line below pads JPEG stream with
        odd length with 0 byte.
        */
        if ((jpgLen&1) != 0) dos.write(0);
        dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
        dos.close();
        Platform.runLater(
                () -> {
                    controller.getInfoLabel().setText("DICOM file saved.");
                }
        );


    }
}
