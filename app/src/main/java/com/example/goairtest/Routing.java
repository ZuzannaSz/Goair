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
    private Path path;
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
        maxLength = 3 * distanceCalculator(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());
        subNodes = new ArrayList<>();
        paths = new ArrayList<>();

    }

    public void mainRouting() {
        begin = new Node();
        begin = closestPoint(closestNode(this.start), this.start);
        dest = new Node();
        dest = closestPoint(closestNode(this.end), this.end);
        nodes.add(begin);
        nodes.add(dest);
        Edge newEdge = new Edge();
        Node newNode = new Node();
        List<Node> visited = new ArrayList<>();
        Path route = new Path();
        visited.add(begin);
        route.getNodes().add(begin);
        for (int i = 0; i < begin.getEdges().size(); i++) {
            newEdge = begin.getEdges().get(i);
            newNode = getNextNode(begin, newEdge);
            newEdge.setChecked(true);
            int index = getEdgeIndex(newNode, newEdge);
            if (!nodeChecked(newNode, visited)) {
                if (newNode.getLatitude() > 90 && newNode.getLongitude() > 180) {
                    Log.i("NODE", "Empty");
                } else {
                    if (index != -1) {
                        newNode.getEdges().set(index, newEdge);
                    }
                    newPath(newNode, newEdge, visited, route);
                }
            }
        }
    }

    public boolean newPath(Node node, Edge edge, List<Node> visited, Path route)
    {
        int iterations =0;
        if(node.isEqual(dest)) {
            route.getNodes().add(node);
            paths.add(route);
            route=new Path();
            return true;
        }
        for(int i=0; i<node.getEdges().size();i++) {
            iterations++;
            List<Node> nodeTemp = new ArrayList<>(visited);
            nodeTemp.add(node);
            Path routeTemp = new Path();
           // routeTemp = route;
           // routeTemp.getNodes().add(node);
            Edge newEdge = new Edge();
            Node newNode = new Node();
            if(!node.getEdges().get(i).isChecked()) {
                newEdge = node.getEdges().get(i);
                newNode = getNextNode(node,newEdge);
                newEdge.setChecked(true);
                int in= getEdgeIndex(node,newEdge);
                node.getEdges().add(in, edge);
                if(newNode.getLatitude()>90 && newNode.getLongitude() >180) {
                    Log.i("NODE", "Empty node");
                    return false;
                }
                else {

                    int index= getEdgeIndex(newNode,newEdge);
                    if(!nodeChecked(newNode,visited)) {
                        if(index!=-1) {
                            newNode.getEdges().set(index, newEdge);
                            Log.i("NODE", "CHECKING NEW NODE" +newEdge.getIndex());
                            if(newPath(newNode,newEdge,nodeTemp,route))
                            {
                                route.getNodes().add(node);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        if(iterations==0)
        {
            Log.i("BACK", "BACKTRACK --------------------------");
            route.getNodes().remove(node);
            visited.add(node);
            int ind = route.getNodes().size()-1;
            newPath(route.getNodes().get(ind-1), edge, visited,route);
        }
        return false;
    }
    public void getPath(Node node, Edge edge)
    {
        Node prev = new Node();
        int index=0;
        iteration++;
        Log.i("ITERATION", "ITERATION NO" + iteration);
        Log.i("NODE ", "NODE " + node.getLatitude() + " " + node.getLongitude());
        if(path!=null) {
            if(path.getNodes().size()>1) {
                index = path.getNodes().size()-1;
                prev = path.getNodes().get(index-1);
            }
        }
        Edge newEdge = new Edge();
        if(node.getLatitude()>90 && node.getLongitude() >180)
        {
            path.getNodes().remove(node);
            path.getEdges().remove(edge);
            path.setPollution(path.getPollution()-edge.getPolution());
            path.setLength(path.getLength()-edge.getDistance());
            if(edge.getPolution()!=0)
            {
                path.setPollutedL(path.getPollutedL()-edge.getDistance());
            }
            Node prevNode= path.getNodes().get(index-1);
            prevNode.getEdges().remove(edge);
            updateNode(prevNode);
            getPath(prevNode,edge);
            return;
        }
        setEdgeUsed(edge);
        int i=getEdgeIndex(node,edge);
        node.getEdges().get(i).setChecked(true);
        updateNode(node);
       if(isEdge(node))
       {
           newEdge = getNewEdge(node);
       }
        if(node.isEqual(begin))
        {
            index = path.getNodes().size()-1;
            if(index==0) {
            //    edge = getNewEdge(begin);
            //    prev = getNextNode(begin, newEdge);
                return;
            }
            path.setPollutedL(0);
            path.setPollution(0);
            path.setLength(0);
            path.getEdges().clear();
            path.getNodes().clear();
            path.getNodes().add(node);
            path.getNodes().add(prev);
            path.getEdges().add(edge);
            path.setPollution(edge.getPolution());
            if(edge.getPolution()>0)
            {
                path.setPollutedL(edge.getDistance());
            }
            path.setLength(edge.getDistance());
            Log.i("NODE", "IS EQUAL TO START");
            Log.i("PREV", "GO TO PREVIOUS  NODE--------------------------------");
            getPath(prev, edge);
        }
        else if(path.getLength()>maxLength)
        {
            nodes.remove(node);
            path=new Path();
            path.getNodes().add(begin);
            newEdge = getNewEdge(begin);
            newEdge.setChecked(true);
            int ind= getEdgeIndex(begin, newEdge);
            begin.getEdges().add(ind,newEdge);
            updateNode(begin);
            prev=getNextNode(begin,newEdge);
            path.getNodes().add(prev);
            path.getEdges().add(newEdge);
                       Log.i("DISTANCE", "DISTANCE TOO BIG");
            Log.i("PREV", "GO TO PREVIOUS  NODE--------------------------------");
            getPath(prev,newEdge);
        }
        else if(node.isEqual(dest))
        {
            node.setVisited(node.getVisited()+1);
            if(getEdgeNo(node)-1==node.getVisited())
            {
                resetEdgeUsed(node);
                node.setVisited(0);
            }
           // path.getNodes().add(node);
            paths.add(path);
            path.getNodes().remove(node);
            path.getEdges().remove(edge);
            path.setPollution(path.getPollution()-edge.getPolution());
            path.setLength(path.getLength()-edge.getDistance());
            if(edge.getPolution()!=0)
            {
                path.setPollutedL(path.getPollutedL()-edge.getDistance());
            }
            if(checkIfPathExists(path))
            {
                return;
            }
            updateNode(node);
            Log.i("DESTINATION", "IS EQUAL TO END");
            Log.i("PREV", "GO TO PREVIOUS  NODE--------------------------------");
            Node prevNode= path.getNodes().get(index);
            getPath(prevNode,edge);
        }
        else if(!isEdge(node)) {
            node.setVisited(node.getVisited()+1);
            if(getEdgeNo(node)!=0)
            {
                if(getEdgeNo(node)-1==node.getVisited()%getEdgeNo(node))
                {
                    resetEdgeUsed(node);
                   // Log.i("RESET", "RESET USED");
                }
            }
            path.getNodes().remove(node);
            path.getEdges().remove(edge);
            path.setPollution(path.getPollution()-edge.getPolution());
            path.setLength(path.getLength()-edge.getDistance());
            if(edge.getPolution()!=0)
            {
                path.setPollutedL(path.getPollutedL()-edge.getDistance());
            }
            updateNode(node);
            Node prevNode= path.getNodes().get(index-1);
           // Log.i("NODE", "THERE ARE NO NODES LEFT BITCH WE TURNIN BACK");
          //  Log.i("PREV", "GO TO PREVIOUS  NODE--------------------------------");
            getPath(prevNode,edge);
        }
        else {
            //new edge
            node.setVisited(node.getVisited()+1);
            if(getEdgeNo(node)-1==node.getVisited())
            {
                resetEdgeUsed(node);
                node.setVisited(0);

            }

            updateNode(node);
            newEdge.setPolution(getPollution(node,prev,newEdge));
            Node next =new Node();
            next = getNextNode(node, newEdge);
            path.getNodes().add(next);
            path.getEdges().add(newEdge);
            //NO VISITING THE SAME NODE TWICE IN ONE PATH!
            path.setLength(path.getLength()+newEdge.getDistance());
            path.setPollution(path.getPollution()+newEdge.getPolution());
            if(newEdge.getPolution()!=0)
            {
                path.setPollutedL(path.getPollutedL()+newEdge.getDistance());
            }
            //this one works
            //Log.i("ELSE", "WE'RE IN THE ZONE");
            //Log.i("EDGE", "NEW EDGE" + newEdge.getIndex());
           // Log.i("NEXT NODE", "NODE" + next.getLatitude() + next.getLongitude() +" " + next.getEdges().get(0).getIndex());

            getPath(next, newEdge);
        }
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

    public Node getNextNode(Node node, Edge edge) {
        Node newNode = new Node();
        newNode.setLongitude(200);
        newNode.setLatitude(100);
        for(int i=0;i<nodes.size();i++) {
            if(nodes.get(i)!=node) {
                for(int j=0; j<nodes.get(i).getEdges().size();j++) {
                    if(nodes.get(i).getEdges().get(j).getIndex()==edge.getIndex()) {
                        newNode =nodes.get(i);
                        break;
                    }
                }
            }
        }
        return  newNode;
    }

    public int getEdgeNo(Node node) {
        int count =0;
        for(int i=0;i<node.getEdges().size();i++) {
            count++;
        }
        return count;
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
        if(pollution!=0)
        {
            result = pollution/counter;
        }
        else
        {
            result = 0;
        }
        return result;
    }

    public void getSubNodes(Node a, Node b, int length) {
        Node subNode =new Node();
        int l= length/2;
        if(l>MIN_LENGTH)
        {
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
        if(pollution!=0)
        {
            result = pollution/counter;
        }
        else
        {
            result = 0;
        }
        return result;
    }

    public boolean isEdge(Node node) {
        for(int i=0; i<node.getEdges().size();i++) {
            if(!node.getEdges().get(i).isChecked()) {
                return true;
            }
        }
        return false;
    }

    public Node closestNode(GeoPoint point) {
        Node closest =new Node();
        double distance=maxLength;
        double temp;
        for (int i=0;i<nodes.size();i++) {
            temp = distanceCalculator(point.getLatitude(),point.getLongitude(), nodes.get(i).getLatitude(), nodes.get(i).getLongitude());
            if(temp<distance) {
                closest= new Node();
                closest = nodes.get(i);
                distance = temp;
            }
        }
        //Log.i("PARSE NODES", "edge " +closest.getLatitude()+ + closest.getLongitude() +" added" );
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
       // Log.i("Node primary", "Node"+node.getLatitude() + node.getLongitude());
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
       // Log.i("Node second closest", "second closest" + t.getLatitude() + t.getLongitude());
        double a1 =  (t.getLongitude()-node.getLongitude())/(t.getLatitude()-node.getLatitude());
        double a2= -(1/a1);
        double temp1 = a2*point.getLatitude();
        double temp2 = a1*node.getLatitude();
        double x= (-(temp1)+(temp2)+point.getLongitude()-node.getLongitude())/(a1-a2);
        double y = a2*x - a2*point.getLatitude() + point.getLongitude();
       // Log.i("NODES", "-------------new Node:" + x+" " + y +" " + a1 +" " + a2);
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
    public double distanceCalculator(double la1, double lo1, double la2, double lo2)
    {
        double d;
        if(la1==la2 && lo1==lo2)
        {
            d=0;
        }
        else
        {
            la1 = la1/(180/Math.PI);
            la2= la2/(180/Math.PI);
            lo1 = lo1/(180/Math.PI);
            lo2= lo2/(180/Math.PI);
            double dlong = lo2 - lo1;
            double dlat = la2 - la1;

            d = Math.pow(Math.sin(dlat / 2), 2) +
                Math.cos(la1) * Math.cos(la2) *
                        Math.pow(Math.sin(dlong / 2), 2);

            d = 2 * Math.asin(Math.sqrt(d));
            double R = 6371;
            d = d * R*1000 *100;
        }
        return d;
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