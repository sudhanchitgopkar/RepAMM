package mm;

import infra.Agent;

abstract public class AMM {
    
    abstract double buy(Agent buyer, double amt, int outcome);
    abstract double sell(Agent seller, double amt, int outcome);
    abstract double getPrice(int outcome);

} //AMM
