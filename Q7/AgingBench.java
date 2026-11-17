import java.util.*;
public class AgingBench {
    static final int W=30; // 30 minutes of waiting = one severity level
    record P(int id,int sev,int arrivalMin){}
    static long ops;
    // ORIGINAL: recompute every patient's effective priority at serve time, scan for the best
    static List<Integer> original(List<P> in,int start){
        List<P> w=new ArrayList<>(in); List<Integer> out=new ArrayList<>(); int now=start;
        while(!w.isEmpty()){
            int best=0; long bestEff=Long.MAX_VALUE;
            for(int i=0;i<w.size();i++){ ops++; P p=w.get(i); long eff=(long)p.sev*W-(now-p.arrivalMin);
                if(eff<bestEff){bestEff=eff;best=i;} }
            out.add(w.get(best).id); w.remove(best); now++; }
        return out;
    }
    // ORIGINAL (variant): rebuild the PriorityQueue with fresh priorities before each serve
    static List<Integer> rebuild(List<P> in,int start){
        List<Integer> out=new ArrayList<>(); List<P> w=new ArrayList<>(in); int now=start;
        while(!w.isEmpty()){
            final int nw=now;
            PriorityQueue<P> pq=new PriorityQueue<>(Comparator.comparingLong((P p)->(long)p.sev*W-(nw-p.arrivalMin)).thenComparingInt(P::id));
            ops+=w.size(); pq.addAll(w); P top=pq.poll(); w.remove(top); out.add(top.id); now++; }
        return out;
    }
    // REDESIGN: effective = sev - (now-arr)/W  <=>  (sev*W + arr) - now ; 'now' is common to all => static key
    static List<Integer> redesign(List<P> in){
        PriorityQueue<P> pq=new PriorityQueue<>(Comparator.comparingLong((P p)->(long)p.sev*W+p.arrivalMin).thenComparingInt(P::id));
        for(P p:in) pq.add(p); List<Integer> out=new ArrayList<>(); while(!pq.isEmpty()) out.add(pq.poll().id); return out;
    }
    public static void main(String[] a){
        System.out.println("n,original_scan_ms,original_ops(comparisons),rebuild_pq_ms,redesign_ms,orders_identical");
        for(int n:new int[]{1000,5000,20000}){
            Random r=new Random(11); List<P> in=new ArrayList<>(); for(int i=0;i<n;i++) in.add(new P(i,1+r.nextInt(5),i/2));
            int start=n/2+1; double o=0,rb=0,rd=0; long ocount=0; boolean same=true; int R= n>=20000?3:5;
            for(int k=0;k<R+1;k++){
                ops=0; long t0=System.nanoTime(); List<Integer> a1=original(in,start); long t1=System.nanoTime(); ocount=ops;
                List<Integer> a2= n<=5000? rebuild(in,start):null; long t2=System.nanoTime();
                List<Integer> a3=redesign(in); long t3=System.nanoTime();
                if(k>=1){o+=(t1-t0)/1e6; rb+=(t2-t1)/1e6; rd+=(t3-t2)/1e6;}
                same=a1.equals(a3)&&(a2==null||a2.equals(a3));
            }
            System.out.printf("%d,%.1f,%d,%s,%.2f,%s%n",n,o/R,ocount,n<=5000?String.format("%.1f",rb/R):"skipped (too slow)",rd/R,same);
        }
        // starvation check: aging serves OldMinor before later critical arrivals
        List<P> in=new ArrayList<>(); in.add(new P(0,5,0)); for(int i=1;i<=300;i++) in.add(new P(i,1,i));
        List<Integer> out=redesign(in); System.out.println("OldMinor(S5, arrived t=0) is served at position "+(out.indexOf(0)+1)+" of 301 with aging (W=30): without aging it would be 301");
    }
}
