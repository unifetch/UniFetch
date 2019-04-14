package com.jay.unidrive.model;

import java.util.HashMap;
import java.util.List;

public class RouteInfo {
    private String destinationName = "";
    private int distance = 0;
    private List<List<HashMap<String, String>>> routes;

    public RouteInfo(String destinationName, int distance, List<List<HashMap<String, String>>> routes) {
        this.destinationName = destinationName;
        this.distance = distance;
        this.routes = routes;
    }

    public RouteInfo(){}

    public String getDestinationName() {
        return destinationName;
    }

    public int getDistance() {
        return distance;
    }

    public List<List<HashMap<String, String>>> getRoutes() {
        return routes;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setRoutes(List<List<HashMap<String, String>>> routes) {
        this.routes = routes;
    }
}
