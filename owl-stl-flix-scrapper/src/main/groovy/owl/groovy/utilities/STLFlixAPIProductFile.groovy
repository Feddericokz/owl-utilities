package owl.groovy.utilities

@Grab('com.squareup.okhttp3:okhttp:4.9.0')
@Grab('org.codehaus.groovy:groovy-json:3.0.7')

import groovy.json.JsonSlurper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Headers
import java.nio.file.Files
import java.nio.file.Paths


// Function to send HTTP request and fetch product file data
def fetchProductFileData(String fid) {
    def jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MjMzNzEsImlhdCI6MTY5OTA3MzI0NiwiZXhwIjoxNzAxNjY1MjQ2fQ.PTA-PYLAVHWl9CQwF0SvjqR8kZkULMXU_fz_9VhhD3k"
    OkHttpClient client = new OkHttpClient()
    MediaType JSON = MediaType.parse("application/json; charset=utf-8")
    def payload = new groovy.json.JsonBuilder([
            jwt: jwtToken,
            fid: fid,
            uid: 23371
    ]).toString()
    RequestBody body = RequestBody.create(payload, JSON)
    Headers headers = new Headers.Builder()
            .add("authority", "api.stlflix.com")
            .add("accept", "*/*")
            .add("accept-language", "es-419,es;q=0.9")
            .add("authorization", "Bearer $jwtToken")
            .add("content-type", "application/json")
            .add("origin", "https://platform.stlflix.com")
            .add("referer", "https://platform.stlflix.com/")
            .add("sec-ch-ua", "\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\"")
            .add("sec-ch-ua-mobile", "?0")
            .add("sec-ch-ua-platform", "\"Linux\"")
            .add("sec-fetch-dest", "empty")
            .add("sec-fetch-mode", "cors")
            .add("sec-fetch-site", "same-site")
            .add("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
            .build()
    Request request = new Request.Builder()
            .url('https://api.stlflix.com/api/product/product-file')
            .headers(headers)
            .post(body)
            .build()

    println "Sending request for fid: $fid"
    def response = client.newCall(request).execute()
    println "Response received for fid: $fid"
    return response.body().string()
}

// Replace with the actual path to your configuration file
def configFilePath = "complete-product-list.txt"
def destinationPath = "stl-products"

println "Reading the configuration file: $configFilePath"

// Read the configuration file
new File(configFilePath).eachLine { line ->
    def parser = new JsonSlurper()
    def json = parser.parseText(line)

    // Extract the slug and fid
    def slug = json.attributes.slug
    def fids = json.attributes.files.findAll { it?.file?.data?.id != null }.collect { it.file.data.id }

    println "Processing slug: $slug with fids: $fids"

    // Create directory for slug
    def dirPath = Paths.get("$destinationPath/$slug")
    if (!Files.exists(dirPath)) {
        println "Creating directory for slug: $slug"
        Files.createDirectories(dirPath)
    }

    // Save the original JSON object
    def productFilePath = dirPath.resolve("product_${slug}.json")
    println "Saving original product data to: $productFilePath"
    Files.write(productFilePath, line.bytes)

    // For each fid, fetch file data and save it
    fids.each { fid ->
        println "Fetching data for fid: $fid"
        def fileData = fetchProductFileData(fid)
        def fileFilePath = dirPath.resolve("file_${fid}.json")
        println "Saving file data to: $fileFilePath"
        Files.write(fileFilePath, fileData.bytes)
    }
}

println "Processing complete."
