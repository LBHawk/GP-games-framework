$GP-games-framework
========

My undergraduate thesis in Computer Science.  All source code is contained in the (frighteningly unorganized) src directory, the thesis document is in the thesis-chapters directory.

Features
--------

- A general purpose game-playing controller
- Interfaces/Abstract classes for 'Game' and 'GameAgent' classes
- Implementations of the board games Go and Hex
- Implementations of one trivial game-playing agent (RandomAgent) and four intelligent agents (MCTSAgent, GAAgent, ANNAgent, NEATAgent)

Dependencies
------------

Compilation requires two external libraries:
- JCommander (https://mvnrepository.com/artifact/com.beust/jcommander/1.27)
- Encog (https://github.com/encog/encog-java-core)

Thesis
----------

The thesis consisted of implementing each of the components outlined in the 'Features' section above.  The four intelligent agent's designs were based on improvements to the MCTS algorithm which have previously been proposed by other researchers.  After implementation, experiments were run for nearly 900hrs of total runtime.  The purpose of my research was to provide a direct comparison between the different algorithms, and provide analysis of the tradeoff between raw performance and the increased computational complexity of each algorithm.
