package infra;

import java.util.Random;
import java.util.ArrayList;
import infra.Agent;
import mm.AMM;

public class Sim {
    //Params
    private final long SEED = 4092002;
    private Random rand = new Random(SEED);
    private final int OUTCOME = rand.nextInt(2); //gen outcome in {0, 1}
    private final int N = 100; //number of agents
    

    private AMM amm;
    private Agent [] agents;
    
    public Sim(AMM amm) {
	this.amm = amm;
	
	//Init Agents
	agents  = new Agent [N];
	for (int i = 0; i < N; i++) {
	    agents[i] = new Agent(rand.nextInt(2), rand.nextDouble() * 100);
	} //for
	
    } //Sim
} //class
