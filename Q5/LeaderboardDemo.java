import java.util.*;
class Team {
    final String name; final int points; final int goalDiff;
    Team(String n,int p,int g){name=n;points=p;goalDiff=g;}
    @Override public String toString(){ return name+"("+points+","+goalDiff+")"; }
}
class Leaderboard<T> {
    private final List<T> items=new ArrayList<>();
    private final Comparator<? super T> order;
    Leaderboard(Comparator<? super T> order){this.order=order;}
    void add(T item){items.add(item);}
    void addAll(List<? extends T> more){items.addAll(more);}
    List<T> ranking(){ List<T> copy=new ArrayList<>(items); copy.sort(order); return copy; }
}
public class LeaderboardDemo {
    static <T extends Comparable<? super T>> T best(List<? extends T> l){ return Collections.min(l); }
    public static void main(String[] a){
        Comparator<Team> byRank=Comparator.comparingInt((Team t)->t.points).reversed()
            .thenComparing(Comparator.comparingInt((Team t)->t.goalDiff).reversed()).thenComparing(t->t.name);
        Leaderboard<Team> board=new Leaderboard<>(byRank);
        board.add(new Team("Panthers",9,4)); board.add(new Team("Eagles",9,6)); board.add(new Team("Cobras",6,1)); board.add(new Team("Bisons",9,4));
        System.out.println(board.ranking());
        // Test set 1: many ties at each level
        List<Team> t1=new ArrayList<>(List.of(new Team("Delta",7,0),new Team("Alpha",7,0),new Team("Charlie",7,2),new Team("Bravo",7,2),new Team("Echo",4,-1),new Team("Foxtrot",4,-1),new Team("Golf",4,3),new Team("Hotel",10,1)));
        Leaderboard<Team> b1=new Leaderboard<>(byRank); b1.addAll(t1); System.out.println("Set 1 : "+b1.ranking());
        // Test set 2: breaks subtraction comparator (overflow)
        Comparator<Team> faulty=(x,y)->y.points-x.points;
        Team p=new Team("Big",Integer.MAX_VALUE,0), q=new Team("Neg",-5,0);
        System.out.println("faulty.compare(Big,Neg)="+faulty.compare(p,q)+" , faulty.compare(Neg,Big)="+faulty.compare(q,p)+"  (Big should rank first => negative)");
        Comparator<Team> noTie=Comparator.comparingInt((Team t)->t.points).reversed().thenComparing(Comparator.comparingInt((Team t)->t.goalDiff).reversed());
        List<Team> s2=new ArrayList<>(List.of(new Team("Panthers",9,4),new Team("Bisons",9,4))); List<Team> s2b=new ArrayList<>(List.of(new Team("Bisons",9,4),new Team("Panthers",9,4)));
        s2.sort(noTie); s2b.sort(noTie);
        System.out.println("tie-breaker ignored: "+s2+" vs "+s2b+"  (output depends on input order)");
        List<Team> s2c=new ArrayList<>(List.of(new Team("A",Integer.MAX_VALUE,0),new Team("B",-5,0),new Team("C",3,0))); s2c.sort(faulty);
        System.out.println("subtraction comparator result: "+s2c+"  (A is the highest but is NOT first)");
        // three stable sorts
        List<Team> s3=new ArrayList<>(t1); s3.sort(Comparator.comparing((Team t)->t.name)); s3.sort(Comparator.comparingInt((Team t)->t.goalDiff).reversed()); s3.sort(Comparator.comparingInt((Team t)->t.points).reversed());
        List<Team> s1=new ArrayList<>(t1); s1.sort(byRank);
        System.out.println("three stable sorts equal to chain: "+s3.equals(s1));
        // timing
        for(int n:new int[]{10000,100000,500000}){
            Random r=new Random(3); List<Team> L=new ArrayList<>(); for(int i=0;i<n;i++) L.add(new Team("T"+r.nextInt(n),r.nextInt(30),r.nextInt(21)-10));
            double c=0,th=0; int R=5;
            for(int k=0;k<R+2;k++){
                List<Team> x=new ArrayList<>(L); long t0=System.nanoTime(); x.sort(byRank); long t1n=System.nanoTime();
                List<Team> y=new ArrayList<>(L); y.sort(Comparator.comparing((Team t)->t.name)); y.sort(Comparator.comparingInt((Team t)->t.goalDiff).reversed()); y.sort(Comparator.comparingInt((Team t)->t.points).reversed()); long t2=System.nanoTime();
                if(k>=2){c+=(t1n-t0)/1e6; th+=(t2-t1n)/1e6;}
            }
            System.out.printf("SORT n=%d: comparator chain %.1f ms, three stable sorts %.1f ms%n",n,c/R,th/R);
        }
    }
}
