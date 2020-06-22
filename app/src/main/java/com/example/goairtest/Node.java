package com.example.goairtest;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private double latitude;
    private double longitude;
    private int pollution;
    private int visited;
    private List<Edge> edges;

    public Node() {    }
    public Node(Node node) {
        this.latitude=node.getLatitude();
        this.longitude=node.getLongitude();
        this.edges= new ArrayList<>();
        this.edges.addAll(node.getEdges());
    }
    public Node(double latitude, double longitude, ArrayList<Edge> edges) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.edges = new ArrayList<>();
        this.edges.addAll(edges);
        this.visited =0;
    }
    public boolean isEqual(Node a) {
        if(this.getLongitude()==a.getLongitude() && this.getLatitude()==a.getLatitude()) {
            return true;
        }
        return false;
    }
    public int getPollution() {
        return pollution;
    }

    public void setPollution(int pollution) {
        this.pollution = pollution;
    }

    public int getVisited() {
        return visited;
    }

    public void setVisited(int visited) {
        this.visited = visited;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = new ArrayList<>(edges);
    }
    public void addEdge(Edge edge)
    {
        this.edges.add(edge);
    }
}
