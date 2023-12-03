package mm;

import infra.Agent;
import mm.AMM;
import java.lang.Math.*;


public class SRMM extends AMM {
    private double [] state;
    private final double BETA = 1;

    public SRMM(int numOutcomes) {
	state = new double [numOutcomes];
	for (int i = 0; i < numOutcomes; i++) {
	    state[i] = 1/(numOutcomes * 1.0);
	} //for
    } //SRMM

    public double buy(Agent buyer, double amt, int outcome) {
	double currState = 0;
	double buyState = 0;

	for (int i = 0; i < state.length; i++) {
	    currState += Math.exp(state[i]);
	} //for
	
	//update market state
	state[outcome] += amt;
	
	//can optimize this buy subtracting old outcome + add new outcome
	for (int i = 0; i < state.length; i++) {
	    buyState += Math.exp(state[i]);
	} //for
	
	return Math.log(buyState) - Math.log(currState);
    } //buy

    public double sell(Agent seller, double amt, int outcome) {
	return buy(seller, -1 * amt, outcome);
    } //sell

    public double getPrice(int outcome) {
	double price = Math.exp(state[outcome]/BETA);
	double sum = 0;

	for (int i = 0; i < state.length; i++) {
	    sum += Math.exp(state[i]/BETA);
	} //for

	return price/sum;
    } //getPrice

    /**
       Returns the quantity of contract `outcome` bought by agent a till its price becomes `price`
       @param a the purchasing agent
       @param outcome the index of the outcome to purchase in state
       @param the price of the outcome after the purchase
       @return the quantity of contract `outcome` bought
     */
    public double buyTillPrice(Agent a, int outcome, double price) {
	double qty = 0;
	
	for (int i = 0; i < state.length; i++) {
	    if (i != outcome) {
		qty += price * Math.exp(state[i]/BETA);
	    } //if
	} //for
	qty /= (1 - price);
	qty -= state[outcome];
	
	this.buy(a, qty, outcome);

	return qty;
    } //buyTillPrice
    
    public double sellTillPrice(Agent a, int outcome, double price) {
	double qty = 0;
	
	for (int i = 0; i < state.length; i++) {
	    if (i != outcome) {
		qty += price * Math.exp(state[i]/BETA);
	    } //if
	} //for
	qty /= (1 - price);
	qty = state[outcome] - qty;
	
	this.buy(a, qty, outcome);

	return qty;
    } //sellTillPrice
} //SRMM
