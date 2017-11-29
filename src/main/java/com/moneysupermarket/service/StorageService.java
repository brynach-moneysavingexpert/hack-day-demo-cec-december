package com.moneysupermarket.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by brynach.jones on 28/11/2017.
 */
@Service
public class StorageService {

    @Autowired
    RemoteImageDetectService remoteImageDetectService;

    public String store() {
        File imageFile = new File("src/main/resources/static/bill.jpg");
        try {
            return store(new FileInputStream(imageFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "uploadFile";
    }

    public String store(InputStream stream) {
        String response = "";

        DateTimeFormatter dtf = DateTimeFormat.forPattern("-YYYY-MM-dd-HHmmssSSS");
        DateTime dt = DateTime.now(DateTimeZone.UTC);
        String dtString = dt.toString(dtf);

        // Instantiates a client
        Storage storage = StorageOptions.getDefaultInstance().getService();

        try {
            // Instantiates a client properly
            storage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("<API-KEY-LOCATION>")))
                    .build()
                    .getService();
        } catch (FileNotFoundException e) {
            System.err.println("------------------------------------------------------------------------------------------------------------------------------------");
            System.err.println("You need to supply a Google Cloud Platform json API key and enter it's location instead of <API-KEY-LOCATION> in StorageService.java");
            System.err.println("------------------------------------------------------------------------------------------------------------------------------------");
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        // The name for the new bucket
        String bucketName = "cec-hack-day-bucket";  // "my-new-bucket";

        // Creates the new bucket
            String fileName = "uniqueFilename" + dtString + ".jpg";
            BlobInfo blobInfo2 =
                    storage.create(
                            BlobInfo
                                    .newBuilder(bucketName, fileName)
                                    // Modify access list to allow all users with link to read file
                                    .setAcl(new ArrayList<>(Arrays.asList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
                                    .build(),
                            stream);
            System.out.println(blobInfo2.getMediaLink());
        try {
            response = remoteImageDetectService.detectDocumentTextGcs("gs://cec-hack-day-bucket/" + fileName, System.out);
            System.out.println(response);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // print the public download link
        return response;
    }
}
