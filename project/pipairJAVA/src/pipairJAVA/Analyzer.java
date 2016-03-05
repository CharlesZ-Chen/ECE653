package pipairJAVA;

import java.util.HashMap;
import java.util.HashSet;

public class Analyzer {
    protected final int T_SUPPORT;
    protected final double T_CONFIDENCE;
   
    public Analyzer(int t_support, double t_confidence) {
        this.T_SUPPORT = t_support;
        this.T_CONFIDENCE = t_confidence;
    }
 
    public void analyzeGraphAndPrintBugs(CallGraph g) {
        HashMap<Integer, String> functionMap = g.getFunctionMap();
        HashMap<Integer, HashSet<Integer>> nodeMap = g.getNodeMap();
        
        for(int nodeId : nodeMap.keySet()) {
            HashSet<Integer> callSet = nodeMap.get(nodeId);
            for(int callId : callSet) {
                HashSet<Integer> pairCallSet = g.getPairCallSet(callId);
                if(pairCallSet == null)
                    continue;
                int cnt_call = g.getSFCount(callId);
                for(int pairCallId : pairCallSet) {
                    //ignore pairs already exist in this node
                    if(callSet.contains(callId) && callSet.contains(pairCallId))
                        continue;
                    
                    int cnt_pair = callId < pairCallId ? 
                            g.getSPCount(callId, pairCallId) : g.getSPCount(pairCallId, callId);
                    if(cnt_pair < this.T_SUPPORT)
                        continue;
                    double conf = (cnt_pair * 100.0) / cnt_call;    
                    if(conf >= this.T_CONFIDENCE) {
                        String call1 = functionMap.get(callId);
                        String call2 = functionMap.get(pairCallId);
                        String temp;
                        if(call1.compareTo(call2) > 0) {
                            temp = call1;
                            call1 = call2;
                            call2 = temp;
                        }
                        
                        System.out.format("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n", 
                                functionMap.get(callId),
                                functionMap.get(nodeId),
                                call1, call2,
                                cnt_pair,
                                conf);
                    }
                }
            }
        }
    }

}
