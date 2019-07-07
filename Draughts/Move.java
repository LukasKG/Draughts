public class Move
{
    public final Position src;
    public final Position tar;
    
    public Move(Position src, Position tar)
    {
        this.src = src;
        this.tar = tar;
    }
    
    public boolean isCap()
    {
        return((Math.abs(src.X-tar.X)==2)&&(Math.abs(src.Y-tar.Y)==2));
    }
    
    public Position getCapPos()
    {
        return new Position(Math.max(src.X,tar.X)-1,Math.max(src.Y,tar.Y)-1);
    }
    
    public String getString()
    {
        return src.getString() + " --> " + tar.getString();
    }
    
    @Override    
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Move move = (Move) o;
        if (!this.src.equals(move.src)) return false;
        if (!this.tar.equals(move.tar)) return false;
        return true;
    } 
    
    @Override    
    public int hashCode() 
    {        
        return 100*this.src.hashCode()+this.tar.hashCode();
    }   
}