# NLP - Natural Language Programming to achieve simple tasks faster. A Web scrapping tutorial using GPT.

## Introduction

I've been the owner of a 3D printer for a while, so it's not un-common for me to talk about 3D printing stuff in
general. We all know that smartphones don't listen to us, right? but conveniently come up with an ad for what you've
been speaking about. I've started to get ads for this particular site called STLFlix, granting access to over 1000 3D designs. 
New collections are uploaded weekly, and it even allows the legal sale of printed models. The cost? About 3000 ARS a month. 
To put it in perspective, I have previously paid between 500 and 1000 ARS for designs on other sites. So, this seemed like a great deal.

After subscribing to STLFlix, I started printing various models. As days went by, some models turned out great, while others didn't. 
Before I knew it, I only had about ten days left on my subscription. By then, I had downloaded just 10 to 15 files 
from the vast collection that initially intrigued me. I try to make sure to cancel monthly subscriptions before I forget 
about them and get billed for months of not using it. So I decided to make the most of it, however, downloading over a 
thousand files by clicking each item individually? That's not only tedious but also not a fun way to spend time.

This is where my ChatGPT subscription came into play. I used it to streamline this task efficiently and enjoyably, 
without needing to code everything myself (although I did tweak a few things—is that considered coding?). 
By the end of this blog post/tutorial, I hope to show you, whether you're an AI enthusiast or not, the incredible 
potential these tools offer in enhancing our daily productivity.

## Manos a la obra!

Ever since I've started with this thing I call **Natural Language ~~Processing~~ Programming**, I've gone through a few scripting languages. 
I started with bash scripts because they're really easy to run in any environment that has a bash shell. 
The thing is, I'm not a bash expert, and language models are far from perfect. So, whenever there was a mistake, it was hard 
for me to quickly fix any syntax or logical errors. Then, I turned to Python. I'm not sure why, as I'm not really a 
Python programmer either, and its syntax and environment setup don't come naturally to me, even though I've done it before. 
Lately, I've started using Groovy, which is a scripting language on top of Java. It's very flexible and powerful. 
It comes naturally to me since I'm really familiar with the Java ecosystem. So, if you're a Java developer like me, I strongly 
recommend you check out Groovy if you haven't. And if you don't like Java, I'm sure you can apply the concepts from 
this blog by asking for scripts in any language of your choice.

### We need a plan.

Headed to the page and logged in, they have an explorer that lets you filter by some attributes or make a search.

![STLFlix_explore_page](./src/main/resources/images/0_0_STLFlix_explore_page.png)

Having worked in web development, I had a good idea of what I was looking for. Depending on the age of the page, 
my approach would be different. For older pages, I would need to parse the HTML and extract the necessary data. 
More modern pages often have an API they use to retrieve data, which I could potentially access. I went 
straight to Chrome Developer Tools, opened the Network tab and reloaded the page. Luckily, going through all the 
request the page have made, I quickly found something with an interesting name.

![GraphQL_GET_LIST_PRODUCTS](./src/main/resources/images/1_0_GraphQL_GET_LIST_PRODUCTS.png)

![GraphQL_GET_LIST_PRODUCTS_response](./src/main/resources/images/1_1_GraphQL_GET_LIST_PRODUCTS_response.png)

A GraphQL request named GET_LIST_PRODUCTS, sounds useful for what I intend to do, right? If I'm going to download every
file, I will need a list of all the files. Response looks super useful as well, it has the **name** and the **ID** of the items
on the page.

*Item from data.products.data list in the JSON response.* [GET_LIST_PRODUCTS_response](./src/main/resources/GET_LIST_PRODUCTS_response.json)

Then I clicked on one of the products, it opened a new page, on which I kept looking at the requests shown in Network tab 
of Chrome Developer Tools. Again quickly found something quite useful.

![GraphQL_GET_PRODUCTS](./src/main/resources/images/1_2_GraphQL_GET_PRODUCTS.png)

![GraphQL_GET_PRODUCTS_response](./src/main/resources/images/1_3_GraphQL_GET_PRODUCTS_response.png)

It seems that this is the API call that fetches the information for the whole product page. It doesn't take an ID as 
parameter as one would expect, it takes the *slug*, and uses that to fetch even more information about this item than
the previous API call I saw. What specially caught my eye about this request, was that this response had an item 
called *files*, and each of these items had another ID. Another interesting thing about this response is that it has a
*gallery* item, and if I'm going to download every stl file there is, I'll need images to know what is in each file.

*JSON response from GET_PRODUCTS call.*  [GET_PRODUCTS_response](./src/main/resources/GET_PRODUCTS_response.json)

So far I knew how to get all the file IDs, but I still needed to transform these IDs into a file, right? So I've continued
the download flow on the site.

![download](./src/main/resources/images/1_4_download.png)

And it immediately triggered the last request I needed.

![API_product-files](./src/main/resources/images/1_5_API_product-files.png)

![API_product-files](./src/main/resources/images/1_6_API_product-files_response.png)

If you pay attention closely, that **fid** number, is the same as the one in the **files** item of the GET_PRODUCTS GraphQL 
response. That probably means that **fid** stands for **file id**. That's it, I have a plan!

### Natural Language Programming

These days, the latest LLMs like ChatGPT, are changing the focus from people needing to have the technical knowledge in order
to achieve some complex task, to just having to think on *what* needs to be done, because we can delegate the *how*, to these AIs.
For example, I don't need to know how to write a script in a language I've never used before, I can just ask ChatGPT to do it for me,
If I know what needs to be done. That is exactly what we're going to do.

There's a very cool functionality in Chrome Developer Tools, that lets you copy one of the request in many formats.
I'll be using that to copy the GET_LIST_PRODUCT GraphQL request as a cURL command.

![GraphQL_GET_LIST_PRODUCTS_copyAsCurl](./src/main/resources/images/2_GraphQL_GET_LIST_PRODUCTS_copyAsCurl.png)

With this curl, I've jump straight to GPT, Look at the following conversation: https://chat.openai.com/share/1e31f543-ed67-4b4e-8641-12f043aec22a

I've asked ChatGPT to:
- Create a groovy script.
- Use okHttp client (I'm just used to it) to mimic the cURL request.
- Loop through pages and fetch until there's no more pages.
- Collect the contents of data list into a file.
- Debug statements.
- Few fixes to the script.

Then saved the file as `STLFlixGraphQLGetProductList.groovy`. When using GPT like this, it doesn't actually execute 
the code it suggests, so it almost always needs a few tweaks to run, even tho sometimes it does run at once. 
Sometimes I fix it myself, sometimes I ask ChatGPT to fix it for me. Here's the difference between what I've built 
from what ChatGPT suggested vs what I ended up running: https://www.diffchecker.com/1tu65xzf/

Let's run it.

```
groovy STLFlixGraphQLGetProductList.groovy
```

![fetched_product_list_1](./src/main/resources/images/4_0_fetched_product_list_1.png)

![fetched_product_list_2](./src/main/resources/images/4_1_fetched_product_list_2.png)

Now we have all the **slugs** we need to make the GraphQL GET_PRODUCT request. So lets follow the same strategy as before.

![GraphQL_GET_PRODUCTS_copyAsCurl](./src/main/resources/images/7_0_GraphQL_GET_PRODUCTS_copyAsCurl.png)

Look at the following conversation: https://chat.openai.com/share/75098574-ed64-4c78-8999-9b0dac33886e

I've asked ChatGPT to:
- Create a groovy script.
- Use okHttp client to mimic the cURL request.
- Parse a file to get the slug input.
- Save results to a configurable file, collect contents of data list.
- Debug statements.
- Few fixes to the script.

Then saved the file as `STLFlixGraphQLGetProducts.groovy`, Here's the difference between what I've built
from what ChatGPT suggested vs what I ended up running: https://www.diffchecker.com/8QGBFTtR/

Let's run it.

```
groovy STLFlixGraphQLGetProductList.groovy
``` 

![fetch_complete_products](./src/main/resources/images/7_1_fetch_complete_product_list.png)

![fetched_complete_products](./src/main/resources/images/7_2_fetched_complete_products.png)

At this point, I have a file with all the file ids we need to make the PRODUCT_FILE API call, which will give me the 
link to download the actual .stl files. Something I should think, is how all these files are going to be stored. 
Right now I have a single file with a bunch of JSON objects holding the information, but I cannot have a single folder with all
the stl and images files in it, that would be chaos. It would be really hard to find a specific file. In my next script,
I'll ask ChatGPT to create this structure for me. Let's follow same strategy as before to make the request script.

![API_PRODUCT_FILE_copyAsCurl](./src/main/resources/images/9_0_API_PRODUCT_FILE_copyAsCurl.png)

Look at the following conversation: https://chat.openai.com/share/fa5aa9d4-623b-49e0-a6c4-51045bcbcada

I've asked ChatGPT to:
- Create a groovy script.
- Use okHttp client to mimic the cURL request.
- Extract the jsonObjects from our file and loop through the file ids to make the request.
- Create folders from the slug values, and create files with the product_file API call response, 
while also saving the original Json.
- Debug statements.
- Few fixes.

Then saved the file as `STLFlixApiProductFile.groovy`, Here's the difference between what I've built
from what ChatGPT suggested vs what I ended up running: https://www.diffchecker.com/shtLIN8r/

Let's run it.

```
groovy STLFlixApiProductFile.groovy
``` 

![fetched_product_files](./src/main/resources/images/10_fetched_product_files.png)

![product_folders](./src/main/resources/images/11_product_folders.png)


Almost done! Now I have a single folder with all the information I need. Among the data in those JSON object,
are the links to download all the images and .stl files. Now I just need to come up with a simple script that will go
through all these folders and files, and download such files.

Look at the following conversation: https://chat.openai.com/share/612df2d3-6e28-4d2e-b73e-b2da0fb52138

I've asked ChatGPT to:
* Create a groovy script.
* Loop through all the folders we've created.
* Extract links from product_ prefixed and file_ prefixed files.
* Use okHttp to download these files.


Then saved the file as `STLFlixDownloadFiles.groovy`, Here's the difference between what I've built
from what ChatGPT suggested vs what I ended up running: https://www.diffchecker.com/bHWSZG65/

There's no difference for this one, ChatGPT did a great job. Let's run the script.

```
groovy STLFlixDownloadFiles.groovy
```

![download_files](./src/main/resources/images/12_download_files.png)

![downloaded_files](./src/main/resources/images/13_downloaded_files.png)

And that's it, I'll just let it run, and it will download all the files.

## Conclusion

I could have written these scripts myself, but it would have taken a lot more time. Understanding what needs to be done 
is still crucial for solving any task, and it's something that AIs can't replace. However, AIs are getting better at 
handling the technical aspects of many jobs we once thought impossible for machines. They are an excellent tool that, 
when combined with our natural intelligence, can significantly enhance our performance and productivity.

If you're an AI user and have had conversations with GPT that made you think, 'This is pretty cool,' please share your 
experiences! We can all benefit from learning about each other's ideas and insights.



