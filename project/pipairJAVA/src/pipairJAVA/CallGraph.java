package pipairJAVA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CallGraph {
    protected HashMap<Integer, String> functionMap;
    protected HashMap<Integer, HashSet<Integer>> nodeMap;
    protected HashMap<Integer, HashMap<Integer, Integer>> supportP;
    protected HashMap<Integer, Integer> supportF;
    protected HashMap<Integer, HashSet<Integer>> pairCallMap;

    public CallGraph() {
        functionMap = new HashMap<Integer, String>();
        nodeMap = new HashMap<Integer, HashSet<Integer>>();
        supportP = new HashMap<Integer, HashMap<Integer, Integer>>();
        supportF = new HashMap<Integer, Integer>();
        pairCallMap = new HashMap<Integer, HashSet<Integer>>();
    }

    /**
     * update supportP by callSet
     * @param callIdSet : a callSet of a new added node
     */
    protected void updateSupportP(HashSet<Integer> callIdSet) {
        List<Integer> sortedList = new ArrayList<Integer>(callIdSet);
        Collections.sort(sortedList);
        for(int i = 0; i < sortedList.size()-1; i++) {
            HashMap<Integer, Integer> supportPi;
            HashSet<Integer> callAPairSet;
            int callAId = sortedList.get(i);
            if((supportPi = this.supportP.get(callAId)) == null) {
                supportPi = new HashMap<Integer, Integer>();
                this.supportP.put(callAId, supportPi);
            }
            
            if((callAPairSet = this.pairCallMap.get(callAId)) == null) {
                callAPairSet = new HashSet<Integer>();
                this.pairCallMap.put(callAId, callAPairSet);
            }
            
            for(int j = i+1; j < sortedList.size(); j++) {
                int callBId = sortedList.get(j);
                if(supportPi.get(callBId) == null) {
                    supportPi.put(callBId, 1);
                } else {
                    supportPi.put(callBId, supportPi.get(callBId)+1);
                }
                
                callAPairSet.add(callBId);
                if(this.pairCallMap.get(callBId) == null) {
                    HashSet <Integer> callBPairSet = new HashSet<Integer>();
                    callBPairSet.add(callAId);
                    this.pairCallMap.put(callBId, callBPairSet);
                } else {
                this.pairCallMap.get(callBId).add(callAId);
                }
            }
        }
    }

    /**
     * update supportF by callSet
     * @param callIdSet: the callSet of a new added node
     */
    protected void updateSupportF(HashSet<Integer> callIdSet) {
        for(int callId : callIdSet) {
            if(this.supportF.get(callId) == null) {
                this.supportF.put(callId, 1);
            }
            else {
                this.supportF.put(callId, this.supportF.get(callId)+1); 
            }
        }
    }

    /**
     * add a new node to this call graph
     * If there is a node already has the same name, do nothing
     * @param nodeName
     * @param callNameList
     */
    public void addNode(String nodeName, List<String> callNameList) {
        int nodeId = nodeName.hashCode();
        if(this.nodeMap.containsKey(nodeId))
            return;

        this.functionMap.put(nodeId, nodeName);
        
        HashSet<Integer> callIdSet = new HashSet<Integer>();
        for(String callName : callNameList) {
            int callId = callName.hashCode();
            this.functionMap.put(callId, callName);
            callIdSet.add(callId);
        }
        this.updateSupportF(callIdSet);
        this.updateSupportP(callIdSet);
        this.nodeMap.put(nodeId, callIdSet);
    }
    
    public HashMap<Integer, String> getFunctionMap() {
        return this.functionMap;
    }
    
    public HashMap<Integer,HashSet<Integer>> getNodeMap() {
        return this.nodeMap;
    }
    
    public int getSPCount(int callAId, int callBId) {
        return this.supportP.get(callAId).get(callBId);
    }
    
    public int getSFCount(int funcId) {
        return this.supportF.get(funcId);
    }
    
    public HashSet<Integer> getPairCallSet(int callId) {
        return this.pairCallMap.get(callId);
    }
    
    public void printNode(String nodeName) {
        int nodeId = nodeName.hashCode();
        System.out.format("node %s calls:\n",
                this.functionMap.get(nodeId));
        for(int callId : this.nodeMap.get(nodeId)) {
            System.out.println(this.functionMap.get(callId));
        }
        System.out.println("has pair:");
//        for(Integer node1 : this.supportP.keySet()) {
//            HashMap<Integer, Integer> node1Nbr = this.supportP.get(node1);
//            for(Integer node2 : node1Nbr.keySet()) {
//                if(this.isNodeHasPair(nodeId, node1, node2)) {
//                    System.out.format("<%s,%s>\n",
//                               this.functionMap.get(node1),
//                               this.functionMap.get(node2));
//                }
//            }
//        }
    }
    
    public void printGraphInfo() {
        System.out.println("size of supportP is:"+this.supportP.size());
        System.out.println("size of supportF is:"+this.supportF.size());
        System.out.println("Pair\t\tcount");
        for(Integer node1 : this.supportP.keySet()) {
            HashMap<Integer, Integer> node1Nbr = this.supportP.get(node1);
            for(Integer node2 : node1Nbr.keySet()) {
                System.out.format("<%s,%s>\t%d\n", 
                        this.functionMap.get(node1),
                        this.functionMap.get(node2),
                        node1Nbr.get(node2));
            }
        }
        System.out.println("Func\tcount");
       for(Integer func : this.supportF.keySet()) {
           System.out.format("%s\t%d\n",
                   this.functionMap.get(func),
                   this.supportF.get(func));
       }
    }
}
