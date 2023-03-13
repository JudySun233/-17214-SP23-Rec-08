# S23-rec8
## Task1
### generate a free PAT from Clarifai
- instructions: https://docs.clarifai.com/clarifai-basics/authentication/personal-access-tokens/#how-to-create-a-pat-in-the-legacy-portal

To run our request, make the following changes:
1. In ​Main.runWebAPIRequest(), change the variable key to the PAT you generated. ​
2. In resources/request-body.json, feel free to change the “url” value to that of any food image’s URL (currently the image of a tomato!)

## Task2
1. Go to runSingleAsync, think about the order in which “do other things” and “do other things finished” are printed.Then run the program, verify your answer by observing when these statements are printed out to the console.
2. Using the runMultipleSynchronous() and runSingleAsync() method for reference, fill in the runMultipleAsynchronous() method to make sure there are at most 10 requests sent to the server at the same time.

More Hints:
1. Create a list of CompletableFuture objects.
2. Use a semaphore to limit the number of parallel requests, by acquiring a permit before sending each request with sendAsync(), and releasing it when the request is completed.
3. Create a list to hold the responses.
4. Add each CompletableFuture object to the list.
5. Wait for all the CompletableFuture objects to complete using the join() method.
6. Retrieve each response and add it to the list of responses.


## Task3
In the logging system package we provide a very simple logger implementation that has two hardcoded logging mechanisms. Rewrite the code to use the discovered pattern such that **different loggers could be easily configured and added**.