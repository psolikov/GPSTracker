package ru.spbau.farutin_solikov.gpstracker;

import java.util.List;

/**
 * A route with name.
 */
public class Route {
    public List<Coordinate> getRoute() {
        return route;
    }

    public String getName() {
        return name;
    }

    private List<Coordinate> route;
    private String name;

    Route(List<Coordinate> route, String name) {
        this.route = route;
        this.name = name;
    }
}