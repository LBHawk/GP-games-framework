#!/bin/bash
clear

export CLASSPATH=/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/lib/encog-core-3.3.0.jar:/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/lib/jcommander-1.48.jar:.

AGENT1="ann"
AGENT2="ga"

for j in {9,11,14}
do
	for i in {1..20}
	do
		java GameController -a1 $AGENT1 -a2 $AGENT2 -g hex -t 500 -s $j --record
	done
	
	for i in {1..20}
	do
		java GameController -a1 $AGENT1 -a2 $AGENT2 -g hex -t 1000 -s $j --record
	done
	
	for i in {1..20}
	do
		java GameController -a1 $AGENT1 -a2 $AGENT2 -g hex -t 2000 -s $j --record
	done
	
	for i in {1..20}
	do
		java GameController -a1 $AGENT1 -a2 $AGENT2 -g hex -t 4000 -s $j --record
	done
	
	for i in {1..20}
	do
		java GameController -a1 $AGENT1 -a2 $AGENT2 -g hex -t 8000 -s $j --record
	done
done
