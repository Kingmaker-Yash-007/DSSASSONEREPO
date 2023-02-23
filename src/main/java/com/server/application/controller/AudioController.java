package com.server.application.controller;

import com.server.application.ServerApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import com.server.application.model.Audio;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@SpringBootApplication
@RestController
@Api(value = "Audio API", tags = { "Audio" })
public class AudioController {

    private final ConcurrentHashMap<String, Audio> audioMap = new ConcurrentHashMap<>(); // In-memory data structure for storing Audio items
    private int totalCopiesSold = 0; // Total number of copies sold for all Audio items
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Thread pool with a fixed size of 10 threads
    @RequestMapping(value="/")
    public void redirect(HttpServletResponse response) throws IOException, IOException {
        response.sendRedirect("/swagger-ui.html");
    }
    // GET endpoint for retrieving a single property of an Audio item
    @GetMapping("/audio/{artistName}/property")
    @ApiOperation(value = "Get a single property of an Audio item", response = String.class)
    public String getAudioProperty(
            @ApiParam(value = "The artist name of the Audio item to retrieve", required = true)
            @PathVariable String artistName,
            @ApiParam(value = "The name of the property to retrieve", required = true)
            @RequestParam String key) {
        Audio audio = null;
        for (Audio item : audioMap.values()) {
            if (item.getArtistName().equals(artistName)) {
                audio = item;
                break;
            }
        }
        if (audio == null) {
            return "Audio item not found";
        }
        switch(key.toLowerCase()) {
            case "artistname":
                return audio.getArtistName();
            case "tracktitle":
                return audio.getTrackTitle();
            case "albumtitle":
                return audio.getAlbumTitle();
            case "tracknumber":
                return String.valueOf(audio.getTrackNumber());
            case "year":
                return String.valueOf(audio.getYear());
            case "numreviews":
                return String.valueOf(audio.getNumReviews());
            case "numcopiessold":
                return String.valueOf(totalCopiesSold);
            default:
                return "Invalid key";
        }
    }


    // POST endpoint for creating a new Audio item
    @PostMapping("/audio")
    @ApiOperation(value = "Create a new Audio item")
    public void createAudio(
            @ApiParam(value = "The new Audio item to create", required = true)
            @RequestBody Audio newAudio) {
        executorService.submit(() -> {
            audioMap.put(newAudio.getTrackTitle(), newAudio);
            totalCopiesSold += newAudio.getNumCopiesSold();
        });
    }

    // GET endpoint for retrieving all Audio items
    @GetMapping("/audio/all")
    @ApiOperation(value = "Get all Audio items", response = List.class)
    public List<Audio> getAllAudio() {
        return new ArrayList<Audio>(audioMap.values());
    }

    // Method to insert seed data
    @PostConstruct
    public void insertSeedData() {
        Audio audio = new Audio("The Beatles", "Help!", "Help!", 1, 1965, 100, 5000000);
        Audio anotherAudio = new Audio("Miles Davis", "So What", "Kind of Blue", 1, 1959, 500, 1000000);
        executorService.submit(() -> {
            audioMap.put(audio.getTrackTitle(), audio);
            audioMap.put(anotherAudio.getTrackTitle(), anotherAudio);
            totalCopiesSold += audio.getNumCopiesSold() + anotherAudio.getNumCopiesSold();
        });
    }
}
