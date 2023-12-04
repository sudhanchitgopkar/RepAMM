package mm;

import infra.Agent;
import mm.AMM;
import java.lang.Math.*;


public class SRMM extends AMM {
    private double [] state;
    private final double BETA = 1;
    
    /**
       Initializes a new SRMM with equal probability across all outcomes.

       @param numOutcomes number of possible outcomes/contracts in the prediction market
       @return SRMM object
     */
    public SRMM(int numOutcomes) {
	state = new double [numOutcomes];
	for (int i = 0; i < numOutcomes; i++) {
	    state[i] = 1/(numOutcomes * 1.0);
	} //for
    } //SRMM
    
    /**
       Facilitates an agent buying some amount of contracts on some outcome.
       Deducts the payment from the buyer, if valid, and changes the market state.
       Does nothing if the call is invalid.

       @param buyer the agent buying the contracts
       @param amt the amount of contracts being bought
       @param the outcome the buyer is buying contracts for
       @return whether the operation was valid (buyer had enough money for purchase)
     */
    public boolean buy(Agent buyer, double amt, int outcome) {
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
	
	double price = Math.log(buyState) - Math.log(currState);
	
	if (price > buyer.getBudget()) {
	    //reset state
	    state[outcome] -= amt;
	    return false;
	} else {
	    buyer.subMoney(price);
	    buyer.addHoldings(amt);
	    return true;
	} //if
    } //buy
    
    /**
       Facilitates an agent selling some amount of contracts on some outcome.
       Gives some payment to the buyer, if valid, and changes the market state.
       Does nothing if the call is invalid.

       @param buyer the agent selling the contracts
       @param amt the amount of contracts being sold
       @param the outcome the seller is selling contracts for
       @return whether the operation was valid (seller has the contracts they intend to sell)
     */

    public boolean sell(Agent seller, double amt, int outcome) {
	if (seller.getHoldings() < amt) {
	    return false;
	} //if

	amt *= -1;
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
	
	double price = Math.log(buyState) - Math.log(currState);
	
	seller.addMoney(price);
	seller.subHoldings(amt);
	return true;
    } //sell
    
    /**
       Gets the instantaneous price of the contract on some outcome.

       @param outcome the outcome to get the contract price of
       @return the price of the contract of the outcome
     */
    public double getPrice(int outcome) {
	double price = Math.exp(state[outcome]/BETA);
	double sum = 0;

	for (int i = 0; i < state.length; i++) {
	    sum += Math.exp(state[i]/BETA);
	} //for

	return price/sum;
    } //getPrice

    /**
       Returns the quantity of contract `outcome` bought by agent a till its price becomes `price`.
       
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
	
	if (!this.buy(a, qty, outcome)) {
	    //ToDo: buy as much as budget allows
	} //if

	return qty;
    } //buyTillPrice

    /**
       Returns the quantity of contract `outcome` bought by agent a till its price becomes `price`.
       
       @param a the purchasing agent
       @param outcome the index of the outcome to purchase in state
       @param the price of the outcome after the purchase
       @return the quantity of contract `outcome` bought
     */    
    public double sellTillPrice(Agent a, int outcome, double price) {
	double qty = 0;
	
	for (int i = 0; i < state.length; i++) {
	    if (i != outcome) {
		qty += price * Math.exp(state[i]/BETA);
	    } //if
	} //for

	qty /= (1 - price);
	qty = state[outcome] - qty;
	
	//if we don't have qty amount, sell all that we can
	this.sell(a, Math.min(qty, a.getHoldings()), outcome);

	return qty;
    } //sellTillPrice
} //SRMM
