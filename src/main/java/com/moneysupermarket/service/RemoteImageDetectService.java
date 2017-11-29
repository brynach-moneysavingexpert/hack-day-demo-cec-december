package com.moneysupermarket.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Block;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.Page;
import com.google.cloud.vision.v1.Paragraph;
import com.google.cloud.vision.v1.Symbol;
import com.google.cloud.vision.v1.TextAnnotation;
import com.google.cloud.vision.v1.Word;
import org.springframework.stereotype.Service;

/**
 * Created by brynach.jones on 28/11/2017.
 */
@Service
public class RemoteImageDetectService {

    public String detectDocumentTextGcs(String gcsPath, PrintStream out) throws Exception {
        List<AnnotateImageRequest> requests = new ArrayList<>();
        String returnText = "";

        ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);


        ImageAnnotatorSettings imageAnnotatorSettings = null;
        try {
            imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream("<API-KEY-LOCATION>")))).build();
        } catch (FileNotFoundException e) {
        System.err.println("------------------------------------------------------------------------------------------------------------------------------------");
        System.err.println("You need to supply a Google Cloud Platform json API key and enter it's location instead of <API-KEY-LOCATION> in StorageService.java");
        System.err.println("------------------------------------------------------------------------------------------------------------------------------------");
        System.err.println(e);
        } catch (Exception e) {
            System.err.println(e);
        }
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create(imageAnnotatorSettings)) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            client.close();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    out.printf("Error: %s\n", res.getError().getMessage());
                    return "Errored...";
                }
                // For full list of available annotations, see http://g.co/cloud/vision/docs
                TextAnnotation annotation = res.getFullTextAnnotation();
                for (Page page: annotation.getPagesList()) {
                    String pageText = "";
                    for (Block block : page.getBlocksList()) {
                        String blockText = "";
                        for (Paragraph para : block.getParagraphsList()) {
                            String paraText = "";
                            for (Word word: para.getWordsList()) {
                                String wordText = "";
                                for (Symbol symbol: word.getSymbolsList()) {
                                    wordText = wordText + symbol.getText();
                                }
                                paraText = paraText + " " + wordText;
                            }
                            // Output Example using Paragraph:
                            out.println("Paragraph: \n" + paraText);
                            returnText += paraText + "\n";
//                            out.println("Bounds: \n" + para.getBoundingBox() + "\n");
//                            blockText = blockText + paraText;
                        }
                        pageText = pageText + "\n" + blockText;
                    }
                }
            }
        }

        return returnText;
    }
}
