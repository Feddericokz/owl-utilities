package owl.groovy.utilities

@Grab(group='com.squareup.okhttp3', module='okhttp', version='4.12.0')
import okhttp3.*
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder

class Config {
    String graphqlQuery = '''
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
        
        query GET_LIST_PRODUCTS($filters: ProductFiltersInput, $productsPage: Int, $productsPageSize: Int, $sort: [String]) {
            products(
                filters: $filters
                pagination: {page: $productsPage, pageSize: $productsPageSize}
                sort: $sort
            ) {
                data {
                    id
                    attributes {
                        name
                        slug
                        thumbnail {
                            ...mediaData
                            __typename
                        }
                        hover {
                            ...mediaData
                            __typename
                        }
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
                        __typename
                    }
                    __typename
                }
                __typename
            }
        }
    '''
    String outputFile = "products-list.txt"
    int maxRetries = 3
    String apiUrl = 'https://api.stlflix.com/graphql'
    OkHttpClient client = new OkHttpClient()
    MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8")
}

def saveToFile(Map product, Config config) {
    String jsonContent = new JsonBuilder(product).toString() // Serialize the product map to a JSON string
    new File(config.outputFile).with { file ->
        file << jsonContent + System.lineSeparator()
    }
    println "Saved content to ${config.outputFile}"
}

def fetchProducts(int page, Config config) {
    String jsonBody = new JsonBuilder([
            operationName: "GET_LIST_PRODUCTS",
            variables: [
                    filters: [:],
                    productsPage: page,
                    productsPageSize: 100,
                    sort: "release_date:DESC"
            ],
            query: config.graphqlQuery
    ]).toString()

    Request request = new Request.Builder()
            .url(config.apiUrl)
            .post(RequestBody.create(config.jsonMediaType, jsonBody))
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

    Response response = null
    int retries = 0

    while (retries < config.maxRetries) {
        try {
            response = config.client.newCall(request).execute()
            break // Success, exit retry loop
        } catch (SocketTimeoutException ste) {
            println "Request timed out, retrying... (${retries + 1})"
            retries++
            if (retries == config.maxRetries) {
                throw new RuntimeException("Max retries reached, aborting.")
            }
        }
    }

    return response?.body()?.string()
}

def main(Config config) {
    println "Starting data fetch process..."
    int currentPage = 1
    boolean hasMoreData = true

    while (hasMoreData) {
        println "Fetching data for page $currentPage"
        String response = fetchProducts(currentPage, config)
        if (response) {
            Map parsedResponse = new JsonSlurper().parseText(response)
            List<Map> products = parsedResponse.data?.products?.data

            if (products && products.size() > 0) {
                println "Processing ${products.size()} products..."
                products.each { product ->
                    saveToFile(product, config)
                }
                currentPage++
            } else {
                println "No more data available."
                hasMoreData = false
            }
        } else {
            println "No response received, stopping fetch process."
            hasMoreData = false
        }
    }

    println "Finished fetching all pages."
}

Config config = new Config()
main(config)
