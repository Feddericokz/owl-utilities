package owl.groovy.utilities

@Grab(group='com.squareup.okhttp3', module='okhttp', version='4.9.0')
import groovy.json.JsonSlurper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class Downloader {
    static final OkHttpClient client = new OkHttpClient()
    static final JsonSlurper slurper = new JsonSlurper()
    static int totalFoldersProcessed = 0

    static void processProductFiles(File productDir) {
        println "Processing product files in ${productDir}"
        productDir.eachFileMatch(~/^product_.*\.json$/, { File productFile ->
            println "Processing product file: ${productFile.name}"
            def productData = slurper.parse(productFile)
            def galleryData = productData.attributes.gallery.data

            galleryData.each { item ->
                downloadFile(item.attributes.url, new File(productDir, item.id + getExtension(item.attributes.url)))
            }
        })
    }

    static void processFileFiles(File productDir) {
        println "Processing file files in ${productDir}"
        productDir.eachFileMatch(~/^file_.*\.json$/, { File fileFile ->
            println "Processing file: ${fileFile.name}"
            def fileData = slurper.parse(fileFile)
            downloadFile(fileData.url, new File(productDir, fileData.name))
        })
    }

    static void downloadFile(String fileUrl, File destFile) {
        println "Downloading file from URL: $fileUrl"
        Request request = new Request.Builder().url(fileUrl).build()
        Response response = client.newCall(request).execute()
        if (!response.isSuccessful()) throw new IOException("Failed to download file: " + fileUrl)

        Files.copy(response.body().byteStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        response.close()
        println "File downloaded successfully: ${destFile.name}"
    }

    static String getExtension(String fileName) {
        return fileName.lastIndexOf('.') != -1 ? fileName.substring(fileName.lastIndexOf('.')) : ""
    }
}

println "Starting processing of stl-products directory..."
new File('stl-products').eachDir { productFolder ->
    Downloader.processProductFiles(productFolder)
    Downloader.processFileFiles(productFolder)
    Downloader.totalFoldersProcessed++
    println "Finished processing folder: ${productFolder.name} (Total folders processed: ${Downloader.totalFoldersProcessed})"
}
println "Processing complete. Total folders processed: ${Downloader.totalFoldersProcessed}"
