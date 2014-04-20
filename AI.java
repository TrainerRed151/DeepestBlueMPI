//Brian Pomerantz

import java.util.*;
import mpi.*;

public class AI {
	private boolean side;
	private int depth;
	private String[] args;
	
	public AI(boolean si, int dep, String[] as) {
		side = si;
		depth = dep;
		args = as;
	}
	
	//Calculates the best move using a MinMax algorithm with ALphaBeta Pruning
	//Input: ArrayList of legal moves
	//Output: The particular legal move which maximizes (or minimizes) the engine's score
	public String move(Board bd) {
		ArrayList<String> moves = bd.legalMoves(side);
		
		//Stalemate
		if (moves.size() == 0) {
			return '\u00AB' + "-" + '\u00AB';
		}

		byte alpha = Byte.MIN_VALUE, beta = Byte.MAX_VALUE;
		
		byte best = -1, val, bestVal;
		if (side) {bestVal = Byte.MIN_VALUE;}
		else {bestVal = Byte.MAX_VALUE;}
		
		int j = 0; //make for loop
		
		
		MPI.Init(args);
		int rank = MPI.COMM_WORLD.Rank(), size = MPI.COMM_WORLD.Size();
		int unitSize=1, tag=100, master=0;
		
		if (rank == master) {
			int[] sendbuf = new int[unitSize*(size-1)];
			
			for (int i = 1; i < size; i++) {
				MPI.COMM_WORLD.Send(sendbuf, (i-1)*unitSize, unitSize, MPI.INT, i, tag);
			}
			
			for (int i = 1; i < size; i++) {
				MPI.COMM_WORLD.Recv(sendbuf, (i-1)*unitSize, unitSize, MPI.INT, i, tag);
			}
			
			for (int i = 1; i < sendbuf.length; i++){
   			
   			if (side && sendbuf[i] > sendbuf[maxIndex]){
   				best = i+j;
   				bestVal = sendbuf[i];
   				alpha = (byte) Math.max(alpha, bestVal);
  			}
  			
  			if (!side && sendbuf[i] < sendbuf[maxIndex]){
   				best = i+j;
   				bestVal = sendbuf[i];
   				beta = (byte) Math.min(beta, bestVal);
  			}
  			
  			if (beta <= alpha) {
	    			break;
	    	}
  		}
  		
  		
  		
		}
		
		else {
			int[] recvbuf[] = new int[unitSize];
			MPI.COMM_WORLD.Recv(recvbuf, 0, unitSize, MPI.INT, master, tag);
			
			Board nBD = new Board(bd.getBoard());
			recvbuf[0] = alphabeta(nBD.move(moves.get(j+rank), side), depth, alpha, beta, !side);
			
			MPI.COMM_WORLD.Recv(recvbuf, 0, unitSize, MPI.INT, master, tag);
		}
		
		MPI.Finalize();
		
		
		//Resign if opponent has forced mate within depth
		//Alternatively, the engine could chose a random move
		if (best == -1) {
			if (side){return "0-1";}
			else{return "1-0";}
		}
		
		return moves.get(best);
	}
	
	//Old MinMax function.  Superseded by AlphaBeta function
//	private byte minmax(Board bd, int dep, boolean maxPlayer) {
//		byte bestValue, val;
//		
//		if (dep == 0) {
//        	return bd.score();
//		}
//		
//    	if (maxPlayer) {
//    		bestValue = Byte.MIN_VALUE;
//    		
//    		for (String s : bd.legalMoves(maxPlayer)) {
//    			Board nBD = new Board(bd.getBoard());
//    			val = minmax(nBD.move(s, maxPlayer), dep - 1, false);
//    			bestValue = (byte) Math.max(bestValue, val);
//    		}
//			
//			return bestValue;
//		}
//    	
//    	else {
//			bestValue = Byte.MAX_VALUE;
//			
//			for (String s : bd.legalMoves(!maxPlayer)) {
//				Board nBD = new Board(bd.getBoard());
//				val = minmax(nBD.move(s, maxPlayer), dep - 1, true);
//    			bestValue = (byte) Math.min(bestValue, val);
//    		}
//			
//			return bestValue;
//		}
//	}
	
	//AlphaBeta function
	//Recursively determines best possible move assuming perfect play within given depth
	//Discontinues branch of search tree if necessarily worse than previously calculated branch
	//More efficient than MinMax
	private byte alphabeta(Board bd, int dep, byte alpha, byte beta, boolean maxPlayer) {
		if (dep == 0) {
        	return bd.score();
		}
		
    	if (maxPlayer) {
    		ArrayList<String> list = bd.legalMoves(true);
    		
    		//Checkmate	
    		if (bd.checkmate(false)) {
    			return Byte.MIN_VALUE;
    		}
    		
    		//Stalemate
    		if (list.size() == 0) {
    			return 0;
    		}
    		
    		for (String s : list) {
    			Board nBD = new Board(bd.getBoard());
    			alpha = (byte) Math.max(alpha, alphabeta(nBD.move(s, true), dep-1, alpha, beta, false));
    			
    			if (beta <= alpha) {
    				break;
    			}
    		}
			
			return alpha;
		}
    	
    	else {
    		ArrayList<String> list = bd.legalMoves(false);
    		
    		//Checkmate
    		if (bd.checkmate(true)) {
    			return Byte.MAX_VALUE;
    		}
    		
    		//Stalemate
    		if (list.size() == 0) {
    			return 0;
    		}
    		
			for (String s : list) {
				Board nBD = new Board(bd.getBoard());
    			beta = (byte) Math.min(beta, alphabeta(nBD.move(s, false), dep-1, alpha, beta, true));
    			
    			if (beta <= alpha) {
    				break;
    			}
    		}
			
			return beta;
		}
	}
}
