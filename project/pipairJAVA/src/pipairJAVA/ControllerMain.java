package pipairJAVA;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main entry of this Program
 * @author charleszhuochen
 *
 */
public class ControllerMain{
    
    public static void main(String [] args) {
        int t_support = 3;
        double t_confidence = 65;
        int expand_level = 0;//modification for Part I c
        
        try {
            switch(args.length) {
                case 0: break;
                case 2: {
                    t_support = Integer.parseInt(args[0]);
                    t_confidence = Double.parseDouble(args[1]);
                    break;
                    }
                case 3: {/*modification for Part I c*/
                    t_support = Integer.parseInt(args[0]);
                    t_confidence = Double.parseDouble(args[1]);
                    expand_level = Integer.parseInt(args[2]);
                    break;
                }
                default:{
                    System.err.println("Error: mismatch number of arguments, cmd arguments sould be "
                            + "null or <T_SUPPORT> <T_CONFIDENCE> or <T_SUPPORT> <T_CONFIDENCE> <EXPAND_LEVEL>");
                    System.exit(1);
                }
            }
        }
        catch(NumberFormatException nfe) {
            System.err.println("Error: the second argument must be an integer,"
                    + "the third argument must be a double or float");
            System.exit(1);
        }
        
    		ControllerMain c = new ControllerMain();
    		Analyzer a = new Analyzer(t_support, t_confidence);
    		CallGraph g = null;
    		
    		try {
    		    g = c.readCallGraph(expand_level);
    		} catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
    		
    		a.analyzeGraphAndPrintBugs(g);
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
    
	public CallGraph readCallGraph(int expand_level) throws IOException {
		CallGraph g = null;
		BufferedReader in= null;

		in = new BufferedReader(new InputStreamReader(System.in));
		g = new CallGraph(expand_level);	
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
		} else {
			thisLine = in.readLine();
		}
	}

	in.close();
	/*modification for Part I c*/
	    g.updateSupport();
//	    g.printNodes();
//		g.printGraphInfo();
		return g;
	}
}
