package pipairJAVA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CallGraph {
    public HashMap<Integer, String> functionMap;
    public HashMap<Integer, HashSet<Integer>> origNodeMap;
    public HashMap<Integer, HashMap<Integer, Integer>> supportP;
    public HashMap<Integer, Integer> supportF;
    public HashMap<Integer, HashSet<Integer>> pairCallMap;
    public HashMap<Integer, HashSet<Integer>> expandNodeMap;
    public final int EXPAND_LEVEL;

    public CallGraph(int expand_level) {
        functionMap = new HashMap<Integer, String>();
        origNodeMap = new HashMap<Integer, HashSet<Integer>>();
        supportP = new HashMap<Integer, HashMap<Integer, Integer>>();
        supportF = new HashMap<Integer, Integer>();
        pairCallMap = new HashMap<Integer, HashSet<Integer>>();
        /*modification for Part I c*/
        this.EXPAND_LEVEL = expand_level;
        this.expandNodeMap = null;
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
        if(this.origNodeMap.containsKey(nodeId))
            return;

        this.functionMap.put(nodeId, nodeName);
        
        HashSet<Integer> callIdSet = new HashSet<Integer>();
        for(String callName : callNameList) {
            int callId = callName.hashCode();
            this.functionMap.put(callId, callName);
            callIdSet.add(callId);
        }
        
        /*modification for Part I c
         * if expand level is 0, then updateSupport during addNode*/
        if(this.EXPAND_LEVEL == 0) {
        this.updateSupportF(callIdSet);
        this.updateSupportP(callIdSet);
        }
        
        this.origNodeMap.put(nodeId, callIdSet);
    }
    
    /**modification for Part I c
     * updateSupport after expand if necesssary
     */
    public void updateSupport() {
        /*if expand level is 0, then Support is already update during addNode*/
        if(this.EXPAND_LEVEL == 0)
            return;
        /*make a deep copy for expanding*/
        this.expandNodeMap = deepcloneNodeMap();
        /*expand call graph first and then update Support*/
        for(int i=0; i < this.EXPAND_LEVEL; i++) {
            expandGraph();
        }
        /*using expandNodeMap to update SupportF & P*/
        for(HashSet<Integer> callIdSet : this.expandNodeMap.values()){
            this.updateSupportF(callIdSet);
            this.updateSupportP(callIdSet);
        }
    }
    
    /**modification for Part I c
     * expand the graph (expandNodeMap) according to the original graph (origNodeMap)
     */
    protected void expandGraph() {
        for(int nodeId : this.expandNodeMap.keySet()) {
            HashSet<Integer> nodeCallSet = this.expandNodeMap.get(nodeId);
            /*do not expand a node that only contain external nodes or contain nothing at all*/
            if(nodeCallSet.size() == 0)
                continue;

            HashSet<Integer> removeSet = new HashSet<Integer>();
            HashSet<Integer> addSet = new HashSet<Integer>();
            for(int calleeId : nodeCallSet){
                HashSet<Integer> calleeCallSet = this.origNodeMap.get(calleeId); //the expand callSet should refer to original call Graph
                if(calleeCallSet.size() > 0){
                    /*using this if-statement to NOT expand a node that contains external node (printf, fputs, etc.)
                     * comment out this to expand all nodes*/
                    if(isContainExternal(calleeCallSet))
                        continue;

                    /*collect the remove & add info first*/
                    removeSet.add(calleeId);
                    addSet.addAll(calleeCallSet);
                }
            }
            /*propagate updates to the nodeCallSet at once*/
           nodeCallSet.removeAll(removeSet);
           nodeCallSet.addAll(addSet);
        }
    }

    /**
     * check whether a callSet contain an "external" node
     * @param callSet
     * @return
     */
    protected boolean isContainExternal(HashSet<Integer> callSet) {
        for(int callId : callSet){
            if(this.origNodeMap.get(callId).size() == 0)
                return true;
        }
        return false;
    }

    /**modification for Part I c
     * deep clone current node map
     * @return a deep copy of current node map
     */
    protected HashMap<Integer, HashSet<Integer>> deepcloneNodeMap() {
        HashMap<Integer, HashSet<Integer>> deepclone = new HashMap<Integer, HashSet<Integer>>();
        for(int nodeId : this.origNodeMap.keySet()) {
            HashSet<Integer> origV = this.origNodeMap.get(nodeId);
            HashSet<Integer> deepCopyV = new HashSet<Integer>();
            for(Integer e : origV) {
                deepCopyV.add(e);
            }
            deepclone.put(nodeId, deepCopyV);
        }
        return deepclone;
    }

    public HashMap<Integer, String> getFunctionMap() {
        return this.functionMap;
    }
    
    public HashMap<Integer,HashSet<Integer>> getNodeMap() {
        return this.EXPAND_LEVEL > 0 ? this.expandNodeMap : this.origNodeMap;
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
    
    public void printNodes() {
        for(int nodeId : this.origNodeMap.keySet()) {
            printNode(this.functionMap.get(nodeId));
        }
    }
    
    public void printNode(String nodeName) {
        int nodeId = nodeName.hashCode();
        HashSet<Integer> callSet = this.origNodeMap.get(nodeId);
        System.out.format("node %s calls:\n",
                this.functionMap.get(nodeId));
        for(int callId : this.origNodeMap.get(nodeId)) {
            System.out.println(this.functionMap.get(callId));
        }
        
        System.out.println("has pair:");
        for(Integer node1 : this.supportP.keySet()) {
            HashMap<Integer, Integer> node1Nbr = this.supportP.get(node1);
            for(Integer node2 : node1Nbr.keySet()) {
                if(callSet.contains(node1) && callSet.contains(node2)) {
                    System.out.format("<%s,%s>\n",
                               this.functionMap.get(node1),
                               this.functionMap.get(node2));
                }
            }
        }
    }
    
    public void printGraphInfo() {
        System.out.println("expand level is:" + this.EXPAND_LEVEL);
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
