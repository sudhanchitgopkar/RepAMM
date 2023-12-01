package mm;

import infra.Agent;

abstract public class AMM {
    
    abstract void buy(Agent buyer, double amt, int outcome);
    abstract void sell(Agent seller, double amt, int outcome);
    abstract double getPrice(int outcome);

} //AMM
