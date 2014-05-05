DeepestBlueMPI
==============

Chess Engine (parallelized with MPI)

This version of Deepest Blue is an attempt to parallelize the AlphaBeta algorithm using MPI so that it can be run on a distributed memory system.  Currently, the code is inoperative.  The Java MPI library employed appears to require that the parallelization take place in the main method (here located in the runChess file).  In this code, the MPI commands are in the alphabeta method of the AI file.  Therefore, the code attempts to run the initial main menu once for each processor and then will not take any input arguments.  Any attempted solution to this bug would require a complete change in the overall structure of the code.
