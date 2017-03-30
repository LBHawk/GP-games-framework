#!/bin/bash
clear

export CLASSPATH=/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/lib/encog-core-3.3.0.jar:/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/lib/jcommander-1.48.jar:.

AGENT1="mcts"
AGENT2="random"
SIZE="5"
TIME="100"

for i in {1..20}
do
	java GameController -a1 $AGENT1 -a2 $AGENT2 -t $TIME -s $SIZE --record
done
