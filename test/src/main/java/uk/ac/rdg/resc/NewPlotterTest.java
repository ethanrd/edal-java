package uk.ac.rdg.resc;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;

import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.graphics.style.ColorPalette;
import uk.ac.rdg.resc.edal.graphics.style.FeatureCollectionAndMemberName;
import uk.ac.rdg.resc.edal.graphics.style.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.Id2FeatureAndMember;
import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser;
import uk.ac.rdg.resc.edal.graphics.style.model.Image;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

public class NewPlotterTest {
    public static void main(String[] args) throws IOException, InstantiationException,
            JAXBException {
        /*
         * First, get the dataset, and find some valid values for elevation and
         * time
         */
        final NcGridSeriesFeatureCollection featureCollection = new NcGridSeriesFeatureCollection(
                "testcollection", "Test Collection", "/home/guy/Data/FOAM_ONE/FOAM_one.ncml");

        final GridSeriesFeature feature = featureCollection.getFeatureById("testcollection2");
        GridSeriesDomain domain = feature.getCoverage().getDomain();

        VerticalPosition vPos = null;
        try {
            vPos = new VerticalPositionImpl(domain.getVerticalAxis().getCoordinateValue(0),
                    domain.getVerticalCrs());
        } catch (NullPointerException e) {
        }
        TimePosition tPos = null;
        try {
            tPos = domain.getTimeAxis().getCoordinateValue(0);
        } catch (NullPointerException e) {
        }

        /*
         * Now set the parameters for drawing the map.
         */
        int width = 800;
        int height = 400;

        BoundingBox bbox = domain.getHorizontalGrid().getCoordinateExtent();
        GlobalPlottingParams params = new GlobalPlottingParams(width, height, bbox, null, null,
                vPos, tPos);

        /*
         * A simple interface mapping strings to FeatureCollection + member
         * name. This just uses the member name and accesses our only
         * FeatureCollection.
         * 
         * In edal-ncwms, this would be map "dataset/variable" to the
         * appropriate FeatureCollection
         */
        Id2FeatureAndMember id2Feature = new Id2FeatureAndMember() {
            @Override
            public FeatureCollectionAndMemberName getFeatureAndMemberName(String id) {
                return new FeatureCollectionAndMemberName(featureCollection, id);
            }
        };

        /*
         * Now get all files in the "xml" directory
         */
        File files = new File(ClassLoader.getSystemResource("xml").getFile());
        for (File file : files.listFiles()) {
            try {
                /*
                 * Read each file, deserialise to an image, and then render and save
                 * the image
                 */
                String xmlString = readFile(file);
                Image image = StyleXMLParser.deserialise(xmlString);
    
                File outputFile = new File("/home/guy/xmlOutput/", file.getName().replaceAll("xml$",
                        "png"));
                ImageIO.write(image.drawImage(params, id2Feature), "png", outputFile);
            } catch (Exception e) {
                System.out.println("Problem with "+file.getAbsolutePath()+": "+e.getMessage());
            }
        }

    }

    private static String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        reader.close();

        return stringBuilder.toString();
    }
}