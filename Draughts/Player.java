import java.util.*;

/**
 * A player can be a human or computer player. 
 * Computer opponents have four different difficulty settings:
 * 
 * 1 - Easy
 * 2 - Medium
 * 3 - Hard
 * 4 - Brutal
 *
 * @author  Lukas Gunthermann
 * @version 1.0
 */
public class Player
{
    private String name;
    private boolean human;
    private boolean active;
    private int difficulty;
    private int exploredNodes;
    private int threshold;
    
    public String getName(){ return name; }
    public boolean isHuman(){ return human; }
    public boolean isAI(){ return !human; }
    public boolean isActive(){ return active; }
    public void deactivate(){ active = false; }
    public int getDifficulty(){ return difficulty; }
    public int getExploredNodes(){ return exploredNodes; }

    private final static int thresholdRange = 15;
    private int getUpperThreshold(){ return this.threshold + thresholdRange; }
    private int getLowerThreshold(){ return this.threshold - thresholdRange; }
    
    /**
     * Constructor for objects of class Player
     */
    public Player(String name, boolean human, int difficulty)
    {
        this.name = name;
        this.human = human;
        this.active = true;
        this.difficulty = difficulty;
        this.exploredNodes = 0;
        this.threshold = 0;
        
        this.verifyDifficulty();
    }

    public Player(String name, boolean human)
    {
        this(name,human,0);
    }
    
    public Player(Player player)
    {
        this(player.getName(),player.isHuman(),player.getDifficulty());
    }
    
    private int getSearchDepth()
    {
        switch(this.difficulty)
        {
            case 1: return 3;
            case 2: return 6;
            case 3: return 10;
            case 4: return 14;
            default: return 0;
        }
    }  
    
    /**
     * @param depth     remaining depth to be explored
     * @param state     state of the game
     * @param alpha     alpha
     * @param beta      beta
     */
    private int minimax(int depth, State state, int alpha, int beta) 
    {
        ArrayList<Move> moves = state.getAvailableMoves();
        this.exploredNodes++;
        
        int player = state.getTurn();
        int bestResult;
        if(player==1)
            bestResult = -Integer.MAX_VALUE;
        else
            bestResult = Integer.MAX_VALUE;
          
        if(moves.isEmpty())
            return bestResult+((player==1?1:-1)*(depth));
           
        if(depth>=this.getSearchDepth())
            return Analysis.evaluate(state);
        
        // check the threshold every 4 steps
        if(depth>0&&depth%4==0)
        {
            int val = Analysis.evaluate(state);
            if((val<this.getLowerThreshold())||(val>this.getUpperThreshold()))
                return val;
        }
            
        for(Move move : moves)
        {
            int result = minimax(depth+1,state.clone().move(move),alpha,beta);
            if(player == 1)
            {
                bestResult = Math.max(bestResult,result);
                alpha = Math.max(alpha,result);
            }
            else
            {
                bestResult = Math.min(bestResult,result);
                beta = Math.min(beta,result);
            }
            
            if(beta <= alpha)
                break;
        }    

        return bestResult;
    }

    /**
     * Compares two values. For player 1 a higher value is better, while it is a lower value for player 2
     * 
     * @return    true, if val1 is better than val2, otherwise false
     */
    private static boolean isBetter(int player, int val1, int val2)
    {
        if(player==1)
            return val1>val2;
        else
            return val1<val2;  
    }

    public Move getAImove(State state)
    {
        ArrayList<Move> availableMoves = state.getAvailableMoves();
        this.exploredNodes = 1;
        
        if(availableMoves==null)
        {
            Error.send(true,"No available moves found.");
            return null;
        }
        
        // Only one move possible 
        if(availableMoves.size()==1)
        {
            return availableMoves.get(0);
        }
        
        // The first three moves can be played randomly
        if(state.getMoveNo()<=3)
        {
            return availableMoves.get(new Random().nextInt(availableMoves.size()));
        }
        
        int player = state.getTurn();
        int bestResult;
        if(player==1)
        {
            bestResult = -Integer.MAX_VALUE;
        }
        else
        {
            bestResult = Integer.MAX_VALUE;
        }
        
        this.threshold = Analysis.evaluate(state);
        ArrayList<Move> moves = new ArrayList<Move>();
        for(Move move : availableMoves)
        {
            int result = minimax(0,state.clone().move(move),-Integer.MAX_VALUE,Integer.MAX_VALUE);
            if(result==bestResult)
            {
                moves.add(move);
            }
            else if(isBetter(player,result,bestResult))
            {
                moves.clear();
                moves.add(move);
                bestResult = result;
            }
        }
        
        // return null if no move is available, otherwise randomely pick one of the highest ranked moves
        if(moves.isEmpty())
        {
            Error.send(true,"No moves available for player "+this.getName());
            return null;
        }
        else
        {
            return moves.get(new Random().nextInt(moves.size()));
        }
    }
    
    /**
     * Ensures that the difficulty has a valid value.
     */
    private void verifyDifficulty()
    {
        if(this.isHuman())
            difficulty=0;
        else if(difficulty<1)
            difficulty=1;
        else if(difficulty>4)
            difficulty=4;
    }
}
