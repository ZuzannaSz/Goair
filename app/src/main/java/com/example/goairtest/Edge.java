package com.example.goairtest;


public class Edge {
    private int index;
    private int distance;
    private int polution;
    private boolean checked;
    public Edge()
    {}
    public  Edge(int index, int distance)
    {
        this.index=index;
        this.distance=distance;
        this.checked =false;
    }
    public Edge(int index, int distance, int polution)
    {
        this.index = index;
        this.distance = distance;
        this.polution=polution;
        this.checked=false;
    }
    public boolean isEqual(Edge edge)
    {
        if(this.index== edge.index)
        {
            return true;
        }
        return false;
    }
    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getDistance() {
        return distance;
    }

    public int getIndex() {
        return index;
    }

    public int getPolution() {
        return polution;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setPolution(int polution) {
        this.polution = polution;
    }
}
