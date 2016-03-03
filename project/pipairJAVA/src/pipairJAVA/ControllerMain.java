package pipairJAVA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Just for testing upper modules which needs controller
 * @author charleszhuochen
 *
 */
public class ControllerMain implements Controller{
    public static void main(String [] args) {
        int t_support = 3;
        double t_confidence = 0.65;
        String fileName = null;
        try {
            switch(args.length) {
                case 1: fileName = args[0]; break;
                case 3: {
                    fileName = args[0];
                    t_support = Integer.parseInt(args[1]);
                    t_confidence = Double.parseDouble(args[2]);
                    break;
                    }
                default:{
                    System.err.println("Error: mismatch number of arguments, cmd arguments sould be "
                            + "<file_name> <T_SUPPORT> <T_CONFIDENCE> or <file_name>");
                    System.exit(1);
                }
            }
        }
        catch(NumberFormatException nfe) {
            System.err.println("Error: the second argument must be an integer,"
                    + "the third argument must be a double or float");
            System.exit(1);
        }
        
    		Controller c = new ControllerMain();
    		Analyzer a = new Analyzer(t_support, t_confidence);
    		CallGraph g = null;
    		
    		try {
    		    g = c.readCallGraph(fileName);
    		} catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
    		
    		List<Bug> bugList = a.analyzeGraph(g);
    		c.printBugList(bugList);
    }
    
    protected Pattern callsP;
    protected Pattern nodeP;
    
    public ControllerMain() {
    		this.callsP = Pattern.compile("^.*calls function "
				+ "\'(\\w+)\'"
				+ ".*$");
    		this.nodeP = Pattern.compile("^Call graph node for function: "
    				+ "\'(\\w+)\'"
    				+ ".*$");
    }
    
	@Override
	public CallGraph readCallGraph(String fileName) throws IOException {
		CallGraph g = null;
		BufferedReader in= null;

		in = new BufferedReader(new FileReader(fileName));
		g = new CallGraph();	
		String thisLine = null;
		thisLine = in.readLine();
		while(true) {
		if(thisLine == null)
			break;
		Matcher nodeM = this.nodeP.matcher(thisLine);
		if(nodeM.matches()) {
		    String nodeName = nodeM.group(1);
		    List<String> callNameList = new ArrayList<String>();
			Matcher callsM = this.callsP.matcher("");
			while(true) {
				thisLine = in.readLine();
				if(thisLine == null)
					break;
				if(callsM.reset(thisLine).matches()) {
				    callNameList.add(callsM.group(1));
				} else if(nodeM.reset(thisLine).matches())
					break;
			}
		g.addNode(nodeName, callNameList);
//				g.printNode(nodeName);
		} else {
			thisLine = in.readLine();
		}
	}

	in.close();
//		g.printGraphInfo();
		return g;
	}
	
	@Override
	public void printBugList(List<Bug> bugList) {
	    for(Bug bug : bugList) {
	        String p1, p2;
	        if(bug.nameHashCode < bug.p1HashCode) {
	            p1 = bug.name;
	            p2 = bug.p1;
	        } else {
	            p1 = bug.p1;
	            p2 = bug.name;
	        }
	        for(String location : bug.locationSet) {
	        System.out.format("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n",
	                bug.name,
	                location,
	                p1, p2,
	                bug.support,
	                bug.conf);
	        }
	    }
	}
}
