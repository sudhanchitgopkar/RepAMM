package mm;

import infra.Agent;
import mm.AMM;
import java.lang.Math.*;


public class RepMM extends AMM {
    private double [][] state; //for ea. outcome, [<contracts sold>, <sum of holder reps>]
    private final double BETA = 1;
    private final boolean LOG = true;
    private final double CONTRACT_WEIGHT = 0.5;
    private final double REP_WEIGHT = 1 - CONTRACT_WEIGHT;
    /**
       Initializes a new RepMM with equal probability across all outcomes.

       @param numOutcomes number of possible outcomes/contracts in the prediction market
       @return RepMM object
     */
    public RepMM(int numOutcomes) {
	state = new double [numOutcomes][2];
	for (int i = 0; i < numOutcomes; i++) {
	    state[i] = new double [] {1,0};
	} //for
    } //RepMM
    
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
	if (amt == 0) return true;
	double currState = 0;
	double buyState = 0;

	for (int i = 0; i < state.length; i++) {
	    currState += Math.exp(mktState(i));
	} //for
	
	//update market state
	state[outcome][0] += amt;
	state[outcome][1] += (buyer.getRep() * amt);
	
	//can optimize this buy subtracting old outcome + add new outcome
	for (int i = 0; i < state.length; i++) {
	    buyState += Math.exp(mktState(i));
	} //for
	
	double price = (BETA * Math.log(buyState)) - (BETA * Math.log(currState));
	
	if (price > buyer.getBudget()) {
	    //reset state
	    state[outcome][0] -= amt;
	    state[outcome][1] -= (buyer.getRep() * amt);
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

	double currState = 0;
	double sellState = 0;

	for (int i = 0; i < state.length; i++) {
	    currState += Math.exp(mktState(i));
	} //for
	
	//update market state
	state[outcome][0] -= amt;
	state[outcome][1] -= (seller.getRep() * amt);
	
	//can optimize this buy subtracting old outcome + add new outcome
	for (int i = 0; i < state.length; i++) {
	    sellState += Math.exp(mktState(i));
	} //for
	
	double price = Math.log(sellState) - Math.log(currState);
	
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
	double price = Math.exp(mktState(outcome)/BETA);
	double sum = 0;

	for (int i = 0; i < state.length; i++) {
	    sum += Math.exp(mktState(i)/BETA);
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
	if (LOG) System.out.println("BUYING TILL PRICE " + price + "...");
	double qty = 0, p = price;
	
	double r = a.getRep(), x0 = state[outcome][0], x1 = state[outcome][1], c = CONTRACT_WEIGHT,
	    y0 = state[outcome == 0 ? 1 : 0][0], y1 = state[outcome == 0 ? 1 : 0][1];
	
	double coeff = 1/(2 * c * x0 * y0);
	
	double innermost = Math.pow((r - c * r + Math.log(-1 * (p/(-1 + p)))), 2) + 4 * (-1 + c) * c * r * x0 - 4 * (-1 + c) * c * x1;
	innermost += (Math.pow(-1 + c, 2) * (x1 * x1))/(x0 * x0) + 2 * c * (r - c * r + Math.log(-p/(-1 + p))) * y0;
	innermost += (c * c) * (y0 * y0) - 2 * (-1 + c) * c * y1 + ((2 * (-1 + c) * ((-1 + c) * r - Math.log((-1 * p)/(-1 + p))) * y1)/(y0));
	innermost += ((Math.pow(-1 + c, 2) * (y1 * y1))/(y0 * y0));
	innermost += ((2 * (-1 + c) * x1 * ((r - c * r + Math.log((-1 * p)/(-1 + p))) * y0 + c * (y0 * y0) - (-1 + c) * y1))/(x0 * y0));
	
	double outer = 1/(c * c);
	
	double outermost = r * x0 * y0 - c * r * x0 * y0 + Math.log((-1 * p)/(-1 + p)) * x0 * y0 - 2 * c * (x0 * x0) * y0 - x1 * y0 + c * x1 * y0;
	outermost += c * x0 * (y0 * y0) + x0 * y1 - c * x0 * y1;
	double sqrtmult = c * x0 * y0;
	

	double sqrtval = outer * innermost;
	sqrtval = Math.sqrt(sqrtval);

	/*
	System.out.println("INNERMOST: " + innermost);
	System.out.println("OUTER: " + outer);
	System.out.println("OUTERMOST: " + outermost + sqrtval);
	System.out.println("COEFF: " + coeff);
	System.out.println("SQRT: " + sqrtval);
	System.out.println("B4: " + (outermost + (sqrtval * sqrtmult)));
	System.out.println("SOLN: " + (coeff * (outermost + (sqrtval * sqrtmult))));
	*/

	qty = (outermost + (sqrtval * sqrtmult));
	if (LOG) System.out.println("\t BUYING " + qty + " CONTRACTS");

	
	if (!this.buy(a, qty, outcome)) {
	    if (LOG) System.out.println("COULDN'T BUY " + qty + " CONTRACTS! BUYING MAX POSSIBLE WITH BUDGET INSTEAD.");
	    //Buy as much as possible with remaining budget. Specifically for binary outcome!
	    double x = Math.exp((price/BETA) + (mktState(outcome)/BETA));
	    double y = Math.exp((price/BETA) + (mktState(outcome == 0 ? 1 : 0)/BETA));
	    double z = Math.exp(mktState(outcome == 0 ? 1 : 0)/BETA);
	    qty = BETA * Math.log(x + y + z) - mktState(outcome);
	    if (!this.buy(a, qty, outcome)) {
		throw new Exception("BUY TILL PRICE FALLBACK ERROR, TRIED TO BUY " + qty + " CONTRACTS");
	    } //if
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
		qty += price * Math.exp(mktState(i)/BETA);
	    } //if
	} //for

	qty /= (1 - price);
	qty = mktState(outcome) - qty;
	
	if (qty < 0) return qty;

	//if we don't have qty amount, sell all that we can
	this.sell(a, Math.min(qty, a.getHolding(outcome)), outcome);
    
	return qty;
    } //sellTillPrice

    @Override
    public String toString() {
	String s = "CONTRACTS SOLD: (" + state[0][0] + "," + state[1][0] + ")\n";
	s += "AVG REP PER CONTRACT: (" + state[0][1]/state[0][0]
	    + "," + state[1][1]/state[1][0] + ")\n";
	s += "MKT STATE: (" + mktState(0) + "," + mktState(1) + ")\n";
	s += "CONTRACT PRICES: (" + getPrice(0) + "," + getPrice(1) + ")";
	return s;
    } //toString

    private double mktState(int outcome) {
	double mktState = CONTRACT_WEIGHT * (state[outcome][0]) + 
	    (REP_WEIGHT) * (state[outcome][1]/state[outcome][0]);
	return mktState;
    } //repPerContract

    public double get_state(int outcome) {
	return mktState(outcome);
    } //get_state

    public String get_MM_type() {
	return "RepMM";
    } //getMMType
} //SRMM
