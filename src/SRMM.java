package mm;

import infra.Agent;
import mm.AMM;
import java.lang.Math.*;


public class SRMM extends AMM {
    private double [] state;
    private final double BETA = 1;
    private final boolean LOG = true;

    /**
       Initializes a new SRMM with equal probability across all outcomes.

       @param numOutcomes number of possible outcomes/contracts in the prediction market
       @return SRMM object
     */
    public SRMM(int numOutcomes) {
	state = new double [numOutcomes];
	if (numOutcomes == 1) {
	    state[0] = 0;
	} else {
	    for (int i = 0; i < numOutcomes; i++) {
		state[i] = 0;
	    } //for
	} //if
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
	    if (LOG) System.out.println("AGENT " + buyer.getID() + " FAILED TO BUY " + amt + 
					" CONTRACTS FOR " + price + " (AGENT BUDGET: " + buyer.getBudget() + ")");
	    return false;
	} else {
	    buyer.subMoney(price);
	    buyer.addHoldings(amt, outcome);
	    if (LOG) System.out.println("AGENT " + buyer.getID() + " BOUGHT " + amt + " CONTRACTS FOR " + price);
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
	if (LOG) System.out.println("INITIATED SELL ON OUTCOME " + outcome + " OF " + amt + " CONTRACTS BY " + seller.getID());
	if (seller.getHolding(outcome) < amt) {
	    if (LOG) System.out.println("AGENT " + seller.getID() + " FAILED TO SELL " + amt + " (ONLY HAS " + seller.getHoldings() + ")");
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
	seller.subHoldings(amt, outcome);
	if (LOG) System.out.println("AGENT " + seller.getID() + " SOLD " + amt + " CONTRACTS FOR " + price);
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
    public double buyTillPrice(Agent a, int outcome, double price) throws Exception {
	double qty = 0;
	double x = (price * Math.exp(state[outcome == 0 ? 1 : 0]))/(1 - price);
	qty = Math.log(x) - state[outcome];

	if (!this.buy(a, qty, outcome)) {
		throw new Exception("BUY TILL PRICE ERROR, TRIED TO BUY " + qty + " CONTRACTS");
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
	
	if (qty < 0) return qty;

	//if we don't have qty amount, sell all that we can
	this.sell(a, Math.min(qty, a.getHolding(outcome)), outcome);
    
	return qty;
    } //sellTillPrice

    @Override
    public String toString() {
	String s = "CONTRACTS SOLD: (" + state[0] + "," + state[1] + ")\n";
	s += "CONTRACT PRICES: (" + getPrice(0) + "," + getPrice(1) + ")";
	return s;
    } //toString
} //SRMM
