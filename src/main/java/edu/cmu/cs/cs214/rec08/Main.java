package edu.cmu.cs.cs214.rec08;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
    private static final int NUM_REQUESTS = 100;
    private static String URL_STR = "http://feature.isri.cmu.edu/";
    private static HttpClient client = HttpClient.newHttpClient();
    private static String REQ_URI = "https://api.clarifai.com/v2/users/clarifai/apps/main/models/general-image-recognition/versions/aa7f35c01e0642fda5cf400f543e7c40/outputs";


    private static void runWebAPIRequest() throws IOException, InterruptedException {
        // read the request body
        String bodyStr = new String(Files.readAllBytes(Paths.get("src/main/resources/request-body.json")));
        String key = ""; // TODO: fill in your PAT here
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(REQ_URI))
            .header("Authorization", "Key " + key)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(bodyStr))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("original response in JSON format: \n" + response.body());

        // parse json
        JSONObject obj = new JSONObject(response.body());
        JSONArray arr = obj.getJSONArray("outputs")
        .getJSONObject(0)
        .getJSONObject("data")
        .getJSONArray("concepts");
        System.out.println("\n\nTop5 results with probability:");
        for (int i = 0; i < 5; i++)
        {
            String name = arr.getJSONObject(i).getString("name");
            Double value = arr.getJSONObject(i).getDouble("value");
            System.out.printf("%s: %f\n", name, value);
        }
        
    }

    /**
     * Making web API requests using the synchronous send method
     * This means that we can only make requests one after another
     * @throws IOException
     * @throws InterruptedException
     */
    private static void runMultipleSynchronous() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create(URL_STR))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

        Instant start = Instant.now();
        for (int i = 0; i < NUM_REQUESTS; i++) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        System.out.println("Total time sync (ms): " + Duration.between(start, Instant.now()).toMillis());
    }

    /**
     * Making one single API requests using the asynchronous sendAsync method
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private static void runSingleAsync() throws IOException, InterruptedException, ExecutionException {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(URL_STR))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        client = HttpClient.newBuilder().executor(executorService).build();

        Instant start = Instant.now();

        CompletableFuture<HttpResponse<String>> responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        // observe when this statement is printed out to the console. 
        responseFuture.thenRun(() -> System.out.println("do other things after finished..."));
        System.out.println("do other things...");
        HttpResponse<String> response = responseFuture.join();
        // System.out.println("The response is:" + response.body());

        System.out.println("Total time async for one request (ms): " + Duration.between(start, Instant.now()).toMillis());
    }

    /**
     * Making web API requests using the asynchronous sendAsync method
     * But make sure that at most 10 requests are sent to the server at the same time.
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    private static void runMultipleAsynchronous() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create(URL_STR))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        client = HttpClient.newBuilder().executor(executorService).build();
        List<CompletableFuture<HttpResponse<String>>> allFutures = new ArrayList<>(100);
        Semaphore parallelRequestCounter = new Semaphore(10);

        Instant start = Instant.now();

        for (int i = 0; i < NUM_REQUESTS; i++) {
            parallelRequestCounter.acquire();
            CompletableFuture<HttpResponse<String>> responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
//            responseFuture.thenAccept(System.out::println);
            responseFuture.thenRun(() -> parallelRequestCounter.release());
            allFutures.add(responseFuture);
        }

        List<HttpResponse<String>> results = new ArrayList<>(100);
        for (CompletableFuture<HttpResponse<String>> future : allFutures) {
            results.add(future.join());
        }

        System.out.println("Total time multiple async (ms): " + Duration.between(start, Instant.now()).toMillis());
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        // Task 1
        // runWebAPIRequest();
        // Task 2
       runMultipleSynchronous();
    //    runSingleAsync();
       runMultipleAsynchronous();
        System.exit(0);
    }
}
