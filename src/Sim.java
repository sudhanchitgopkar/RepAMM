package infra;

import java.util.Random;
import java.util.ArrayList;
import infra.Agent;
import mm.AMM;
import mm.SRMM;

public class Sim {
    //Params
    private final long SEED = 4092002;
    private Random rand = new Random(SEED);
    private final int NUM_OUTCOMES = 2;
    private final int OUTCOME = rand.nextInt(2);
    private final int N = 2; //number of agents
    private final int ROUNDS = 2;
    private final boolean LOG = true;

    private AMM amm;
    private Agent [] agents;
    
    public Sim(AMM amm) {
	this.amm = amm;//new SRMM(NUM_OUTCOMES);
	
	//Init Agents
	agents  = new Agent [N];
	for (int i = 0; i < N; i++) {
	    agents[i] = new Agent(i, OUTCOME, NUM_OUTCOMES, 100);
	} //for

	if (LOG) System.out.println("MADE NEW SIM:\nNum Outcomes: " + NUM_OUTCOMES + 
				    "\nOutcome: " + OUTCOME + "\nROUNDS: " + ROUNDS + "\nAGENTS: " + N);
    } //Sim

    public void run() {
	for (int i = 0; i < ROUNDS; i++) {
	    if (LOG) System.out.println("ROUND: " + i + "\n" + amm + "\n----\n");
	    
	    for (Agent a : agents) {
		if (LOG) System.out.println("NOW PLAYING: " + a);
		if (LOG) System.out.println("TRADING OUTCOME 0");

		if (a.getBelief() < amm.getPrice(0)) {
		    amm.sellTillPrice(a, 0, a.getBelief());
		} else if (a.getBelief() > amm.getPrice(0)) {
		    try {
			amm.buyTillPrice(a, 0, a.getBelief());
		    } catch (Exception e) {
			System.out.println(e);
		    } //catch
		} //if
		if (LOG) System.out.println("Updated Agent\n" + a);
		if (LOG) System.out.println("Updated Market State\n" + amm);
		if (LOG) System.out.println("TRADING OUTCOME 1");
		
		if (1 - a.getBelief() < amm.getPrice(1)) {
		    amm.sellTillPrice(a, 1, 1 - a.getBelief());
		} else if (1 - a.getBelief() > amm.getPrice(1)) {
		    try {
			amm.buyTillPrice(a, 1, 1 - a.getBelief());
		    } catch (Exception e) {
			System.out.println(e);
		    } //catch
		} //if
		if (LOG) System.out.println("Updated Agent\n" + a);
		if (LOG) System.out.println("Updated Market State\n" + amm);
	    } //for
	    
	} //for
    } //run

} //class
