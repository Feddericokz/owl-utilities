package owl.groovy.utilities

@Grab('com.squareup.okhttp3:okhttp:4.9.0')

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import okhttp3.*

// Define your GraphQL query here as a multi-line string
static def getGraphQLQuery() {
   '''
        fragment mediaData on UploadFileEntityResponse {
            data {
                attributes {
                    alternativeText
                    url
                    __typename
                }
                __typename
            }
            __typename
        }
        
        fragment CommentFragment on Comment {
            users_permissions_user {
                data {
                    attributes {
                        username
                        __typename
                    }
                    __typename
                }
                __typename
            }
            text
            createdAt
            __typename
        }
        
        fragment ProductComments on Product {
            comments(pagination: {pageSize: 50}, sort: "createdAt:asc") {
                data {
                    attributes {
                        ...CommentFragment
                        reply_comments {
                            data {
                                attributes {
                                    ...CommentFragment
                                    __typename
                                }
                                __typename
                            }
                            __typename
                        }
                        __typename
                    }
                    __typename
                }
                __typename
            }
            __typename
        }
        
        fragment seoData on ComponentSharedSeo {
            metaTitle
            metaDescription
            metaImage {
                ...mediaData
                __typename
            }
            metaSocial {
                socialNetwork
                title
                description
                image {
                    ...mediaData
                    __typename
                }
                __typename
            }
            keywords
            metaRobots
            structuredData
            metaViewport
            canonicalURL
            __typename
        }
        
        query GET_PRODUCTS($slug: String) {
            products(filters: {slug: {eq: $slug}}) {
                data {
                    __typename
                    id
                    attributes {
                        name
                        slug
                        description
                        iframe
                        updatedAt
                        bambu_file {
                            data {
                                id
                                __typename
                            }
                            __typename
                        }
                        stl_preview {
                            ...mediaData
                            __typename
                        }
                        release_date
                        ...ProductComments
                        thumbnail {
                            ...mediaData
                            __typename
                        }
                        hover {
                            ...mediaData
                            __typename
                        }
                        gallery(pagination: {pageSize: 50}) {
                            data {
                                id
                                attributes {
                                    alternativeText
                                    url
                                    __typename
                                }
                                __typename
                            }
                            __typename
                        }
                        keywords
                        files(pagination: {pageSize: 50}) {
                            text
                            commercial_only
                            file {
                                data {
                                    id
                                    __typename
                                }
                                __typename
                            }
                            __typename
                        }
                        categories {
                            data {
                                id
                                attributes {
                                    name
                                    slug
                                    __typename
                                }
                                __typename
                            }
                            __typename
                        }
                        tags {
                            data {
                                attributes {
                                    name
                                    parent_tag {
                                        data {
                                            attributes {
                                                name
                                                __typename
                                            }
                                            __typename
                                        }
                                        __typename
                                    }
                                    __typename
                                }
                                __typename
                            }
                            __typename
                        }
                        related_products {
                            data {
                                id
                                attributes {
                                    ...productsData
                                    __typename
                                }
                                __typename
                            }
                            __typename
                        }
                        products_related {
                            data {
                                id
                                attributes {
                                    ...productsData
                                    __typename
                                }
                                __typename
                            }
                            __typename
                        }
                        seo {
                            ...seoData
                            __typename
                        }
                        __typename
                    }
                }
                __typename
            }
        }
        
        fragment productsData on Product {
            name
            slug
            categories {
                data {
                    id
                    attributes {
                        slug
                        name
                        __typename
                    }
                    __typename
                }
                __typename
            }
            tags {
                data {
                    id
                    attributes {
                        slug
                        name
                        __typename
                    }
                    __typename
                }
                __typename
            }
            thumbnail {
                ...mediaData
                __typename
            }
            hover {
                ...mediaData
                __typename
            }
            __typename
        }
   '''
}


// Function to read slugs from file and return as a list
def readSlugsFromFile(String filePath) {
    File file = new File(filePath)
    List<String> slugs = []

    if (!file.exists()) {
        println "File does not exist: $filePath"
        return slugs
    }

    file.eachLine { line ->
        try {
            def json = new JsonSlurper().parseText((line))
            def slug = json.attributes.slug
            slugs.add(slug)
        } catch (Exception e) {
            println "Error parsing line to JSON: $line"
            println "Exception: $e.message"
        }
    }

    return slugs
}

// Function to make a GraphQL query and return the response
def fetchGraphQLData(httpClient, slug, token, maxRetries) {
    println "Fetching data for slug: $slug"
    def mediaType = MediaType.parse("application/json")
    String content = """
        {
            "operationName": "GET_PRODUCTS",
            "variables": {"slug": "${slug}"},
            "query": ${JsonOutput.toJson(getGraphQLQuery())}
        }
    """
    def body = RequestBody.create(mediaType, content)
    def response = null
    def attempt = 0
    while (attempt < maxRetries) {
        try {
            println "Attempt $attempt for slug: $slug"
            def request = new Request.Builder()
                    .url('https://api.stlflix.com/graphql')
                    .post(body)
                    .addHeader('authority', 'api.stlflix.com')
                    .addHeader('accept', '*/*')
                    .addHeader('accept-language', 'es-419,es;q=0.9')
                    .addHeader('authorization', 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MjMzNzEsImlhdCI6MTY5OTA3MzI0NiwiZXhwIjoxNzAxNjY1MjQ2fQ.PTA-PYLAVHWl9CQwF0SvjqR8kZkULMXU_fz_9VhhD3k')
                    .addHeader('content-type', 'application/json')
                    .addHeader('origin', 'https://platform.stlflix.com')
                    .addHeader('referer', 'https://platform.stlflix.com/')
                    .addHeader('sec-ch-ua', '"Google Chrome";v="119", "Chromium";v="119", "Not?A_Brand";v="24"')
                    .addHeader('sec-ch-ua-mobile', '?0')
                    .addHeader('sec-ch-ua-platform', '"Linux"')
                    .addHeader('sec-fetch-dest', 'empty')
                    .addHeader('sec-fetch-mode', 'cors')
                    .addHeader('sec-fetch-site', 'same-site')
                    .addHeader('sec-gpc', '1')
                    .addHeader('user-agent', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36')
                    .build()
            response = httpClient.newCall(request).execute()
            println "Data fetch successful for slug: $slug"
            break // Break the loop if successful
        } catch (SocketTimeoutException ste) {
            println "Timeout occurred for slug: $slug on attempt $attempt"
            attempt++
            if (attempt >= maxRetries) {
                println "Max retry attempts reached for slug: $slug"
                throw ste // Rethrow exception if max retries reached
            }
        }
    }
    return response.body().string()
}

// Function to write data to file
def writeDataToFile(data, filePath) {
    String jsonContent = new JsonBuilder(data).toString() // Serialize the product map to a JSON string
    new File(filePath).with { file ->
        file << jsonContent + System.lineSeparator()
    }
    println "Saved content to ${filePath}"
}

// Main execution function
def main(configFilePath, outputFilePath, token, maxRetries) {
    println "Starting main execution"
    def httpClient = new OkHttpClient()
    def slugs = readSlugsFromFile(configFilePath)

    slugs.each { slug ->
        //def endOfData = false
        //while (!endOfData) {
            try {
                def response = fetchGraphQLData(httpClient, slug, token, maxRetries)
                def parsedResponse = new JsonSlurper().parseText(response)
                if (parsedResponse.data?.products?.data) {
                    println "Data found for slug: $slug"
                    writeDataToFile(parsedResponse.data.products.data[0], outputFilePath)
                    // TODO: Implement pagination logic here. The script currently assumes there's only one page of data.
                } //else {
                    //println "No more data found for slug: $slug"
                    //endOfData = true
                //}
            } catch (Exception e) {
                println "An error occurred: ${e.message}"
                //endOfData = true
            }
        //}
    }
    println "Main execution completed"
}

// Configurable parameters
def configFilePath = 'products-list.txt'
def outputFilePath = 'complete-product-list.txt'
def token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MjMzNzEsImlhdCI6MTY5OTA3MzI0NiwiZXhwIjoxNzAxNjY1MjQ2fQ.PTA-PYLAVHWl9CQwF0SvjqR8kZkULMXU_fz_9VhhD3'
def maxRetries = 3 // Configure the maximum number of retries

// Replace 'path_to_slug_file.txt' with the actual path to the slug file
// Replace 'path_to_output_file.json' with the actual path to the output file
// Replace 'your_bearer_token' with the actual bearer token
// Set maxRetries to the number of times you want to retry the request after a timeout

// Run the script
main(configFilePath, outputFilePath, token, maxRetries)
