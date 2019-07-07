import java.util.*;

/**
 *  This class represents a game state. 
 * 
 * @author  Lukas Gunthermann
 * @version 1.0
 */
public class State
{
    /**
     * 1 .. black
     * 2 .. white
     */
    private int turn;
    private int moveNo;
    private int[][] board;
    private Position lastHit;
    private ArrayList<Move> availableMoves;
    
    public int getTurn(){ return this.turn; }
    public int getMoveNo(){ return this.moveNo; }
    public int[][] getBoard(){ return this.board; }
    public ArrayList<Move> getAvailableMoves(){ return this.availableMoves; }
    public boolean isOver(){ return this.availableMoves.isEmpty(); }

    /**
     * Constructor
     */
    public State()
    {
        this.turn = 1;
        this.moveNo = 1;
        this.board = new int[][] 
        {
            {0, 2, 0, 2, 0, 2, 0, 2},
            {2, 0, 2, 0, 2, 0, 2, 0},
            {0, 2, 0, 2, 0, 2, 0, 2},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 1, 0, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 1, 0, 1},
            {1, 0, 1, 0, 1, 0, 1, 0},
        };
        // this.board = new int[][] 
        // {
            // {0, 0, 0, 2, 0, 0, 0, 0},
            // {0, 0, 0, 0, 0, 0, 0, 0},
            // {0, 0, 0, 0, 0, 2, 0, 0},
            // {0, 0, 0, 0, 2, 0, 0, 0},
            // {0, 0, 0, 2, 0, 0, 0, 0},
            // {0, 0, 0, 0, 0, 0, 0, 0},
            // {0, 0, 0, 0, 0, 0, 0, 0},
            // {1, 0, 0, 0, 0, 0, 0, 0},
        // };
        this.lastHit = null;
        this.calcAvailableMoves();
    }
    
    /**
     * Copy-Constructor
     */
    public State(State state)
    {
        this.turn = state.turn;
        this.moveNo = state.moveNo;
        
        this.board = new int[state.board.length][state.board[0].length];
        for(int i=0;i<state.board.length;i++)
            for(int j=0;j<state.board[i].length;j++)
                this.board[i][j] = state.board[i][j];
                
        if(state.lastHit!=null)
            this.lastHit = state.lastHit.clone();
        else
            this.lastHit = null;
            
        this.availableMoves = new ArrayList<Move>();
        for(Move move : state.availableMoves)
            this.availableMoves.add(move);
    }
    
    @Override
    public State clone(){ return new State(this); }
    
    public boolean isValidMove(Move move){ return move!=null&&this.getAvailableMoves().contains(move); }
    
    public State move(Move move)
    {
        if(!this.isValidMove(move))
            return this;
        
        // place the piece at its new postion
        this.setStone(move.tar,this.getStone(move.src));
        
        // clear source position
        this.setStone(move.src,0);
        
        if(move.isCap())
        {
            // remove captured stone
            this.setStone(move.getCapPos(),0);
            
            // check for capturing sequence
            if(this.canHit(move.tar))
            {
                this.lastHit = move.tar;
                return this.calcAvailableMoves();
            }
        }

        // Promotions end capturing sequences
        this.promote(move.tar);
        
        this.lastHit = null;
        return this.changeTurn();
    }
    
    /**
     * @return    a list with all legal source positions
     */
    public ArrayList<Position> getSrcPositionitions()
    {
        ArrayList<Position> source = new ArrayList<Position>();
        ArrayList<Move> moves = this.getAvailableMoves();
        
        for(Move move : moves)
        {
            Position src = move.src;
            if(!source.contains(src))
                source.add(src);
        }

        return source;
    }
    
    public boolean isValidSrcPosition(Position src){ return this.getSrcPositionitions().contains(src); }
    
    /**
     * The moves are only legal if the position is a valid source position!
     * 
     * @return    a list with all legal target positions from a selected position
     */
    public ArrayList<Position> getTarPositionitions(Position pos)
    {
        ArrayList<Position> target = new ArrayList<Position>();
        ArrayList<Move> moves = this.getMoves(pos);
        
        if(this.containCap(moves))
            moves = this.getCapMoves(moves);
        
        for(Move move : moves)
        {
            target.add(move.tar);
        }

        return target;
    }
    
    /**
     * Access a postion on the board.
     * 
     * @param pos   a position on the board
     * 
     * @return    the piece on the board at the given position
     */
    public int getStone(Position pos)
    {
        if(pos.isValid())
        {
            return board[pos.Y][pos.X];
        }
        else
        {
            Error.send(false,"Can't find stone at Positionition "+pos.getString());
            return -1;
        }
    }
    
    /**
     * Place a piece on the Board.
     * 
     * @param pos   a position on the board
     * @param stone the piece to be placed
     * 
     * @return    true, if the piece could be placed, otherwise false
     */
    private boolean setStone(Position pos, int stone)
    {
        if(pos.isValid()&&0<=stone&&stone<=4)
        {
            board[pos.Y][pos.X] = stone;
            return true;
        }
        else
        {
            Error.send(false,"Can't place piece "+stone+" at Positionition "+pos.getString());
            return false;
        }
    }
    
    /**
     * @return    The number of the other player.
     */
    public static int otherPlayer(int player)
    {
        if(player==1)
            return 2;
        else
            return 1;
    }
    
    public State changeTurn()
    {
        this.turn=otherPlayer(this.turn);
        this.moveNo++;
        return this.calcAvailableMoves();
    }
    
    /**
     * Promote a stone if possible.
     * 
     * @return    true, if the stone could be promoted, otherwise false.
     */
    private boolean promote(Position pos)
    {
        if(pos.Y==0||pos.Y==7)
        {
            int stone = this.getStone(pos);
            if(stone==1||stone==2)
            {
                return this.setStone(pos,stone+2);
            }
        }
        return false;
    }
    
    /**
     * @return    To which player the stone belongs.
     */
    private static int getPlayer(int stone)
    {
        switch(stone)
        {
            case 0: return 0;
            case 1: return 1;
            case 2: return 2;
            case 3: return 1;
            case 4: return 2;
            default: return 0;
        }
    }
    
    /**
     * @return    To which player the stone at the given position belongs.
     */
    public int getPlayer(Position pos){ return getPlayer(this.getStone(pos)); }
    
    /**
     * The move is not necessarily legal in the current state of the game!
     * 
     * @return    a move from source position in the selected direction, null if none is possible.
     */
    private Move getMove(Position src, int DirX, int DirY)
    {
        int stone = this.getStone(src);
        if(stone==0)
            return null;
        
        int player = getPlayer(stone);
        
        Position tar1 = new Position(src.X+DirX,src.Y+DirY);
        Position tar2 = new Position(src.X+DirX*2,src.Y+DirY*2);
        
        if( tar1.isValid() )
        {
            // normal move
            if( this.getStone(tar1)==0 )
                return new Move(src,tar1);
            // cap move
            else if( tar2.isValid() && (this.getPlayer(tar1)!=player) && (this.getStone(tar2)==0) )
                return new Move(src,tar2);
        }
        
        return null;
    }
    
    /**
     * The moves are not necessarily legal in the current state of the game!
     * 
     * @return    a list with all possible moves from the selected position.
     */
    public ArrayList<Move> getMoves(Position pos)
    {
        ArrayList<Move> moves = new ArrayList<Move>();
        
        int stone = this.getStone(pos);
        if(stone==0)
            return moves;
        
        int player = getPlayer(stone);
        boolean isKing = (stone==player+2);
        
        // add moves in all possible directions, invalid moves will be null values
        if(isKing)
        {
            moves.add(getMove(pos,-1,-1));
            moves.add(getMove(pos,1,-1));
            moves.add(getMove(pos,-1,1));
            moves.add(getMove(pos,1,1));
        }
        else if(player==1)
        {
            moves.add(getMove(pos,-1,-1));
            moves.add(getMove(pos,1,-1));
        }
        else
        {
            moves.add(getMove(pos,-1,1));
            moves.add(getMove(pos,1,1));
        }
        
        // remove all null values
        moves.removeIf(Objects::isNull);
        return moves;
    }
    
    /**
     * The moves are not necessarily legal in the current state of the game!
     * 
     * @return    a list with all possible moves from the selected positions.
     */
    private ArrayList<Move> getMoves(ArrayList<Position> stones)
    {
        ArrayList<Move> moves = new ArrayList<Move>();
        for(Position pos : stones)
        {
            moves.addAll(this.getMoves(pos));
        }
        return moves;
    }
    
    /**
     * @return    true, if selected stone can capture anything, otherwise false
     */
    private boolean canHit(Position pos)
    {
        for(Move move : getMoves(pos))
        {
            if(move.isCap())
                return true;
        }
        return false;
    }
    
    /**
     * @return    true, if the selected stones can capture anything, otherwise false
     */
    private boolean canHit(ArrayList<Position> stones)
    {
        for(Position pos : stones)
        {
            if(this.canHit(pos))
                return true;
        }
        return false;
    }
    
    /**
     * @return    a list with all stones of the selected player
     */
    public ArrayList<Position> getStones(int player)
    {
        ArrayList<Position> stones = new ArrayList<Position>();
        for(int i=0; i<8; i++)
            for(int j=0; j<8; j++)
            {
                Position pos = new Position(j,i);
                if(this.getPlayer(pos)==player)
                    stones.add(pos);
            }
        return stones;
    }
    
    public String getBoardAsString()
    {
        String s = "#--------#";
        for(int i=0; i<8; i++)
        {
            s += "\n|";
            for(int j=0; j<8; j++)
            {
                Position pos = new Position(j,i);
                int stone = this.getStone(pos);
                switch(stone)
                {
                    case 0: s+="#";break;
                    case 1: s+="x";break;
                    case 2: s+="o";break;
                    case 3: s+="X";break;
                    case 4: s+="O";break;
                }
            }
            s += "|";
        }
        return s + "\n#--------#";
    }
    
    /**
     * @return    true, if the selected moves contain a capturing move, otherwise false
     */
    private static boolean containCap(ArrayList<Move> moves)
    {
        for(Move move : moves)
        {
            if(move.isCap())
                return true;
        }
        return false;
    }
    
    /**
     * @return    a list with all capturing moves from the input list
     */
    private static ArrayList<Move> getCapMoves(ArrayList<Move> moves)
    {
        ArrayList<Move> capMoves = new ArrayList<Move>();
        for(Move move : moves)
        {
            if(move.isCap())
                capMoves.add(move);
        }
        return capMoves;
    }

    /**
     * calculate all legal moves
     */
    private State calcAvailableMoves()
    {
        ArrayList<Move> moves;
        
        if(this.lastHit!=null)
            moves = this.getMoves(this.lastHit);
        else
            moves = this.getMoves(this.getStones(this.turn));
        
        if(containCap(moves))
            this.availableMoves = getCapMoves(moves);
        else
            this.availableMoves = moves;
            
        return this;
    }
}