public class Position
{
    public final int X;
    public final int Y;
    
    public int getX(){ return X; }
    public int getY(){ return Y; }
    
    public Position(int X, int Y)
    {
        this.X = X;
        this.Y = Y;
    }
    
    public boolean isValid()
    {
        if( 0<=X && X<8 && 0<=Y && Y<8 )
            return true;
        else
            return false;
    }
    
    public String getString()
    {
        return "("+X+"|"+Y+")";
    }
    
    @Override
    public Position clone()
    {
        return new Position(this.getX(),this.getY());
    }
    
    @Override    
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Position pos = (Position) o;
        if (this.getX() != pos.getX()) return false;
        if (this.getY() != pos.getY()) return false;
        return true;
    } 
    
    @Override    
    public int hashCode() 
    {        
        return 10*this.getX()+this.getY();
    }    
}
    
    