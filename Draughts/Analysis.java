import java.util.*;

/**
 * The class Analysis is there to evaluate a position.
 *
 * @author  Lukas Gunthermann
 * @version 1.0
 */
public class Analysis
{
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> oldList) 
    { 
        ArrayList<T> newList = new ArrayList<T>();
        
        for(T element : oldList)
            if (!newList.contains(element)) 
                newList.add(element); 
                
        return newList; 
    } 
    
    private static ArrayList<Position> getNeighbours(ArrayList<Position> source)
    {
        ArrayList<Position> tmp = new ArrayList<Position>();
        ArrayList<Position> neighbours = new ArrayList<Position>();

        for(Position src : source)
            tmp.addAll(getNeighbours(src));
        
        tmp = removeDuplicates(tmp);
        
        for(Position pos : tmp)
            if(!source.contains(pos))
                neighbours.add(pos);
        
        return neighbours;
    }
    
    private static ArrayList<Position> getNeighbours(Position src)
    {
        ArrayList<Position> neighbours = new ArrayList<Position>();
        Position pos;

        pos = new Position(src.getX()+1,src.getY()+1);
        if(pos.isValid())
            neighbours.add(pos);
            
        pos = new Position(src.getX()+1,src.getY()-1);
        if(pos.isValid())
            neighbours.add(pos);
            
        pos = new Position(src.getX()-1,src.getY()+1);
        if(pos.isValid())
            neighbours.add(pos);
            
        pos = new Position(src.getX()-1,src.getY()-1);
        if(pos.isValid())
            neighbours.add(pos);
        
        return neighbours;
    }

    private static int getPlayerFactor(int player)
    {
        switch(player)
        {
            case 1: return +1;
            case 2: return -1;
            default: return 0;
        }
    }
    
    private static boolean isKing(int piece)
    {
        return(piece==3||piece==4);
    }
    
    private static int getPlayer(int piece)
    {
        switch(piece)
        {
            case 0: return 0;
            case 1: return 1;
            case 2: return 2;
            case 3: return 1;
            case 4: return 2;
            default: return 0;
        }
    }
    
    private static int getCover(State state, Position src)
    {
        if(!src.isValid())
            return 0;
        int count1=0;
        int count2=0;
        
        ArrayList<Position> source = new ArrayList<Position>();
        ArrayList<Position> neighbours = new ArrayList<Position>();
        source.add(src);
  
        while(count1==0&&count2==0)
        {
            source.addAll(neighbours);
            neighbours = getNeighbours(source);
            for(Position pos : neighbours)
            {
                if(pos.isValid())
                {
                    int player=getPlayer(state.getStone(pos));
                    if(player==1)
                        count1++;
                    if(player==2)
                        count2++;
                }
            }
        }
        
        if(count1>count2)
            return 1;
        else if(count2>count1)
            return 2;
        else
            return 0;
    }
    
    // One point for each covered field (closest piece is friendly)
    private static int getCoverVal(State state)
    {
        int val = 0;
        
        for(int y=0;y<8;y++)
            for(int x=(y%2)^1;x<8;x+=2)
                val += getPlayerFactor(getCover(state,new Position(x,y)));
        
        return val;
    }
    
    private static int getPieceVal(State state, Position pos)
    {
        int val;
        int piece = state.getStone(pos);
        int player = getPlayer(piece);
        if(player==0)
            return 0;
        
        // a piece is worth more if it is a king or closer to the promotion line
        if(isKing(piece))
            val = 16;
        else
            if(player==1)
                val = 2*(8-pos.getY());
            else
                val = 2*(1+pos.getY());
        
        // one point for each neighbouring own piece
        for(Position position : getNeighbours(pos))
            if(position.isValid())
                if(player==getPlayer(state.getStone(position)))
                    val++;

        // one point for each possible move      
        val += state.getMoves(pos).size();
        
        return getPlayerFactor(player)*(5+val);
    }
    
    public static int evaluate(State gameState)
    {
        State state = gameState.clone();
        int val = 0;
        int turn = state.getTurn();
        
        // Turn .. 3 points
        val += getPlayerFactor(turn)*3;
        
        // Add individual points for each piece
        for(int y=0;y<8;y++)
            for(int x=(y%2)^1;x<8;x+=2)
                val += getPieceVal(state,new Position(x,y));
            
        // Add 7 points for each capturing move
        ArrayList<Move> moves = state.getAvailableMoves();
        if(!moves.isEmpty())
            if(moves.get(0).isCap())
                val += moves.size()*getPlayerFactor(turn)*7;
            
        state.changeTurn();
        turn = state.getTurn();
        moves = state.getAvailableMoves();
        if(!moves.isEmpty())
            if(moves.get(0).isCap())
                val += moves.size()*getPlayerFactor(turn)*7;
        
        // Add 1 point for each covered field
        val += getCoverVal(state);  
            
        return val;
    }
}
