package ru.spbau.farutin_solikov.gpstracker;

/**
 * Start and stop of a route with name.
 */
public class Displacement {

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    public String getName() {
        return name;
    }

    private int start;
    private int stop;
    private String name;

    Displacement(int start, int stop, String name) {
        this.start = start;
        this.stop = stop;
        this.name = name;
    }
}