package com.example.goairtest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Path {
    private List<Node> nodes;
    private int pollution;
    private int length;
    private int pollutedL;
    public Path() {
        nodes = new ArrayList<>();
        this.length=0;
        this.pollution=0;
        this.pollutedL=0;
    }
    public Path(List<Node> nodes ) {
        this.length=0;
        this.nodes = new ArrayList<>();
        this.nodes.addAll(nodes);
        this.pollutedL=0;
        this.pollution=0;
    }
    public int getPollutedL() {
        return pollutedL;
    }

    public void setPollutedL(int pollutedL) {
        this.pollutedL = pollutedL;
    }

    public void setPollution(int pollution) {
        this.pollution = pollution;
    }
    public boolean isEqual(Path p) {
        boolean result = true;
        if(this.getNodes().size()==p.getNodes().size()) {
            for(int i=0;i<this.getNodes().size();i++) {
                if(this.getNodes().get(i)!=p.getNodes().get(i)) {
                    result = false;
                    break;
                }
            }
        }
        else {
            result=false;
        }
        return result;
    }

    public int getPollution() {
        return pollution;
    }

    public int getLength() {
        return length;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
class SortByDistance implements Comparator<Path> {
    public int compare(Path a, Path b)
    {
        return a.getLength()- b.getLength();
    }
}
class SortByPollution implements Comparator<Path> {
    public int compare(Path a, Path b)
    {
        return a.getPollution()- b.getPollution();
    }
}