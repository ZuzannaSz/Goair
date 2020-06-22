package com.example.goairtest;

import android.util.Log;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Routing {
    private final int MIN_LENGTH = 500;
    private final int RANGE = 500;
    private List<Node> nodes;
    private List<Data> data;
    private List<Path> paths;
    private GeoPoint start;
    private GeoPoint end;
    private Node dest;
    private Node begin;
    private int iteration;
    private List<Node> subNodes;
    private double maxLength;

    public Routing() {
        nodes = new ArrayList<>();
        data = new ArrayList<>();
        paths = new ArrayList<>();
        subNodes = new ArrayList<>();
    }

    public Routing(List<Node> nodes, List<Data> data, GeoPoint start, GeoPoint end) {
        this.nodes = nodes;
        this.data = data;
        this.start = start;
        this.end = end;
        this.iteration = 0;

        maxLength = 2 * distanceCalculator(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());
        subNodes = new ArrayList<>();
        paths = new ArrayList<>();
    }

    public void mainRouting() {
        begin = new Node();
        begin = closestNode(this.start);
        dest = new Node();
        dest = closestNode(this.end);
        Log.i("DESTINATION", "IS " + dest.getLatitude() +" "+ dest.getLongitude());
        Log.i("START", "IS " + begin.getLatitude() + " "+ begin.getLongitude());
        nodes.add(begin);
        nodes.add(dest);
        Edge newEdge = new Edge();
        Node newNode = new Node();
        List<Node> visited = new ArrayList<>();
        Path route = new Path();
        route.getNodes().add(begin);
        newPath(begin, visited, route);
    }
    public void newPath(Node node, List<Node> visited, Path route) {
        visited.add(node);
        if(dest.isEqual(node)) {
            if(route.getPollution()>0) {
                route.setPollution(route.getPollution()/route.getPollutedL());
            }
          paths.add(new Path(route.getNodes()));
          visited.remove(node);
          return;
        }
        if(node.getLongitude()<=180 && paths.size()<20 && route.getLength()<maxLength) {
            for(int i=0; i<node.getEdges().size();i++) {
                Edge newEdge = new Edge();
                Node newNode = new Node();
                List<Node> newNodes = new ArrayList<>();
                newEdge = node.getEdges().get(i);
                newNodes = getNextNode(node, newEdge);
                for(int j=0; j<newNodes.size();j++) {
                    newNode = newNodes.get(j);
                    if (!visited(newNode, visited)) {
                        int pollution = getPollution(node, newNode, newEdge);
                        if (pollution != 0) {
                            route.setPollution(route.getPollution() + pollution);
                            route.setPollutedL(route.getPollutedL() + 1);
                        }
                        route.getNodes().add(newNode);
                        route.setLength(route.getLength() + newEdge.getDistance());
                        newPath(newNode, visited, route);
                        route.getNodes().remove(newNode);
                        route.setLength(route.getLength() - newEdge.getDistance());
                        if (pollution != 0) {
                            route.setPollution(route.getPollution() - pollution);
                            route.setPollutedL(route.getPollutedL() - 1);
                        }
                    }
                }
            }
        }
        visited.remove(node);
    }
    public boolean visited(Node node, List<Node> visited) {
        for(int i=0; i<visited.size();i++) {
            if(visited.get(i).isEqual(node)) {
                return true;
            }
        }
        return false;
    }

    public List<Node> getNextNode(Node node, Edge edge) {
        Node newNode = new Node();
        List<Node> temp = new ArrayList<>();
        newNode.setLongitude(200);
        newNode.setLatitude(100);
        boolean added =false;
        for(int i=0;i<nodes.size();i++) {
            if(!nodes.get(i).isEqual(node)) {
                for(int j=0; j<nodes.get(i).getEdges().size();j++) {
                    if(nodes.get(i).getEdges().get(j).getIndex()==edge.getIndex()) {
                        newNode =nodes.get(i);
                        temp.add(newNode);
                        added =true;
                        }
                }
            }
        }
        if(!added) {
            temp.add(newNode);
        }
        return  temp;
    }

    public int getEdgeNo(Node node) {
        int count =0;
        for(int i=0;i<node.getEdges().size();i++) {
            count++;
        }
        return count;
    }

    public int getPollution(Node a, Node b, Edge e) {
        int pollution=0;
        int result;
        subNodes.clear();
        getSubNodes(a,b,e.getDistance());
        int counter=0;
        for(int i=0;i<subNodes.size();i++) {
            if(subNodes.get(i).getPollution()!=0) {
                pollution+=subNodes.get(i).getPollution();
                counter++;
            }
        }
        if(pollution!=0) {
            result = pollution/counter;
        }
        else {
            result = 0;
        }
        return result;
    }

    public void getSubNodes(Node a, Node b, int length) {
        Node subNode =new Node();
        int l= length/2;
        if(l>MIN_LENGTH) {
            subNode.setLongitude((a.getLongitude()+b.getLongitude())/2);
            subNode.setLatitude((a.getLatitude()+b.getLatitude())/2);
            subNode.setPollution(getSubNodePollution(subNode));
            subNodes.add(subNode);
            getSubNodes(a, subNode, l);
            getSubNodes(subNode,b,l);
        }
    }

    public int getSubNodePollution(Node node) {
        double distance;
        int result;
        int pollution=0;
        int counter=0;
        for(int i=0;i<data.size();i++) {
            distance = distanceCalculator(data.get(i).getLatitude(),data.get(i).getLongitude(),node.getLatitude(),node.getLongitude());
            if(distance<RANGE) {
                pollution +=data.get(i).getPollution();
                counter++;
            }
        }
        if(pollution!=0) {
            result = pollution/counter;
        }
        else {
            result = 0;
        }
        return result;
    }

    public Node closestNode(GeoPoint point) {
        Node closest =new Node();
        double distance=maxLength;
        double temp;
        for (int i=0;i<nodes.size();i++) {
            temp = distanceCalculator(point.getLatitude(),point.getLongitude(), nodes.get(i).getLatitude(), nodes.get(i).getLongitude());
            if(temp<distance) {
                closest= new Node(nodes.get(i));
                distance = temp;
            }
        }
        return closest;
    }

    public Node closestPoint(Node node, GeoPoint point) {
        List<Node> temp = new ArrayList<>();
        Node t = new Node();
        Node m = new  Node();
        double prevD=0;
        int curD;
        int eno = getEdgeNo(node);
        Log.i("edges", "Edge number " +eno);

        for (int i=0; i<node.getEdges().size(); i++) {
            m = new Node();
            m=findByEdge(node, node.getEdges().get(i));

            temp.add(m);
        }
        for (int i=0;i<temp.size();i++) {
            curD =(int)distanceCalculator(node.getLatitude(),node.getLongitude(), temp.get(i).getLatitude(),temp.get(i).getLongitude());

            if(prevD!=0&&curD<prevD) {
                t = new Node();
                t = temp.get(i);
            }
            else if(prevD==0) {
                t = new Node();
                t = temp.get(i);
            }
            prevD = curD;
        }
        double a1 =  (t.getLongitude()-node.getLongitude())/(t.getLatitude()-node.getLatitude());
        double a2= -(1/a1);
        double temp1 = a2*point.getLatitude();
        double temp2 = a1*node.getLatitude();
        double x= (-(temp1)+(temp2)+point.getLongitude()-node.getLongitude())/(a1-a2);
        double y = a2*x - a2*point.getLatitude() + point.getLongitude();
        int e1 = (int)distanceCalculator(x,y,node.getLatitude(),node.getLongitude());
        int e2 = (int)distanceCalculator(x,y,t.getLatitude(),t.getLongitude());
        Edge edge1 = new Edge(edgeIndex()+1,e1);
        Edge edge2 = new Edge(edgeIndex()+2,e2);
        ArrayList<Edge> e = new ArrayList<>();
        e.add(edge1);
        e.add(edge2);
        Node newNode = new Node(x,y,e);
        for(int i=0;i<nodes.size();i++) {
            if(nodes.get(i).isEqual(node)) {
                nodes.get(i).addEdge(edge1);
            }
            else if(nodes.get(i).isEqual(t)) {
                nodes.get(i).addEdge(edge2);
            }
        }
        return newNode;
    }

    public int edgeIndex() {
        int i=0;
        for(int a=0;a<nodes.size();a++) {
            for(int b=0;b<nodes.get(a).getEdges().size();b++) {
                if(nodes.get(a).getEdges().get(b).getIndex()>i) {
                    i=nodes.get(a).getEdges().get(b).getIndex();
                }
            }
        }
        return i;
    }

    public Node findByEdge(Node src, Edge edge) {
        Node next = new Node();
        for(int i=0;i<nodes.size();i++) {
            if(nodes.get(i)!=src) {
                for(int j=0; j<nodes.get(i).getEdges().size();j++) {
                    if(nodes.get(i).getEdges().get(j).getIndex() == edge.getIndex()) {
                        next = new Node();
                        next = nodes.get(i);
                        break;
                    }
                }
            }
        }

        return next;
    }
    public double distanceCalculator(double la1, double lo1, double la2, double lo2) {
        double d;
        if(la1==la2 && lo1==lo2) {
            d=0;
        }
        else {
            la1 = la1/(180/Math.PI);
            la2= la2/(180/Math.PI);
            lo1 = lo1/(180/Math.PI);
            lo2= lo2/(180/Math.PI);
            double dlong = lo2 - lo1;
            double dlat = la2 - la1;
            d = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(la1) * Math.cos(la2) * Math.pow(Math.sin(dlong / 2), 2);
            d = 2 * Math.asin(Math.sqrt(d));
            double R = 6371;
            d = d * R*1000 *100;
        }
        return d;
    }
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
    public List<Node> getNodes() {
        return nodes;
    }
    public void setData(List<Data> data) {
        this.data = data;
    }
    public List<Data> getData() {
        return data;
    }
    public GeoPoint getEnd() {
        return end;
    }

    public GeoPoint getStart() {
        return start;
    }

    public List<Node> getSubNodes() {
        return subNodes;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public void setEnd(GeoPoint end) {
        this.end = end;
    }

    public void setPaths(List<Path> paths) {
        this.paths = paths;
    }

    public void setStart(GeoPoint start) {
        this.start = start;
    }

    public void setSubNodes(List<Node> subNodes) {
        this.subNodes = subNodes;
    }
}
/*



    public void resetEdgeUsed(Node node) {
        for(int i=0; i<node.getEdges().size();i++) {
            node.getEdges().get(i).setChecked(false);
        }
        updateNode(node);
    }
    public boolean nodeChecked(Node node, List<Node> nodes)
    {
        for(int i=0; i<nodes.size();i++)
        {
            if(nodes.get(i).isEqual(node))
            {
                return true;
            }
        }
        return false;
    }

    public Edge getNewEdge(Node node) {
        Edge edge = new Edge();
        for(int i=0;i<node.getEdges().size();i++) {
            if(!node.getEdges().get(i).isChecked()) {
                edge =node.getEdges().get(i);
            }
        }
        return  edge;
    }

    public void setEdgeUsed(Edge edge) {
        for(int i=0;i<nodes.size();i++) {
            for(int j=0; j<nodes.get(i).getEdges().size();j++) {
                if(nodes.get(i).getEdges().get(j).getIndex() == edge.getIndex()) {
                    nodes.get(i).getEdges().get(j).setChecked(true);
                }
            }
        }
    }

    public boolean checkIfPathExists(Path path) {
        for(int i=0;i<paths.size();i++)
        {
            if(path.isEqual(paths.get(i))) {
                return true;
            }
        }
        return false;
    }

 public int getEdgeIndex(Node node,Edge edge) {
        int index=-1;
        for(int i=0;i<node.getEdges().size();i++) {
            if(node.getEdges().get(i).isEqual(edge)) {
                index=i;
            }
        }
        return index;
    }

    public int getNodeIndex(Node node) {
        int index=-1;
        for(int i=0;i<nodes.size();i++) {
            if(nodes.get(i).isEqual(node)) {
                index=i;
                break;
            }
        }
        return index;
    }

    public void updateNode(Node node) {
        for(int i=0; i<nodes.size();i++) {
            if(nodes.get(i).getLatitude()==node.getLatitude() && nodes.get(i).getLongitude() == node.getLongitude()) {
                nodes.get(i).setEdges(node.getEdges());
                nodes.get(i).setVisited(node.getVisited());
            }
        }
    }
    public boolean isEdge(Node node) {
        for(int i=0; i<node.getEdges().size();i++) {
            if(!node.getEdges().get(i).isChecked()) {
                return true;
            }
        }
        return false;
    }
 */
      /*  this.path = new Path();
        Edge newEdge = getNewEdge(begin);
        newEdge.setChecked(true);
        Node next = getNextNode(begin, newEdge);
        path.getNodes().add(begin);
        int index= getNodeIndex(begin);
        int ind= getEdgeIndex(begin, newEdge);
        begin.getEdges().add(ind,newEdge);
        nodes.add(index,begin);
        getPath(next,newEdge);*/
