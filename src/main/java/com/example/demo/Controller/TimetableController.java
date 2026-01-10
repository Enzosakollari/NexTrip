package com.example.demo.Controller;

import com.example.demo.Service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping("/completion")
    public List<Map<String, Object>> getCompletion(
            @RequestParam String term,
            @RequestParam(required = false) Boolean nofavorites,
            @RequestParam(required = false) Boolean showIds,
            @RequestParam(required = false) Boolean showCoordinates
    ) {
        return timetableService.getCompletion(term, nofavorites, showIds, showCoordinates);
    }

    @GetMapping("/completion/coordinates")
    public List<Map<String, Object>> getStationsByCoordinates(
            @RequestParam String latlon,
            @RequestParam(required = false) Integer accuracy,
            @RequestParam(required = false) Boolean showIds,
            @RequestParam(required = false) Boolean showCoordinates
    ) {
        return timetableService.getStationsByCoordinates(latlon, accuracy, showIds, showCoordinates);
    }

    @GetMapping("/route")
    public Map<String, Object> searchRoute(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) String via,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String timeType,
            @RequestParam(required = false) Integer num,
            @RequestParam(required = false) Integer pre,
            @RequestParam(required = false) Boolean showDelays,
            @RequestParam(required = false) Boolean showTrackChanges,
            @RequestParam(required = false) Boolean oneToMany,
            @RequestParam(required = false) Integer interestDuration,
            @RequestParam(required = false) String transportationTypes,
            @RequestParam(required = false) Boolean summary
    ) {
        return timetableService.searchRoute(from, to, via, date, time, timeType, num, pre,
                showDelays, showTrackChanges, oneToMany, interestDuration, transportationTypes, summary);
    }

    @GetMapping("/station")
    public Map<String, Object> getStation(@RequestParam String stop) {
        return timetableService.getStation(stop);
    }

    @GetMapping("/stationboard")
    public Map<String, Object> getStationboard(
            @RequestParam String stop,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean showTracks,
            @RequestParam(required = false) Boolean showSubsequentStops,
            @RequestParam(required = false) Boolean showDelays,
            @RequestParam(required = false) Boolean showTrackChanges,
            @RequestParam(required = false) String transportationTypes
    ) {
        return timetableService.getStationboard(stop, date, time, mode, limit, showTracks,
                showSubsequentStops, showDelays, showTrackChanges, transportationTypes);
    }
}
