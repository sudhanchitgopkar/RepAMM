javac -d ../bin Agent.java AMM.java SRMM.java TradeFeeMM.java RepMM.java Sim.java Runner.java

if [ $# == 1 ]; then
    cd ..
    java -cp bin infra.Runner
    cd src
fi
