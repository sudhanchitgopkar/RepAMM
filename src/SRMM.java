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
	 *  Returns the cost  (positive if buying, negative
	 *  if selling) to trade a given amount of a contract.
	 *
	 * @param outcome  the contract to buy
	 * @param amt  the amount being traded (neg if selling)
	 * @return cost
	 */
	public double trade_cost(int outcome, double amt) {
		double current_C = 0;
		double new_C = 0;

		for (int i = 0; i < state.length; ++i) {
			current_C += Math.exp(state[i] / BETA);
			if (i == outcome) {
				new_C += Math.exp((state[i] + amt) / BETA);
			} else {
				new_C += Math.exp(state[i] / BETA);
			}
		}

		current_C = BETA * Math.log(current_C);
		new_C = BETA * Math.log(new_C);

		return new_C - current_C;
	}

	/**
       Facilitates an agent buying some amount of contracts on some outcome.
       Deducts the payment from the buyer, if valid, and changes the market state.
       Does nothing if the call is invalid.

       @param buyer the agent buying the contracts
       @param amt the amount of contracts being bought
       @param outcome the buyer is buying contracts for
       @return whether the operation was valid (buyer had enough money for purchase)
     */
	public boolean buy(Agent buyer, double amt, int outcome) {
		double price = trade_cost(outcome, amt);

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

       @param seller the agent selling the contracts
       @param amt the amount of contracts being sold
       @param outcome the seller is selling contracts for
       @return whether the operation was valid (seller has the contracts they intend to sell)
     */

    public boolean sell(Agent seller, double amt, int outcome) {
		if (seller.getHoldings() < amt) {
			return false;
		} //if

		amt *= -1;
		double price = trade_cost(outcome, amt);

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
       @param price of the outcome after the purchase
       @return the quantity of contract `outcome` bought
     */
    public double buyTillPrice(Agent a, int outcome, double price) {
		// buy a contract until the price reaches "price", given the budget
		// of the agent
		double Sigma = 0;
		for (int i = 0; i < state.length; ++i) {
			if (i == outcome) continue;
			Sigma += Math.exp(state[i] / BETA);
		}
		double max_buy = BETA * Math.log(price * Sigma / (1 - price)) - state[outcome];

		double cost = trade_cost(outcome, max_buy);

		if (cost > a.getBudget()) {
			// only buy as much as they can afford
			return contracts_for_cost(outcome, a.getBudget());
		} else {
			return max_buy;
		}

    } //buyTillPrice

	/**
	 * Returns the amount of contracts that can be bought for a certain price.
	 *
	 * @param outcome - the contract being bought
	 * @param cost - the cash offered by the agent
	 * @return the amount of contract bought
	 */
	public double contracts_for_cost(int outcome, double cost) {
		// TODO - recheck math on this lol
		return 0;
	}

    /**
       Returns the quantity of contract `outcome` bought by agent a till its price becomes `price`.
       
       @param a the purchasing agent
       @param outcome the index of the outcome to purchase in state
       @param price of the outcome after the purchase
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
