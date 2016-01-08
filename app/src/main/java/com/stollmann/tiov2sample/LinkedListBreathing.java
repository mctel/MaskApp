package com.stollmann.tiov2sample;

/**
 * Created by marcoscandelaboti on 17/12/15.
 */
public class LinkedListBreathing {

    private Node headV;
    private Node headK;
    private Node headTimeV;
    private Node headTimeK;
    private int listCountV;
    private int listCountK;
    private int listCountTimeV;
    private int listCountTimeK;

    public LinkedListBreathing(){
        headV = new Node(null);
        headK = new Node(null);
        headTimeV = new Node(null);
        headTimeK = new Node(null);
        listCountV = 0;
        listCountK = 0;
        listCountTimeK = 0;
        listCountTimeV = 0;
    }


    // post: appends the specified element to the end of this list.
    public void addVolume(double time, double volume){
        Node tempTime = new Node(time);
        Node tempVolume = new Node(volume);
        Node currentT = headTimeV;
        Node currentV = headV;
        // starting at the head node, crawl to the end of the list
        while(currentT.getNext() != null) {
            currentT = currentT.getNext();
        }
        while(currentV.getNext() != null) {
            currentV = currentV.getNext();
        }
        // the last node's "next" reference set to our new node
        currentT.setNext(tempTime);
        currentV.setNext(tempVolume);
        listCountV++;// increment the number of elements variable
        listCountTimeV++;
    }

    public void addKcal(double time, double kcal){
        Node tempTime = new Node(time);
        Node tempKcal = new Node(kcal);
        Node currentT = headTimeK;
        Node currentK = headK;
        while(currentT.getNext() != null) {
            currentT = currentT.getNext();
        }
        while(currentK.getNext() != null) {
            currentK = currentK.getNext();
        }
        currentT.setNext(tempTime);
        currentK.setNext(tempKcal);
        listCountK++;
        listCountTimeK++;
    }

    // post: inserts the specified element at the specified position in this list.
    public void addVolume(double time, double volume, int index) {
        Node tempTime = new Node(time);
        Node tempVolume = new Node(volume);
        Node currentT = headTimeV;
        Node currentV = headV;
        // crawl to the requested index or the last element in the list,
        // whichever comes first
        for(int i = 1; i < index && currentT.getNext() != null; i++) {
            currentT = currentT.getNext();
        }
        for(int i = 1; i < index && currentV.getNext() != null; i++) {
            currentV = currentV.getNext();
        }
        // set the new node's next-node reference to this node's next-node reference
        tempTime.setNext(currentT.getNext());
        tempTime.setNext(currentV.getNext());
        // now set this node's next-node reference to the new node
        currentT.setNext(tempTime);
        currentV.setNext(tempVolume);
        listCountTimeV++;// increment the number of elements variable
        listCountV++;
    }

    public void addKcal(double time, double kcal, int index) {
        Node tempTime = new Node(time);
        Node tempKcal = new Node(kcal);
        Node currentT = headTimeK;
        Node currentK = headK;
        // crawl to the requested index or the last element in the list,
        // whichever comes first
        for(int i = 1; i < index && currentT.getNext() != null; i++) {
            currentT = currentT.getNext();
        }
        for(int i = 1; i < index && currentK.getNext() != null; i++) {
            currentK = currentK.getNext();
        }
        // set the new node's next-node reference to this node's next-node reference
        tempTime.setNext(currentT.getNext());
        tempTime.setNext(currentK.getNext());
        // now set this node's next-node reference to the new node
        currentT.setNext(tempTime);
        currentT.setNext(tempKcal);
        listCountK++;// increment the number of elements variable
        listCountTimeK++;
    }

    // post: returns the element at the specified position in this list.
    public Object getVolume(int index){
        // index must be 1 or higher
        if(index <= 0) return null;

        Node current = headV.getNext();
        for(int i = 1; i < index; i++) {
            if(current.getNext() == null) return null;
            current = current.getNext();
        }
        return current.getData();
    }

    public Object getKcal(int index){
        // index must be 1 or higher
        if(index <= 0) return null;

        Node current = headK.getNext();
        for(int i = 1; i < index; i++) {
            if(current.getNext() == null) return null;
            current = current.getNext();
        }
        return current.getData();
    }

    // post: removes the element at the specified position in this list.
    public boolean removeVolume(int index) {
        // if the index is out of range, exit
        if(index < 1 || index > sizeV()) return false;
        if(index < 1 || index > sizeTimeV()) return false;

        Node currentVolume = headV;
        Node currentTime = headTimeV;
        for(int i = 1; i < index; i++) {
            if(currentVolume.getNext() == null) return false;
            currentVolume = currentVolume.getNext();
            if(currentTime.getNext() == null) return false;
            currentTime = currentTime.getNext();
        }
        currentVolume.setNext(currentVolume.getNext().getNext());
        currentTime.setNext(currentTime.getNext().getNext());
        listCountV--; // decrement the number of elements variable
        listCountTimeV--;
        return true;
    }

    public boolean removeKcal(int index) {
        if(index < 1 || index > sizeK()) return false;
        if(index < 1 || index > sizeTimeK()) return false;


        Node currentKcal = headK;
        Node currentTime = headTimeK;
        for(int i = 1; i < index; i++) {
            if(currentKcal.getNext() == null) return false;
            currentKcal = currentKcal.getNext();
            if(currentTime.getNext() == null) return false;
            currentTime = currentTime.getNext();
        }
        currentKcal.setNext(currentKcal.getNext().getNext());
        currentTime.setNext(currentTime.getNext().getNext());
        listCountK--; // decrement the number of elements variable
        listCountTimeK--;
        return true;
    }

    // post: returns the number of elements in this list.
    public int sizeV(){
        return listCountV;
    }
    public int sizeK(){
        return listCountK;
    }

    public int sizeTimeK(){
        return listCountTimeK;
    }
    public int sizeTimeV(){
        return listCountTimeV;
    }


    public String toStringVolume(){
        Node current = headV.getNext();
        String output = "";
        while(current != null) {
            output += "[" + current.getData().toString() + "]";
            current = current.getNext();
        }
        return output;
    }

    public String toStringKcal(){
        Node current = headK.getNext();
        String output = "";
        while(current != null) {
            output += "[" + current.getData().toString() + "]";
            current = current.getNext();
        }
        return output;
    }

    public String toStringVolumeTime(){
        Node current = headTimeV.getNext();
        String output = "";
        while(current != null) {
            output += "[" + current.getData().toString() + "]";
            current = current.getNext();
        }
        return output;
    }

    public String toStringKcalTime(){
        Node current = headTimeK.getNext();
        String output = "";
        while(current != null) {
            output += "[" + current.getData().toString() + "]";
            current = current.getNext();
        }
        return output;
    }

    private class Node {
        // reference to the next node in the chain,
        // or null if there isn't one.
        Node next;
        // data carried by this node.
        // could be of any type you need.
        Object data;

        public Node(Object _data){
            next = null;
            data = _data;
        }

        // another Node constructor if we want to
        // specify the node to point to.
        public Node(Object _data, Node _next) {
            next = _next;
            data = _data;
        }

        // these methods should be self-explanatory
        public Object getData() {
            return data;
        }

        public void setData(Object _data) {
            data = _data;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node _next) {
            next = _next;
        }
    }
}