// https://stackoverflow.com/questions/2670982/using-pairs-or-2-tuples-in-java
public class Tuple<X, Y> { 
  public final X first; 
  public final Y second; 
  public Tuple(X x, Y y) { 
    this.first = x; 
    this.second = y; 
  } 
} 
