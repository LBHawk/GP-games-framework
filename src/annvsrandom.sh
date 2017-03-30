#!/bin/bash
clear

export CLASSPATH=/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/lib/encog-core-3.3.0.jar:/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/lib/jcommander-1.48.jar:.

AGENT1="ann"
AGENT2="random"

for j in {0..4}
do
	for i in {1..20}
	do
		java GameController -a1 $AGENT1 -a2 $AGENT2 -t 500 -s $(($j*2+5)) --record
	done
	
	for i in {1..20}
	do
		java GameController -a1 $AGENT1 -a2 $AGENT2 -t 1000 -s $(($j*2+5)) --record
	done
	
	for i in {1..20}
	do
		java GameController -a1 $AGENT1 -a2 $AGENT2 -t 2000 -s $(($j*2+5)) --record
	done
	
	for i in {1..20}
	do
		java GameController -a1 $AGENT1 -a2 $AGENT2 -t 4000 -s $(($j*2+5)) --record
	done
	
	for i in {1..20}
	do
		java GameController -a1 $AGENT1 -a2 $AGENT2 -t 8000 -s $(($j*2+5)) --record
	done
done
