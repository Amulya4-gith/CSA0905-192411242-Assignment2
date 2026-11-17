import java.util.*;
public class Verify {
    static class S { private final String r,n; S(String r,String n){this.r=r;this.n=n;}
        @Override public boolean equals(Object o){ if(this==o) return true; if(!(o instanceof S)) return false; return r.equals(((S)o).r);}
        @Override public int hashCode(){ return r.hashCode(); } }
    record T(String name,int points,int gd){ public String toString(){return name+"("+points+","+gd+")";} }
    public static void main(String[] a){
        Set<S> set=new HashSet<>(); set.add(new S("192311001","Arun"));
        System.out.println("(2) contains(new S(192311001,Anything)) = "+set.contains(new S("192311001","Anything")));
        Set<S> big=new HashSet<>(); for(int i=0;i<1000;i++) big.add(new S("19231"+(1000+i%100),"N"+i));
        System.out.println("(3) 1000 scans over 100 ids -> size = "+big.size());
        boolean ok=true; for(int i=0;i<200;i++) for(int j=0;j<200;j++){ S x=new S("R"+i%50,"a"), y=new S("R"+j%50,"b"); if(x.equals(y)&&x.hashCode()!=y.hashCode()) ok=false; }
        System.out.println("(4) equal objects always had equal hash codes: "+ok);
        List<T> l=new ArrayList<>(List.of(new T("Panthers",9,4),new T("Eagles",9,6),new T("Cobras",6,1),new T("Bisons",9,4)));
        Comparator<T> wrong=Comparator.comparingInt(T::points).thenComparingInt(T::gd).thenComparing(T::name).reversed();
        l.sort(wrong); System.out.println("reversed() placed at the end: "+l);
    }
}
