javac -d ../bin Agent.java AMM.java SRMM.java Sim.java Runner.java

if [ $# == 1 ]; then
    cd ..
    java -cp bin infra.Runner
    cd src
fi
