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
    private final int NUM_OUTCOMES = 1;
    private final int OUTCOME = rand.nextInt(2);
    private final int N = 100; //number of agents
    private final int ROUNDS = 100;

    private AMM amm;
    private Agent [] agents;
    
    public Sim(AMM amm) {
	this.amm = new SRMM(NUM_OUTCOMES);
	
	//Init Agents
	agents  = new Agent [N];
	for (int i = 0; i < N; i++) {
	    agents[i] = new Agent(OUTCOME, NUM_OUTCOMES, 100);
	} //for
    } //Sim

    public void run() {
	for (int i = 0; i < ROUNDS; i++) {
	    for (Agent a : agents) {
		if (a.getBelief() < amm.getPrice(0)) {
		    amm.sellTillPrice(a, 0, a.getBelief());
		} else if (a.getBelief() > amm.getPrice(0)) {
		    amm.buyTillPrice(a, 0, a.getBelief());
		} //if
	    } //for
	} //for
    } //run

} //class
